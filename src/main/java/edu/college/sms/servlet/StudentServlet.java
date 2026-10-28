package edu.college.sms.servlet;

import edu.college.sms.dao.StudentDAO;
import edu.college.sms.model.Student;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.Date;

@WebServlet("/students")
public class StudentServlet extends HttpServlet {
    private final StudentDAO dao = new StudentDAO();

    @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            String action = req.getParameter("action");
            if ("edit".equals(action) || "view".equals(action)) {
                String id = req.getParameter("id");
                if (id == null || !id.matches("\\d+")) { resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid student ID."); return; }
                Student student = dao.findById(Integer.parseInt(id));
                if (student == null) { resp.sendError(404); return; }
                req.setAttribute("student", student);
                req.setAttribute("departments", dao.findDepartments());
                req.getRequestDispatcher("/student-form.jsp").forward(req,resp); return;
            }
            if ("add".equals(action)) {
                req.setAttribute("departments", dao.findDepartments());
                req.getRequestDispatcher("/student-form.jsp").forward(req,resp);
                return;
            }
            req.setAttribute("students", dao.findAll(req.getParameter("q")));
            req.getRequestDispatcher("/students.jsp").forward(req, resp);
        } catch (Exception e) { throw new ServletException("Unable to process student request.",e); }
    }

    @Override protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        try {
            String action = req.getParameter("action");
            if ("delete".equals(action)) {
                String deleteId = req.getParameter("id");
                if (deleteId == null || !deleteId.matches("\\d+")) {
                    resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid student ID.");
                    return;
                }
                dao.delete(Integer.parseInt(deleteId));
                resp.sendRedirect(req.getContextPath()+"/students?message=deleted");
                return;
            }
            String id = req.getParameter("studentId");
            String rollNo = req.getParameter("rollNo");
            String firstName = req.getParameter("firstName");
            String lastName = req.getParameter("lastName");
            String email = req.getParameter("email");
            String semesterValue = req.getParameter("semester");
            String status = req.getParameter("status");
            String department = req.getParameter("departmentId");
            if (rollNo == null || rollNo.isBlank() || rollNo.length() > 30
                    || firstName == null || firstName.isBlank() || firstName.length() > 60
                    || lastName == null || lastName.isBlank() || lastName.length() > 60
                    || email == null || !email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$") || email.length() > 150) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Enter a valid roll number, first name, last name, and email address.");
                return;
            }
            if (semesterValue == null || !semesterValue.matches("\\d{1,2}")
                    || Integer.parseInt(semesterValue) < 1 || Integer.parseInt(semesterValue) > 12) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Semester must be between 1 and 12.");
                return;
            }
            if (!java.util.Arrays.asList("ACTIVE", "INACTIVE", "GRADUATED").contains(status)) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Choose a valid student status.");
                return;
            }
            if (id != null && !id.isBlank() && !id.matches("\\d+")) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid student ID.");
                return;
            }
            if (department != null && !department.isBlank() && !department.matches("\\d+")) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Choose a valid department.");
                return;
            }

            Student s = new Student();
            if (id != null && !id.isBlank()) s.setStudentId(Integer.parseInt(id));
            s.setRollNo(rollNo.trim());
            s.setFirstName(firstName.trim());
            s.setLastName(lastName.trim());
            s.setEmail(email.trim());
            s.setPhone(req.getParameter("phone"));
            s.setGender(req.getParameter("gender"));
            s.setStatus(status);
            s.setSemester(Integer.parseInt(semesterValue));
            String dob = req.getParameter("dateOfBirth");
            if (dob != null && !dob.isBlank()) s.setDateOfBirth(Date.valueOf(dob));
            s.setDepartmentId(department == null || department.isBlank() ? 0 : Integer.parseInt(department));
            if (s.getStudentId() > 0) dao.update(s); else dao.save(s);
            resp.sendRedirect(req.getContextPath()+"/students?message=saved");
        } catch (Exception e) { throw new ServletException("Unable to save student. Check required fields and unique roll/email.",e); }
    }
}