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

INSERT INTO campusdesk.enrollments(student_id,course_id,academic_year,semester)
SELECT s.student_id,c.course_id,'2026-27',3
FROM campusdesk.students s CROSS JOIN campusdesk.courses c
WHERE s.roll_no='CSE2026001' AND c.course_code='CS301'
ON CONFLICT DO NOTHING;

INSERT INTO campusdesk.attendance(enrollment_id,attendance_date,attendance_status)
SELECT e.enrollment_id,CURRENT_DATE,'PRESENT'
FROM campusdesk.enrollments e
JOIN campusdesk.students s ON s.student_id=e.student_id
JOIN campusdesk.courses c ON c.course_id=e.course_id
WHERE s.roll_no='CSE2026001' AND c.course_code='CS301' AND e.academic_year='2026-27' AND e.semester=3
ON CONFLICT DO NOTHING;

INSERT INTO campusdesk.assessments(course_id,assessment_name,max_marks,assessment_date)
SELECT c.course_id,'MST 1',30,CURRENT_DATE FROM campusdesk.courses c
WHERE c.course_code='CS301'
AND NOT EXISTS (SELECT 1 FROM campusdesk.assessments a WHERE a.course_id=c.course_id AND a.assessment_name='MST 1');

INSERT INTO campusdesk.marks(assessment_id,student_id,marks_obtained)
SELECT a.assessment_id,s.student_id,25
FROM campusdesk.assessments a
JOIN campusdesk.courses c ON c.course_id=a.course_id
JOIN campusdesk.students s ON s.roll_no='CSE2026001'
WHERE c.course_code='CS301' AND a.assessment_name='MST 1'
ON CONFLICT DO NOTHING;
