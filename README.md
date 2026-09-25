# Kanban Board

[![CI](https://github.com/subham-dahal/Kanban-board/actions/workflows/ci.yml/badge.svg)](https://github.com/subham-dahal/Kanban-board/actions/workflows/ci.yml)
![React](https://img.shields.io/badge/React-19-61dafb)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.1-6db33f)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-316192)

A Trello/Jira-style kanban board I built to practice going full-stack: a React frontend talking to a real Spring Boot + PostgreSQL backend, so tasks actually persist instead of resetting on every refresh.

You can drag cards between "To Do", "In Progress", and "Done", add new cards, and delete them — all of it saved to a database.

## Why I built it this way

I already had a decent backend background from Java, but most of my projects stopped at the API layer. This one was about closing that gap: a proper SPA frontend with drag-and-drop, wired up to a backend I built myself, instead of hardcoded data. It's also a nice showcase project because it touches the whole stack — relational schema design, REST API design, and frontend state management — without being so big it never gets finished.

## Tech stack

**Frontend**
- React 19 + Vite
- [@hello-pangea/dnd](https://github.com/hello-pangea/dnd) for drag-and-drop (a maintained fork of `react-beautiful-dnd`)
- Tailwind CSS for styling

**Backend**
- Spring Boot 4 (Java 17)
- Spring Data JPA / Hibernate
- PostgreSQL

**Testing**
- JUnit 5, Mockito, Spring MockMvc, H2 (in-memory database for tests)
- Vitest, React Testing Library

## How it's structured

The board is modeled the way you'd actually store a kanban board relationally, rather than faking it with a status string on each task:

```
Board (1) ──< BoardColumn (many) ──< Task (many)
```

A board has columns, each column holds an ordered list of tasks, and every task/column keeps a `position` field so ordering survives drags and page reloads.

```
frontend/                     React app
  src/
    App.jsx                   board UI and drag-and-drop wiring
    components/TaskCard.jsx   a draggable card
    lib/board.js              board state helpers (card reordering)
    api.js                    fetch wrapper for the backend
    *.test.js(x)              tests

backend/                      Spring Boot API
  src/main/java/com/kanban/kanbanbackend/
    entity/                   Board, BoardColumn, Task (JPA entities)
    repository/               Spring Data repositories
    service/                  business logic (create/move/delete and reindexing)
    controller/               REST endpoints
    dto/                      request/response records and validation
    exception/                JSON error responses (404, 400)
    config/                   CORS and startup data seeding
  src/test/java/...           tests
```

## Running it locally

You'll need Java 17, Node 20+ and PostgreSQL. It works on macOS, Linux and Windows.

**1. Database**

Open a Postgres shell (`psql -U postgres`, or SQL Shell (psql) from the Start menu on Windows) and run:

```sql
CREATE DATABASE kanban;
CREATE USER kanban_user WITH ENCRYPTED PASSWORD 'kanban_pass';
GRANT ALL PRIVILEGES ON DATABASE kanban TO kanban_user;
\c kanban
GRANT ALL ON SCHEMA public TO kanban_user;
```

(Match these to whatever you put in `backend/src/main/resources/application.properties`.)

**2. Backend**

```bash
cd backend
./mvnw spring-boot:run          # macOS / Linux
.\mvnw.cmd spring-boot:run      # Windows
```

Starts on `http://localhost:8080`. On first run it seeds a default board with a few sample cards so the UI isn't empty.

**3. Frontend**

```bash
cd frontend
npm install
npm run dev
```

Starts on `http://localhost:5173`.

## Tests

The backend tests use an in-memory H2 database, so you don't need Postgres running to run them.

```bash
cd backend
./mvnw test                     # macOS / Linux
.\mvnw.cmd test                 # Windows

cd frontend
npm test
```

What's covered:

- `TaskServiceTest`: create, update, delete and move logic with Mockito, including reindexing after a delete, reordering inside a column, moving between columns and out-of-range positions
- `TaskControllerTest`, `ColumnControllerTest`: status codes, validation errors, 404 responses and the CORS setup
- `TaskRepositoryTest`: the queries that return tasks and columns in position order
- `KanbanApiIntegrationTest`: real HTTP requests through to the database, checking that create, move, delete and edit all save the right order
- `lib/board.test.js`: the drag-and-drop reorder logic
- `api.test.js`: the fetch wrapper (URLs, methods, bodies, error messages)
- `App.test.jsx`: loading the board, adding and deleting cards, and the error banner

GitHub Actions runs both suites on Linux, Windows and macOS on every push.

## API

| Method | Endpoint              | Description                              |
|--------|-----------------------|-------------------------------------------|
| GET    | `/api/columns`        | Get all columns with their tasks          |
| POST   | `/api/tasks`           | Create a task (`title`, `description`, `columnId`) |
| PUT    | `/api/tasks/{id}`      | Edit a task's title/description           |
| PUT    | `/api/tasks/{id}/move` | Move a task to a column/position (used by drag-and-drop) |
| DELETE | `/api/tasks/{id}`      | Delete a task                             |

## What's next

- Multiple boards instead of a single hardcoded one
- Editing a card's title/description from the UI (the API already supports it)
- Basic auth so boards aren't shared by everyone hitting the API
