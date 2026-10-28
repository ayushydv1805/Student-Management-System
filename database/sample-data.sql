USE student_management_db;

INSERT IGNORE INTO departments (department_name, department_code) VALUES
('Computer Science and Engineering','CSE'),
('Electronics and Communication','ECE'),
('Mechanical Engineering','ME');

INSERT IGNORE INTO students
(roll_no, first_name, last_name, email, phone, date_of_birth, gender, department_id, semester, status)
SELECT 'CSE2026001','Aarav','Sharma','aarav.sharma@example.com','9876500001','2006-02-14','Male',department_id,3,'ACTIVE'
FROM departments WHERE department_code='CSE';

INSERT IGNORE INTO students
(roll_no, first_name, last_name, email, phone, date_of_birth, gender, department_id, semester, status)
SELECT 'CSE2026002','Meera','Singh','meera.singh@example.com','9876500002','2005-11-09','Female',department_id,3,'ACTIVE'
FROM departments WHERE department_code='CSE';

INSERT IGNORE INTO faculty (faculty_name,email,department_id)
SELECT 'Dr. Kavita Rao','kavita.rao@example.com',department_id FROM departments WHERE department_code='CSE';

INSERT IGNORE INTO courses (course_code,course_name,credits,department_id,faculty_id)
SELECT 'CS301','Database Management Systems',4,d.department_id,f.faculty_id
FROM departments d JOIN faculty f ON f.department_id=d.department_id
WHERE d.department_code='CSE' AND f.email='kavita.rao@example.com';
