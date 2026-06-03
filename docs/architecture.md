# HRMS — Architecture Overview

## Decision Log

### DB: PostgreSQL 16
JSONB support for employee metadata, window functions for payroll, 
MVCC prevents read/write locking. Managed via Flyway migrations.

### Phase 1: Monolith (Weeks 1–4)
Single Spring Boot application. All modules in one deployable JAR.
Understand the domain before splitting it.

### Phase 2: Microservices (Week 5+)
Split by business domain:
- hrms-employee-service  (port 8081)
- hrms-leave-service     (port 8082)
- hrms-payroll-service   (port 8083)
- hrms-notification-service (port 8084)
- hrms-auth-service      (port 8085)
- hrms-api-gateway       (port 8080)