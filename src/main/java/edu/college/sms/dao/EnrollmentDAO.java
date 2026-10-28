package edu.college.sms.dao;

import edu.college.sms.config.Database;
import java.sql.*;
import java.util.*;

public class EnrollmentDAO {
    public List<Map<String,Object>> findAll(String search) throws SQLException {
        String sql="SELECT e.enrollment_id,e.academic_year,e.semester,s.roll_no,s.first_name,s.last_name,c.course_code,c.course_name " +
            "FROM enrollments e JOIN students s ON s.student_id=e.student_id JOIN courses c ON c.course_id=e.course_id " +
            "WHERE (? IS NULL OR LOWER(s.roll_no) LIKE LOWER(?) OR LOWER(s.first_name) LIKE LOWER(?) OR LOWER(s.last_name) LIKE LOWER(?) OR LOWER(c.course_code) LIKE LOWER(?) OR LOWER(c.course_name) LIKE LOWER(?)) " +
            "ORDER BY e.academic_year DESC,e.semester DESC,s.last_name,c.course_code";
        List<Map<String,Object>> rows=new ArrayList<>();
        try(Connection c=Database.getConnection();PreparedStatement p=c.prepareStatement(sql)){
            String q=(search==null||search.isBlank())?null:"%"+search.trim()+"%";
            p.setString(1,q);for(int i=2;i<=6;i++)p.setString(i,q);
            try(ResultSet r=p.executeQuery()){while(r.next()){
                Map<String,Object> row=new LinkedHashMap<>();row.put("id",r.getInt("enrollment_id"));
                row.put("year",r.getString("academic_year"));row.put("semester",r.getInt("semester"));
                row.put("roll",r.getString("roll_no"));row.put("student",r.getString("first_name")+" "+r.getString("last_name"));
                row.put("courseCode",r.getString("course_code"));row.put("course",r.getString("course_name"));rows.add(row);
            }}
        }return rows;
    }
    public List<Map<String,Object>> students() throws SQLException {
        List<Map<String,Object>> rows=new ArrayList<>();
        try(Connection c=Database.getConnection();PreparedStatement p=c.prepareStatement("SELECT student_id,roll_no,first_name,last_name FROM students WHERE status='ACTIVE' ORDER BY last_name,first_name");ResultSet r=p.executeQuery()){
            while(r.next()){Map<String,Object> row=new LinkedHashMap<>();row.put("id",r.getInt(1));row.put("roll",r.getString(2));row.put("name",r.getString(3)+" "+r.getString(4));rows.add(row);}
        }return rows;
    }
    public List<Map<String,Object>> courses() throws SQLException {
        List<Map<String,Object>> rows=new ArrayList<>();
        try(Connection c=Database.getConnection();PreparedStatement p=c.prepareStatement("SELECT course_id,course_code,course_name FROM courses ORDER BY course_code");ResultSet r=p.executeQuery()){
            while(r.next()){Map<String,Object> row=new LinkedHashMap<>();row.put("id",r.getInt(1));row.put("code",r.getString(2));row.put("name",r.getString(3));rows.add(row);}
        }return rows;
    }
    public void save(int studentId,int courseId,String year,int semester)throws SQLException{
        String sql="INSERT INTO enrollments(student_id,course_id,academic_year,semester) VALUES(?,?,?,?)";
        try(Connection c=Database.getConnection();PreparedStatement p=c.prepareStatement(sql)){
            p.setInt(1,studentId);p.setInt(2,courseId);p.setString(3,year);p.setInt(4,semester);p.executeUpdate();
        }
    }
    public void delete(int id)throws SQLException{
        try(Connection c=Database.getConnection();PreparedStatement p=c.prepareStatement("DELETE FROM enrollments WHERE enrollment_id=?")){p.setInt(1,id);if(p.executeUpdate()==0)throw new SQLException("Enrollment not found.");}
    }
}
