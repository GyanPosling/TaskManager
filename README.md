# Task Manager

## Overview
This is a Spring Boot REST API for managing tasks in a simple task management domain. It uses PostgreSQL for persistence and Liquibase for schema migrations. The current API surface focuses on task CRUD, with entities for users, projects, tags, and comments already modeled in the domain layer.

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
- Lombok
- Maven

## Project Structure
- `src/main/java/com/bsuir/taskmanager/controller` REST controllers
- `src/main/java/com/bsuir/taskmanager/service` business logic
- `src/main/java/com/bsuir/taskmanager/repository` Spring Data repositories
- `src/main/java/com/bsuir/taskmanager/dto` request and response DTOs
- `src/main/java/com/bsuir/taskmanager/mapper` DTO/entity mapping
- `src/main/java/com/bsuir/taskmanager/model/entity` JPA entities
- `src/main/resources/db/changelog` Liquibase changelogs
- `src/main/resources/application.yml` application configuration
- `compose.yaml` Docker Compose services

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

## API
Current HTTP endpoints are under the task resource:
- List all tasks, optionally filtered by status
- Get task by id
- Create task
- Update task
- Delete task

## Running the Project
You can run the application either with Maven locally or with Docker Compose. For Docker Compose, the setup includes both the app and PostgreSQL with a named volume for data persistence.

## Testing and Quality
The project includes a basic test skeleton and Checkstyle configuration under `config/checkstyle`.
