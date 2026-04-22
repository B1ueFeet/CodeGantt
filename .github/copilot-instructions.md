# Copilot instructions for this repository

This file helps Copilot-based assistants understand and work with this multi-service Gantt project.

Quick commands
- Start local DB: docker-compose up -d (runs Postgres on 5432 as defined in docker-compose.yml).

Backends (Quarkus, Maven wrappers)
- Dev server (live coding):
  - Unix/macOS: ./mvnw quarkus:dev
  - Windows: back-gantt\mvnw.cmd quarkus:dev
- Package: ./mvnw package  (or use -Dquarkus.package.jar.type=uber-jar)
- Run tests (all): ./mvnw test
- Run a single test class or method:
  - Unix/macOS: ./mvnw -Dtest=FullyQualifiedTestClassName test
  - Windows: back-gantt\mvnw.cmd -Dtest=FullyQualifiedTestClassName test
  - To run a single method: -Dtest=TestClassName#methodName
- Native build: ./mvnw package -Dnative  (or add -Dquarkus.native.container-build=true for containerized native builds)
- Quarkus Dev UI (when running dev): http://localhost:8080/q/dev

Front-end (Quasar/Vue)
- Install deps (from front-gantt): npm install (or pnpm/yarn per engines)
- Dev server: npm run dev (from front-gantt)
- Build: npm run build
- Note: front-gantt package.json has scripts: dev, build, postinstall (quasar prepare). No unit-test scripts present.

Docker / Compose
- Start DB only: docker-compose up -d
- Useful to bring up DB before starting Quarkus apps that expect Postgres.

High-level architecture
- front-gantt (Quasar SPA) — client-side app that calls the backend APIs and stores JWT in localStorage.
- back-gantt (Quarkus JVM app) — main application providing domain models, repositories (Panache), services, and JAX-RS resources. Uses PanacheRepositoryBase and DTOs; services are @ApplicationScoped.
- auth-service (Quarkus) — dedicated auth microservice exposing /auth endpoints (login, seed-admin) and issuing JWTs (RSA keys in src/main/resources).
- Shared Postgres database (docker-compose.yml) used by the services; schema snippets live in the repo root README.md.

Key conventions and patterns
- Java packages under com.bf. Repositories extend PanacheRepositoryBase (e.g., AppUserRepository) and expose convenience finders.
- Service layer (com.bf.Service) contains transactional business logic; JAX-RS resources expose endpoints under / (e.g., /auth).
- Passwords: BCrypt used for hashing (org.mindrot.jbcrypt.BCrypt).
- JWTs: auth-service generates tokens using RSA keys in src/main/resources (publicKey.pem/privateKey.pem). The front-end stores the token in localStorage and parses roles/groups from the JWT payload (see src/services/auth.js).
- Database: schema in README.md shows tables: app_user, project, task, project_member, task_assignee.
- Windows notes: Use mvnw.cmd when invoking Maven from Windows shells in service folders (back-gantt, auth-service).

IDE / Dev notes for Copilot sessions
- Prefer opening the specific service folder (back-gantt, auth-service, front-gantt) for targeted edits.
- For backend changes, run Quarkus in dev mode (quarkus:dev) to get hot reload and the Dev UI.
- To verify DB work, bring up Postgres with docker-compose before starting Quarkus apps.

Files to check for additional assistant guidance
- back-gantt/README.md
- auth-service/README.md
- front-gantt/README.md
- root README.md (contains DB schema)

If there are other AI assistant config files (CLAUDE.md, AGENTS.md, .cursorrules, .windsurfrules, AIDER_CONVENTIONS.md), incorporate their contents here.
