# Task Manager

## Required links:
### [SonarCloud analysis](https://sonarcloud.io/summary/new_code?id=GyanPosling_TaskManager&branch=main)
### [ER-Diagram](docs/ER-Diagramm.png)

## Overview
This project contains:
- Java Spring Boot REST API for managing tasks in a simple task management domain
- Separate React frontend service (Vite build + Nginx runtime)

## Features
- CRUD for users, projects, tasks, tags, and comments
- Task filtering by status
- Task queries with tags and comments preloaded
- JPQL task search by nested entity (`project.owner`) with pagination
- Native SQL task search by tag and due date with pagination
- In-memory HashMap index for repeated task search requests
- Cache invalidation on task, project, and tag changes
- Bulk task creation endpoints with and without transaction support
- Async bulk task creation with operation ID and status polling
- Race condition demo with unsafe, `synchronized`, and `AtomicInteger` counters
- 50-thread concurrency demo via `ExecutorService`
- JMeter load testing scenario for real REST API endpoints
- CLI-ready JMeter report generation instructions
- Validation for incoming requests
- Composite creation demo showing partial save vs transactional rollback
- Service execution time logging via Spring AOP
- Mockito-based unit tests for the service layer
- OpenAPI documentation via Springdoc
- PostgreSQL schema migrations via Liquibase
- Docker Compose setup for app + PostgreSQL
- Docker Compose setup for app + frontend + PostgreSQL

## Notes
- Maximum page size for the new paginated task search endpoints is limited to `10`
- Repeated identical task search requests are served from an in-memory cache
- Cache is cleared after task, project, or tag updates

## Helpers
- Frontend app - http://localhost:3000
- Swagger link - http://localhost:8080/swagger-ui/index.html
- Checkstyle command - sh mvnw checkstyle:check
- Coverage command - sh mvnw verify
- Coverage report - target/site/jacoco/index.html
- JMeter assets - `docs/jmeter/taskmanager-load-test.jmx`
- JMeter guide - `docs/jmeter/README.md`

## Run with Docker Compose
```bash
docker compose up --build
```

## Deploy to Railway (backend)
- Create a PostgreSQL service in Railway.
- Create a service for this repository (Dockerfile deploy).
- Set environment variable `JWT_SECRET` in Railway.
- Do not set `DB_HOST=localhost` for Railway.
- App now supports Railway Postgres variables out of the box:
  - `PGHOST`
  - `PGPORT`
  - `PGDATABASE`
  - `PGUSER`
  - `PGPASSWORD`
- Railway provides `PORT`; app is configured to bind to it automatically.
