# Task Manager
## Bursh Anton 450502

## Required links:
### [SonarCloud analysis](https://sonarcloud.io/summary/new_code?id=GyanPosling_TaskManager&branch=main)
### [ER-Diagram](docs/ER-Diagramm.png)

## Overview
This is a Java Spring Boot REST API for managing tasks in a simple task management domain.

## Features
- CRUD for users, projects, tasks, tags, and comments
- Task filtering by status
- Task queries with tags and comments preloaded
- Validation for incoming requests
- Composite creation demo showing partial save vs transactional rollback
- OpenAPI documentation via Springdoc
- PostgreSQL schema migrations via Liquibase
- Docker Compose setup for app + PostgreSQL

