# Task Manager
## Bursh Anton 450502
### [SonarCloud Checking](https://sonarcloud.io/project/overview?id=GyanPosling_TaskManager)
### [ER - Diagram](docs/ER-Diagramm.png)
## Overview
This is a Java Spring Boot REST API for managing tasks in a simple task management domain.

## Features
- Task CRUD endpoints with optional filtering by status
- Validation of incoming requests
- OpenAPI documentation via Springdoc
- Database schema managed with Liquibase
- Docker Compose setup for app + PostgreSQL

## Tech Stack
- Java 17
- Spring Boot (Web MVC, Data JPA, Validation, Actuator)
- PostgreSQL
- Liquibase
- Maven
- Git


## Configuration
The application reads environment variables from `.env` via Spring configuration import. Key variables:
- `DB_NAME`
- `DB_USER`
- `DB_PASSWORD`
- `DB_HOST`
- `DB_PORT`
- `DOCKER_COMPOSE_ENABLED`

## Database and Migrations
Liquibase runs on startup and applies changesets from `src/main/resources/db/changelog/db.changelog-master.yaml`. The schema includes users, projects, tasks, tags, comments, and the task-to-tag join table.

## Cascade and Fetch Strategy
Cascade and fetch choices are set to keep the domain consistent while avoiding heavy default loads.
- `Project -> tasks` uses `CascadeType.ALL` with `orphanRemoval=true` so tasks are created/removed with their owning project.
- `Task -> comments` uses `CascadeType.ALL` with `orphanRemoval=true` because comments are strictly tied to a task lifecycle.
- `Task <-> tags` has no cascade because tags are shared entities and must not be deleted when a task is deleted.
- `ManyToOne` associations use `FetchType.LAZY` to avoid loading parent entities unless needed (project, assignee, author).
- `OneToMany`/`ManyToMany` collections are `LAZY` to keep list queries light; when relationships are required they should be fetched explicitly.

## API
Current HTTP endpoints are under the task resource:
- List all tasks, optionally filtered by status
- Get task by id
- Create task
- Update task
- Delete task

Additional task endpoints for JPA fetch strategy:
- `/api/tasks/with-tags` loads tasks with tags using `@EntityGraph`.
- `/api/tasks/with-comments` loads tasks with comments using `JOIN FETCH`.

## Running the Project
You can run the application either with Maven locally or with Docker Compose. For Docker Compose, the setup includes both the app and PostgreSQL with a named volume for data persistence.

## Testing and Quality
The project includes a basic test skeleton and Checkstyle configuration under `config/checkstyle`.
SonarCloud for analysing code.

