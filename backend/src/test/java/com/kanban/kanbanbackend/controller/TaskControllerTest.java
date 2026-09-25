package com.kanban.kanbanbackend.controller;

import com.kanban.kanbanbackend.dto.CreateTaskRequest;
import com.kanban.kanbanbackend.dto.MoveTaskRequest;
import com.kanban.kanbanbackend.dto.TaskDto;
import com.kanban.kanbanbackend.dto.UpdateTaskRequest;
import com.kanban.kanbanbackend.exception.ResourceNotFoundException;
import com.kanban.kanbanbackend.service.TaskService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TaskController.class)
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TaskService taskService;

    @Test
    void createReturns201WithTask() throws Exception {
        when(taskService.createTask(new CreateTaskRequest("New card", "details", 1L)))
                .thenReturn(new TaskDto(7L, "New card", "details", 0, 1L));

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title": "New card", "description": "details", "columnId": 1}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.title").value("New card"))
                .andExpect(jsonPath("$.columnId").value(1));
    }

    @Test
    void createWithBlankTitleReturns400() throws Exception {
        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title": " ", "columnId": 1}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("title must not be blank"));

        verifyNoInteractions(taskService);
    }

    @Test
    void createWithoutColumnReturns400() throws Exception {
        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title": "Card"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("columnId must not be null"));
    }

    @Test
    void updateReturnsUpdatedTask() throws Exception {
        when(taskService.updateTask(eq(3L), eq(new UpdateTaskRequest("Renamed", null))))
                .thenReturn(new TaskDto(3L, "Renamed", null, 0, 1L));

        mockMvc.perform(put("/api/tasks/3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title": "Renamed"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Renamed"));
    }

    @Test
    void moveDelegatesToService() throws Exception {
        when(taskService.moveTask(3L, new MoveTaskRequest(2L, 1)))
                .thenReturn(new TaskDto(3L, "Card", null, 1, 2L));

        mockMvc.perform(put("/api/tasks/3/move")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"columnId": 2, "position": 1}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.columnId").value(2))
                .andExpect(jsonPath("$.position").value(1));
    }

    @Test
    void moveWithNegativePositionReturns400() throws Exception {
        mockMvc.perform(put("/api/tasks/3/move")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"columnId": 2, "position": -1}
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(taskService);
    }

    @Test
    void deleteReturns204() throws Exception {
        mockMvc.perform(delete("/api/tasks/3"))
                .andExpect(status().isNoContent());

        verify(taskService).deleteTask(3L);
    }

    @Test
    void missingTaskReturns404WithErrorBody() throws Exception {
        doThrow(new ResourceNotFoundException("Task not found: 42")).when(taskService).deleteTask(42L);

        mockMvc.perform(delete("/api/tasks/42"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Task not found: 42"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void updateMissingTaskReturns404() throws Exception {
        when(taskService.updateTask(eq(42L), any())).thenThrow(new ResourceNotFoundException("Task not found: 42"));

        mockMvc.perform(put("/api/tasks/42")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title": "x"}
                                """))
                .andExpect(status().isNotFound());
    }
}
