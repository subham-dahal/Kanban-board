package com.kanban.kanbanbackend.dto;

import java.util.List;

public record ColumnDto(Long id, String name, int position, List<TaskDto> tasks) {
}
