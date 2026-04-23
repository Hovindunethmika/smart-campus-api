# Smart Campus Sensor & Room Management API

## API Design Overview

The API follows REST architectural principles with a versioned base path `/api/v1`.  
It is packaged as a WAR file and deployed to Apache Tomcat 10.

| Resource | Base Path | Description |
|----------|-----------|-------------|
| Discovery | `GET /api/v1/` | API metadata and hypermedia links (HATEOAS) |
| Rooms | `/api/v1/rooms` | Campus room management (CRUD + safety logic) |
| Sensors | `/api/v1/sensors` | IoT sensor registration and type filtering |
| Readings | `/api/v1/sensors/{id}/readings` | Historical sensor data via sub-resource locator |

**Storage:** Entirely in-memory using `ConcurrentHashMap` and `ArrayList` — no database used.  
**Stack:** Java 25 · JAX-RS (Jersey 3.1.3) · Apache Tomcat 10 · Jackson JSON · Jakarta EE 10.

### Project Structure

```
smart-campus-api/
├── pom.xml
├── README.md
└── src/main/
    ├── webapp/WEB-INF/
    │   └── web.xml                          ← registers Jersey servlet
    └── java/com/smartcampus/
        ├── config/
        │   └── AppConfig.java               ← ResourceConfig + package scan
        ├── model/
        │   ├── Room.java
        │   ├── Sensor.java
        │   └── SensorReading.java
        ├── store/
        │   └── DataStore.java               ← singleton ConcurrentHashMap store
        ├── resource/
        │   ├── DiscoveryResource.java        ← GET /api/v1/
        │   ├── RoomResource.java             ← /api/v1/rooms
        │   ├── SensorResource.java           ← /api/v1/sensors
        │   └── SensorReadingResource.java    ← sub-resource /readings
        ├── exception/
        │   ├── RoomNotEmptyException.java
        │   ├── RoomNotEmptyMapper.java       ← 409 Conflict
        │   ├── LinkedResourceNotFoundException.java
        │   ├── LinkedResourceNotFoundMapper.java  ← 422 Unprocessable Entity
        │   ├── SensorUnavailableException.java
        │   ├── SensorUnavailableMapper.java  ← 403 Forbidden
        │   └── GlobalExceptionMapper.java    ← 500 catch-all
        └── filter/
            └── LoggingFilter.java            ← request/response logging
```

---

## How to Build and Run the Project

### Prerequisites

- Java JDK 25
- Apache Maven 3.6 or higher
- Apache Tomcat 10.x
- NetBeans IDE (recommended) or any IDE with Maven support

### Step 1 — Clone the repository

```bash
git clone https://github.com/Hovindunethmika/smart-campus-api.git
cd smart-campus-api
```

### Step 2 — Open in NetBeans

1. Open NetBeans
2. Go to **File → Open Project**
3. Navigate to the cloned `smart-campus-api` folder and click **Open**

### Step 3 — Configure the server

1. Go to **Tools → Servers** in NetBeans
2. Click **Add Server** → select **Apache Tomcat or TomEE**
3. Browse to your Tomcat 10 installation directory
4. Set a username and password (e.g. admin / admin)
5. Click **Finish**

### Step 4 — Build the project

Right-click the project → **Clean and Build**

You should see `BUILD SUCCESS` in the Output window.

### Step 5 — Run the project

Right-click the project → **Run**

NetBeans will deploy the WAR to Tomcat automatically. Wait for:
```
OK - Started application at context path [/]
```

### Step 6 — Verify it is running

Open your browser or Postman and hit:
```
http://localhost:8080/api/v1/
```

You should see the discovery JSON response confirming the API is running.

> **Note:** The app deploys to the root context path `/` as configured by NetBeans.  
> All API endpoints are accessible at `http://localhost:8080/api/v1/...`

---

## Sample curl Commands

### 1. Discovery — GET /api/v1/
```bash
curl -s http://localhost:8080/api/v1/
```

Expected response:
```json
{
    "name": "Smart Campus Sensor & Room Management API",
    "version": "1.0.0",
    "status": "running",
    "contact": "admin@smartcampus.ac.uk",
    "resources": {
        "rooms": "/api/v1/rooms",
        "sensors": "/api/v1/sensors"
    }
}
```

### 2. Create a new room — POST /api/v1/rooms
```bash
curl -s -X POST http://localhost:8080/api/v1/rooms \
  -H "Content-Type: application/json" \
  -d '{"id":"ENG-201","name":"Engineering Lab","capacity":35}'
```

Expected: `201 Created` with a `Location` header and room JSON.

### 3. List all rooms — GET /api/v1/rooms
```bash
curl -s http://localhost:8080/api/v1/rooms
```

### 4. Get a specific room — GET /api/v1/rooms/{roomId}
```bash
curl -s http://localhost:8080/api/v1/rooms/LIB-301
```

### 5. Delete a room that has sensors (expect 409) — DELETE /api/v1/rooms/{roomId}
```bash
curl -s -X DELETE http://localhost:8080/api/v1/rooms/LIB-301
```

Expected: `409 Conflict` — room has sensors assigned.

### 6. Delete an empty room — DELETE /api/v1/rooms/{roomId}
```bash
curl -s -X DELETE http://localhost:8080/api/v1/rooms/ENG-201
```

Expected: `204 No Content`

### 7. Register a sensor with invalid roomId (expect 422) — POST /api/v1/sensors
```bash
curl -s -X POST http://localhost:8080/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{"type":"Temperature","roomId":"DOES-NOT-EXIST"}'
```

Expected: `422 Unprocessable Entity`

### 8. Register a valid sensor — POST /api/v1/sensors
```bash
curl -s -X POST http://localhost:8080/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{"id":"CO2-002","type":"CO2","roomId":"LAB-101","currentValue":400.0}'
```

Expected: `201 Created`

### 9. Filter sensors by type — GET /api/v1/sensors?type=CO2
```bash
curl -s "http://localhost:8080/api/v1/sensors?type=CO2"
```

Expected: Only CO2 sensors returned.

### 10. Post a reading to a MAINTENANCE sensor (expect 403)
```bash
curl -s -X POST http://localhost:8080/api/v1/sensors/OCC-001/readings \
  -H "Content-Type: application/json" \
  -d '{"value":12}'
```

Expected: `403 Forbidden` — OCC-001 is in MAINTENANCE status.

### 11. Post a valid reading — POST /api/v1/sensors/{id}/readings
```bash
curl -s -X POST http://localhost:8080/api/v1/sensors/TEMP-001/readings \
  -H "Content-Type: application/json" \
  -d '{"value":25.3}'
```

Expected: `201 Created`. Check that `currentValue` on TEMP-001 is now updated to `25.3`.

### 12. Get reading history — GET /api/v1/sensors/{id}/readings
```bash
curl -s http://localhost:8080/api/v1/sensors/TEMP-001/readings
```

### 13. Update sensor status — PUT /api/v1/sensors/{id}/status
```bash
curl -s -X PUT http://localhost:8080/api/v1/sensors/OCC-001/status \
  -H "Content-Type: application/json" \
  -d '{"status":"ACTIVE"}'
```

---

## Report: Answers to Coursework Questions

### Part 1.1 — JAX-RS Resource Lifecycle

By default, JAX-RS creates a **new instance** of each resource class for every
incoming HTTP request (request-scoped lifecycle). This means instance fields on
resource classes are not shared between requests and cannot hold persistent state.

To manage shared in-memory data safely across concurrent requests, this project
uses a **singleton `DataStore` class** backed by `ConcurrentHashMap<K,V>`.
`ConcurrentHashMap` provides thread-safe atomic operations for `put`, `get`, and
`remove` without requiring explicit `synchronized` blocks, preventing race
conditions when multiple clients send requests simultaneously. Reading lists are
wrapped with `Collections.synchronizedList()` to guard list-level operations
such as `add`. This design ensures that despite a new resource instance being
created per request, all requests share and safely modify the same underlying
data without data loss or corruption.

---

### Part 1.2 — HATEOAS and Hypermedia Design

HATEOAS (Hypermedia as the Engine of Application State) is the principle that
API responses should embed links to related resources and available actions,
rather than requiring clients to construct URLs from external documentation.

Benefits over static documentation:
1. **Self-discoverability** — a client can start at `GET /api/v1/` and navigate
   the entire API by following embedded links, with no prior knowledge of URLs.
2. **Reduced coupling** — if a server-side URL changes, clients that follow
   links rather than hardcode paths adapt automatically without breaking.
3. **Always in sync** — embedded links reflect the live API state; static
   documentation can become stale after updates.
4. **Lower onboarding cost** — new developers can explore the API interactively
   instead of reading a lengthy specification first.

---

### Part 2.1 — IDs Only vs Full Objects in List Responses

Returning **only IDs** is bandwidth-efficient when the client does not need full
details immediately, but it forces clients to make N additional GET requests to
resolve each ID — the classic "N+1 problem" — which adds latency and increases
server load under high traffic.

Returning **full objects** increases payload size but eliminates extra
round-trips and simplifies client logic. It is preferred when clients regularly
consume the full data, such as rendering a room list in a UI. For very large
collections, pagination with field projection (returning a subset of fields)
offers the best of both approaches. This API returns full objects since the
dataset is bounded and clients are expected to need complete room details.

---

### Part 2.2 — DELETE Idempotency

DELETE is **idempotent** in this implementation. The HTTP specification defines
idempotency in terms of server state changes, not response codes.

- **First call:** room exists → deleted → server returns `204 No Content`.
- **Second call:** room is already absent → server returns `404 Not Found`.

The server state after both calls is identical — the room does not exist.
Returning `404` on a repeated call does not violate idempotency because the
underlying state change (room deletion) happened exactly once. The different
response code is acceptable and expected per RFC 9110.

---

### Part 3.1 — @Consumes Mismatch

JAX-RS inspects the `Content-Type` header of every incoming request and matches
it against the `@Consumes` annotation on the candidate method. If a client sends
`Content-Type: text/plain` or `application/xml` to a method annotated with
`@Consumes(MediaType.APPLICATION_JSON)`, JAX-RS **automatically returns
HTTP 415 Unsupported Media Type** — the method body never executes. No custom
code is required. This protects the API from malformed or unexpected payloads at
the framework level, before any deserialisation is attempted.

---

### Part 3.2 — @QueryParam vs @PathParam for Filtering

Using `@QueryParam` (`GET /sensors?type=CO2`) is the correct approach for
collection filtering because:

1. **Optional by nature** — path segments imply a mandatory fixed hierarchy;
   query parameters are inherently optional, matching the semantics of a filter.
2. **Multiple filters compose naturally** — `?type=CO2&status=ACTIVE` requires
   no change to the path template, whereas path-based filters would need a new
   route for every combination.
3. **REST semantics** — `/sensors` always identifies the full collection; the
   query string narrows the result set without changing the resource identity.
4. **Tooling compatibility** — HTTP caches, proxies, and analytics platforms
   treat query strings as modifiers, not as distinct resources, enabling better
   caching and logging behaviour.

---

### Part 4.1 — Sub-Resource Locator Pattern Benefits

The sub-resource locator delegates handling of nested paths to a dedicated class
rather than adding more methods to the parent resource. Architectural benefits:

1. **Single Responsibility** — `SensorResource` handles sensor registration and
   retrieval; `SensorReadingResource` handles reading history only. Neither class
   grows unmanageably large.
2. **Context injection at construction** — `sensorId` is passed to
   `SensorReadingResource` via its constructor, avoiding repeated `@PathParam`
   declarations on every method in the sub-resource.
3. **Independent testability** — each class can be unit-tested in isolation
   without loading the entire parent resource.
4. **Scalability** — in large APIs with deep nesting (buildings/floors/rooms/
   sensors/readings), each level is one focused class rather than one large
   monolithic controller with dozens of methods and complex routing logic.

---

### Part 5.1 — Why HTTP 422 is More Appropriate than 404

`404 Not Found` signals that the **request URL** does not resolve to a known
resource. Here, `POST /api/v1/sensors` is a valid, existing endpoint — the URL
itself is perfectly fine.

The problem is that a **value inside the JSON payload** (`roomId`) references an
entity that does not exist in the system. The request is syntactically valid JSON
and the endpoint was found; only the semantic constraint (a sensor must belong to
an existing room) was violated.

`422 Unprocessable Entity` is defined precisely for this scenario: the request
was well-formed, the Content-Type was understood, but the server cannot process
the semantic instructions contained in the body. Using `404` would mislead
clients into thinking the endpoint itself is missing, rather than that their
payload contains an invalid reference value.

---

### Part 5.2 — Cybersecurity Risks of Exposing Stack Traces

Exposing raw Java stack traces to external API consumers gives attackers:

1. **Internal file paths** — reveals the full server directory structure
   (e.g. `C:\Users\USER\smart-campus-api\...`), helping attackers understand
   deployment layout.
2. **Library names and versions** — enables targeted searches for known CVEs
   against specific Jersey, Jackson, or JVM versions present in the trace.
3. **Class and method names** — exposes internal business logic, package
   structure, and identifies potential attack entry points.
4. **Error conditions and logic paths** — shows exactly which input triggered
   the error, allowing an attacker to craft inputs that repeatedly exploit the
   same flaw.
5. **Infrastructure details** — JVM version and OS type narrow the attack
   surface considerably and help attackers choose appropriate exploits.

The `GlobalExceptionMapper` prevents all information disclosure by logging the
full trace server-side only and returning only a generic, non-revealing message
to the client.

---

### Part 5.3 — JAX-RS Filters vs Manual Logging

Inserting `Logger.info()` calls manually into every resource method duplicates
code across dozens of methods and is error-prone. Using JAX-RS filters for
cross-cutting concerns is superior because:

1. **DRY (Don't Repeat Yourself)** — one filter class covers every endpoint in
   the API automatically with no duplication.
2. **Consistency** — new endpoints added in future are logged without any
   developer remembering to add logging code.
3. **Separation of concerns** — resource classes contain only business logic;
   observability is handled orthogonally by the filter layer.
4. **Centrally controllable** — logging can be enabled, disabled, or modified in
   one place without touching any resource code.
5. **Covers all paths** — the response filter runs even when an exception mapper
   intercepts an error, ensuring every single interaction is logged including
   error responses.