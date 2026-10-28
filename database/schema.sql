CREATE DATABASE IF NOT EXISTS student_management_db
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE student_management_db;

CREATE TABLE IF NOT EXISTS departments (
  department_id INT AUTO_INCREMENT PRIMARY KEY,
  department_name VARCHAR(100) NOT NULL UNIQUE,
  department_code VARCHAR(20) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS students (
  student_id INT AUTO_INCREMENT PRIMARY KEY,
  roll_no VARCHAR(30) NOT NULL UNIQUE,
  first_name VARCHAR(60) NOT NULL,
  last_name VARCHAR(60) NOT NULL,
  email VARCHAR(150) NOT NULL UNIQUE,
  phone VARCHAR(25),
  date_of_birth DATE,
  gender VARCHAR(20),
  admission_date DATE NOT NULL DEFAULT (CURRENT_DATE),
  department_id INT,
  semester TINYINT NOT NULL DEFAULT 1,
  status ENUM('ACTIVE','INACTIVE','GRADUATED') NOT NULL DEFAULT 'ACTIVE',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_student_department FOREIGN KEY (department_id)
    REFERENCES departments(department_id) ON DELETE SET NULL,
  CONSTRAINT chk_student_semester CHECK (semester BETWEEN 1 AND 12)
);

CREATE TABLE IF NOT EXISTS faculty (
  faculty_id INT AUTO_INCREMENT PRIMARY KEY,
  faculty_name VARCHAR(120) NOT NULL,
  email VARCHAR(150) NOT NULL UNIQUE,
  department_id INT,
  CONSTRAINT fk_faculty_department FOREIGN KEY (department_id)
    REFERENCES departments(department_id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS courses (
  course_id INT AUTO_INCREMENT PRIMARY KEY,
  course_code VARCHAR(20) NOT NULL UNIQUE,
  course_name VARCHAR(120) NOT NULL,
  credits TINYINT NOT NULL DEFAULT 3,
  department_id INT,
  faculty_id INT,
  CONSTRAINT chk_course_credits CHECK (credits BETWEEN 1 AND 10),
  CONSTRAINT fk_course_department FOREIGN KEY (department_id)
    REFERENCES departments(department_id) ON DELETE SET NULL,
  CONSTRAINT fk_course_faculty FOREIGN KEY (faculty_id)
    REFERENCES faculty(faculty_id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS enrollments (
  enrollment_id INT AUTO_INCREMENT PRIMARY KEY,
  student_id INT NOT NULL,
  course_id INT NOT NULL,
  academic_year VARCHAR(9) NOT NULL,
  semester TINYINT NOT NULL,
  enrolled_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uq_enrollment (student_id, course_id, academic_year, semester),
  CONSTRAINT fk_enrollment_student FOREIGN KEY (student_id)
    REFERENCES students(student_id) ON DELETE CASCADE,
  CONSTRAINT fk_enrollment_course FOREIGN KEY (course_id)
    REFERENCES courses(course_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS attendance (
  attendance_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  enrollment_id INT NOT NULL,
  attendance_date DATE NOT NULL,
  attendance_status ENUM('PRESENT','ABSENT','LATE','EXCUSED') NOT NULL,
  UNIQUE KEY uq_attendance_day (enrollment_id, attendance_date),
  CONSTRAINT fk_attendance_enrollment FOREIGN KEY (enrollment_id)
    REFERENCES enrollments(enrollment_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS assessments (
  assessment_id INT AUTO_INCREMENT PRIMARY KEY,
  course_id INT NOT NULL,
  assessment_name VARCHAR(100) NOT NULL,
  max_marks DECIMAL(6,2) NOT NULL,
  assessment_date DATE,
  CONSTRAINT chk_max_marks CHECK (max_marks > 0),
  CONSTRAINT fk_assessment_course FOREIGN KEY (course_id)
    REFERENCES courses(course_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS marks (
  mark_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  assessment_id INT NOT NULL,
  student_id INT NOT NULL,
  marks_obtained DECIMAL(6,2) NOT NULL,
  UNIQUE KEY uq_assessment_student (assessment_id, student_id),
  CONSTRAINT chk_marks_nonnegative CHECK (marks_obtained >= 0),
  CONSTRAINT fk_marks_assessment FOREIGN KEY (assessment_id)
    REFERENCES assessments(assessment_id) ON DELETE CASCADE,
  CONSTRAINT fk_marks_student FOREIGN KEY (student_id)
    REFERENCES students(student_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS admin_users (
  admin_id INT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(60) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_students_name ON students(last_name, first_name);
CREATE INDEX idx_students_department ON students(department_id);
CREATE INDEX idx_enrollment_course ON enrollments(course_id);
CREATE INDEX idx_attendance_date ON attendance(attendance_date);
