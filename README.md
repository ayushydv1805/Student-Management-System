# Student Management System — CampusDesk

A DBMS academic project built with **Java 21, JDBC, MySQL/PostgreSQL, JSP, Servlets, HTML, and CSS**.

## Features
- Dashboard with live database-backed statistics
- Student CRUD, search, and department assignment
- Course catalog CRUD with department/faculty assignment and credit validation
- Student-course enrollments by academic year/semester, including duplicate-term protection
- Daily attendance: Present, Absent, Late, and Excused
- Course assessments and marks entry with maximum-mark validation
- Student performance report with marks average and attendance percentage
- Prepared JDBC statements and server-side validation
- HTTP Basic administrator protection for the web application
- Local MySQL schema plus a separate PostgreSQL/Supabase schema
- Docker/Tomcat deployment for Render and a Vercel reverse-proxy entry point

## Local development (MySQL)

Requirements: JDK 21, Maven 3.9+, MySQL 8+, and Apache Tomcat 10.1.

1. Run `database/schema.sql` in MySQL Workbench.
2. Optionally run `database/sample-data.sql` to add demo records.
3. Configure your local credentials in PowerShell. Never commit credentials:
   ```powershell
   $env:SMS_DB_URL="jdbc:mysql://localhost:3306/student_management_db?useSSL=false&serverTimezone=UTC"
   $env:SMS_DB_USER="root"
   $env:SMS_DB_PASSWORD="YOUR_MYSQL_PASSWORD"
   $env:SMS_ADMIN_USER="campusadmin"
   $env:SMS_ADMIN_PASSWORD="SET_A_STRONG_LOCAL_PASSWORD"
   ```
4. Run `mvn clean package`.
5. Copy `target/student-management-system.war` into Tomcat 10.1's `webapps` folder and start Tomcat.
6. Open `http://localhost:8080/student-management-system/`.

## Supabase PostgreSQL

The hosted CampusDesk project is separate from other Supabase projects. Its application tables live in the `campusdesk` schema so they do not collide with other projects. Files:
- `database/schema-postgres.sql` — PostgreSQL schema.
- `database/sample-data-postgres.sql` — idempotent demo seed.
- `campusdesk_app` — restricted `NOLOGIN` database role with DML permissions only on this schema.

The `campusdesk-sql-gateway` Edge Function exposes a small authenticated server-to-server interface. It accepts a single prepared SELECT/INSERT/UPDATE/DELETE statement and executes it as the restricted role. The gateway token belongs only in the Edge Function and Render's encrypted environment variables—never in this repository or a public browser.

## Deployment

### Render
The root `Dockerfile` builds the WAR with Maven and runs it on Tomcat 10.1 / Java 21. `/health` is available for health checks. Render environment variables:
- `SMS_GATEWAY_URL` = `https://nhqiaocrqjepzfidvkre.supabase.co/functions/v1/campusdesk-sql-gateway`
- `SMS_GATEWAY_TOKEN` = the same private token configured in the Supabase Edge Function
- `SMS_ADMIN_USER` and `SMS_ADMIN_PASSWORD` = administrator HTTP Basic credentials

### Vercel
The `deploy/vercel-proxy` directory holds a tiny reverse-proxy front door; it forwards application paths to Render and keeps the visible URL on the Vercel domain. Link the GitHub repository with **Root Directory** set to `deploy/vercel-proxy`. The proxy destination in `deploy/vercel-proxy/vercel.json` must match the actual Render service URL.

## Security notes
- Never commit database passwords, gateway tokens, access tokens, or administrator passwords.
- The public Edge Function requires the private `X-CampusDesk-Token` header. Do not add that token to frontend JavaScript.
- HTTP Basic authentication is a minimal single-admin gate for this academic deployment, not a full multi-account user-management system.
- Before using real student records, add individual accounts/roles, CSRF protection, audit logs, backups, and institutional privacy controls.

## Project structure
- `database/` — MySQL and PostgreSQL schema/sample SQL and practice queries
- `src/main/java/` — database config, DAO, servlets, authentication filter, and HTTP JDBC bridge
- `src/main/webapp/` — JSP pages, CSS, and web configuration
- `deploy/` — Tomcat start script and Vercel proxy
- `Dockerfile` / `render.yaml` — container build and Render deployment description
- `docs/` — ER diagram and report outline
