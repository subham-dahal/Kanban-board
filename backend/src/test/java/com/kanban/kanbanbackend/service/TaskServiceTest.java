package com.kanban.kanbanbackend.service;

import com.kanban.kanbanbackend.dto.CreateTaskRequest;
import com.kanban.kanbanbackend.dto.MoveTaskRequest;
import com.kanban.kanbanbackend.dto.TaskDto;
import com.kanban.kanbanbackend.dto.UpdateTaskRequest;
import com.kanban.kanbanbackend.entity.BoardColumn;
import com.kanban.kanbanbackend.entity.Task;
import com.kanban.kanbanbackend.exception.ResourceNotFoundException;
import com.kanban.kanbanbackend.repository.BoardColumnRepository;
import com.kanban.kanbanbackend.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private BoardColumnRepository columnRepository;

    @InjectMocks
    private TaskService taskService;

    private BoardColumn todo;
    private BoardColumn done;

    @BeforeEach
    void setUp() {
        todo = column(1L, "To Do");
        done = column(2L, "Done");
    }

    @Test
    void createTaskAppendsToEndOfColumn() {
        when(columnRepository.findById(1L)).thenReturn(Optional.of(todo));
        when(taskRepository.findAllByColumnIdOrderByPositionAsc(1L))
                .thenReturn(List.of(task(10L, todo, 0), task(11L, todo, 1)));
        when(taskRepository.save(any(Task.class))).thenAnswer(inv -> {
            Task saved = inv.getArgument(0);
            saved.setId(12L);
            return saved;
        });

        TaskDto dto = taskService.createTask(new CreateTaskRequest("Write tests", "JUnit + Mockito", 1L));

        assertThat(dto).isEqualTo(new TaskDto(12L, "Write tests", "JUnit + Mockito", 2, 1L));
    }

    @Test
    void createTaskInUnknownColumnThrows() {
        when(columnRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.createTask(new CreateTaskRequest("x", null, 99L)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Column not found: 99");
        verify(taskRepository, never()).save(any());
    }

    @Test
    void updateTaskChangesTitleAndDescription() {
        Task existing = task(10L, todo, 0);
        when(taskRepository.findById(10L)).thenReturn(Optional.of(existing));
        when(taskRepository.save(existing)).thenReturn(existing);

        TaskDto dto = taskService.updateTask(10L, new UpdateTaskRequest("Renamed", "New description"));

        assertThat(dto.title()).isEqualTo("Renamed");
        assertThat(dto.description()).isEqualTo("New description");
    }

    @Test
    void updateUnknownTaskThrows() {
        when(taskRepository.findById(5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.updateTask(5L, new UpdateTaskRequest("x", null)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Task not found: 5");
    }

    @Test
    void deleteTaskReindexesRemainingTasks() {
        Task doomed = task(10L, todo, 0);
        Task second = task(11L, todo, 1);
        Task third = task(12L, todo, 2);
        when(taskRepository.findById(10L)).thenReturn(Optional.of(doomed));
        when(taskRepository.findAllByColumnIdOrderByPositionAsc(1L)).thenReturn(List.of(second, third));

        taskService.deleteTask(10L);

        verify(taskRepository).delete(doomed);
        assertThat(second.getPosition()).isZero();
        assertThat(third.getPosition()).isEqualTo(1);
    }

    @Test
    void moveWithinColumnReordersTasks() {
        Task a = task(10L, todo, 0);
        Task b = task(11L, todo, 1);
        Task c = task(12L, todo, 2);
        when(taskRepository.findById(10L)).thenReturn(Optional.of(a));
        when(columnRepository.findById(1L)).thenReturn(Optional.of(todo));
        when(taskRepository.findAllByColumnIdOrderByPositionAsc(1L)).thenReturn(new ArrayList<>(List.of(a, b, c)));

        TaskDto dto = taskService.moveTask(10L, new MoveTaskRequest(1L, 2));

        assertThat(dto.position()).isEqualTo(2);
        assertThat(List.of(b.getPosition(), c.getPosition(), a.getPosition())).containsExactly(0, 1, 2);
    }

    @Test
    void moveAcrossColumnsReindexesSourceAndDestination() {
        Task a = task(10L, todo, 0);
        Task b = task(11L, todo, 1);
        Task x = task(20L, done, 0);
        Task y = task(21L, done, 1);
        when(taskRepository.findById(10L)).thenReturn(Optional.of(a));
        when(columnRepository.findById(2L)).thenReturn(Optional.of(done));
        when(taskRepository.findAllByColumnIdOrderByPositionAsc(1L)).thenReturn(new ArrayList<>(List.of(a, b)));
        when(taskRepository.findAllByColumnIdOrderByPositionAsc(2L)).thenReturn(new ArrayList<>(List.of(x, y)));

        TaskDto dto = taskService.moveTask(10L, new MoveTaskRequest(2L, 1));

        assertThat(dto.columnId()).isEqualTo(2L);
        assertThat(a.getColumn()).isSameAs(done);
        assertThat(b.getPosition()).isZero();
        assertThat(List.of(x.getPosition(), a.getPosition(), y.getPosition())).containsExactly(0, 1, 2);
    }

    @Test
    void movePositionIsClampedToEndOfColumn() {
        Task a = task(10L, todo, 0);
        Task x = task(20L, done, 0);
        when(taskRepository.findById(10L)).thenReturn(Optional.of(a));
        when(columnRepository.findById(2L)).thenReturn(Optional.of(done));
        when(taskRepository.findAllByColumnIdOrderByPositionAsc(1L)).thenReturn(new ArrayList<>(List.of(a)));
        when(taskRepository.findAllByColumnIdOrderByPositionAsc(2L)).thenReturn(new ArrayList<>(List.of(x)));

        TaskDto dto = taskService.moveTask(10L, new MoveTaskRequest(2L, 50));

        assertThat(dto.position()).isEqualTo(1);
    }

    @Test
    void moveToUnknownColumnThrows() {
        when(taskRepository.findById(10L)).thenReturn(Optional.of(task(10L, todo, 0)));
        when(columnRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.moveTask(10L, new MoveTaskRequest(99L, 0)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Column not found: 99");
    }

    private static BoardColumn column(Long id, String name) {
        BoardColumn column = new BoardColumn();
        column.setId(id);
        column.setName(name);
        return column;
    }

    private static Task task(Long id, BoardColumn column, int position) {
        Task task = new Task();
        task.setId(id);
        task.setTitle("Task " + id);
        task.setColumn(column);
        task.setPosition(position);
        return task;
    }
}
