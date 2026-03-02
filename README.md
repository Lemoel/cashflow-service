# Cashflow Service

**Backend-only** REST API microservice in Kotlin + Spring Boot 4 with hexagonal architecture (5 Gradle modules), PostgreSQL and Flyway. APIs are REST/RESTful level 3 (Richardson Maturity Model). All data access uses **Spring Data JDBC**. In development, the server runs on port **8081** (`http://localhost:8081`). All application code (Kotlin, SQL) is in English.

## Prerequisites

- Java 21
- PostgreSQL (or Docker)
- Docker (optional, for containerized runs and Testcontainers)
- GraalVM JDK 21 (optional, for native image builds; use `sdk install java 21-graalce` or download from graalvm.org)

## Database Setup

Create the database before running:

```sql
CREATE DATABASE eventosdb;
```

## Commands (Makefile)

| Command | Description |
|---------|-------------|
| `make run` | Run the application (dev server on port 8081) |
| `make build` | Build everything |
| `make test` | Run all tests |
| `make quality` | Run ktlint, tests, and coverage |
| `make format` | Format code with ktlint |
| `make lint` | Run ktlint check |
| `make coverage` | Generate coverage report |
| `make docker-build` | Build Docker image (Jib, no Dockerfile) |
| `make docker-push` | Push image to registry (requires Jib config) |
| `make docker-run` | Run container with .env |
| `make native-build` | Build GraalVM native binary (requires GraalVM JDK) |
| `make native-run` | Run native binary |
| `make setup-hooks` | Install git pre-commit hook (ktlint) |

You can also run all quality checks in one go: `./gradlew qualityCheck` (ktlint, tests, coverage).

## CI (GitHub Actions)

On every push and pull request to `main` or `develop`, the pipeline runs: ktlint, build, unit tests, integration tests (PostgreSQL service), and coverage. The coverage report is uploaded as an artifact.

### Deploy to Cloud Run (Artifact Registry)

The deploy workflow pushes images to **Artifact Registry** (gcr.io was deprecated in March 2025). One-time setup:

1. Create secrets in the repo: `GCP_PROJECT_ID`, `GCP_SA_KEY` (service account JSON key).
2. Run the setup script locally (with gcloud authenticated):
   ```bash
   PROJECT_ID=your-project SA_EMAIL=sa@project.iam.gserviceaccount.com ./scripts/setup-artifact-registry.sh
   ```
3. Trigger the workflow manually: **Actions** → **Deploy to Cloud Run** → **Run workflow**.

## Module Structure

| Module | Role |
|--------|------|
| `cashflow-service-commons` | Shared infrastructure (audit, exceptions, utilities) |
| `cashflow-service-database` | Flyway SQL migrations |
| `cashflow-service-usecase` | Domain logic, entities, ports, services, adapters |
| `cashflow-service-app` | Spring Boot bootstrap, configuration |
| `cashflow-service-tests` | Integration tests (Testcontainers) |

## Architecture

```
API/Client -> Controller REST -> ExampleInputPort -> ExampleService -> ExampleOutputPort <- ExamplePersistenceAdapter -> ExampleRepository (JDBC) -> PostgreSQL
```
