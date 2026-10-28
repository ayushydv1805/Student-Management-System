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
                Student student = dao.findById(Integer.parseInt(req.getParameter("id")));
                if (student == null) { resp.sendError(404); return; }
                req.setAttribute("student", student);
                req.getRequestDispatcher("/student-form.jsp").forward(req,resp); return;
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
            Student s = new Student();
            String id = req.getParameter("studentId");
            if (id != null && !id.isBlank()) s.setStudentId(Integer.parseInt(id));
            s.setRollNo(req.getParameter("rollNo").trim());
            s.setFirstName(req.getParameter("firstName").trim());
            s.setLastName(req.getParameter("lastName").trim());
            s.setEmail(req.getParameter("email").trim());
            s.setPhone(req.getParameter("phone"));
            s.setGender(req.getParameter("gender"));
            s.setStatus(req.getParameter("status"));
            s.setSemester(Integer.parseInt(req.getParameter("semester")));
            String dob = req.getParameter("dateOfBirth");
            if (dob != null && !dob.isBlank()) s.setDateOfBirth(Date.valueOf(dob));
            String department = req.getParameter("departmentId");
            s.setDepartmentId(department == null || department.isBlank() ? 0 : Integer.parseInt(department));
            if (s.getStudentId() > 0) dao.update(s); else dao.save(s);
            resp.sendRedirect(req.getContextPath()+"/students?message=saved");
        } catch (Exception e) { throw new ServletException("Unable to save student. Check required fields and unique roll/email.",e); }
    }
}