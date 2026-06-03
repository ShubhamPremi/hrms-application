# HRMS — Human Resource Management System

A production-grade HRMS built for learning modern Java backend and DevOps practices.

## Modules
Employee Management · Leave Management · Payroll · Attendance · Recruitment · Notifications · Departments · Reports

## Tech Stack
**Backend** Java 21, Spring Boot 3, Spring Security 6, JPA/Hibernate, PostgreSQL, Redis, Kafka  
**DevOps** Docker, Kubernetes, Jenkins, ArgoCD, SonarQube  
**Cloud** AWS (EKS, RDS, ElastiCache, S3)  
**Frontend** Angular 17 (NgModule)

## Branching Strategy
`main` → production only, never commit directly  
`develop` → integration branch, all features merge here first  
`feature/[name]` → one branch per feature, raised as PR into develop

## Commit Convention
`feat:` new feature · `fix:` bug fix · `refactor:` restructuring  
`test:` tests · `docs:` documentation · `chore:` maintenance