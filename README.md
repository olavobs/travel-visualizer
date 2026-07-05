# Travel Price Monitor

A multi-user web application for planning and monitoring travel prices across different transport types (flights, buses, cars, boats), built with Clean Architecture.

## Data model

```
Route  (1) ──── (N)  Segment  (1) ──── (N)  PriceRecord
GRU→LIS             FLIGHT                  R$3200, 2026-04-10
Dec 2026            BUS                     R$150,  2026-04-11
                    CAR (Rent in Lisbon)     …
```

A **Route** represents an A → B trip on a specific travel date. Each route can have multiple **Segments** — one per transport mode. Each segment has its own **price history**, letting you compare the total cost of different transport combinations.

Transport types: `FLIGHT | BUS | CAR | BOAT | OTHER` — `OTHER` (and any type) also accepts a free-text `label`.

## Architecture

```
com.flightmonitor
├── domain/                    ← Pure business rules, no framework dependencies
│   ├── model/                 ← Route, Segment, TransportType, PriceRecord, User, Money, Currency
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
    └── web/                   ← RouteController, AuthController, GlobalExceptionHandler
        └── dto/               ← HTTP request/response records
```

### Authentication

The app uses stateless JWT authentication (HS256, 7-day expiry). Each request must carry an `Authorization: Bearer <token>` header. The security boundary is the controller layer only — use cases receive a plain `Long userId` parameter extracted from the token, keeping the application layer free of Spring Security dependencies.

- `POST /api/auth/register` — creates an account, returns a JWT
- `POST /api/auth/login` — validates credentials, returns a JWT
- All `/api/routes/**` endpoints require a valid JWT

### Multi-tenancy

Every route and price record is owned by a user. All queries are scoped to the authenticated user — `existsByIdAndUserId` prevents one user from reading or modifying another's data (IDOR protection at the DB level).

### Why no ORM?

The project uses raw `JdbcTemplate` instead of JPA/Hibernate intentionally. The domain models are plain Java objects with hand-written constructors that enforce business invariants (positive price, non-null fields, etc.). An ORM would require no-arg constructors and mutable state, which would weaken those guarantees. SQL is explicit, queries are readable, and there is no N+1 surprise.

### Caching strategy

`GetRoutePriceSummaryUseCase` uses a read-through cache (Redis, 24h TTL) **per segment**:

1. For each segment, check Redis (`key = segmentId`) → return if hit
2. Query DB for that segment → store in Redis → return

`AddPriceRecordUseCase`, `UpdatePriceRecordUseCase`, and `DeletePriceRecordUseCase` evict the cache entry for the affected segment after every write. `DeleteSegmentUseCase` and `DeleteRouteUseCase` also evict all relevant segment cache entries before deleting.

---

## Prerequisites

| Tool | Version | Purpose |
|------|---------|---------|
| Java | 21+ | Backend |
| Maven | 3.9+ | Backend build |
| Docker & Docker Compose | any recent | Full stack / infra |
| Node.js | **18+** | Frontend dev server & tests |

> **Node 18 is required.** Vite 5 (used by the frontend) explicitly requires Node ≥ 18. Node 16 will fail to start the dev server and the test runner.
>
> If you are on an older version, upgrade via nvm:
> ```bash
> nvm install 18
> nvm use 18
> ```

---

## Environment variables

| Variable | Required | Default | Description |
|----------|----------|---------|-------------|
| `JWT_SECRET` | Recommended in prod | `change-this-secret-to-32-or-more-chars!!` | HS256 signing key — must be ≥ 32 characters |

In production, always set `JWT_SECRET` to a strong random value:
```bash
openssl rand -base64 48
```

Pass it via Docker Compose or as an OS environment variable before starting the app. The default is intentionally weak and should never be used in production.

---

## Running the full stack in Docker

**First run or after schema changes — wipe the database volume first:**

```bash
docker compose down -v && docker compose up --build
```

The `-v` flag removes the MySQL data volume so Flyway applies the migration from scratch.

For subsequent runs (no schema changes):

```bash
docker compose up --build
```

This builds both images and starts all four services: MySQL, Redis, the Spring Boot backend, and the Nginx frontend.

| Service  | URL                                   |
|----------|---------------------------------------|
| Frontend | http://localhost:3000                 |
| Backend  | http://localhost:8080/api             |
| Swagger  | http://localhost:8080/swagger-ui.html |

The frontend container talks to the backend through Docker's internal network
(Nginx proxies `/api/` to `http://app:8080`), so port 8080 does not need to be
open to the browser — only port 3000 does.

To stop and remove containers:

```bash
docker compose down

# Also remove persistent volumes (wipes database):
docker compose down -v
```

---

## Recommended development workflow (fast iteration)

Run only the infrastructure in Docker and everything else locally. This gives you automatic backend restarts on recompile and instant frontend hot-module replacement — no image rebuilds needed.

```bash
# Terminal 1 — infrastructure only (run once, leave it up)
docker compose up -d mysql redis

# Terminal 2 — backend with auto-restart on recompile
mvn spring-boot:run

# Terminal 3 — frontend with instant HMR
cd frontend && npm install && npm run dev
```

| Service  | URL                                   |
|----------|---------------------------------------|
| Frontend | http://localhost:5173                 |
| Backend  | http://localhost:8080/api             |
| Swagger  | http://localhost:8080/swagger-ui.html |

**Backend auto-restart:** Spring Boot DevTools watches for classfile changes. Whenever you recompile a `.java` file (`Cmd+F9` in IntelliJ, or save in VS Code with the Java extension), the Spring context restarts in ~1–2 seconds without you doing anything.

**Frontend HMR:** Vite reflects React changes in the browser instantly, often without a full page reload. The `/api` proxy in `vite.config.js` forwards API calls to `localhost:8080`.

> Docker is still useful for `mvn test` (integration tests need the DB/Redis ports) and for building the final production image.

---

## Running backend tests

Integration tests connect to the **already-running Docker Compose services**
(MySQL on `localhost:3306`, Redis on `localhost:6379`).  
Start the stack before running tests:

```bash
# Terminal 1 — start infrastructure
docker compose up -d mysql redis

# Terminal 2 — run all tests
mvn test

# Just integration tests
mvn test -Dtest="*IntegrationTest"

# Just unit tests (no Docker needed)
mvn test -Dtest="RouteTest,PriceRecordTest,*UseCaseTest"
```

> **Why this approach?**  
> Integration tests use the `test` Spring profile (`application-test.yml`),
> which points directly to the Docker-exposed ports on localhost.
> Database tests are annotated with `@Transactional` so every test rolls back
> automatically — no data leaks between runs.

---

## Running frontend tests

```bash
cd frontend
npm install
npm test          # run once
npm run test:watch  # watch mode
```

Tests use **Vitest** + **React Testing Library** with a jsdom environment. Test files live next to the components they cover (`*.test.jsx` / `*.test.js`).

> **Requires Node 18+.** See the Prerequisites section.

---

## Input validation

Validation is enforced at two layers:

| Layer | What it checks | Error returned |
|-------|---------------|----------------|
| HTTP (DTO, `@Valid`) | `price` not null and positive; `recordedDate` not null; IATA codes are exactly 3 letters; email format; password min 8 chars | `400 Bad Request` with a field-level message |
| Domain model constructor | Same invariants as a second line of defence | `IllegalArgumentException` → `400 Bad Request` |

This means invalid requests are rejected at the controller before they touch the domain, but the domain itself also stays safe if called directly (e.g. in tests).

---

## Debugging the app running in Docker

Use the debug compose override, which adds the JDWP agent and exposes port 5005:

```bash
docker compose -f docker-compose.yml -f docker-compose.debug.yml up --build
```

The app starts normally (`suspend=n`) and listens for a debugger on port 5005.

### Attach from IntelliJ IDEA

1. **Run → Edit Configurations → + → Remote JVM Debug**
2. Set:
   - **Host:** `localhost`
   - **Port:** `5005`
   - **Debugger mode:** Attach to remote JVM
3. Click **Debug** — the debugger connects to the running container.
4. Set breakpoints as usual; they hit on the next matching request.

### Attach from VS Code

Add this to `.vscode/launch.json`:

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

Then open the **Run and Debug** panel and select **Attach to Docker**.

---

## REST API

The full interactive API reference is available at **`/swagger-ui.html`** when the app is running. All `/api/routes/**` endpoints require an `Authorization: Bearer <token>` header.

### Register

```bash
curl -s -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"alice@example.com","password":"password123"}' | jq
# → {"token":"eyJ..."}
```

- `email` must be a valid email address.
- `password` must be at least 8 characters.
- Returns `409 Conflict` if the email is already registered.

### Login

```bash
curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"alice@example.com","password":"password123"}' | jq
# → {"token":"eyJ..."}
```

- Returns `401 Unauthorized` for invalid credentials.
- The token is valid for 7 days.

---

All examples below assume you've stored your token:

```bash
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"alice@example.com","password":"password123"}' | jq -r .token)
```

### Create a travel route

```bash
curl -s -X POST http://localhost:8080/api/routes \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"origin":"REC","destination":"LIS","travelDate":"2026-12-01"}' | jq
```

- `origin` and `destination` must be exactly 3 letters (IATA airport codes).
- `travelDate` is the planned travel date.

### List all routes

```bash
curl -s http://localhost:8080/api/routes \
  -H "Authorization: Bearer $TOKEN" | jq
```

Returns only routes belonging to the authenticated user.

### Delete a route

```bash
curl -s -X DELETE http://localhost:8080/api/routes/{id} \
  -H "Authorization: Bearer $TOKEN"
```

Deletes the route, all its segments, and all price records. Returns `404` if the route doesn't exist or belongs to a different user.

### Mark / unmark a route as booked

```bash
curl -s -X PATCH http://localhost:8080/api/routes/{id}/booked \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"booked":true}' | jq
```

---

### Add a segment to a route

```bash
curl -s -X POST http://localhost:8080/api/routes/{id}/segments \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"transportType":"FLIGHT","label":null}' | jq
```

- `transportType` must be one of `FLIGHT`, `BUS`, `CAR`, `BOAT`, `OTHER`.
- `label` is optional free text (useful for `OTHER` or to add a note to any type).

### List segments for a route

```bash
curl -s http://localhost:8080/api/routes/{id}/segments \
  -H "Authorization: Bearer $TOKEN" | jq
```

### Delete a segment

```bash
curl -s -X DELETE http://localhost:8080/api/routes/{id}/segments/{segId} \
  -H "Authorization: Bearer $TOKEN"
```

Deletes the segment and all its price records.

---

### Add a price observation to a segment

```bash
curl -s -X POST http://localhost:8080/api/routes/{id}/segments/{segId}/prices \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"price":3200.00,"currency":"BRL","recordedDate":"2026-07-01"}' | jq
```

- `currency` must be one of `BRL`, `USD`, `EUR`, `GBP`.
- `recordedDate` is the date you spotted the price — can be any date, past or future.
- Adding a price evicts the segment's Redis cache entry.

### Update a price observation

```bash
curl -s -X PUT http://localhost:8080/api/routes/{id}/segments/{segId}/prices/{priceId} \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"price":2950.00,"currency":"USD","recordedDate":"2026-07-02"}' | jq
```

### List price history for a segment

```bash
curl -s http://localhost:8080/api/routes/{id}/segments/{segId}/prices \
  -H "Authorization: Bearer $TOKEN" | jq
```

Results are ordered by `recordedDate` descending (most recent first).

### Delete a price record

```bash
curl -s -X DELETE http://localhost:8080/api/routes/{id}/segments/{segId}/prices/{priceId} \
  -H "Authorization: Bearer $TOKEN"
```

### Get price summary (per-segment breakdown)

```bash
curl -s http://localhost:8080/api/routes/{id}/prices/summary \
  -H "Authorization: Bearer $TOKEN" | jq
```

Returns a list of per-segment summaries, each with `latestPrice` and `lowestPrice`. Both are `null` if the segment has no price records yet.

---

## Planned features

- **Email verification** — send an activation link on registration; block login until the email is confirmed. Requires `spring-boot-starter-mail` and an SMTP provider (SendGrid, Mailgun, AWS SES, etc.).
- **JWT revocation** — store a blocklist of invalidated token IDs in Redis so logout is effective immediately, even before the 7-day expiry.
- **External price API** — implement `PriceCachePort` in a new adapter; zero changes to domain or application code:

```java
@Component
@Primary  // takes precedence over RedisPriceCacheAdapter
public class ExternalApiPriceCacheAdapter implements PriceCachePort {

    private final FlightApiClient apiClient;
    private final RedisPriceCacheAdapter redisCache;

    @Override
    public Optional<Money> getLatestPrice(Long routeId) {
        return redisCache.getLatestPrice(routeId)
            .or(() -> apiClient.fetchLatestPrice(routeId)
                .map(money -> { redisCache.storeLatestPrice(routeId, money); return money; }));
    }
    // storeLatestPrice and evict delegate to redisCache
}
```
