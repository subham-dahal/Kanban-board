package com.kanban.kanbanbackend.controller;

import com.kanban.kanbanbackend.dto.ColumnDto;
import com.kanban.kanbanbackend.dto.TaskDto;
import com.kanban.kanbanbackend.service.BoardService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ColumnController.class)
class ColumnControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BoardService boardService;

    @Test
    void returnsColumnsWithTasks() throws Exception {
        when(boardService.getColumns()).thenReturn(List.of(
                new ColumnDto(1L, "To Do", 0, List.of(new TaskDto(5L, "Card", "desc", 0, 1L))),
                new ColumnDto(2L, "Done", 1, List.of())));

        mockMvc.perform(get("/api/columns"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("To Do"))
                .andExpect(jsonPath("$[0].tasks[0].title").value("Card"))
                .andExpect(jsonPath("$[1].tasks").isEmpty());
    }

    @Test
    void allowsCorsFromViteDevServer() throws Exception {
        mockMvc.perform(options("/api/columns")
                        .header("Origin", "http://localhost:5173")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"));
    }

    @Test
    void rejectsCorsFromOtherOrigins() throws Exception {
        mockMvc.perform(options("/api/columns")
                        .header("Origin", "http://evil.example")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isForbidden());
    }
}
