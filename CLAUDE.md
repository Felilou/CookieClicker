# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

CockieClicker is a multiplayer cookie-clicking game built as a Spring Boot microservices application with a Nuxt 4 frontend. Services are secured with Keycloak OAuth2/JWT.

## Architecture

Four independent services communicate over HTTP and WebSocket:

| Service | Port | Role |
|---|---|---|
| `auth-service` | 8081 | User registration/login, JWT via Keycloak |
| `game-service` | 8080 | Real-time multiplayer game rooms (WebSocket/STOMP) |
| `persistance-service` | 8082 | Game stats & history (JPA + PostgreSQL) |
| `frontend` | 3000 | Nuxt 4 / Vue 3 SPA |

Infrastructure (via Docker Compose):
- **Keycloak 24** on port 8180 — identity provider (realm: `cookieclicker`, admin: `admin/admin`)
- **postgres-keycloak** on port 5433 — Keycloak's database
- **postgres-app** on port 5434 — application database (`cookieclicker`, credentials: `postgres/postgres`)

### Inter-service Communication

- **persistance-service → auth-service**: REST via `AuthServiceClient` (WebClient). Uses `GET /auth/username` and `GET /auth/validate` with `Authorization: Bearer <token>` headers.
- **frontend → game-service**: WebSocket STOMP at `ws://localhost:8080/ws`. Destinations: `/room/{gameID}` and `/game/createRoom/{roomID}`.
- **frontend → persistance-service**: REST for stats/history.
- All backend services validate JWT tokens issued by Keycloak.

### Game Service Internals

Rooms are stored in-memory (`ConcurrentHashMap`). State machine: `WAITING_FOR_PLAYERS → COUNTDOWN → IN_PROGRESS → ENDED`. Messages use `Action` enum (JOIN, CLICK, LEAVE).

## Build & Run

### Start Infrastructure First

```bash
docker-compose up -d
```

### Build All Services

```bash
./mvnw clean install
```

### Run Individual Services

```bash
./mvnw -pl auth-service spring-boot:run
./mvnw -pl game-service spring-boot:run
./mvnw -pl persistance-service spring-boot:run
```

### Run Tests

```bash
# All modules
./mvnw clean test

# Single module
./mvnw -pl auth-service test
./mvnw -pl game-service test
./mvnw -pl persistance-service test
```

### Frontend

```bash
cd frontend
npm install
npm run dev      # dev server on port 3000
npm run build    # production build
```

## Tech Stack

- **Java 25**, **Spring Boot 4.0.3**, Spring Security + OAuth2 Resource Server
- **Spring WebSocket** (STOMP) for real-time game communication
- **Spring Data JPA** / Hibernate with PostgreSQL (`update` DDL mode)
- **Spring WebClient** for inter-service HTTP calls
- **Lombok** for code generation
- **Nuxt 4.3.1** / Vue 3 / TypeScript on the frontend
- Maven multi-module project (root `pom.xml` defines parent; `game-service` has its own pom but is not in the root modules list)

## Package Conventions

- Backend: `at.spengergasse.<service-name>` (e.g., `at.spengergasse.gameservice`)
- Group ID: `at.spengergasse`, artifact IDs match service folder names