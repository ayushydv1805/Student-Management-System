# CampusDesk — Student Management System

A web-based **DBMS academic project** for managing student records and academic activity. Built with Java 21, Jakarta Servlets, JSP, JDBC-style data access, HTML/CSS, and MySQL/PostgreSQL.

[![Java CI](https://github.com/ayushydv1805/Student-Management-System/actions/workflows/java-build.yml/badge.svg?branch=main)](https://github.com/ayushydv1805/Student-Management-System/actions/workflows/java-build.yml)

**Live demo:** [campusdesk-student-management.vercel.app](https://campusdesk-student-management.vercel.app)  
**Java service:** [Render app](https://campusdesk-student-management.onrender.com) · [Health endpoint](https://campusdesk-student-management.onrender.com/health)  
**Source code:** [GitHub repository](https://github.com/ayushydv1805/Student-Management-System)

> **Demo access warning:** The hosted app opens without a login prompt, as requested for easier demonstration. Anyone with the public URL can view, create, update, or delete records. Use fictional/demo data only; do not enter real student, contact, or other sensitive information.

## Contents

- [Features](#features)
- [Application architecture](#application-architecture)
- [Database ER diagram](#database-er-diagram)
- [Modules and routes](#modules-and-routes)
- [Technology stack](#technology-stack)
- [Run locally with MySQL](#run-locally-with-mysql)
- [Hosted deployment](#hosted-deployment)
- [Configuration reference](#configuration-reference)
- [Repository structure](#repository-structure)
- [Validation and troubleshooting](#validation-and-troubleshooting)
- [Security and production readiness](#security-and-production-readiness)

## Features

- **Dashboard:** live student and department counts queried from the database.
- **Student directory:** create, view, edit, delete, and search student records; assign departments and semesters.
- **Course catalogue:** maintain course codes, names, credits, department, and faculty assignment.
- **Enrollments:** connect a student to a course for an academic year and semester, with database-level duplicate protection.
- **Attendance:** record Present, Absent, Late, or Excused for each course enrollment and date; saving the same enrollment/date updates its status.
- **Assessments and marks:** create course assessments and enter marks for students enrolled in the relevant course; marks are checked against maximum marks.
- **Performance report:** summarize course enrollments, assessments, average marks percentage, and attendance percentage.
- **Validation:** server-side checks, parameterized SQL, unique constraints, and foreign keys protect common data-integrity rules.
- **Responsive interface:** shared styles and navigation across the JSP pages.

## Application architecture

The same Java application supports two database routes: local development can connect directly to MySQL, while the hosted deployment sends SQL over an authenticated HTTPS gateway to Supabase PostgreSQL. The gateway uses a restricted database role for the `campusdesk` schema.

~~~mermaid
flowchart TD
    User["Browser / user"] --> Vercel["Vercel<br/>public entry point and rewrites"]
    Vercel --> Render["Render<br/>Tomcat 10.1 · Java 21"]
    Render --> Web["JSP pages and Servlets"]
    Web --> DAO["DAO layer<br/>parameterized SQL"]
    DAO -->|Hosted deployment: HTTPS| Bridge["HttpSqlConnection"]
    Bridge --> Gateway["Supabase Edge Function<br/>campusdesk-sql-gateway"]
    Gateway -->|restricted campusdesk_app role| PG[("Supabase PostgreSQL<br/>campusdesk schema")]
    DAO -. "Local development" .-> MySQL[("MySQL 8<br/>student_management_db")]
    Render --> Health["/health<br/>database connectivity check"]
~~~

### Hosted data request

1. The browser opens the Vercel domain.
2. Vercel rewrites the requested path to the Render-hosted Java application.
3. A servlet calls the DAO layer to read or write a record.
4. The hosted JDBC-compatible bridge sends the SQL and bound parameters to the Supabase Edge Function over HTTPS.
5. The gateway authenticates the private service token, runs the request as the restricted database role, and returns JSON results.

The gateway token must remain server-side. Never put it in JSP, HTML, CSS, browser JavaScript, source-control files, or public issue reports.

## Database ER diagram

The diagrams show the principal entities and their relationships. Both SQL schemas define the application tables; PostgreSQL uses the dedicated `campusdesk` schema, while the local MySQL schema uses `student_management_db`.

~~~mermaid
erDiagram
    DEPARTMENTS ||--o{ STUDENTS : assigns
    DEPARTMENTS ||--o{ FACULTY : groups
    DEPARTMENTS ||--o{ COURSES : owns
    FACULTY ||--o{ COURSES : teaches
    STUDENTS ||--o{ ENROLLMENTS : registers
    COURSES ||--o{ ENROLLMENTS : includes
    ENROLLMENTS ||--o{ ATTENDANCE : records
    COURSES ||--o{ ASSESSMENTS : has
    ASSESSMENTS ||--o{ MARKS : receives
    STUDENTS ||--o{ MARKS : earns

    DEPARTMENTS {
        bigint department_id PK
        varchar department_name UK
        varchar department_code UK
    }
    STUDENTS {
        bigint student_id PK
        varchar roll_no UK
        varchar email UK
        varchar first_name
        varchar last_name
        bigint department_id FK
        smallint semester
        varchar status
    }
    FACULTY {
        bigint faculty_id PK
        varchar faculty_name
        varchar email UK
        bigint department_id FK
    }
    COURSES {
        bigint course_id PK
        varchar course_code UK
        varchar course_name
        smallint credits
        bigint department_id FK
        bigint faculty_id FK
    }
    ENROLLMENTS {
        bigint enrollment_id PK
        bigint student_id FK
        bigint course_id FK
        varchar academic_year
        smallint semester
    }
    ATTENDANCE {
        bigint attendance_id PK
        bigint enrollment_id FK
        date attendance_date
        varchar attendance_status
    }
    ASSESSMENTS {
        bigint assessment_id PK
        bigint course_id FK
        varchar assessment_name
        numeric max_marks
        date assessment_date
    }
    MARKS {
        bigint mark_id PK
        bigint assessment_id FK
        bigint student_id FK
        numeric marks_obtained
    }
~~~

### Key integrity rules

- A student roll number, student email, department name/code, and course code must be unique.
- The combination of student, course, academic year, and semester must not be duplicated in enrollments.
- Attendance is unique per enrollment and date.
- A student can have only one marks record per assessment.
- Semester values are limited to 1–12; course credits to 1–10; marks cannot be negative.
- The application validates that marks do not exceed the assessment maximum and that the student is enrolled in the assessment's course.

## Modules and routes

All paths below are relative to the application root.

| Route | Purpose |
|---|---|
| `/` | Dashboard |
| `/students` | Student directory; use `?action=add` to add and `?action=edit&id=1` to edit |
| `/courses` | Course catalogue; use `?action=add` or `?action=edit&id=1` |
| `/enrollments` | Enrollment register; use `?action=add` to create |
| `/attendance` | Daily attendance, selectable by date |
| `/assessments` | Assessment register and assessment creation |
| `/assessments?action=marks&id=1` | Marks entry for an assessment |
| `/performance` | Student performance summary |
| `/health` | Plain-text service/database health check |

The numeric IDs above are examples. Use IDs that exist in your database.

## Technology stack

| Layer | Technology |
|---|---|
| Language/runtime | Java 21 |
| Web layer | Jakarta Servlet 6, JSP, Tomcat 10.1 |
| Data access | JDBC interfaces, prepared statements, DAO classes |
| Local database | MySQL 8+ |
| Hosted database | Supabase PostgreSQL |
| Hosted SQL transport | Java HTTPS JDBC-compatible bridge and Supabase Edge Function |
| Container | Docker |
| Hosting | Render (Java service) and Vercel (rewrite/proxy entry point) |
| CI | GitHub Actions (Maven build and MySQL schema/sample-data checks) |

## Run locally with MySQL

### Requirements

- JDK 21
- Maven 3.9+
- MySQL 8+
- Apache Tomcat 10.1

### Setup

1. Clone the repository:

   ~~~bash
   git clone https://github.com/ayushydv1805/Student-Management-System.git
   cd Student-Management-System
   ~~~

2. Run `database/schema.sql` in MySQL Workbench or the MySQL command-line client. To populate fictional sample rows, run `database/sample-data.sql` afterward.

3. Configure database connection variables in PowerShell. Replace the placeholder with your local MySQL password; do not commit it:

   ~~~powershell
   $env:SMS_DB_URL="jdbc:mysql://localhost:3306/student_management_db?useSSL=false&serverTimezone=UTC"
   $env:SMS_DB_USER="root"
   $env:SMS_DB_PASSWORD="YOUR_LOCAL_MYSQL_PASSWORD"
   ~~~

   When `SMS_GATEWAY_URL` is set, the application uses the HTTPS gateway instead of local MySQL. For local MySQL development, leave `SMS_GATEWAY_URL` and `SMS_GATEWAY_TOKEN` unset.

4. Build the WAR:

   ~~~bash
   mvn clean package
   ~~~

5. Copy `target/student-management-system.war` into Tomcat 10.1's `webapps` directory and start Tomcat.

6. Open `http://localhost:8080/student-management-system/`.

For a local Docker test, build the root `Dockerfile` and provide an appropriate `PORT` and database environment. The deployed Render configuration is specifically prepared to use Supabase's HTTPS gateway.

## Hosted deployment

### Render — Java application

The root `Dockerfile` builds the WAR with Maven and runs it on Tomcat 10.1 with Java 21. Render uses the `PORT` environment value and checks `/health`.

- Service: [campusdesk-student-management.onrender.com](https://campusdesk-student-management.onrender.com)
- Render dashboard: [open service settings](https://dashboard.render.com/web/srv-dbgo1tfb9u1s738rehmg)

Required Render environment variables:

| Variable | Value |
|---|---|
| `SMS_GATEWAY_URL` | `https://nhqiaocrqjepzfidvkre.supabase.co/functions/v1/campusdesk-sql-gateway` |
| `SMS_GATEWAY_TOKEN` | Private token that matches the gateway's server-side configuration |

Never share the token in screenshots, chat, source control, or browser requests.

### Supabase — hosted PostgreSQL

- Project dashboard: [CampusDesk Supabase project](https://supabase.com/dashboard/project/nhqiaocrqjepzfidvkre)
- SQL schema: `database/schema-postgres.sql`
- Demo seed: `database/sample-data-postgres.sql`
- The hosted application tables are isolated in the `campusdesk` schema.
- The restricted `campusdesk_app` database role is used by the SQL gateway.

The Edge Function endpoint is not a public data API: it requires the private gateway token and accepts only a restricted set of single-statement SELECT/INSERT/UPDATE/DELETE requests.

### Vercel — public entry point

- Website: [campusdesk-student-management.vercel.app](https://campusdesk-student-management.vercel.app)
- Vercel project: [open deployment settings](https://vercel.com/ayush0018/campusdesk-student-management)
- Vercel configuration: `deploy/vercel-proxy/vercel.json`
- Set the Vercel project's root directory to `deploy/vercel-proxy`.
- The rewrite destination should remain the deployed Render service URL.

Vercel does not host the Java servlet runtime; it forwards requests to Render.

## Configuration reference

| Variable | Required when | Meaning |
|---|---|---|
| `SMS_DB_URL` | Local MySQL setup | JDBC URL for MySQL |
| `SMS_DB_USER` | Local MySQL setup | Database username |
| `SMS_DB_PASSWORD` | Local MySQL setup | Database password |
| `SMS_GATEWAY_URL` | Hosted gateway setup | Supabase Edge Function endpoint |
| `SMS_GATEWAY_TOKEN` | Hosted gateway setup | Secret shared only by the server and gateway |

If the gateway URL is configured but the gateway token is missing, the app intentionally returns a database configuration error rather than silently falling back to a different database.

## Repository structure

~~~text
.
├── .github/workflows/java-build.yml   # Java build and MySQL checks
├── database/
│   ├── schema.sql                     # MySQL schema
│   ├── sample-data.sql                # MySQL demo data
│   ├── schema-postgres.sql            # PostgreSQL/Supabase schema
│   ├── sample-data-postgres.sql       # PostgreSQL demo data
│   └── practice-queries.sql           # DBMS practice queries
├── deploy/
│   ├── start-tomcat.sh                # Render port and Tomcat startup
│   └── vercel-proxy/                  # Vercel reverse-proxy config
├── docs/
│   ├── ER-DIAGRAM.md
│   └── PROJECT-REPORT-OUTLINE.md
├── src/main/java/                     # Config, models, DAOs, servlets
├── src/main/webapp/                   # JSP screens, CSS, web.xml
├── Dockerfile
├── pom.xml
├── render.yaml
└── README.md
~~~

## Validation and troubleshooting

- **Build locally:** run `mvn clean package`. The GitHub Actions workflow also builds the WAR and validates the MySQL schema and sample data.
- **Check hosting:** open [`/health`](https://campusdesk-student-management.onrender.com/health). A response of `ok` means the application could execute its database health query; a service-unavailable response means the database gateway needs checking.
- **Dashboard shows an error:** check the Render app logs and confirm the two gateway environment variables are present and match the Edge Function configuration.
- **Search returns unexpected results:** search uses case-insensitive matching and surrounds the entered term with wildcards.
- **Duplicate course/student/email/enrollment:** the database's unique constraints reject duplicates. Use a unique identifier or correct the existing record.
- **Cannot delete a course:** first remove or reassign its linked enrollments and assessments. Dependent records must not be removed unexpectedly.
- **Local application won't connect:** confirm the MySQL server is running, the schema was created, the environment variables are set in the same terminal used to start Tomcat, and the JDBC URL/database name are correct.
- **Vercel page not forwarding:** confirm the Vercel root directory is `deploy/vercel-proxy` and the rewrite destination points to the current Render URL.

## Security and production readiness

This is an academic demonstration project, not a production-ready student information system. Before using real records:

1. Add proper authentication, user roles, and authorization for reads and writes.
2. Add CSRF protection for state-changing forms and rate limiting for publicly exposed endpoints.
3. Keep the gateway token in a managed secret/environment variable, rotate it if exposed, and do not hard-code it in source.
4. Add audit trails, automated backups, monitoring, and a recovery plan.
5. Review privacy, retention, access, and institutional policy requirements for student data.

Prepared statements and database constraints reduce several classes of data-handling errors but do not replace authentication or a full security review.
