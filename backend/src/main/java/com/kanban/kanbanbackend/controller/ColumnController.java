package com.kanban.kanbanbackend.controller;

import com.kanban.kanbanbackend.dto.ColumnDto;
import com.kanban.kanbanbackend.service.BoardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/columns")
public class ColumnController {

    private final BoardService boardService;

    public ColumnController(BoardService boardService) {
        this.boardService = boardService;
    }

    @GetMapping
    public List<ColumnDto> getColumns() {
        return boardService.getColumns();
    }
}
