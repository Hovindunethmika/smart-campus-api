# Smart Campus Sensor & Room Management API

A JAX-RS (Jersey) RESTful web application for managing campus rooms and IoT sensors.  
Built for **5COSC022W Client-Server Architectures** coursework — University of Westminster.

---

## API Design Overview

The API follows REST architectural principles with a versioned base path `/api/v1`.

| Resource | Base Path | Description |
|----------|-----------|-------------|
| Discovery | `GET /api/v1/` | API metadata and hypermedia links |
| Rooms | `/api/v1/rooms` | Campus room management |
| Sensors | `/api/v1/sensors` | IoT sensor registration and filtering |
| Readings | `/api/v1/sensors/{id}/readings` | Historical sensor data (sub-resource) |

**Storage:** Entirely in-memory using `ConcurrentHashMap` and `ArrayList` — no database used.  
**Stack:** Java 11 · JAX-RS 2.x (Jersey 2.40) · Servlet container (GlassFish / Tomcat) · Jackson JSON.

---

## Building and Running the Project

### Prerequisites
- Java 11 or higher
- Maven 3.6 or higher
- NetBeans 12+ with GlassFish or Apache Tomcat configured

### Option A — NetBeans (recommended)

1. Open NetBeans → **File → Open Project** → select the `smart-campus-api` folder
2. Right-click the project → **Clean and Build**
3. Right-click the project → **Run**
4. NetBeans deploys the WAR to GlassFish/Tomcat automatically

The API will be available at:
```
http://localhost:8080/smart-campus-api/api/v1
```

### Option B — Maven command line with Tomcat

```bash
# Build the WAR
mvn clean package

# Deploy the WAR to a running Tomcat instance, or use the tomcat7 plugin:
mvn tomcat7:run
```

> **Note:** The context path (`/smart-campus-api`) comes from the WAR file name.
> All Postman requests and curl commands below use this full path.

---

## Sample curl Commands

### 1. Discovery — GET /api/v1/
```bash
curl -s http://localhost:8080/smart-campus-api/api/v1/ | python3 -m json.tool
```

### 2. List all rooms — GET /api/v1/rooms
```bash
curl -s http://localhost:8080/smart-campus-api/api/v1/rooms | python3 -m json.tool
```

### 3. Create a new room — POST /api/v1/rooms
```bash
curl -s -X POST http://localhost:8080/smart-campus-api/api/v1/rooms \
  -H "Content-Type: application/json" \
  -d '{"id":"ENG-201","name":"Engineering Lab","capacity":35}' \
  | python3 -m json.tool
```
Expected: `201 Created` with a `Location` header pointing to the new room.

### 4. Get a specific room — GET /api/v1/rooms/{roomId}
```bash
curl -s http://localhost:8080/smart-campus-api/api/v1/rooms/LIB-301 \
  | python3 -m json.tool
```

### 5. Delete a room with sensors (expect 409) — DELETE /api/v1/rooms/{roomId}
```bash
curl -s -X DELETE http://localhost:8080/smart-campus-api/api/v1/rooms/LIB-301 \
  | python3 -m json.tool
```
Expected: `409 Conflict` — LIB-301 has TEMP-001 assigned.

### 6. Delete an empty room (expect 204) — DELETE /api/v1/rooms/{roomId}
```bash
# First create a room with no sensors
curl -s -X POST http://localhost:8080/smart-campus-api/api/v1/rooms \
  -H "Content-Type: application/json" \
  -d '{"id":"EMPTY-01","name":"Empty Room","capacity":10}'

# Then delete it
curl -s -o /dev/null -w "%{http_code}" \
  -X DELETE http://localhost:8080/smart-campus-api/api/v1/rooms/EMPTY-01
```
Expected: `204 No Content`

### 7. Register a sensor with invalid roomId (expect 422)
```bash
curl -s -X POST http://localhost:8080/smart-campus-api/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{"type":"Temperature","roomId":"DOES-NOT-EXIST"}' \
  | python3 -m json.tool
```
Expected: `422 Unprocessable Entity`

### 8. Register a valid sensor — POST /api/v1/sensors
```bash
curl -s -X POST http://localhost:8080/smart-campus-api/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{"id":"CO2-002","type":"CO2","roomId":"ENG-201","currentValue":400.0}' \
  | python3 -m json.tool
```
Expected: `201 Created`

### 9. Filter sensors by type — GET /api/v1/sensors?type=CO2
```bash
curl -s "http://localhost:8080/smart-campus-api/api/v1/sensors?type=CO2" \
  | python3 -m json.tool
```

### 10. Post a reading to an active sensor — POST /api/v1/sensors/{id}/readings
```bash
curl -s -X POST \
  http://localhost:8080/smart-campus-api/api/v1/sensors/TEMP-001/readings \
  -H "Content-Type: application/json" \
  -d '{"value":23.7}' \
  | python3 -m json.tool
```
Expected: `201 Created`. Check that `currentValue` on TEMP-001 is now `23.7`.

### 11. Get reading history — GET /api/v1/sensors/{id}/readings
```bash
curl -s http://localhost:8080/smart-campus-api/api/v1/sensors/TEMP-001/readings \
  | python3 -m json.tool
```

### 12. Post a reading to a MAINTENANCE sensor (expect 403)
```bash
curl -s -X POST \
  http://localhost:8080/smart-campus-api/api/v1/sensors/OCC-001/readings \
  -H "Content-Type: application/json" \
  -d '{"value":12}' \
  | python3 -m json.tool
```
Expected: `403 Forbidden` — OCC-001 starts in MAINTENANCE status.

### 13. Update sensor status — PUT /api/v1/sensors/{id}/status
```bash
curl -s -X PUT \
  http://localhost:8080/smart-campus-api/api/v1/sensors/OCC-001/status \
  -H "Content-Type: application/json" \
  -d '{"status":"ACTIVE"}' \
  | python3 -m json.tool
```

---

## Project Structure

```
smart-campus-api/
├── pom.xml
├── README.md
└── src/main/
    ├── webapp/WEB-INF/
    │   └── web.xml                        ← registers Jersey servlet
    └── java/com/smartcampus/
        ├── config/
        │   └── AppConfig.java             ← @ApplicationPath + class registry
        ├── model/
        │   ├── Room.java
        │   ├── Sensor.java
        │   └── SensorReading.java
        ├── store/
        │   └── DataStore.java             ← singleton ConcurrentHashMap store
        ├── resource/
        │   ├── DiscoveryResource.java     ← GET /api/v1/
        │   ├── RoomResource.java          ← /api/v1/rooms
        │   ├── SensorResource.java        ← /api/v1/sensors
        │   └── SensorReadingResource.java ← sub-resource /readings
        ├── exception/
        │   ├── RoomNotEmptyException.java
        │   ├── RoomNotEmptyMapper.java         ← 409
        │   ├── LinkedResourceNotFoundException.java
        │   ├── LinkedResourceNotFoundMapper.java   ← 422
        │   ├── SensorUnavailableException.java
        │   ├── SensorUnavailableMapper.java    ← 403
        │   └── GlobalExceptionMapper.java      ← 500 catch-all
        └── filter/
            └── LoggingFilter.java         ← request/response logging
```

---

## Report: Answers to Coursework Questions

### Part 1.1 — JAX-RS Resource Lifecycle

By default, JAX-RS creates a **new instance** of each resource class for every
incoming HTTP request (request-scoped lifecycle). This means instance fields on
resource classes are not shared between requests and cannot hold persistent state.

To manage shared in-memory data safely, this project uses a **singleton
`DataStore` class** backed by `ConcurrentHashMap<K,V>`. ConcurrentHashMap
provides thread-safe atomic operations for `put`, `get`, and `remove` without
requiring explicit `synchronized` blocks, preventing race conditions when
multiple clients send concurrent requests. Reading lists are wrapped with
`Collections.synchronizedList()` to guard list-level operations such as `add`.

---

### Part 1.2 — HATEOAS and Hypermedia Design

HATEOAS (Hypermedia as the Engine of Application State) means that API responses
embed links to related resources and available actions, so clients can navigate
the API dynamically rather than relying on external documentation.

Benefits over static documentation:
1. **Self-discoverability** — a client can start at `GET /api/v1/` and navigate
   the entire API by following links, with no prior knowledge of URLs.
2. **Reduced coupling** — if a server-side URL changes, clients that follow links
   rather than hardcode paths adapt automatically.
3. **Always in sync** — embedded links reflect the live API state; static docs
   can go stale after updates.
4. **Lower onboarding cost** — new developers explore the API interactively
   instead of reading a lengthy specification first.

---

### Part 2.1 — IDs Only vs Full Objects in List Responses

Returning **only IDs** is bandwidth-efficient but forces clients to make N
additional GET requests to resolve each ID — the classic "N+1 problem" — which
adds latency and increases server load under high traffic.

Returning **full objects** increases payload size but eliminates round-trips and
simplifies client logic. It is preferred when clients regularly consume the full
data, such as rendering a room list in a UI. For very large collections,
pagination with field projection (returning a subset of fields) offers the best
of both approaches.

---

### Part 2.2 — DELETE Idempotency

DELETE is **idempotent** in this implementation. The HTTP specification defines
idempotency in terms of server state, not response codes.

- **First call:** room exists → deleted → server returns `204 No Content`.
- **Second call:** room is already absent → server returns `404 Not Found`.

The server state after both calls is identical — the room does not exist.
Returning `404` on a repeated call does not violate idempotency because the
state change (room deletion) happened exactly once.

---

### Part 3.1 — @Consumes Mismatch

JAX-RS inspects the `Content-Type` header of every incoming request and matches
it against the `@Consumes` annotation on the candidate method. If a client sends
`Content-Type: text/plain` or `application/xml` to a method annotated with
`@Consumes(MediaType.APPLICATION_JSON)`, JAX-RS **automatically returns
HTTP 415 Unsupported Media Type** — the method body never executes. No custom
code is required. This protects the API from malformed or unexpected payloads at
the framework level before any deserialisation is attempted.

---

### Part 3.2 — @QueryParam vs @PathParam for Filtering

Using `@QueryParam` (`GET /sensors?type=CO2`) is the correct approach for
collection filtering because:

1. **Optional by nature** — path segments imply a fixed resource hierarchy;
   query parameters are inherently optional, matching the semantics of a filter.
2. **Multiple filters compose naturally** — `?type=CO2&status=ACTIVE` requires
   no change to the path template, whereas path-based filters would need a new
   route per combination.
3. **REST semantics** — `/sensors` always identifies the full collection; the
   query string narrows the result set without changing the resource identity.
4. **Tooling compatibility** — HTTP caches, proxies, and analytics tools treat
   query strings as modifiers, not as distinct resources.

---

### Part 4.1 — Sub-Resource Locator Pattern Benefits

The sub-resource locator delegates handling of nested paths to a dedicated class,
rather than adding more methods to the parent resource. Benefits:

1. **Single Responsibility** — `SensorResource` handles sensor registration;
   `SensorReadingResource` handles reading history only. Neither class grows
   unmanageably large.
2. **Context injection at construction** — `sensorId` is passed to
   `SensorReadingResource` via its constructor, avoiding repeated `@PathParam`
   declarations on every method.
3. **Independent testability** — each class can be unit-tested in isolation
   without loading the entire parent resource.
4. **Scalability** — in large APIs with deep nesting (buildings/floors/rooms/
   sensors/readings), each level is one small class rather than one large
   controller with dozens of methods.

---

### Part 5.1 — Why HTTP 422 is More Appropriate than 404

`404 Not Found` signals that the **request URL** does not resolve to a known
resource. Here, `POST /api/v1/sensors` is a valid, existing endpoint — the URL
is fine.

The problem is that a **value inside the JSON payload** (`roomId`) references an
entity that does not exist. The request is syntactically valid JSON and the
endpoint was found; only the semantic constraint (a sensor must belong to an
existing room) was violated.

`422 Unprocessable Entity` is defined precisely for this: the request was
well-formed and the Content-Type was understood, but the server cannot process
the semantic instructions in the body. Using `404` would mislead clients into
thinking the endpoint itself is missing.

---

### Part 5.2 — Cybersecurity Risks of Exposing Stack Traces

Exposing raw Java stack traces to external API consumers reveals:

1. **Internal file paths** — full server directory structure
   (e.g. `/home/ubuntu/smart-campus-api/...`).
2. **Library names and versions** — enables targeted searches for known CVEs
   against specific Jersey, Jackson, or JVM versions.
3. **Class and method names** — exposes business logic and identifies potential
   attack entry points.
4. **Error conditions** — shows exactly which input triggered the error,
   allowing an attacker to craft repeated inputs that exploit the same flaw.
5. **Infrastructure details** — JVM version and OS type narrow the attack
   surface considerably.

The `GlobalExceptionMapper` prevents all information disclosure by logging the
full trace server-side only and returning a generic message to the client.

---

### Part 5.3 — JAX-RS Filters vs Manual Logging

Inserting `Logger.info()` calls manually into every resource method duplicates
code and is error-prone. Using JAX-RS filters for cross-cutting concerns is
superior because:

1. **DRY** — one filter class covers every endpoint in the API automatically.
2. **Consistency** — new endpoints added in future are logged without any
   developer action.
3. **Separation of concerns** — resource classes contain only business logic;
   observability is handled orthogonally by the filter.
4. **Centrally controllable** — logging can be enabled or disabled in one place
   (`AppConfig`) without touching any resource code.
5. **Covers error paths** — the response filter runs even when an exception
   mapper intercepts an error, ensuring every interaction is logged.
