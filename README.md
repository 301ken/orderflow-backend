# Orderflow API

Production-style Spring Boot REST API for managing clients, products, and orders.

## Features

- CRUD APIs for `Client`, `Product`, and `Order`
- Request validation and global exception handling
- OpenAPI/Swagger UI for interactive API testing
- Spring Security with JWT-based authentication
- PostgreSQL for runtime data and H2 for tests
- Dockerized app + database via Docker Compose

## Tech Stack

- Java 17
- Spring Boot 3.3
- Spring Data JPA + Hibernate
- Spring Security + JWT (`jjwt`)
- springdoc OpenAPI
- PostgreSQL, H2
- Maven, Docker

## API Documentation

After starting the app, open:

- Swagger UI: [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)
- OpenAPI JSON: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

## Authentication

All business endpoints are protected. Get a token first:

`POST /auth/login`

```json
{
  "username": "admin",
  "password": "admin123"
}
```

Use the returned token as:

`Authorization: Bearer <token>`

## Local Run (Without Docker)

### 1) Configure environment variables (optional)

- `DB_URL` (default: `jdbc:postgresql://localhost:5432/orderflow_db`)
- `DB_USERNAME` (default: `postgres`)
- `DB_PASSWORD` (default: `postgres`)
- `APP_USERNAME` (default: `admin`)
- `APP_PASSWORD` (default: `admin123`)
- `JWT_SECRET` (Base64, default already set for local dev)
- `JWT_EXPIRATION_SECONDS` (default: `3600`)

### 2) Start application

The app will start on port `8080`. By default, it uses an in-memory H2 database if no PostgreSQL environment variables are provided.

- Windows: `mvnw.cmd spring-boot:run`
- macOS/Linux: `./mvnw spring-boot:run`

### 3) Access H2 Console (to see data)
Open [http://localhost:8080/h2-console](http://localhost:8080/h2-console)
- **JDBC URL**: `jdbc:h2:mem:orderflow_db`
- **User**: `sa`
- **Password**: (leave empty)

## Run with Docker

`docker compose up --build`

This starts:

- App: `http://localhost:8080`
- PostgreSQL: `localhost:5432`

## Tests

- Windows: `mvnw.cmd test`
- macOS/Linux: `./mvnw test`

Security and order service tests run on in-memory H2 (`src/test/resources/application.properties`).

## Resume / Portfolio Highlights

This project demonstrates:

- secure REST API design with JWT
- API documentation with Swagger/OpenAPI
- relational modeling with JPA and validation
- containerized deployment using Docker Compose
- integration testing for auth and service flows

## Publish Checklist (GitHub)

1. Ensure tests pass: `mvnw.cmd test`
2. Create repository and push:
   - `git add .`
   - `git commit -m "Finalize Orderflow API with JWT, Swagger, Docker, and tests"`
   - `git push origin main`
3. Add repository link to your resume
4. In resume bullet, focus on measurable outcomes (security, docs, deployability, testing)
