package com.kanban.kanbanbackend.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateTaskRequest(
        @NotBlank String title,
        String description
) {
}
