package com.kanban.kanbanbackend.config;

import com.kanban.kanbanbackend.entity.Board;
import com.kanban.kanbanbackend.entity.BoardColumn;
import com.kanban.kanbanbackend.entity.Task;
import com.kanban.kanbanbackend.repository.BoardRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    private final BoardRepository boardRepository;

    public DataSeeder(BoardRepository boardRepository) {
        this.boardRepository = boardRepository;
    }

    @Override
    public void run(String... args) {
        if (boardRepository.count() > 0) {
            return;
        }

        Board board = new Board();
        board.setName("My Kanban Board");

        BoardColumn todo = newColumn(board, "To Do", 0);
        BoardColumn inProgress = newColumn(board, "In Progress", 1);
        BoardColumn done = newColumn(board, "Done", 2);

        board.getColumns().add(todo);
        board.getColumns().add(inProgress);
        board.getColumns().add(done);

        todo.getTasks().add(newTask(todo, "Learn React basics", "Understand components and props", 0));
        inProgress.getTasks().add(newTask(inProgress, "Build Kanban UI", "Implement Tailwind CSS design", 0));
        done.getTasks().add(newTask(done, "Setup Vite project", "Install dependencies and configure Vite", 0));

        boardRepository.save(board);
    }

    private static BoardColumn newColumn(Board board, String name, int position) {
        BoardColumn column = new BoardColumn();
        column.setBoard(board);
        column.setName(name);
        column.setPosition(position);
        return column;
    }

    private static Task newTask(BoardColumn column, String title, String description, int position) {
        Task task = new Task();
        task.setColumn(column);
        task.setTitle(title);
        task.setDescription(description);
        task.setPosition(position);
        return task;
    }
}
