package edu.college.sms.dao;

import edu.college.sms.config.Database;
import edu.college.sms.model.Student;
import edu.college.sms.model.Department;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StudentDAO {
    public List<Student> findAll(String search) throws SQLException {
        String sql = "SELECT s.*, d.department_name FROM students s LEFT JOIN departments d ON d.department_id=s.department_id " +
                     "WHERE (? IS NULL OR s.roll_no LIKE ? OR s.first_name LIKE ? OR s.last_name LIKE ? OR s.email LIKE ?) ORDER BY s.student_id DESC";
        List<Student> rows = new ArrayList<>();
        try (Connection c = Database.getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
            String term = (search == null || search.isBlank()) ? null : "%" + search.trim() + "%";
            p.setString(1, term); p.setString(2, term); p.setString(3, term); p.setString(4, term); p.setString(5, term);
            try (ResultSet r = p.executeQuery()) {
                while (r.next()) rows.add(map(r));
            }
        }
        return rows;
    }

    public Student findById(int id) throws SQLException {
        String sql = "SELECT s.*, d.department_name FROM students s LEFT JOIN departments d ON d.department_id=s.department_id WHERE s.student_id=?";
        try (Connection c = Database.getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setInt(1, id);
            try (ResultSet r = p.executeQuery()) { return r.next() ? map(r) : null; }
        }
    }

    public List<Department> findDepartments() throws SQLException {
        String sql = "SELECT department_id, department_name, department_code FROM departments ORDER BY department_name";
        List<Department> rows = new ArrayList<>();
        try (Connection c = Database.getConnection();
             PreparedStatement p = c.prepareStatement(sql);
             ResultSet r = p.executeQuery()) {
            while (r.next()) {
                Department d = new Department();
                d.setDepartmentId(r.getInt("department_id"));
                d.setDepartmentName(r.getString("department_name"));
                d.setDepartmentCode(r.getString("department_code"));
                rows.add(d);
            }
        }
        return rows;
    }

    public int countAll() throws SQLException {
        try (Connection c = Database.getConnection(); PreparedStatement p = c.prepareStatement("SELECT COUNT(*) FROM students");
             ResultSet r = p.executeQuery()) { r.next(); return r.getInt(1); }
    }

    public int countDepartments() throws SQLException {
        try (Connection c = Database.getConnection(); PreparedStatement p = c.prepareStatement("SELECT COUNT(*) FROM departments");
             ResultSet r = p.executeQuery()) { r.next(); return r.getInt(1); }
    }

    public void save(Student s) throws SQLException {
        String sql = "INSERT INTO students (roll_no,first_name,last_name,email,phone,date_of_birth,gender,department_id,semester,status) VALUES (?,?,?,?,?,?,?,?,?,?)";
        try (Connection c = Database.getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
            bind(p,s); p.executeUpdate();
        }
    }

    public void update(Student s) throws SQLException {
        String sql = "UPDATE students SET roll_no=?,first_name=?,last_name=?,email=?,phone=?,date_of_birth=?,gender=?,department_id=?,semester=?,status=? WHERE student_id=?";
        try (Connection c = Database.getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
            bind(p,s); p.setInt(11,s.getStudentId()); p.executeUpdate();
        }
    }

    private void bind(PreparedStatement p, Student s) throws SQLException {
        p.setString(1,s.getRollNo()); p.setString(2,s.getFirstName()); p.setString(3,s.getLastName());
        p.setString(4,s.getEmail()); p.setString(5,s.getPhone());
        if (s.getDateOfBirth()==null) p.setNull(6,Types.DATE); else p.setDate(6,s.getDateOfBirth());
        p.setString(7,s.getGender());
        if (s.getDepartmentId()<=0) p.setNull(8,Types.INTEGER); else p.setInt(8,s.getDepartmentId());
        p.setInt(9,s.getSemester()); p.setString(10,s.getStatus());
    }

    public void delete(int id) throws SQLException {
        try (Connection c = Database.getConnection(); PreparedStatement p = c.prepareStatement("DELETE FROM students WHERE student_id=?")) {
            p.setInt(1,id); p.executeUpdate();
        }
    }

    private Student map(ResultSet r) throws SQLException {
        Student s = new Student();
        s.setStudentId(r.getInt("student_id")); s.setRollNo(r.getString("roll_no"));
        s.setFirstName(r.getString("first_name")); s.setLastName(r.getString("last_name"));
        s.setEmail(r.getString("email")); s.setPhone(r.getString("phone"));
        s.setDateOfBirth(r.getDate("date_of_birth")); s.setGender(r.getString("gender"));
        s.setDepartmentId(r.getInt("department_id")); s.setSemester(r.getInt("semester"));
        s.setStatus(r.getString("status")); s.setDepartmentName(r.getString("department_name"));
        return s;
    }
}