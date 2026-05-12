# AGENTS.md — TripMap

## Setup order (must follow this)

```
cp .env.example .env   # then fill in real values
docker-compose up -d   # MySQL 8.0 on :3306
mvn spring-boot:run    # backend on :8080, Hibernate auto-creates schema
cd travel-plan-frontend && npm install && npm run dev  # frontend on :3000
```

## Project boundaries

- **Backend**: root `pom.xml` -> `src/main/java/com/travel/plan/` (Spring Boot 3.2.5, Java 17, Maven)
- **Frontend**: `travel-plan-frontend/` (Vue 3 + TS + Vite 5, npm)
- They run independently. In dev, Vite proxies `/api` -> `localhost:8080`.
- Authoritative project docs: `openspec/project.md`

## Commands

| What | Command |
|------|---------|
| Frontend dev | `cd travel-plan-frontend && npm run dev` |
| Frontend build | `cd travel-plan-frontend && npm run build` (runs `vue-tsc -b` then `vite build`) |
| Typecheck only | `cd travel-plan-frontend && npx vue-tsc -b` |
| Backend run | `mvn spring-boot:run` |
| Backend compile | `mvn compile` |

**No test framework is configured.** No Jest, Vitest, or Maven test suite. Type checking is the only verification step (`vue-tsc -b`, included in build).

**No linter/formatter is configured.** No ESLint or Prettier.

## Architecture and conventions

### Backend (Spring Boot)
- **Jakarta** namespace (`jakarta.persistence.*`), not `javax`.
- **Lombok** is used everywhere (`@Data`, `@RequiredArgsConstructor`, `@Slf4j`).
- **JPA `ddl-auto: update`** -- Hibernate auto-creates/updates tables from entities. No manual SQL migrations.
- **ApiResult\<T\>** wraps every response: `{ code, message, data, timestamp }`.
- JWT stateless auth via `JwtFilter`, BCrypt passwords, 7-day token expiry.
- CORS is configured in `CorsConfig.java`.
- Global exception handling via `GlobalExceptionHandler`.

### Frontend (Vue 3 + TypeScript)
- **`<script setup>`** syntax with Composition API throughout.
- **`@/`** path alias maps to `src/` (configured in both Vite and tsconfig).
- **API inconsistency warning**: `travelApi.ts` response interceptor unwraps `ApiResult` and returns only `res.data`, while `auth.ts` returns the full `response.data` (the raw `ApiResult`). The stores in `src/stores/` use `auth.ts` directly and expect the full `ApiResult` shape.
- `src/types/api.ts` defines `ApiResponse`, `Plan`, `DailyPlan`, and status codes.

## Environment

- Copy `.env.example` to `.env` and fill in DB credentials, mail settings (Gmail app password), and a 64-char hex JWT secret.
- `.env` is gitignored. `.env.example` should stay in sync when adding new env vars.
