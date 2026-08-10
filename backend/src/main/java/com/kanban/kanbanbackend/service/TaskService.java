package com.kanban.kanbanbackend.service;

import com.kanban.kanbanbackend.dto.CreateTaskRequest;
import com.kanban.kanbanbackend.dto.DtoMapper;
import com.kanban.kanbanbackend.dto.MoveTaskRequest;
import com.kanban.kanbanbackend.dto.TaskDto;
import com.kanban.kanbanbackend.dto.UpdateTaskRequest;
import com.kanban.kanbanbackend.entity.BoardColumn;
import com.kanban.kanbanbackend.entity.Task;
import com.kanban.kanbanbackend.exception.ResourceNotFoundException;
import com.kanban.kanbanbackend.repository.BoardColumnRepository;
import com.kanban.kanbanbackend.repository.TaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final BoardColumnRepository columnRepository;

    public TaskService(TaskRepository taskRepository, BoardColumnRepository columnRepository) {
        this.taskRepository = taskRepository;
        this.columnRepository = columnRepository;
    }

    @Transactional
    public TaskDto createTask(CreateTaskRequest request) {
        BoardColumn column = columnRepository.findById(request.columnId())
                .orElseThrow(() -> new ResourceNotFoundException("Column not found: " + request.columnId()));

        Task task = new Task();
        task.setTitle(request.title());
        task.setDescription(request.description());
        task.setColumn(column);
        task.setPosition(taskRepository.findAllByColumnIdOrderByPositionAsc(column.getId()).size());

        return DtoMapper.toDto(taskRepository.save(task));
    }

    @Transactional
    public TaskDto updateTask(Long taskId, UpdateTaskRequest request) {
        Task task = getTaskOrThrow(taskId);
        task.setTitle(request.title());
        task.setDescription(request.description());
        return DtoMapper.toDto(taskRepository.save(task));
    }

    @Transactional
    public void deleteTask(Long taskId) {
        Task task = getTaskOrThrow(taskId);
        Long columnId = task.getColumn().getId();
        taskRepository.delete(task);

        List<Task> remaining = taskRepository.findAllByColumnIdOrderByPositionAsc(columnId);
        for (int i = 0; i < remaining.size(); i++) {
            remaining.get(i).setPosition(i);
        }
        taskRepository.saveAll(remaining);
    }

    @Transactional
    public TaskDto moveTask(Long taskId, MoveTaskRequest request) {
        Task task = getTaskOrThrow(taskId);
        BoardColumn sourceColumn = task.getColumn();
        BoardColumn targetColumn = columnRepository.findById(request.columnId())
                .orElseThrow(() -> new ResourceNotFoundException("Column not found: " + request.columnId()));

        if (sourceColumn.getId().equals(targetColumn.getId())) {
            List<Task> tasks = taskRepository.findAllByColumnIdOrderByPositionAsc(sourceColumn.getId());
            tasks.removeIf(t -> t.getId().equals(taskId));
            int index = clamp(request.position(), tasks.size());
            tasks.add(index, task);
            reindex(tasks);
            taskRepository.saveAll(tasks);
        } else {
            List<Task> sourceTasks = taskRepository.findAllByColumnIdOrderByPositionAsc(sourceColumn.getId());
            sourceTasks.removeIf(t -> t.getId().equals(taskId));
            reindex(sourceTasks);
            taskRepository.saveAll(sourceTasks);

            List<Task> destTasks = taskRepository.findAllByColumnIdOrderByPositionAsc(targetColumn.getId());
            int index = clamp(request.position(), destTasks.size());
            task.setColumn(targetColumn);
            destTasks.add(index, task);
            reindex(destTasks);
            taskRepository.saveAll(destTasks);
        }

        return DtoMapper.toDto(task);
    }

    private Task getTaskOrThrow(Long taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found: " + taskId));
    }

    private static int clamp(int value, int size) {
        return Math.max(0, Math.min(value, size));
    }

    private static void reindex(List<Task> tasks) {
        for (int i = 0; i < tasks.size(); i++) {
            tasks.get(i).setPosition(i);
        }
    }
}
