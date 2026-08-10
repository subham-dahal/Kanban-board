package com.kanban.kanbanbackend.repository;

import com.kanban.kanbanbackend.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findAllByColumnIdOrderByPositionAsc(Long columnId);
}
