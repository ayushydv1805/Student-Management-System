# Student Management System

A DBMS academic project built with **Java, JDBC, MySQL, JSP, Servlets, HTML, and CSS**.

## Features
- Dashboard with database-backed student statistics
- Student listing, searching, adding, editing, viewing, and deleting
- Department dropdown populated from MySQL, with server-side checks for required fields, email format, semester, status, and record IDs
- Course catalog CRUD with department/faculty assignment, search, and credit validation
- Student-course enrollment registration with academic year/semester, search, removal, and duplicate-term protection
- Course deletion is blocked while enrollments exist to avoid accidentally removing linked academic records
- Normalized relational schema for departments, students, faculty, courses, enrollments, attendance, assessments, marks, and admin users
- SQL sample data and practice queries
- Responsive interface

## Requirements
- JDK 21
- Maven 3.9+
- MySQL 8+
- Apache Tomcat 10.1

## Setup
1. Create the database by running `database/schema.sql` in MySQL Workbench.
2. Optionally load demo data using `database/sample-data.sql`.
3. Set environment variables in PowerShell (use your own MySQL credentials):
   ```powershell
   $env:SMS_DB_URL="jdbc:mysql://localhost:3306/student_management_db?useSSL=false&serverTimezone=UTC"
   $env:SMS_DB_USER="root"
   $env:SMS_DB_PASSWORD="YOUR_MYSQL_PASSWORD"
   ```
4. From the project root, run `mvn clean package`.
5. Copy `target/student-management-system.war` into Tomcat 10.1's `webapps` folder and start Tomcat.
6. Open http://localhost:8080/student-management-system/

## Important
Do not commit database passwords or production secrets. This is an academic starter project; add and test authentication/authorization before using it with real student data.

## Project structure
- `database/` — schema, sample data, practice SQL
- `src/main/java/` — JDBC configuration, model, DAO, servlets
- `src/main/webapp/` — JSP pages for dashboard, students, courses, and enrollments
- `src/main/webapp/` — JSP pages, CSS, web configuration
- `docs/` — ER diagram and report outline
