# SheVault API Contract (v1)

This specification defines the exact REST and WebSocket contract between the SheVault mobile clients, web guardian dashboard, and backend services.

---

## 1. Global Conventions

### 1.1 Headers
| Header | Description | Required | Example |
| :--- | :--- | :---: | :--- |
| `Authorization` | Bearer JWT token | Yes (protected routes) | `Bearer eyJhbGciOi...` |
| `X-Request-ID` | Unique request tracking token | Optional (generated if missing) | `req_9c13d80a14b5` |
| `Idempotency-Key` | Ensures non-duplication of safety activations | Highly recommended for `/incidents` | `client_sess_550e8400` |
| `Content-Type` | Payload serialization format | Yes | `application/json` |

### 1.2 Error Format
All errors conform strictly to:
```json
{
  "error": {
    "code": "INVALID_STATE_TRANSITION",
    "message": "Transition from 'ENDED' to 'ESCALATING' is not permitted by incident protocol.",
    "request_id": "req_9c13d80a14b5",
    "details": {
      "current_state": "ENDED",
      "attempted_state": "ESCALATING"
    }
  }
}
```

---

## 2. Authentication & Credentials

### `POST /api/v1/auth/register`
- **Auth**: None
- **Request**:
```json
{
  "name": "Ananya Sharma",
  "phone": "+919876543210",
  "email": "ananya@example.com",
  "password": "SecurePassword123!"
}
```
- **Response**: `201 Created`
```json
{
  "success": true,
  "data": {
    "access_token": "...",
    "refresh_token": "...",
    "token_type": "Bearer",
    "expires_in": 3600
  },
  "message": "User registered successfully"
}
```

### `POST /api/v1/auth/login`
- **Auth**: None
- **Request**:
```json
{
  "email": "ananya@example.com",
  "password": "SecurePassword123!"
}
```
- **Response**: `200 OK` (token pair)

### `POST /api/v1/auth/refresh`
- **Auth**: None
- **Request**: `{ "refresh_token": "..." }`
- **Response**: `200 OK` (new token pair)

### `POST /api/v1/auth/pins`
- **Auth**: Bearer
- **Request**:
```json
{
  "safe_pin": "1234",
  "duress_pin": "9999"
}
```
- **Invariants**: `safe_pin` and `duress_pin` must be 4 numeric digits and cannot be identical.
- **Response**: `200 OK`

---

## 3. Trusted Contacts & Permissions

### `POST /api/v1/contacts`
- **Auth**: Bearer
- **Request**:
```json
{
  "name": "Priya Sharma",
  "relationship": "Mother",
  "phone": "+919876543211",
  "priority": "PRIMARY",
  "permissions": {
    "incident_alerts": true,
    "location": true,
    "battery": true,
    "evidence": false
  }
}
```
- **Response**: `201 Created`

### `GET /api/v1/contacts`
- **Auth**: Bearer
- **Response**: `200 OK` (List of contacts with granular permissions)

### `PUT /api/v1/contacts/{id}/permissions`
- **Auth**: Bearer
- **Request**:
```json
{
  "incident_alerts": true,
  "location": false,
  "battery": true,
  "evidence": false
}
```
- **Response**: `200 OK`

---

## 4. Incidents & State Transitions

### Permitted State Machine Transitions
```
STARTING -> ACTIVE_GRACE | FAILED
ACTIVE_GRACE -> SAFE_CANCELLED | DURESS_CANCELLED | CANCEL_AUTHENTICATION | ESCALATING
CANCEL_AUTHENTICATION -> SAFE_CANCELLED | DURESS_CANCELLED | ESCALATING
ESCALATING -> ESCALATED | NETWORK_RETRY | DELIVERY_UNKNOWN | FAILED
NETWORK_RETRY -> ESCALATED | FALLBACK_PENDING | FAILED
FALLBACK_PENDING -> ESCALATED | DELIVERY_UNKNOWN | FAILED
ESCALATED -> ENDED | FAILED
DELIVERY_UNKNOWN -> ENDED | ESCALATED
```

### `POST /api/v1/incidents`
- **Auth**: Bearer
- **Idempotency**: Scoped to `(user_id, client_session_id)` or `Idempotency-Key` header.
- **Request**:
```json
{
  "client_session_id": "sess_8f2940ea-9b4f-4d32",
  "device_id": "dev_39201",
  "activation_method": "HOLD",
  "started_at": "2026-10-02T16:45:00.000Z",
  "battery": 84,
  "location": {
    "latitude": 28.5355,
    "longitude": 77.3910,
    "accuracy": 8.5
  },
  "connectivity_state": "ONLINE"
}
```
- **Response**: `201 Created`
```json
{
  "success": true,
  "data": {
    "incident_id": "inc_77a90b41-2bce-4001-9e23",
    "status": "ACTIVE_GRACE",
    "server_time": "2026-10-02T16:45:00.120Z",
    "accepted": true
  }
}
```

### `POST /api/v1/incidents/{id}/cancel` (Legitimate Disarm)
- **Auth**: Bearer
- **Request**: `{ "pin": "1234" }`
- **Internal Status**: `SAFE_CANCELLED`
- **Response**: `200 OK`
```json
{
  "success": true,
  "data": {
    "accepted": true,
    "status": "SAFE_CANCELLED",
    "server_time": "...",
    "message": "Safety session terminated"
  }
}
```

### `POST /api/v1/incidents/{id}/duress` (Covert Disarm Under Coercion)
- **Auth**: Bearer
- **Request**: `{ "pin": "9999" }`
- **Internal Status**: `DURESS_CANCELLED`
- **Covert Action**: Publishes emergency escalation to Redis stream `shevault:deliveries`.
- **Response**: `200 OK`
```json
{
  "success": true,
  "data": {
    "accepted": true,
    "status": "SAFE_CANCELLED",
    "server_time": "...",
    "message": "Safety session terminated"
  }
}
```
> **Security Invariant**: Never return `{ "duress": true }` to the client device.

### `POST /api/v1/incidents/{id}/escalate`
- **Auth**: Bearer
- **Transitions**: `ACTIVE_GRACE` -> `ESCALATED`

### `POST /api/v1/incidents/{id}/end`
- **Auth**: Bearer
- **Transitions**: `ESCALATED` -> `ENDED`

---

## 5. Telemetry & Location Ingestion

### `POST /api/v1/incidents/{id}/locations`
- **Auth**: Bearer
- **Request**:
```json
{
  "timestamp": "2026-10-02T16:46:12Z",
  "latitude": 28.5358,
  "longitude": 77.3915,
  "accuracy": 4.2,
  "speed": 1.2,
  "bearing": 180.0,
  "provider": "FUSED"
}
```

### `POST /api/v1/incidents/{id}/device-state`
- **Auth**: Bearer
- **Request**:
```json
{
  "battery_percent": 82,
  "is_charging": false,
  "network": "CELLULAR",
  "connectivity": "ONLINE",
  "movement_state": "WALKING"
}
```

### `GET /api/v1/incidents/{id}/telemetry-policy`
- **Auth**: Bearer
- **Response**: `200 OK`
```json
{
  "success": true,
  "data": {
    "location_interval_seconds": 15,
    "upload_interval_seconds": 10,
    "movement_mode": "ACTIVE",
    "audio_enabled": false
  }
}
```

---

## 6. Real-time WebSocket Protocol

### `WS /api/v1/ws/incidents/{incident_id}?token={JWT}`
- **Authentication**: Validated via query token.
- **Authorization**: Caller must be incident owner or authorized trusted contact.
- **Broadcast Events**:
  - `incident.started`
  - `incident.status_changed`
  - `location.updated`
  - `battery.updated`
  - `delivery.updated`
  - `incident.cancelled`
  - `incident.escalated`
  - `incident.ended`

---

## 7. Data Retention & Privacy

### `GET /api/v1/privacy/export`
- **Auth**: Bearer
- **Response**: Comprehensive JSON archive of user, contacts, incidents, and audit timeline.

### `DELETE /api/v1/privacy/incidents/{id}`
- **Auth**: Bearer
- **Response**: Purges incident details while maintaining cryptographic hash in audit log.
