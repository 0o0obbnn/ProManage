# ProManage Project Management System

## Project Overview

ProManage is a comprehensive project management system designed to enhance team collaboration and streamline development workflows. It features a modular architecture with a Spring Boot (Java) backend and a Vue.js frontend.

**Key Technologies:**

*   **Backend:**
    *   Java 21
    *   Spring Boot 3.2.10
    *   Maven
    *   PostgreSQL
    *   Elasticsearch
    *   Redis
    *   MinIO/AWS S3
*   **Frontend:**
    *   Vue.js 3
    *   TypeScript
    *   Vite
    *   Pinia
    *   Ant Design
*   **DevOps & Quality:**
    *   Docker/Kubernetes
    *   Checkstyle, SpotBugs, PMD
    *   JaCoCo (for code coverage)
    *   Vitest (for frontend testing)

**Architecture:**

The backend follows a multi-module Maven structure, separating concerns into `common`, `dto`, `domain`, `infrastructure`, `service`, and `api` modules. The frontend is a modern Vue.js application built with Vite.

## Building and Running

### Backend (Java/Spring Boot)

To build and run the backend, use the following Maven commands from the `backend` directory:

*   **Build:**
    ```bash
    mvn clean install
    ```
*   **Run (from the `promanage-api` module):**
    ```bash
    mvn spring-boot:run
    ```
*   **Run with a specific profile (e.g., `dev`):**
    ```bash
    mvn spring-boot:run -Dspring-boot.run.profiles=dev
    ```
*   **Run tests:**
    ```bash
    mvn test
    ```

### Frontend (Vue.js)

To build and run the frontend, use the following npm scripts from the `frontend` directory:

*   **Install dependencies:**
    ```bash
    npm install
    ```
*   **Run in development mode:**
    ```bash
    npm run dev
    ```
*   **Build for production:**
    ```bash
    npm run build
    ```
*   **Run tests:**
    ```bash
    npm run test
    ```

## Development Conventions

*   **Code Style:** The project enforces a consistent code style using Checkstyle for the backend and ESLint/Prettier for the frontend.
*   **Testing:**
    *   Backend: Unit and integration tests are written using JUnit 5 and Mockito. Code coverage is measured with JaCoCo.
    *   Frontend: Unit and component tests are written with Vitest and Vue Test Utils.
*   **API Documentation:** The backend uses SpringDoc/OpenAPI to generate API documentation.
*   **Database Migrations:** Flyway is used for managing database schema changes.
