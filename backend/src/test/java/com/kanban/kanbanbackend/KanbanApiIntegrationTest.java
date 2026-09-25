package com.kanban.kanbanbackend;

import com.jayway.jsonpath.JsonPath;
import com.kanban.kanbanbackend.config.DataSeeder;
import com.kanban.kanbanbackend.repository.BoardRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.contains;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Full-stack tests: HTTP → controller → service → JPA → in-memory H2 database.
 */
@SpringBootTest
@AutoConfigureMockMvc
class KanbanApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private DataSeeder dataSeeder;

    private long todoId;
    private long inProgressId;

    @BeforeEach
    void resetBoard() throws Exception {
        boardRepository.deleteAll();
        dataSeeder.run();

        String columns = mockMvc.perform(get("/api/columns")).andReturn().getResponse().getContentAsString();
        todoId = ((Number) JsonPath.read(columns, "$[0].id")).longValue();
        inProgressId = ((Number) JsonPath.read(columns, "$[1].id")).longValue();
    }

    @Test
    void seedsDefaultBoardInColumnOrder() throws Exception {
        mockMvc.perform(get("/api/columns"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].name", contains("To Do", "In Progress", "Done")))
                .andExpect(jsonPath("$[0].tasks[0].title").value("Learn React basics"));
    }

    @Test
    void createdTaskIsPersistedAtEndOfColumn() throws Exception {
        createTask("Second card", todoId);

        mockMvc.perform(get("/api/columns"))
                .andExpect(jsonPath("$[0].tasks[*].title", contains("Learn React basics", "Second card")))
                .andExpect(jsonPath("$[0].tasks[1].position").value(1));
    }

    @Test
    void movingTaskAcrossColumnsPersistsNewOrder() throws Exception {
        long taskId = createTask("Moving card", todoId);

        mockMvc.perform(put("/api/tasks/" + taskId + "/move")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"columnId\": " + inProgressId + ", \"position\": 0}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.columnId").value(inProgressId))
                .andExpect(jsonPath("$.position").value(0));

        mockMvc.perform(get("/api/columns"))
                .andExpect(jsonPath("$[0].tasks[*].title", contains("Learn React basics")))
                .andExpect(jsonPath("$[1].tasks[*].title", contains("Moving card", "Build Kanban UI")))
                .andExpect(jsonPath("$[1].tasks[*].position", contains(0, 1)));
    }

    @Test
    void deletingTaskClosesGapInPositions() throws Exception {
        long first = createTask("First", inProgressId);
        createTask("Second", inProgressId);

        mockMvc.perform(delete("/api/tasks/" + first)).andExpect(status().isNoContent());

        mockMvc.perform(get("/api/columns"))
                .andExpect(jsonPath("$[1].tasks[*].title", contains("Build Kanban UI", "Second")))
                .andExpect(jsonPath("$[1].tasks[*].position", contains(0, 1)));
    }

    @Test
    void editingTaskUpdatesItsFields() throws Exception {
        long taskId = createTask("Draft", todoId);

        mockMvc.perform(put("/api/tasks/" + taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\": \"Final\", \"description\": \"Polished\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/columns"))
                .andExpect(jsonPath("$[0].tasks[1].title").value("Final"))
                .andExpect(jsonPath("$[0].tasks[1].description").value("Polished"));
    }

    @Test
    void creatingTaskInMissingColumnReturns404() throws Exception {
        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\": \"Orphan\", \"columnId\": 999999}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Column not found: 999999"));
    }

    private long createTask(String title, long columnId) throws Exception {
        String body = mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\": \"" + title + "\", \"columnId\": " + columnId + "}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(body, "$.id")).longValue();
    }
}
