package com.kanban.kanbanbackend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateTaskRequest(
        @NotBlank String title,
        String description,
        @NotNull Long columnId
) {
}
