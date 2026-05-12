# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

See also `AGENTS.md` and `openspec/project.md` for additional project context.

## Quick start

```bash
cp .env.example .env   # fill in real values
docker-compose up -d    # MySQL 8.0 on :3306
mvn spring-boot:run     # backend on :8080
cd travel-plan-frontend && npm install && npm run dev  # frontend on :3000
```

## Project layout

- **Backend**: `src/main/java/com/travel/plan/` — Spring Boot 3.2.5, Java 17, Maven, JPA with Hibernate `ddl-auto: update`
- **Frontend**: `travel-plan-frontend/` — Vue 3 + TypeScript + Vite 5, Element Plus, Leaflet, Pinia
- Vite proxies `/api` → `localhost:8080` in dev

## Commands

| What | Command |
|------|---------|
| Backend run | `mvn spring-boot:run` |
| Backend compile | `mvn compile` |
| Frontend dev | `cd travel-plan-frontend && npm run dev` |
| Frontend build | `cd travel-plan-frontend && npm run build` |
| Typecheck only | `cd travel-plan-frontend && npx vue-tsc -b` |

No linter, formatter, or test framework is configured. Type checking (`vue-tsc -b`) is the only verification step.

## Backend architecture

### Layered structure
`controller` → `service` (interface) → `service/impl` → `repository` → `entity`

- **Controllers**: `AuthController`, `TravelPlanController`, `DailyPlanController`, `ProfileController`, `GeocodeController`
- **DTOs**: `controller/dto/` — request/response objects (not entities directly)
- **Entities**: `User`, `TravelPlan`, `DailyPlan`, `EmailVerifyCode`
- **Repositories**: Spring Data JPA interfaces, one per entity
- **Services**: `UserService`, `JwtService`, `TravelPlanService`, `DailyPlanService`, `EmailService`, `GeocodeService` — each with `impl/` implementation

### Key conventions
- **Jakarta** namespace (`jakarta.persistence.*`), not `javax`
- **Lombok** everywhere: `@Data`, `@RequiredArgsConstructor`, `@Slf4j`
- **`ApiResult<T>`** wraps every response: `{ code, message, data, timestamp }`. Static factories: `ApiResult.success(data)`, `ApiResult.error(code, message)`
- **BusinessException** thrown from service layer for business-logic errors. `GlobalExceptionHandler` catches unchecked exceptions and maps to `ApiResult`
- **Error codes** in `common/code/` — each domain has its own enum implementing `ErrorCode` interface
- JWT stateless auth via `JwtAuthenticationFilter` (in `config/`), BCrypt passwords, 7-day token expiry
- `ddl-auto: update` — Hibernate auto-creates/updates schema, no manual migrations
- `SecurityConfig` whitelists `/api/auth/send-code`, `/api/auth/register`, `/api/auth/login`, and `/api/geocode/**` as public

### JWT auth flow
1. Client sends Bearer token in `Authorization` header
2. `JwtAuthenticationFilter` extracts token, validates via `JwtService`, sets `UserPrincipal(id, email)` into `SecurityContextHolder`
3. Controllers get current user via `@AuthenticationPrincipal UserPrincipal` or by looking up `SecurityContextHolder`

## Frontend architecture

### Key files
- `src/api/travelApi.ts` — plan/dailyPlan/geocode API calls, uses shared axios instance with interceptors
- `src/api/auth.ts` — auth API calls (login/register/me), **separate** axios instance
- `src/api/profile.ts` — profile API calls (update avatar, nickname, password, email)
- `src/stores/auth.ts` — Pinia auth store (token, userInfo, login/logout/register)
- `src/router/index.ts` — vue-router with auth guard (`meta.requiresAuth`)
- `src/types/api.ts` — `ApiResponse<T>`, `Plan`, `DailyPlan` type definitions

### Conventions
- **`<script setup>`** syntax with Composition API throughout
- **`@/`** path alias → `src/` (configured in Vite and tsconfig)
- Pinia stores use Composition API style (`defineStore('name', () => { ... })`)

### Critical API inconsistency
`travelApi.ts` response interceptor unwraps `ApiResult` and returns only `res.data`, while `auth.ts` returns the full `response.data` (the raw `ApiResult`). The auth store (`stores/auth.ts`) uses `auth.ts` and expects the full `ApiResult` shape — e.g., `response.data` is the token string from login. New API modules must be consistent with whichever pattern they follow.

## Environment variables

Copy `.env.example` to `.env` and fill in:
- `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` — MySQL connection
- `MAIL_USERNAME`, `MAIL_PASSWORD` — Gmail (app password required)
- `JWT_SECRET` — 64-character hex string

`.env` is gitignored. Keep `.env.example` in sync when adding new vars.
