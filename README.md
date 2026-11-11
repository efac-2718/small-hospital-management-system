# Small Hospital Management System – Secure REST API (JWT)

COSC 32133 / BECS 32263 – Full-Stack Software Development, Assignment 02.

A RESTful backend built with **Spring Boot 4**, **Spring Security** (6+ API: `SecurityFilterChain`, `@PreAuthorize`),
**JWT (JJWT 0.11.5)**, **Spring Data JPA** and **MySQL**. Passwords are stored as **BCrypt** hashes, sessions are
**stateless**, and access is controlled by role (**ADMIN, DOCTOR, NURSE, RECEPTIONIST, PATIENT**).

---

## 1. Running the project

**Requirements:** Java 21, MySQL 8 running on `localhost:3306`, Postman.

The database `hospital_db` is created automatically. The defaults are user `root` / password `root`. If yours are
different, set environment variables before starting:

| Variable       | Default                                         |
|----------------|-------------------------------------------------|
| `DB_USERNAME`  | `root`                                          |
| `DB_PASSWORD`  | `root`                                          |
| `DB_URL`       | `jdbc:mysql://localhost:3306/hospital_db?...`   |
| `JWT_SECRET`   | dev-only Base64 key (≥ 48 bytes, HS384)         |

```bash
./mvnw spring-boot:run
```

On Windows: `mvnw.cmd spring-boot:run`. The API listens on **http://localhost:8080**.

### Seeded accounts (created on first start)

| Username         | Password       | Role         |
|------------------|----------------|--------------|
| `admin`          | `admin123`     | ADMIN        |
| `dr.silva`       | `doctor123`    | DOCTOR (Cardiology, id 2) |
| `dr.perera`      | `doctor123`    | DOCTOR (Paediatrics, id 3) |
| `nurse.fernando` | `nurse123`     | NURSE        |
| `reception`      | `reception123` | RECEPTIONIST |
| `patient`        | `patient123`   | PATIENT      |

Three sample patients and one sample appointment (with `dr.silva`) are also created.

### Automated tests

```bash
./mvnw test
```

The tests use an in-memory H2 database, so they do not need MySQL. They call the API over real HTTP and cover
login, registration rules, role checks, ownership rules and the Task 2 tampered-token scenario.

### Postman

Import `postman/Hospital-Management-System.postman_collection.json`. Set the collection variable `studentNo` to
your student number. Login requests save their token into `patientToken`, `adminToken` or `doctorToken`
automatically. For Task 2, paste your modified token into `tamperedToken`.

---

## 2. Project structure

```
src/main/java/com/example/small_hospital_management_system
├── model        Role, AppointmentStatus, AppUser, Patient, Appointment, MedicalRecord
├── repository   UserRepository, PatientRepository, AppointmentRepository, MedicalRecordRepository
├── dto          LoginRequest, RegisterRequest, AuthResponse, UserResponse, AppointmentRequest,
│                StatusRequest, AppointmentResponse, MedicalRecordRequest, MedicalRecordResponse,
│                ErrorResponse, StatsResponse
├── exception    ApiException, GlobalExceptionHandler
├── security     JwtService, JwtAuthenticationFilter, SecurityConfig, CustomUserDetailsService,
│                JsonAuthenticationEntryPoint, JsonAccessDeniedHandler, ErrorResponseWriter
├── service      AuthService, PatientService, AppointmentService, MedicalRecordService, StatsService
├── controller   AuthController, StaffController, PatientController, AppointmentController,
│                MedicalRecordController
└── config       DataLoader
```

### How a request is authenticated

1. `JwtAuthenticationFilter` reads `Authorization: Bearer <token>`.
2. `JwtService.parseAndValidate` checks the **HS384 signature** and the **expiry**.
3. The user is loaded from MySQL by `CustomUserDetailsService`. Roles always come from the database.
4. The user is placed in the `SecurityContext` for this request only. No session is created.
5. URL rules in `SecurityConfig` and `@PreAuthorize` on each controller method decide access.

A request with **no** token is anonymous: public URLs work and protected URLs return `401`. A request whose token
is **tampered, expired or malformed** is rejected with `401` at once. It is never treated as anonymous.

---

## 3. API

All protected endpoints need `Authorization: Bearer <JWT_TOKEN>`.

### Assignment specification (section 4)

| Method | Endpoint                     | Allowed roles          | Description |
|--------|------------------------------|------------------------|-------------|
| POST   | `/api/auth/register`         | Public*                | Register a new user |
| POST   | `/api/auth/login`            | Public                 | Authenticate and return a JWT |
| GET    | `/api/admin/patients`        | ADMIN                  | List all registered patients |
| POST   | `/api/admin/doctors`         | ADMIN                  | Add a new doctor profile |
| GET    | `/api/doctors/appointments`  | DOCTOR, ADMIN          | View doctor appointments (a doctor sees only their own) |
| POST   | `/api/patients/appointments` | PATIENT                | Book a doctor appointment |

\* Anyone may register a **PATIENT** account (the default role). Creating an **ADMIN, DOCTOR, NURSE or RECEPTIONIST**
account requires a valid ADMIN token: no token gives `401`, a non-admin token gives `403`. Without this rule anyone
could register themselves as ADMIN and RBAC would be meaningless. The field `name` is accepted as an alias of
`fullName`.

### All endpoints

| Method       | Endpoint                             | Roles |
|--------------|--------------------------------------|-------|
| GET          | `/api/auth/me`                       | any logged-in user |
| GET          | `/api/users`                         | ADMIN |
| DELETE       | `/api/users/{id}` (`/api/admin/users/{id}`) | ADMIN (409 if the user has appointments/records) |
| GET          | `/api/doctors`                       | any logged-in user |
| GET          | `/api/admin/stats`                   | ADMIN – overall hospital statistics |
| GET          | `/api/patients?name=`                | ADMIN, DOCTOR, NURSE, RECEPTIONIST |
| GET          | `/api/patients/{id}`                 | ADMIN, DOCTOR, NURSE, RECEPTIONIST |
| GET          | `/api/patients/me`                   | PATIENT – own profile |
| POST / PUT   | `/api/patients`, `/api/patients/{id}`| ADMIN, RECEPTIONIST |
| DELETE       | `/api/patients/{id}` (`/api/admin/patients/{id}`) | ADMIN (409 if the patient has appointments/records) |
| POST         | `/api/appointments`                  | ADMIN, RECEPTIONIST, NURSE (`patientId` required), PATIENT (self only) |
| GET          | `/api/appointments`                  | any logged-in user. Doctors and patients only see their own |
| GET          | `/api/appointments/{id}`             | any logged-in user, ownership enforced |
| GET          | `/api/patients/appointments`         | PATIENT – own appointments |
| PATCH / PUT  | `/api/appointments/{id}/status`      | ADMIN, RECEPTIONIST, DOCTOR (own), PATIENT (cancel own) |
| POST         | `/api/records` (`/api/doctors/records`) | DOCTOR. The author is the doctor in the token |
| GET          | `/api/records/patient/{patientId}`   | ADMIN, DOCTOR, NURSE, PATIENT (own only) |
| GET          | `/api/records/{id}`                  | ADMIN, DOCTOR, NURSE, PATIENT (own only) |
| GET          | `/api/patients/records`              | PATIENT – own records |

### Example bodies

```json
// POST /api/auth/register
{ "username": "PS/2022/XXX", "password": "PS/2022/XXX", "fullName": "Test User", "role": "PATIENT" }

// POST /api/auth/login  ->  200
{ "token": "eyJhbGciOiJIUzM4NCJ9...", "tokenType": "Bearer", "expiresInMs": 3600000,
  "username": "PS/2022/XXX", "fullName": "Test User", "role": "PATIENT" }

// POST /api/patients/appointments  (appointmentTime must be in the future)
{ "doctorId": 2, "appointmentTime": "2026-12-01T10:00:00", "reason": "Check-up" }

// POST /api/records
{ "patientId": 1, "appointmentId": 1, "diagnosis": "Stable angina", "prescription": "Aspirin 75mg" }

// PATCH /api/appointments/1/status
{ "status": "COMPLETED" }
```

### Error format

Every error (400, 401, 403, 404, 405, 409, 500) has the same JSON shape:

```json
{
  "timestamp": "2026-11-11T11:45:29.687Z",
  "status": 401,
  "error": "Unauthorized",
  "message": "Invalid JWT signature: the token has been modified or was not issued by this server",
  "path": "/api/auth/register"
}
```

Validation errors (`400`) also include `fieldErrors`, for example `{ "email": "Email must be a valid email address" }`.

---

## 4. Task 2 – Tampering with the JWT role claim

**Question:** A patient logs in and receives a JWT. The `role` claim is changed from `PATIENT` to `ADMIN` with a
JWT decoder or Base64 encoder. Then the modified token is used to register a new nurse. Does the modified token work?

### Steps (Postman + jwt.io)

1. **Register a patient** with your student number. `POST /api/auth/register`
   `{"username":"PS/2022/XXX","password":"PS/2022/XXX","fullName":"Test User","role":"PATIENT"}` gives `201 Created`.
2. **Log in.** `POST /api/auth/login` with the same credentials. Copy `token`. *(Screenshot 01)*
3. **Decode** the token on jwt.io:
   - Header: `{"alg":"HS384"}`
   - Payload: `{"sub":"PS/2022/XXX","role":"PATIENT","iat":...,"exp":...}`
   - Signature: HMAC-SHA384 of `base64url(header) + "." + base64url(payload)` made with the server's secret.
4. **Modify** the payload to `"role": "ADMIN"` and encode it again (jwt.io *JWT Encoder*).
5. **Register a nurse with the modified token.** `POST /api/auth/register`, Authorization → Bearer Token → the
   modified token, body `{"name":"Test","username":"...","password":"...","role":"NURSE"}`.
6. **View patient records with the same token.** `GET /api/patients`.

### Result (verified against the running application)

| Request | Token | Response |
|---|---|---|
| `POST /api/auth/register` (NURSE) | modified (role = ADMIN) | **401 Unauthorized** – `Invalid JWT signature: the token has been modified or was not issued by this server` |
| `GET /api/patients` | modified (role = ADMIN) | **401 Unauthorized** – same message |
| `GET /api/patients` | original patient token | 403 Forbidden – patients may not list patient records |
| `POST /api/auth/register` (NURSE) | original patient token | 403 Forbidden – `Only an ADMIN can create NURSE accounts` |
| `POST /api/auth/register` (NURSE) | real ADMIN token | 201 Created |

### Answer: No, the modified token does not work

- A JWT is **signed, not encrypted**. Anyone can Base64URL-decode the header and payload and read the claims. That is
  why the role is visible on jwt.io. But nobody can change the claims without the server noticing.
- The third part of the token is `HMACSHA384(base64url(header) + "." + base64url(payload), secret)`. Only the server
  knows `secret` (`app.jwt.secret`).
- When the role is changed, the payload bytes change. On every request `JwtService.parseAndValidate` recomputes
  the HMAC over the received header and payload and compares it with the received signature. They no longer match,
  so JJWT throws `SignatureException`. `JwtAuthenticationFilter` then stops the request with **401**. The controller
  is never reached.
- Re-signing the token on jwt.io does not help. The attacker does not know the server's secret, so any key they
  choose gives a signature the server rejects (also 401). An unsigned token with `"alg": "none"` is also rejected.
- Defence in depth: even for a correctly signed token, the server loads the user's role **from the database**, not
  from the token. A role change in the database also takes effect at once.
- Only a token issued by the server after a real login as an ADMIN can create a nurse. So the RBAC rules cannot be
  bypassed by editing the token on the client side.

Remaining good practice: keep `JWT_SECRET` long, random and out of source control in production. Use HTTPS so tokens
cannot be stolen in transit. Keep expiry times short (1 hour here).
