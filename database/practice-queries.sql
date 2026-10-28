USE student_management_db;

-- 1. List active students with department names.
SELECT s.roll_no, CONCAT(s.first_name, ' ', s.last_name) AS student_name,
       s.email, d.department_name, s.semester
FROM students s
LEFT JOIN departments d ON d.department_id=s.department_id
WHERE s.status='ACTIVE'
ORDER BY s.last_name, s.first_name;

-- 2. Count students per department (including empty departments).
SELECT d.department_name, COUNT(s.student_id) AS student_count
FROM departments d
LEFT JOIN students s ON s.department_id=d.department_id
GROUP BY d.department_id, d.department_name
ORDER BY student_count DESC;

-- 3. Course enrollment totals.
SELECT c.course_code, c.course_name, COUNT(e.enrollment_id) AS enrolled_students
FROM courses c
LEFT JOIN enrollments e ON e.course_id=c.course_id
GROUP BY c.course_id, c.course_code, c.course_name;

-- 4. Attendance percentage by enrollment.
SELECT e.enrollment_id, s.roll_no, c.course_code,
       ROUND(100.0 * SUM(a.attendance_status IN ('PRESENT','LATE')) / NULLIF(COUNT(a.attendance_id),0), 2) AS attendance_pct
FROM enrollments e
JOIN students s ON s.student_id=e.student_id
JOIN courses c ON c.course_id=e.course_id
LEFT JOIN attendance a ON a.enrollment_id=e.enrollment_id
GROUP BY e.enrollment_id, s.roll_no, c.course_code;

-- 5. Marks and percentage.
SELECT s.roll_no, a.assessment_name, m.marks_obtained, a.max_marks,
       ROUND(100*m.marks_obtained/a.max_marks,2) AS percentage
FROM marks m
JOIN students s ON s.student_id=m.student_id
JOIN assessments a ON a.assessment_id=m.assessment_id;
