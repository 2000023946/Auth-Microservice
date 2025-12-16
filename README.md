User Authentication Service

A secure, scalable microservice managing user identity with JWTs, HTTP-only cookies, Redis, and asynchronous background jobs.

🏗 Architecture & Layers

The service follows a Layered Architecture to ensure clear separation of concerns:

Layer	Responsibility
Domain	Rich User models encapsulating business logic (password hashing, input validation).
Services	Orchestration layer (UserService, TokenService) handling workflows like login and registration.
Repository	UserRepo managing database persistence and SQL procedures.
Cache	RedisCache for session management and JWT blacklisting.
Interface	Controllers with Zod input validation and Serializers for consistent JSON responses.

🚀 Core Workflows (Use Cases)
Feature	Description
Sign Up	Validates input, hashes password, creates a user, issues JWT, and enqueues asynchronous welcome email.
Login	Verifies credentials, issues HTTP-only JWT cookie, and caches session in Redis.
Silent Auth	Restores session on app load using HTTP-only cookie without re-entering credentials.
Refresh Token	Rotates tokens for security, blacklists the old token in Redis, and issues a new token pair.
Logout	Terminates session by blacklisting the JWT in Redis and clearing client cookies.

📊 Observability & Quality

Performance Metrics: Real-time request latency & error rates via Prometheus/Grafana.

Load Testing Analysis: Throughput under high concurrency.
<img width="1414" height="529" alt="Screenshot 2025-12-15 at 8 40 21 PM" src="https://github.com/user-attachments/assets/a200616a-2ea8-4472-bf99-b66bf2124846" />
<img width="981" height="225" alt="Screenshot 2025-12-15 at 8 27 09 PM" src="https://github.com/user-attachments/assets/61f1e8dd-3947-4860-85af-9766e50eb931" />

Code Quality: Target 93%+ test coverage.
<img width="987" height="536" alt="Screenshot 2025-12-15 at 9 26 12 PM" src="https://github.com/user-attachments/assets/0ade94d1-be41-4f7a-93c1-afeba6ebe44e" />


🛠 Installation & Usage
1. Make scripts executable:
chmod +x run_app.sh
chmod +x run_tests.sh

2. Run Application or Tests:
# Start the service
./run_app.sh

# Run test suite
./run_tests.sh
