package com.kanban.kanbanbackend.repository;

import com.kanban.kanbanbackend.entity.BoardColumn;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BoardColumnRepository extends JpaRepository<BoardColumn, Long> {
    List<BoardColumn> findAllByOrderByPositionAsc();
}
