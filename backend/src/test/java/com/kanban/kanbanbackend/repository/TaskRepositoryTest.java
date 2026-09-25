package com.kanban.kanbanbackend.repository;

import com.kanban.kanbanbackend.entity.Board;
import com.kanban.kanbanbackend.entity.BoardColumn;
import com.kanban.kanbanbackend.entity.Task;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class TaskRepositoryTest {

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private BoardColumnRepository columnRepository;

    @Test
    void findsTasksAndColumnsOrderedByPosition() {
        Board board = new Board();
        board.setName("Test board");
        BoardColumn second = column(board, "Second", 1);
        BoardColumn first = column(board, "First", 0);
        board.getColumns().addAll(List.of(second, first));
        first.getTasks().addAll(List.of(task(first, "c", 2), task(first, "a", 0), task(first, "b", 1)));
        second.getTasks().add(task(second, "other", 0));
        boardRepository.saveAndFlush(board);

        List<Task> tasks = taskRepository.findAllByColumnIdOrderByPositionAsc(first.getId());
        List<BoardColumn> columns = columnRepository.findAllByOrderByPositionAsc();

        assertThat(tasks).extracting(Task::getTitle).containsExactly("a", "b", "c");
        assertThat(columns).extracting(BoardColumn::getName).containsExactly("First", "Second");
    }

    private static BoardColumn column(Board board, String name, int position) {
        BoardColumn column = new BoardColumn();
        column.setBoard(board);
        column.setName(name);
        column.setPosition(position);
        return column;
    }

    private static Task task(BoardColumn column, String title, int position) {
        Task task = new Task();
        task.setColumn(column);
        task.setTitle(title);
        task.setPosition(position);
        return task;
    }
}
