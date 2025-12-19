# User Authentication Service

A **secure, scalable authentication microservice** designed with real-world backend and security practices in mind. This service manages user identity using **JWTs stored in HTTP-only cookies**, **Redis-backed token blacklisting**, and a clean, testable architecture.

---

## 🏗 Architecture & Layers

The service follows a **Hexagonal (Ports & Adapters) Architecture**, ensuring strong separation of concerns, testability, and long-term maintainability.

| Layer          | Responsibility                                                                                                               |
| -------------- | ---------------------------------------------------------------------------------------------------------------------------- |
| **Domain**     | Rich `User` models encapsulating business rules (password hashing, email & password validation).                             |
| **Services**   | Orchestration layer (`UserService`, `TokenService`) coordinating authentication workflows and enforcing use-case boundaries. |
| **Repository** | `UserRepo` handling database persistence and credential validation via SQL.                                                  |
| **Cache**      | `RedisCache` managing session state and JWT JTI blacklisting.                                                                |
| **Interface**  | Controllers as HTTP entry points with schema-based input validation and consistent JSON serialization.                       |

This architecture keeps business logic isolated from frameworks and infrastructure,
enabling secure evolution as the system grows.

---

## 🌐 Frontend Integration & API Gateway

The project includes a **React frontend** integrated through an **Nginx API Gateway**, demonstrating how the authentication service is consumed in a real browser-based environment.

- **Reverse Proxy**
  - Serves the React frontend
  - Routes `/api/**` requests to backend services

- **Single-Origin Architecture**
  - Frontend and backend exposed under one origin
  - Eliminates CORS issues in production

- **Path-Based Routing**
  - `/**` → static frontend assets
  - `/api/**` → authentication microservice (Docker internal network)

- **Security & Isolation**
  - Backend services remain private
  - No direct browser access to internal services
For more detailed information about the frontend implementation, see `client/README.md`.
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

Security is treated as a **core system concern**, not an afterthought.

* **JWTs stored in HTTP-only, Secure cookies**
  Prevents JavaScript access and significantly reduces the risk of XSS-based token theft compared to localStorage-based JWTs.

* **Access & Refresh Token Rotation**
  Short-lived access tokens are paired with rotating refresh tokens to minimize the impact of token compromise.

* **Redis-backed Token Blacklisting (JTI-based)**
  Logged-out or rotated tokens are immediately invalidated server-side, preventing replay attacks.

* **Explicit Logout Invalidation**
  Sessions are terminated deterministically rather than relying solely on token expiration.

* **Relational (SQL) Database for Identity Data**
  A relational database is intentionally used for authentication data to ensure strong consistency, enforced constraints, and safer handling of critical user identity information.

These mechanisms together reflect industry-standard practices used in production authentication systems.

---

## 📈 Scalability & Performance

The service is designed to scale horizontally with predictable performance under load.
* **Kubernetes Orchestration**  
  Backend runs with replicas for load balancing and self-healing.

* **Redis-backed session caching**
  User session payloads and token metadata are cached in Redis, avoiding repeated database access.

* **Constant-time token validation**
  Redis provides *O(1)* lookups for token validation and blacklist checks, compared to *O(log n)* database queries.

* **Hot-path optimization**

  * **Login**: single database lookup to validate credentials
  * **Refresh Token (highest-frequency endpoint)**: Redis-only path, no database hit
  * **Silent Auth**: Redis-backed validation without unnecessary persistence access

This design minimizes database contention and enables horizontal scaling behind a Kubernetes LoadBalancer.

---
## 🛠 Local Setup & Deployment

### Prerequisites

* Docker Desktop with Kubernetes enabled or Minikube
* `kubectl` CLI

### Deploy to Kubernetes
```bash
./deploy.sh
```

Visit **http://localhost:8080** in your browser 

---

## 📊 Observability & Quality

### Metrics & Monitoring

* Real-time request latency and error rates exposed via **Prometheus**
* Visualized using **Grafana dashboards**

### Load Testing Results

Authentication flows were load tested under concurrent traffic. Observed **p95 latencies**:

* **Login**: ~0.5s
* **Refresh Token**: ~0.09s
* **Silent Auth**: ~0.15s

These results demonstrate the effectiveness of Redis-backed caching on high-frequency endpoints.

<img width="1414" height="529" alt="Load test latency dashboard" src="https://github.com/user-attachments/assets/a200616a-2ea8-4472-bf99-b66bf2124846" />
<img width="981" height="225" alt="Request throughput under load" src="https://github.com/user-attachments/assets/61f1e8dd-3947-4860-85af-9766e50eb931" />


### Code Quality

* Extensive unit and integration testing and functional testing
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

## 🧩 Maintainability & Extensibility

Maintainability is enforced through deliberate architectural boundaries and tooling.

* **Strict separation of concerns**
  Domain logic, orchestration, and infrastructure are isolated to prevent tight coupling.

* **Framework-agnostic core**
  Business logic does not depend on web frameworks or databases, enabling safe refactors and replacements.

* **Explicit contracts between layers**
  Controllers depend on services, services depend on interfaces, and infrastructure details remain interchangeable.

* **High testability by design**
  Most logic is unit-testable without external systems; infrastructure is covered via focused integration tests.

* **CI-enforced quality gates**
  Automated testing and coverage checks prevent architectural drift as the codebase evolves.

This structure keeps the system understandable, extensible, and safe to modify as new features are added.

---

## 🏗 Infrastructure

The microservice includes a modular Terraform-based IaC component to deploy live infrastructure securely and efficiently:

* **Modular Design**
Network, Redis cache, Aurora global database, and application servers are implemented as separate modules for reusability across multiple projects or environments.

* **Multi-region Deployment**
Infrastructure modules can be deployed in multiple regions without repeating code, enabling global scalability and high availability.

* **Private Networking**
VPC with public and private subnets, along with properly configured security groups, isolates services and enforces secure communication.

* **Global Aurora Database**
One write cluster with regional read replicas ensures consistency, fault tolerance, and horizontal scalability.

* **Redis Cache**
Deployed in private subnets to handle session caching and JWT token blacklisting efficiently.

* **Application Servers**
Auto-scaled EC2 instances running Docker containers behind an Application Load Balancer, providing horizontal scaling and high availability.

For full Terraform module definitions see the /infra directory.

--

## 📌 Project Goals

This project was built to demonstrate:

* Secure authentication system design
* Clean architecture principles in practice
* Observability-driven backend development
* Production-style testing and CI/CD workflows

It intentionally goes beyond a minimal auth service to reflect the rigor expected in real-world backend systems.
