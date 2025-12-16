# User Authentication Service

A **secure, scalable authentication microservice** designed with real-world backend and security practices in mind. This service manages user identity using **JWTs stored in HTTP-only cookies**, **Redis-backed token blacklisting**, and a clean, testable architecture.

---

## 🏗 Architecture & Layers

The service follows a **Layered / Hexagonal Architecture** to ensure strong separation of concerns, testability, and long-term scalability.

| Layer          | Responsibility                                                                                                          |
| -------------- | ----------------------------------------------------------------------------------------------------------------------- |
| **Domain**     | Rich `User` models encapsulating business logic (password hashing, email & password validation).                        |
| **Services**   | Orchestration layer (`UserService`, `TokenService`) handling workflows such as registration, login, and token rotation. |
| **Repository** | `UserRepo` managing database persistence and credential validation via SQL procedures.                                  |
| **Cache**      | `RedisCache` for session management and JWT JTI blacklisting.                                                           |
| **Interface**  | Controllers acting as entry points with schema-based input validation and consistent JSON serialization.                |

This structure isolates business rules from infrastructure, making the system easy to test, reason about, and evolve.

---

## 🚀 Core Workflows (Use Cases)

| Feature           | Description                                                                                                    |
| ----------------- | -------------------------------------------------------------------------------------------------------------- |
| **Sign Up**       | Validates input, hashes passwords, persists the user, issues JWTs, and enqueues asynchronous background tasks. |
| **Login**         | Verifies credentials, issues HTTP-only JWT cookies, and stores session metadata in Redis.                      |
| **Silent Auth**   | Restores sessions on application load using secure cookies without re-entering credentials.                    |
| **Refresh Token** | Rotates tokens for security, blacklists old tokens in Redis, and issues a new token pair.                      |
| **Logout**        | Terminates the session by blacklisting the JWT and clearing client cookies.                                    |

---

## 🔐 Security Design

Security is a **first-class concern** in this service and influenced multiple architectural decisions:

* **JWTs stored in HTTP-only, Secure cookies**
  Prevents access from JavaScript and mitigates XSS-based token theft compared to traditional localStorage-based JWTs.

* **Access & Refresh Token Rotation**
  Short-lived access tokens are paired with refresh tokens that are rotated on use, reducing the blast radius of token leakage.

* **Redis-backed Token Blacklisting (JTI-based)**
  Every rotated or logged-out token is invalidated by blacklisting its JTI, preventing replay and reuse.

* **Explicit Logout Invalidation**
  Sessions are immediately terminated server-side instead of relying solely on token expiration.

* **SQL-based Persistence for User Data**
  A relational database is intentionally used for user authentication data to ensure:

  * Strong consistency guarantees
  * Enforced constraints (uniqueness, integrity)
  * Safer handling of critical identity data compared to eventually-consistent NoSQL alternatives

These choices reflect security patterns commonly used in production authentication systems.

---

## 📊 Observability & Quality

### Metrics & Monitoring

* Real-time request latency and error rates exposed via **Prometheus**
* Visualized using **Grafana dashboards**

### Load Testing

Authentication flows were tested under concurrent load to validate system behavior and latency characteristics.

<img width="1414" height="529" alt="Load test latency dashboard" src="https://github.com/user-attachments/assets/a200616a-2ea8-4472-bf99-b66bf2124846" />
<img width="981" height="225" alt="Request throughput under load" src="https://github.com/user-attachments/assets/61f1e8dd-3947-4860-85af-9766e50eb931" />

### Code Quality

* Extensive unit and integration testing
* **93%+ automated test coverage** enforced during development

<img width="987" height="536" alt="Test coverage report" src="https://github.com/user-attachments/assets/0ade94d1-be41-4f7a-93c1-afeba6ebe44e" />

---

## 🛠 Installation & Usage

### 1. Make scripts executable

```bash
chmod +x run_app.sh
chmod +x run_tests.sh
```

### 2. Run Application or Tests

```bash
# Start the service
./run_app.sh

# Run test suite
./run_tests.sh
```

This spins up the API along with its database, Redis cache, and observability stack using Docker Compose.

---

## 📌 Project Goals

This project was built to demonstrate:

* Secure authentication system design
* Clean architecture principles in practice
* Observability-driven backend development
* Production-style testing and CI/CD workflows

It intentionally goes beyond a minimal auth service to reflect the rigor expected in real-world backend systems.
