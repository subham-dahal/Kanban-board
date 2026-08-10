package com.kanban.kanbanbackend.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record MoveTaskRequest(
        @NotNull Long columnId,
        @PositiveOrZero int position
) {
}
