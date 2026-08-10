package com.kanban.kanbanbackend.service;

import com.kanban.kanbanbackend.dto.ColumnDto;
import com.kanban.kanbanbackend.dto.DtoMapper;
import com.kanban.kanbanbackend.repository.BoardColumnRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BoardService {

    private final BoardColumnRepository columnRepository;

    public BoardService(BoardColumnRepository columnRepository) {
        this.columnRepository = columnRepository;
    }

    @Transactional(readOnly = true)
    public List<ColumnDto> getColumns() {
        return columnRepository.findAllByOrderByPositionAsc().stream()
                .map(DtoMapper::toDto)
                .toList();
    }
}
