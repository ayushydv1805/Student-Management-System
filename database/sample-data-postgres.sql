-- Small demo dataset for Supabase/PostgreSQL. Safe to run repeatedly.
INSERT INTO campusdesk.departments(department_name, department_code) VALUES
 ('Computer Science and Engineering','CSE'),
 ('Electronics and Communication','ECE'),
 ('Mechanical Engineering','ME')
ON CONFLICT DO NOTHING;

INSERT INTO campusdesk.students(roll_no,first_name,last_name,email,phone,date_of_birth,gender,department_id,semester,status)
SELECT 'CSE2026001','Aarav','Sharma','aarav.sharma@example.com','9876500001','2006-02-14','Male',d.department_id,3,'ACTIVE'
FROM campusdesk.departments d
WHERE d.department_code='CSE'
ON CONFLICT DO NOTHING;

INSERT INTO campusdesk.students(roll_no,first_name,last_name,email,phone,date_of_birth,gender,department_id,semester,status)
SELECT 'CSE2026002','Meera','Singh','meera.singh@example.com','9876500002','2005-11-09','Female',d.department_id,3,'ACTIVE'
FROM campusdesk.departments d
WHERE d.department_code='CSE'
ON CONFLICT DO NOTHING;

INSERT INTO campusdesk.faculty(faculty_name,email,department_id)
SELECT 'Dr. Kavita Rao','kavita.rao@example.com',d.department_id
FROM campusdesk.departments d WHERE d.department_code='CSE'
ON CONFLICT DO NOTHING;

INSERT INTO campusdesk.courses(course_code,course_name,credits,department_id,faculty_id)
SELECT 'CS301','Database Management Systems',4,d.department_id,f.faculty_id
FROM campusdesk.departments d
JOIN campusdesk.faculty f ON f.department_id=d.department_id
WHERE d.department_code='CSE' AND f.email='kavita.rao@example.com'
ON CONFLICT DO NOTHING;
