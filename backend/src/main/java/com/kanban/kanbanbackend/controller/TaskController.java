package com.kanban.kanbanbackend.controller;

import com.kanban.kanbanbackend.dto.CreateTaskRequest;
import com.kanban.kanbanbackend.dto.MoveTaskRequest;
import com.kanban.kanbanbackend.dto.TaskDto;
import com.kanban.kanbanbackend.dto.UpdateTaskRequest;
import com.kanban.kanbanbackend.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping
    public ResponseEntity<TaskDto> createTask(@Valid @RequestBody CreateTaskRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(taskService.createTask(request));
    }

    @PutMapping("/{id}")
    public TaskDto updateTask(@PathVariable Long id, @Valid @RequestBody UpdateTaskRequest request) {
        return taskService.updateTask(id, request);
    }

    @PutMapping("/{id}/move")
    public TaskDto moveTask(@PathVariable Long id, @Valid @RequestBody MoveTaskRequest request) {
        return taskService.moveTask(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }
}
