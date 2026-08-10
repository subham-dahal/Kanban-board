package com.kanban.kanbanbackend.dto;

import com.kanban.kanbanbackend.entity.BoardColumn;
import com.kanban.kanbanbackend.entity.Task;

import java.util.List;

public class DtoMapper {

    private DtoMapper() {
    }

    public static TaskDto toDto(Task task) {
        return new TaskDto(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getPosition(),
                task.getColumn().getId()
        );
    }

    public static ColumnDto toDto(BoardColumn column) {
        List<TaskDto> tasks = column.getTasks().stream().map(DtoMapper::toDto).toList();
        return new ColumnDto(column.getId(), column.getName(), column.getPosition(), tasks);
    }
}
