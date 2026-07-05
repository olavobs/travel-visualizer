# TripTrack

[![CI](https://github.com/olavobs/travel-visualizer/actions/workflows/ci.yml/badge.svg)](https://github.com/olavobs/travel-visualizer/actions/workflows/ci.yml)

A multi-user web application for planning and monitoring travel prices across different transport types (flights, buses, cars, boats). Built with Clean Architecture on Spring Boot + React.

---

## What it does

- Create **Routes** (A → B, on a travel date) and track how prices evolve over time
- Add multiple **Segments** per route — one per transport mode (flight, bus, car, boat, or custom)
- Log **Price observations** manually whenever you check a fare; the app stores the history
- **Mark a price as purchased** — the app records which fare you actually bought and auto-sets the route to Booked
- **Journey tree** — visualises all your routes as a tree; click any destination node to see the total trip cost
- **Price trend chart** — line chart per segment showing how prices changed over time
- **Route status** — WATCHING → BOOKED → CANCELLED lifecycle
- Available in **English and Portuguese** (language toggle, persists in localStorage)

---

## Data model

```
Route  (1) ──── (N)  Segment  (1) ──── (N)  PriceRecord
GRU→LIS             FLIGHT                  R$3200, 2026-04-10
Dec 2026            BUS                     R$150,  2026-04-11
                    CAR (Rent in Lisbon)     …
```

A **Route** is an A → B trip on a specific date. Each route has multiple **Segments** (one per transport leg). Each segment has its own **price history** so you can compare the total cost of different transport combinations.

Transport types: `FLIGHT | BUS | CAR | BOAT | OTHER`  
`OTHER` (and any type) accepts a free-text `label`.

---

## Tech stack

| Layer | Technology |
|-------|-----------|
| Backend | Java 21, Spring Boot 3.2, Spring Security, Spring JDBC |
| Database | MySQL 8.0 with Flyway migrations |
| Cache | Redis 7 |
| Auth | JWT (HS256, 7-day expiry, stateless) |
| API docs | SpringDoc OpenAPI (Swagger UI) |
| External fares | SerpApi (Google Flights) |
| Frontend | React 18, Vite 5 |
| Charts | Recharts |
| Containerisation | Docker, Docker Compose |
| Testing | JUnit 5, Mockito, Vitest, React Testing Library |

---

## Architecture

```
com.flightmonitor
├── domain/                    ← Pure business rules, no framework dependencies
│   ├── model/                 ← Route, Segment, TransportType, PriceRecord, User, Money, Currency, RouteStatus
│   ├── repository/            ← RouteRepository, SegmentRepository, PriceRecordRepository, UserRepository (interfaces)
│   ├── port/                  ← PriceCachePort (interface)
│   └── exception/             ← RouteNotFoundException, PriceRecordNotFoundException
├── application/               ← Orchestrates domain objects, no HTTP/DB/Security knowledge
│   ├── usecase/               ← One class per feature
│   └── dto/                   ← Request/Response records (Money-typed, userId-scoped)
├── infrastructure/            ← I/O adapters (DB, cache, security)
│   ├── persistence/           ← JdbcRouteRepository, JdbcSegmentRepository, JdbcPriceRecordRepository, JdbcUserRepository
│   ├── cache/                 ← RedisPriceCacheAdapter
│   └── security/              ← JwtService, JwtAuthenticationFilter, SecurityConfig, UserAuthentication
└── interfaces/
    └── web/                   ← RouteController, SegmentController, PriceRecordController, AuthController, GlobalExceptionHandler
        ├── docs/              ← OpenAPI/Swagger annotation interfaces (RouteControllerDocs, SegmentControllerDocs, PriceRecordControllerDocs)
        └── dto/               ← HTTP request/response records
```

---

## Key design decisions

### Why no ORM?

Raw `JdbcTemplate` is used instead of JPA/Hibernate. The domain models are plain Java objects with hand-written constructors that enforce business invariants (positive price, non-null fields, etc.). An ORM would require no-arg constructors and mutable state, weakening those guarantees. SQL is explicit, queries are readable, and there are no N+1 surprises.

### Authentication boundary

The security boundary is the controller layer only. Use cases receive a plain `Long userId` extracted from the JWT — they know nothing about Spring Security. This keeps the application layer testable without a security context.

- `POST /v1/auth/register` — creates an account, returns a JWT
- `POST /v1/auth/login` — validates credentials, returns a JWT
- All `/v1/routes/**` endpoints require `Authorization: Bearer <token>`

### Multi-tenancy (IDOR protection)

Every resource is owned by a user. All queries are scoped with `userId` — `existsByIdAndUserId` is used before any mutation, preventing one user from reading or modifying another's data at the database level.

### Caching strategy

`GetRoutePriceSummaryUseCase` uses a read-through Redis cache (24h TTL) **keyed by segmentId**:

1. Check Redis → return on hit
2. Query DB → store in Redis → return

All write use cases (add/update/delete price, mark as purchased) evict the affected segment's cache entry. Delete cascade (route or segment delete) evicts all relevant entries before deletion.

### Route status lifecycle

Routes go through three statuses: `WATCHING → BOOKED → CANCELLED`. Status is changed manually from the route detail screen, or automatically set to `BOOKED` when a price record is marked as purchased.

### Purchased price

Each segment can have at most one price record flagged as purchased at a time. The `markAsPurchased` toggle:
- If not purchased → clears all purchased flags on the segment, marks this one, sets route to BOOKED
- If already purchased → clears the flag (undo)

The journey tree uses `purchasedPrice ?? lowestPrice` as the representative cost per segment.

### Database migrations

Five Flyway migrations in order:

| Version | Change |
|---------|--------|
| V1 | Initial schema: users, flight_routes, price_records |
| V2 | Add `booked` boolean to routes |
| V3 | Rename to travel_routes, add travel_segments, migrate price_records to segment-based |
| V4 | Replace `booked` boolean with `status` enum (WATCHING/BOOKED/CANCELLED) |
| V5 | Add `purchased` boolean to price_records |

---

## Prerequisites

| Tool | Version | Purpose |
|------|---------|---------|
| Java | 21+ | Backend |
| Maven | 3.9+ | Backend build |
| Docker & Docker Compose | any recent | Full stack / infrastructure |
| Node.js | **18+** | Frontend dev server & tests |

> **Node 18 is required.** Vite 5 requires Node ≥ 18. Node 16 will fail.
> Upgrade via nvm: `nvm install 18 && nvm use 18`

---

## Environment variables

| Variable | Required | Default | Description |
|----------|----------|---------|-------------|
| `JWT_SECRET` | Recommended in prod | `change-this-secret-to-32-or-more-chars!!` | HS256 signing key — must be ≥ 32 characters |
| `SERPAPI_KEY` | Optional | _(empty)_ | SerpApi key for automated daily fare fetching — get one at serpapi.com |

Generate a strong JWT secret for production:
```bash
openssl rand -base64 48
```

When `SERPAPI_KEY` is not set, the app starts normally and a **stub adapter** activates instead, returning random prices — useful for local development without spending API credits.

---

## Running with Docker (full stack)

**First run or after schema changes — wipe the DB volume first:**

```bash
docker compose down -v && docker compose up --build
```

Subsequent runs (no schema changes):

```bash
docker compose up --build
```

| Service  | URL |
|----------|-----|
| Frontend | http://localhost:3000 |
| Backend  | http://localhost:8080/v1 |
| Swagger  | http://localhost:8080/swagger-ui.html |

The frontend Nginx container proxies `/v1/` to the backend over Docker's internal network — port 8080 does not need to be open in the browser.

Stop and remove containers:

```bash
docker compose down        # keep volumes
docker compose down -v     # also wipe database
```

---

## Development workflow (fast iteration)

Run only the infrastructure in Docker, everything else locally. This gives hot-module replacement on the frontend and fast backend restarts without rebuilding images.

A `Makefile` at the project root provides shortcut commands:

```bash
make up       # infra + backend with stub fare fetcher (no API key needed)
make up-real  # infra + backend with real SerpApi fare fetcher (reads SERPAPI_KEY from .env)
make frontend # frontend dev server
make stop     # docker compose down
```

Or run manually:

```bash
# Terminal 1 — infrastructure only (run once)
docker compose up -d mysql redis

# Terminal 2 — backend (stub mode)
mvn spring-boot:run

# Terminal 2 — backend (real SerpApi mode)
export SERPAPI_KEY=your_key_here
mvn spring-boot:run

# Terminal 3 — frontend
cd frontend && npm install && npm run dev
```

| Service  | URL |
|----------|-----|
| Frontend | http://localhost:5173 |
| Backend  | http://localhost:8080/v1 |
| Swagger  | http://localhost:8080/swagger-ui.html |

**Backend auto-restart:** Spring Boot DevTools watches for classfile changes. Recompile a `.java` file (`Cmd+F9` in IntelliJ) and the context restarts in ~1–2 seconds.

**Frontend HMR:** Vite reflects React changes instantly. The `/api` proxy in `vite.config.js` forwards requests to `localhost:8080`.

---

## CI

Every push and pull request to `main` runs the full test suite automatically via GitHub Actions (`.github/workflows/ci.yml`). The workflow runs two jobs in parallel:

| Job | What it does |
|-----|-------------|
| `backend` | Spins up MySQL 8 + Redis 7, then runs `mvn test` (unit + integration) |
| `frontend` | Installs Node 20, then runs Vitest |

---

## Running tests

### Backend tests

Integration tests connect to the already-running Docker Compose services.

```bash
# Start infrastructure first
docker compose up -d mysql redis

# Run all tests
mvn test

# Integration tests only
mvn test -Dtest="*IntegrationTest"

# Unit tests only (no Docker needed)
mvn test -Dtest="RouteTest,PriceRecordTest,*UseCaseTest"
```

Integration tests use the `test` Spring profile (`src/test/resources/application-test.yml`) and are annotated with `@Transactional` — every test rolls back automatically, no data leaks between runs.

### Frontend tests

```bash
cd frontend
npm install
npm test            # run once
npm run test:watch  # watch mode
```

Uses **Vitest** + **React Testing Library** with jsdom. Test files live next to the components they cover (`*.test.jsx` / `*.test.js`).

---

## Debugging in Docker

Use the debug compose override, which adds the JDWP agent on port 5005:

```bash
docker compose -f docker-compose.yml -f docker-compose.debug.yml up --build
```

**IntelliJ IDEA:** Run → Edit Configurations → + → Remote JVM Debug → host `localhost`, port `5005`.

**VS Code** — add to `.vscode/launch.json`:

```json
{
  "version": "0.2.0",
  "configurations": [
    {
      "type": "java",
      "name": "Attach to Docker",
      "request": "attach",
      "hostName": "localhost",
      "port": 5005
    }
  ]
}
```

---

## REST API

Full interactive docs at **`/swagger-ui.html`** when the app is running.

All `/v1/routes/**` endpoints require: `Authorization: Bearer <token>`

Store your token:
```bash
TOKEN=$(curl -s -X POST http://localhost:8080/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"alice@example.com","password":"password123"}' | jq -r .token)
```

### Auth

```bash
# Register
curl -s -X POST http://localhost:8080/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"alice@example.com","password":"password123"}' | jq

# Login
curl -s -X POST http://localhost:8080/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"alice@example.com","password":"password123"}' | jq
```

### Routes

```bash
# Create
curl -s -X POST http://localhost:8080/v1/routes \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"origin":"REC","destination":"LIS","travelDate":"2026-12-01"}' | jq

# List
curl -s http://localhost:8080/v1/routes -H "Authorization: Bearer $TOKEN" | jq

# Delete
curl -s -X DELETE http://localhost:8080/v1/routes/{id} -H "Authorization: Bearer $TOKEN"

# Update status (WATCHING | BOOKED | CANCELLED)
curl -s -X PATCH http://localhost:8080/v1/routes/{id}/status \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"status":"BOOKED"}' | jq

# Price summary (latest/lowest/purchased per segment)
curl -s http://localhost:8080/v1/routes/{id}/prices/summary \
  -H "Authorization: Bearer $TOKEN" | jq
```

### Segments

```bash
# Add segment
curl -s -X POST http://localhost:8080/v1/routes/{id}/segments \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"transportType":"FLIGHT","label":null}' | jq

# List segments
curl -s http://localhost:8080/v1/routes/{id}/segments \
  -H "Authorization: Bearer $TOKEN" | jq

# Delete segment
curl -s -X DELETE http://localhost:8080/v1/routes/{id}/segments/{segId} \
  -H "Authorization: Bearer $TOKEN"
```

### Prices

```bash
# Add price
curl -s -X POST http://localhost:8080/v1/routes/{id}/segments/{segId}/prices \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"price":3200.00,"currency":"BRL","recordedDate":"2026-07-01"}' | jq

# List price history (newest first)
curl -s http://localhost:8080/v1/routes/{id}/segments/{segId}/prices \
  -H "Authorization: Bearer $TOKEN" | jq

# Update price
curl -s -X PUT http://localhost:8080/v1/routes/{id}/segments/{segId}/prices/{priceId} \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"price":2950.00,"currency":"USD","recordedDate":"2026-07-02"}' | jq

# Delete price
curl -s -X DELETE http://localhost:8080/v1/routes/{id}/segments/{segId}/prices/{priceId} \
  -H "Authorization: Bearer $TOKEN"

# Toggle purchased flag (mark if not purchased, unmark if already purchased)
curl -s -X PATCH http://localhost:8080/v1/routes/{id}/segments/{segId}/prices/{priceId}/purchase \
  -H "Authorization: Bearer $TOKEN" | jq
```

Currencies supported: `BRL`, `USD`, `EUR`, `GBP`

---

## Input validation

| Layer | What it checks | Error |
|-------|---------------|-------|
| HTTP DTO (`@Valid`) | positive price, non-null date, 3-letter IATA codes, valid email, password ≥ 8 chars | `400 Bad Request` |
| Domain constructor | Same invariants as a second line of defence | `IllegalArgumentException` → `400` |

---

## Automated price fetching

Every day at **8 am (America/Sao_Paulo)** a scheduled job runs across all routes with status `WATCHING`. For each `FLIGHT` segment it calls the **SerpApi Google Flights** endpoint, extracts the lowest fare, and saves it as a new `PriceRecord` — exactly as if you had logged the price manually.

### How it works

```
[Cron 08:00 BRT]
  PriceFetchScheduler
    → FetchAndStoreDailyPricesUseCase
        → RouteRepository.findAllByStatus(WATCHING)
        → SegmentRepository.findByRouteId()     [per route]
        → [skip non-FLIGHT segments]
        → FareFetcherPort.fetchLowestFare(route) [per FLIGHT segment]
              real: SerpApiFareFetcherAdapter → serpapi.com/search?engine=google_flights
              stub: StubFareFetcherAdapter   → random price (when SERPAPI_KEY is unset)
        → PriceRecordRepository.save()
        → PriceCachePort.evict()
```

Non-FLIGHT segments (BUS, CAR, BOAT, OTHER) are silently skipped — SerpApi only covers flights. A per-segment error is logged and skipped without aborting the rest of the job.

### Trigger manually (dev/testing)

To run the job immediately without waiting for 8 am, call the use case directly or temporarily shorten the cron in `PriceFetchScheduler.java`:

```java
@Scheduled(cron = "0 */1 * * * *")  // every minute — revert after testing
```

### Architecture note

The external API dependency is isolated behind `FareFetcherPort` (a domain port interface). Swapping to a different fare provider requires only a new infrastructure adapter — the domain and application layers stay unchanged.

---

## Planned features

- **Email price alerts** — notify the user when a segment's price drops below a threshold (Spring Mail + threshold stored per segment)
- **PWA / installable** — `manifest.json` + service worker so the app can be installed on a phone's home screen
- **JWT revocation** — store invalidated token IDs in Redis so logout is effective before the 7-day expiry
