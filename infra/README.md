# Infrastructure as Code (IaC)

This directory contains the **production-grade Infrastructure as Code (IaC)** for the User Authentication Service. The infrastructure is designed to be **modular, multi-region ready, secure by default, and horizontally scalable**.

The system follows a **module-based Terraform architecture**, allowing core infrastructure components to be reused across regions without duplication.

---

## 🧠 Design Principles

* **Modular & Reusable** – Each infrastructure concern is isolated into a dedicated module.
* **Multi-Region Ready** – Database and application layers are designed to scale across regions.
* **Security First** – Private networking, least-privilege security groups, and controlled ingress.
* **Separation of Concerns** – Networking, compute, cache, and database are independently managed.

---

## 🗂 Directory Structure

```
infra/
├── modules/
│   ├── network_module/        # VPC, subnets, routing, security groups
│   ├── cache_module/          # ElastiCache Redis
│   ├── rds_modules/
│   │   ├── primary_module/    # Aurora Global Database (writer + readers)
│   │   └── secondary_module/  # Cross-region read replica
│   └── server_module/         # ALB, Auto Scaling Group, EC2 launch template
│
├── live/
│   └── us-east-1/             # Region-specific deployment
│       ├── main.tf
│       ├── variables.tf
│       └── terraform.tfvars
└── versions.tf
```

---

## 🧩 Core Modules

### 🌐 Network Module

**Purpose:** Provides shared networking used by all other modules.

**Responsibilities:**

* VPC creation
* Public & private subnets (multi-AZ)
* Internet Gateway & routing
* Security Groups:

  * Load Balancer SG (public HTTP)
  * Application Server SG (LB-only ingress)
  * RDS SG (Postgres access from app only)
  * Redis SG (Redis access from app only)

> This module is consumed by **application, database, and cache modules**, ensuring consistent networking across regions.

---

### ⚡ Cache Module (Redis)

**Purpose:** High-performance session and token data storage.

**Key Features:**

* AWS ElastiCache (Redis)
* Deployed into **private subnets only**
* Access restricted to application servers
* Used for:

  * JWT JTI blacklisting
  * Session caching

**Why Redis?**

* Constant-time (O(1)) lookups
* Prevents unnecessary database hits on hot auth paths (refresh, silent auth)

---

### 🗄 Database Module (Aurora Global PostgreSQL)

#### Primary Module

* Aurora PostgreSQL Global Database
* Single **writer** instance
* Configurable number of **read replicas** per region
* Private subnet deployment

#### Secondary Module

* Cross-region read-only replica
* Automatically synced via Aurora Global Database
* Enables:

  * Low-latency regional reads
  * Disaster recovery

**Why SQL for Auth?**

* Strong consistency guarantees
* Safer handling of credentials
* Mature transactional semantics for identity systems

---

### 🖥 Application Server Module

**Purpose:** Runs the authentication service securely and at scale.

**Components:**

* Public Application Load Balancer (ALB)
* EC2 Launch Template
* Auto Scaling Group (ASG)
* Docker-based application deployment

**Security Highlights:**

* EC2 instances live in **private subnets**
* Only ALB is publicly accessible
* App servers can access DB and Redis but are not internet-facing

**Scalability:**

* Horizontal scaling via ASG
* Stateless application design
* Database reads routed to replicas
* Redis absorbs high-frequency auth traffic

---

## 🌍 Live Deployment Pattern

Each region has its own `live/<region>` directory.

Example:

```
infra/live/us-east-1/
├── provider.tf
├── main.tf
├── variables.tf
└── terraform.tfvars
```

This pattern allows:

* Independent regional deployments
* Shared module reuse
* Clean multi-region expansion without duplication

---

## 🔐 Security Posture

* No databases or caches are publicly accessible
* Security groups enforce strict service-to-service access
* Application traffic flows:

```
Internet → ALB → App Servers → (Redis / Aurora)
```

* SSH access is IP-restricted
* Sensitive values passed via variables (recommended: Secrets Manager for production)

---

## 📈 Scalability & Performance

* Stateless application layer enables horizontal scaling
* Redis offloads high-frequency auth requests
* Refresh & silent auth flows avoid database hits entirely
* Read replicas distribute database read load
* Infrastructure is region-agnostic and repeatable

---

## ✅ Why This Matters

This infrastructure mirrors **real-world production systems**:

* Clear boundaries
* Region-safe architecture
* Strong security defaults
* Zero duplicated configuration

It is designed to support growth, high traffic, and future evolution without architectural rewrites.

---

> This IaC setup is intentionally decoupled from application logic while remaining tightly aligned with the system’s security and scalability goals.
