package com.kanban.kanbanbackend.repository;

import com.kanban.kanbanbackend.entity.Board;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoardRepository extends JpaRepository<Board, Long> {
}
