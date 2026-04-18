# Tulipe Backend TODO

- Project: `src-backend` (Tauri app backend module)
- Created At: `2026-04-18 00:00`
- Updated At: `2026-04-18 00:00`
- Status: `Active`

## P0 (Highest Priority)

### 1) Add user connection entity for SQLite persistence
- Goal: Create an entity/table for storing user connection info.
- Required fields (minimum):
  - `id` (primary key)
  - `user_id`
  - `connection_name` (user-defined display name)
  - `db_type`
  - `host`
  - `port`
  - `database_name` (nullable for MySQL server-level connect)
  - `username`
  - `password_encrypted` (do not store plain text)
  - `group_name` (for grouping/folders)
  - `last_connected_at`
  - `created_at`
  - `updated_at`
- Acceptance:
  - Table/entity is created and mapped.
  - Can insert and query one record successfully.

### 2) Save connection info to SQLite after successful connect
- Goal: After `/data-source/connect` succeeds, persist connection metadata to SQLite.
- Notes:
  - This likely requires request/response and service flow adjustment.
  - Keep controller thin: controller calls service; service handles persistence; errors throw to global exception handler.
- Acceptance:
  - Successful connection writes/updates one row.
  - `last_connected_at` is updated on every successful connect.
  - Duplicate strategy is defined (e.g., unique by `user_id + connection_name` or a generated `fingerprint`).

## P1 (High Priority)

### 3) Add connection list API for frontend startup page
- Goal: Provide API for Tauri frontend to display saved connections.
- Suggested API:
  - `GET /data-source/list`
  - supports filtering by `group_name`
  - supports sorting by `last_connected_at desc`
- Acceptance:
  - Frontend can load and render saved connections from backend API.

### 4) Add update/delete APIs for saved connections
- Goal: Complete CRUD lifecycle for user-managed connections.
- Suggested APIs:
  - `PUT /data-source/{id}`
  - `DELETE /data-source/{id}`
- Acceptance:
  - Update and delete paths are available and validated.

### 5) Standardize dbType and driver/url build strategy
- Goal: Keep multi-database extension maintainable.
- Notes:
  - Continue using `DbType` enum as the single source of truth.
  - Add new databases through enum + service branch only.
- Acceptance:
  - MySQL/PostgreSQL pass through same connect endpoint.
  - Unsupported dbType throws clear business/argument exception.

## P2 (Security / Reliability)

### 6) Secure credential handling
- Goal: Avoid plain-text credential storage.
- Tasks:
  - Encrypt `password` before persistence.
  - Mask sensitive info in logs and API responses.
- Acceptance:
  - No plain password appears in DB/logs.

### 7) Add integration tests for connect + persistence flow
- Goal: Ensure stability for future multi-database expansion.
- Scope:
  - success path
  - bad credentials path
  - unsupported dbType path
- Acceptance:
  - Tests run and cover core service paths.

---

## Milestone Suggestion
- M1: Complete P0
- M2: Complete P1
- M3: Complete P2

