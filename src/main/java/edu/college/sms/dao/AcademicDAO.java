package edu.college.sms.dao;

import edu.college.sms.config.Database;
import java.math.BigDecimal;
import java.sql.*;
import java.util.*;

public class AcademicDAO {
    private Map<String,Object> row(ResultSet r, String... columns) throws SQLException {
        Map<String,Object> m=new LinkedHashMap<>();
        for(String col:columns)m.put(col,r.getObject(col));
        return m;
    }
    public List<Map<String,Object>> attendance(String date, String search) throws SQLException {
        String sql="SELECT e.enrollment_id,e.academic_year,e.semester,s.roll_no,s.first_name,s.last_name,c.course_code,c.course_name,a.attendance_status " +
            "FROM enrollments e JOIN students s ON s.student_id=e.student_id JOIN courses c ON c.course_id=e.course_id " +
            "LEFT JOIN attendance a ON a.enrollment_id=e.enrollment_id AND a.attendance_date=? " +
            "WHERE (? IS NULL OR LOWER(s.roll_no) LIKE LOWER(?) OR LOWER(s.first_name) LIKE LOWER(?) OR LOWER(s.last_name) LIKE LOWER(?) OR LOWER(c.course_code) LIKE LOWER(?)) " +
            "ORDER BY c.course_code,s.last_name,s.first_name";
        List<Map<String,Object>> rows=new ArrayList<>();
        try(Connection c=Database.getConnection();PreparedStatement p=c.prepareStatement(sql)){
            p.setDate(1,java.sql.Date.valueOf(date));String q=search==null||search.isBlank()?null:"%"+search.trim()+"%";
            p.setString(2,q);p.setString(3,q);p.setString(4,q);p.setString(5,q);
            try(ResultSet r=p.executeQuery()){while(r.next()){
                Map<String,Object> m=new LinkedHashMap<>();m.put("enrollmentId",r.getInt("enrollment_id"));m.put("year",r.getString("academic_year"));
                m.put("semester",r.getInt("semester"));m.put("roll",r.getString("roll_no"));m.put("student",r.getString("first_name")+" "+r.getString("last_name"));
                m.put("courseCode",r.getString("course_code"));m.put("course",r.getString("course_name"));m.put("status",r.getString("attendance_status"));rows.add(m);
            }}
        }return rows;
    }
    public void saveAttendance(int enrollmentId, String date, String status) throws SQLException {
        try (Connection c=Database.getConnection()) {
            String sql=Database.isPostgres(c)
                ? "INSERT INTO attendance(enrollment_id,attendance_date,attendance_status) VALUES(?,?,?) ON CONFLICT (enrollment_id,attendance_date) DO UPDATE SET attendance_status=EXCLUDED.attendance_status"
                : "INSERT INTO attendance(enrollment_id,attendance_date,attendance_status) VALUES(?,?,?) ON DUPLICATE KEY UPDATE attendance_status=VALUES(attendance_status)";
            try(PreparedStatement p=c.prepareStatement(sql)){
                p.setInt(1,enrollmentId);p.setDate(2,java.sql.Date.valueOf(date));p.setString(3,status);p.executeUpdate();
            }
        }
    }
    public List<Map<String,Object>> courses() throws SQLException {
        List<Map<String,Object>> rows=new ArrayList<>();
        try(Connection c=Database.getConnection();PreparedStatement p=c.prepareStatement("SELECT course_id,course_code,course_name FROM courses ORDER BY course_code");ResultSet r=p.executeQuery()){
            while(r.next()){Map<String,Object> m=new LinkedHashMap<>();m.put("id",r.getInt(1));m.put("code",r.getString(2));m.put("name",r.getString(3));rows.add(m);}
        }return rows;
    }
    public List<Map<String,Object>> assessments(String search) throws SQLException {
        String sql="SELECT a.assessment_id,a.assessment_name,a.max_marks,a.assessment_date,c.course_id,c.course_code,c.course_name,COUNT(m.mark_id) AS entered_count " +
            "FROM assessments a JOIN courses c ON c.course_id=a.course_id LEFT JOIN marks m ON m.assessment_id=a.assessment_id " +
            "WHERE (? IS NULL OR LOWER(a.assessment_name) LIKE LOWER(?) OR LOWER(c.course_code) LIKE LOWER(?) OR LOWER(c.course_name) LIKE LOWER(?)) " +
            "GROUP BY a.assessment_id,a.assessment_name,a.max_marks,a.assessment_date,c.course_id,c.course_code,c.course_name ORDER BY a.assessment_date DESC,a.assessment_id DESC";
        List<Map<String,Object>> rows=new ArrayList<>();
        try(Connection c=Database.getConnection();PreparedStatement p=c.prepareStatement(sql)){
            String q=search==null||search.isBlank()?null:"%"+search.trim()+"%";p.setString(1,q);p.setString(2,q);p.setString(3,q);p.setString(4,q);
            try(ResultSet r=p.executeQuery()){while(r.next()){Map<String,Object> m=new LinkedHashMap<>();m.put("id",r.getInt("assessment_id"));m.put("name",r.getString("assessment_name"));m.put("max",r.getBigDecimal("max_marks"));m.put("date",r.getDate("assessment_date"));m.put("courseId",r.getInt("course_id"));m.put("courseCode",r.getString("course_code"));m.put("course",r.getString("course_name"));m.put("entered",r.getInt("entered_count"));rows.add(m);}}
        }return rows;
    }
    public void saveAssessment(int courseId,String name,BigDecimal max,String date)throws SQLException{
        String sql="INSERT INTO assessments(course_id,assessment_name,max_marks,assessment_date) VALUES(?,?,?,?)";
        try(Connection c=Database.getConnection();PreparedStatement p=c.prepareStatement(sql)){p.setInt(1,courseId);p.setString(2,name);p.setBigDecimal(3,max);if(date==null||date.isBlank())p.setNull(4,Types.DATE);else p.setDate(4,java.sql.Date.valueOf(date));p.executeUpdate();}
    }
    public Map<String,Object> assessment(int id)throws SQLException{
        try(Connection c=Database.getConnection();PreparedStatement p=c.prepareStatement("SELECT assessment_id,assessment_name,max_marks,course_id FROM assessments WHERE assessment_id=?")){p.setInt(1,id);try(ResultSet r=p.executeQuery()){if(!r.next())return null;Map<String,Object> m=new LinkedHashMap<>();m.put("id",r.getInt(1));m.put("name",r.getString(2));m.put("max",r.getBigDecimal(3));m.put("courseId",r.getInt(4));return m;}}
    }
    public List<Map<String,Object>> marks(int assessmentId)throws SQLException{
        String sql="SELECT DISTINCT e.student_id,s.roll_no,s.first_name,s.last_name,m.marks_obtained,a.max_marks " +
            "FROM assessments a JOIN enrollments e ON e.course_id=a.course_id JOIN students s ON s.student_id=e.student_id " +
            "LEFT JOIN marks m ON m.assessment_id=a.assessment_id AND m.student_id=s.student_id " +
            "WHERE a.assessment_id=? ORDER BY s.last_name,s.first_name";
        List<Map<String,Object>> rows=new ArrayList<>();try(Connection c=Database.getConnection();PreparedStatement p=c.prepareStatement(sql)){p.setInt(1,assessmentId);try(ResultSet r=p.executeQuery()){while(r.next()){Map<String,Object> m=new LinkedHashMap<>();m.put("studentId",r.getInt("student_id"));m.put("roll",r.getString("roll_no"));m.put("student",r.getString("first_name")+" "+r.getString("last_name"));m.put("marks",r.getBigDecimal("marks_obtained"));m.put("max",r.getBigDecimal("max_marks"));rows.add(m);}}}return rows;
    }
    public void saveMarks(int assessmentId,int studentId,BigDecimal marks)throws SQLException{
        try(Connection c=Database.getConnection()){
            String sql=Database.isPostgres(c)
                ? "INSERT INTO marks(assessment_id,student_id,marks_obtained) SELECT a.assessment_id,?,? FROM assessments a WHERE a.assessment_id=? AND ?<=a.max_marks AND EXISTS(SELECT 1 FROM enrollments e WHERE e.course_id=a.course_id AND e.student_id=?) ON CONFLICT (assessment_id,student_id) DO UPDATE SET marks_obtained=EXCLUDED.marks_obtained"
                : "INSERT INTO marks(assessment_id,student_id,marks_obtained) SELECT a.assessment_id,?,? FROM assessments a WHERE a.assessment_id=? AND ?<=a.max_marks AND EXISTS(SELECT 1 FROM enrollments e WHERE e.course_id=a.course_id AND e.student_id=?) ON DUPLICATE KEY UPDATE marks_obtained=VALUES(marks_obtained)";
            try(PreparedStatement p=c.prepareStatement(sql)){
                p.setInt(1,studentId);p.setBigDecimal(2,marks);p.setInt(3,assessmentId);p.setBigDecimal(4,marks);p.setInt(5,studentId);
                if(p.executeUpdate()==0)throw new SQLException("Marks must be within the maximum and the student must be enrolled in the assessment course.");
            }
        }
    }
    public List<Map<String,Object>> performance(String search)throws SQLException{
        String sql="SELECT s.student_id,s.roll_no,s.first_name,s.last_name, " +
            "(SELECT COUNT(*) FROM enrollments e WHERE e.student_id=s.student_id) AS courses, " +
            "(SELECT COUNT(DISTINCT a.assessment_id) FROM assessments a JOIN enrollments e ON e.course_id=a.course_id WHERE e.student_id=s.student_id) AS assessments, " +
            "(SELECT COUNT(*) FROM marks m WHERE m.student_id=s.student_id) AS marks_entered, " +
            "(SELECT ROUND(AVG(m.marks_obtained/a.max_marks*100),2) FROM marks m JOIN assessments a ON a.assessment_id=m.assessment_id WHERE m.student_id=s.student_id) AS average_percent, " +
            "(SELECT ROUND(100.0*SUM(CASE WHEN at.attendance_status IN ('PRESENT','LATE','EXCUSED') THEN 1 ELSE 0 END)/NULLIF(COUNT(*),0),2) FROM attendance at JOIN enrollments e ON e.enrollment_id=at.enrollment_id WHERE e.student_id=s.student_id) AS attendance_percent " +
            "FROM students s WHERE (? IS NULL OR LOWER(s.roll_no) LIKE LOWER(?) OR LOWER(s.first_name) LIKE LOWER(?) OR LOWER(s.last_name) LIKE LOWER(?)) ORDER BY s.last_name,s.first_name";
        List<Map<String,Object>> rows=new ArrayList<>();
        try(Connection c=Database.getConnection();PreparedStatement p=c.prepareStatement(sql)){
            String q=search==null||search.isBlank()?null:"%"+search.trim()+"%";p.setString(1,q);p.setString(2,q);p.setString(3,q);p.setString(4,q);
            try(ResultSet r=p.executeQuery()){while(r.next()){
                Map<String,Object> m=new LinkedHashMap<>();m.put("roll",r.getString("roll_no"));m.put("student",r.getString("first_name")+" "+r.getString("last_name"));
                m.put("courses",r.getInt("courses"));m.put("assessments",r.getInt("assessments"));m.put("marksEntered",r.getInt("marks_entered"));
                m.put("average",r.getBigDecimal("average_percent"));m.put("attendance",r.getBigDecimal("attendance_percent"));rows.add(m);
            }}
        }return rows;
    }
}
