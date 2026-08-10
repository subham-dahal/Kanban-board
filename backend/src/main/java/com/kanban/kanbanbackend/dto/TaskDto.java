package com.kanban.kanbanbackend.dto;

public record TaskDto(Long id, String title, String description, int position, Long columnId) {
}
