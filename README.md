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
- Admin HTTP Basic authentication when `SMS_ADMIN_USER` and `SMS_ADMIN_PASSWORD` are configured
- MySQL local-development schema and a separate PostgreSQL/Supabase schema
- Docker/Tomcat deployment for Render and a Vercel external-origin proxy configuration

## Local development (MySQL)

Requirements: JDK 21, Maven 3.9+, MySQL 8+, and Apache Tomcat 10.1.

1. Run `database/schema.sql` in MySQL Workbench.
2. Optionally run `database/sample-data.sql` to add demo records.
3. Configure the environment variables in your terminal. Do not put real credentials in Git:
   ```powershell
   $env:SMS_DB_URL="jdbc:mysql://localhost:3306/student_management_db?useSSL=false&serverTimezone=UTC"
   $env:SMS_DB_USER="root"
   $env:SMS_DB_PASSWORD="YOUR_MYSQL_PASSWORD"
   $env:SMS_ADMIN_USER="campusadmin"
   $env:SMS_ADMIN_PASSWORD="SET_A_STRONG_LOCAL_PASSWORD"
   ```
4. Run `mvn clean package`.
5. Copy `target/student-management-system.war` into Tomcat 10.1's `webapps` folder and start Tomcat.
6. Open `http://localhost:8080/student-management-system/`. The browser will request the configured administrator credentials.

## Supabase PostgreSQL

Use the dedicated **CampusDesk Student Management** Supabase project. The schema is isolated under the `campusdesk` PostgreSQL schema so it does not collide with other projects/tables. Run `database/schema-postgres.sql` first, then optionally `database/sample-data-postgres.sql`.

For a Render-hosted Java server, use the **Session pooler** connection details from Supabase's **Connect** panel (port 5432). Render outbound connections are IPv4-only, while a Supabase direct connection is IPv6-only on the Free plan; use the exact pooler host shown in your own project rather than guessing it.

Set these Render environment variables:
- `SMS_DB_URL`: `jdbc:postgresql://<SESSION_POOLER_HOST>:5432/postgres?sslmode=require&currentSchema=campusdesk`
- `SMS_DB_USER`: `campusdesk_app.<SUPABASE_PROJECT_REF>`
- `SMS_DB_PASSWORD`: password for the dedicated `campusdesk_app` database role
- `SMS_ADMIN_USER` and `SMS_ADMIN_PASSWORD`: administrator credentials for the public web app

The PostgreSQL JDBC driver is included alongside the MySQL driver, so local MySQL development remains supported.

## Deployment

### Render
The root `Dockerfile` builds the WAR with Maven and runs it on Tomcat 10.1. The container listens on Render's `PORT` environment variable, and `/health` provides an unauthenticated health probe. `render.yaml` documents the service and intentionally marks credentials as unsynced secrets.

### Vercel
The `deploy/vercel-proxy` project is a reverse-proxy front door to Render. Its rewrites preserve the requested path while forwarding requests to the Java/JSP application. Configure its project root directory as `deploy/vercel-proxy` when linking the GitHub repository.

## Security notes
- Do not commit database passwords, access tokens, or admin credentials.
- Production data should only be added after verifying the admin password and database access.
- The application is an academic starter; for real student data, add individual accounts, roles, CSRF protection, audit logs, and institution-approved privacy controls.

## Project structure
- `database/` — MySQL and PostgreSQL schema/sample SQL plus practice queries
- `src/main/java/` — database config, models, DAOs, servlets, and authentication filter
- `src/main/webapp/` — JSP pages, CSS, and web configuration
- `deploy/` — Tomcat start script and Vercel proxy frontend
- `Dockerfile` / `render.yaml` — container build and Render service configuration
- `docs/` — ER diagram and report outline
