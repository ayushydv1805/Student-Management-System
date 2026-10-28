package edu.college.sms.dao;

import edu.college.sms.config.Database;
import java.sql.*;
import java.util.*;

public class CourseDAO {
    public List<Map<String,Object>> findAll(String search) throws SQLException {
        String sql = "SELECT c.course_id,c.course_code,c.course_name,c.credits,d.department_name,f.faculty_name " +
            "FROM courses c LEFT JOIN departments d ON d.department_id=c.department_id " +
            "LEFT JOIN faculty f ON f.faculty_id=c.faculty_id " +
            "WHERE (? IS NULL OR LOWER(c.course_code) LIKE LOWER(?) OR LOWER(c.course_name) LIKE LOWER(?) OR LOWER(d.department_name) LIKE LOWER(?)) ORDER BY c.course_code";
        List<Map<String,Object>> rows = new ArrayList<>();
        try (Connection c=Database.getConnection(); PreparedStatement p=c.prepareStatement(sql)) {
            String q=(search==null||search.isBlank())?null:"%"+search.trim()+"%";
            p.setString(1,q); p.setString(2,q); p.setString(3,q); p.setString(4,q);
            try(ResultSet r=p.executeQuery()){while(r.next()){
                Map<String,Object> row=new LinkedHashMap<>();
                row.put("id",r.getInt("course_id")); row.put("code",r.getString("course_code"));
                row.put("name",r.getString("course_name")); row.put("credits",r.getInt("credits"));
                row.put("department",r.getString("department_name")); row.put("faculty",r.getString("faculty_name"));
                rows.add(row);
            }}
        }
        return rows;
    }

    public Map<String,Object> findById(int id) throws SQLException {
        String sql="SELECT course_id,course_code,course_name,credits,department_id,faculty_id FROM courses WHERE course_id=?";
        try(Connection c=Database.getConnection();PreparedStatement p=c.prepareStatement(sql)){
            p.setInt(1,id);try(ResultSet r=p.executeQuery()){if(!r.next())return null;
                Map<String,Object> row=new LinkedHashMap<>();
                row.put("id",r.getInt("course_id"));row.put("code",r.getString("course_code"));
                row.put("name",r.getString("course_name"));row.put("credits",r.getInt("credits"));
                row.put("departmentId",r.getObject("department_id"));row.put("facultyId",r.getObject("faculty_id"));
                return row;
            }
        }
    }

    public List<Map<String,Object>> departments() throws SQLException {
        List<Map<String,Object>> rows=new ArrayList<>();
        try(Connection c=Database.getConnection();PreparedStatement p=c.prepareStatement("SELECT department_id,department_name,department_code FROM departments ORDER BY department_name");ResultSet r=p.executeQuery()){
            while(r.next()){Map<String,Object> row=new LinkedHashMap<>();row.put("id",r.getInt(1));row.put("name",r.getString(2));row.put("code",r.getString(3));rows.add(row);}
        } return rows;
    }

    public List<Map<String,Object>> faculty() throws SQLException {
        List<Map<String,Object>> rows=new ArrayList<>();
        try(Connection c=Database.getConnection();PreparedStatement p=c.prepareStatement("SELECT faculty_id,faculty_name,department_id FROM faculty ORDER BY faculty_name");ResultSet r=p.executeQuery()){
            while(r.next()){Map<String,Object> row=new LinkedHashMap<>();row.put("id",r.getInt(1));row.put("name",r.getString(2));row.put("departmentId",r.getObject(3));rows.add(row);}
        } return rows;
    }

    public void save(String code,String name,int credits,Integer departmentId,Integer facultyId) throws SQLException {
        String sql="INSERT INTO courses(course_code,course_name,credits,department_id,faculty_id) VALUES(?,?,?,?,?)";
        try(Connection c=Database.getConnection();PreparedStatement p=c.prepareStatement(sql)){
            bind(p,code,name,credits,departmentId,facultyId);p.executeUpdate();
        }
    }
    public void update(int id,String code,String name,int credits,Integer departmentId,Integer facultyId) throws SQLException {
        String sql="UPDATE courses SET course_code=?,course_name=?,credits=?,department_id=?,faculty_id=? WHERE course_id=?";
        try(Connection c=Database.getConnection();PreparedStatement p=c.prepareStatement(sql)){
            bind(p,code,name,credits,departmentId,facultyId);p.setInt(6,id);
            if(p.executeUpdate()==0)throw new SQLException("Course not found.");
        }
    }
    private void bind(PreparedStatement p,String code,String name,int credits,Integer departmentId,Integer facultyId)throws SQLException{
        p.setString(1,code);p.setString(2,name);p.setInt(3,credits);
        if(departmentId==null)p.setNull(4,Types.INTEGER);else p.setInt(4,departmentId);
        if(facultyId==null)p.setNull(5,Types.INTEGER);else p.setInt(5,facultyId);
    }
    public void delete(int id)throws SQLException{
        try(Connection c=Database.getConnection()){
            try(PreparedStatement check=c.prepareStatement("SELECT (SELECT COUNT(*) FROM enrollments WHERE course_id=?) + (SELECT COUNT(*) FROM assessments WHERE course_id=?)")){
                check.setInt(1,id);check.setInt(2,id);
                try(ResultSet r=check.executeQuery()){r.next();if(r.getInt(1)>0)throw new SQLException("This course has enrollments or assessments. Remove linked academic records before deleting the course.");}
            }
            try(PreparedStatement p=c.prepareStatement("DELETE FROM courses WHERE course_id=?")){p.setInt(1,id);if(p.executeUpdate()==0)throw new SQLException("Course not found.");}
        }
    }
}
