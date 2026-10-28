package edu.college.sms.servlet;

import edu.college.sms.dao.StudentDAO;
import edu.college.sms.model.Student;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Arrays;

@WebServlet("/students")
public class StudentServlet extends HttpServlet {
    private final StudentDAO dao = new StudentDAO();

    @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            String action = req.getParameter("action");
            if ("edit".equals(action) || "view".equals(action)) {
                Integer id = RequestValidation.positiveInt(req.getParameter("id"));
                if (id == null) { resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid student ID."); return; }
                Student student = dao.findById(id);
                if (student == null) { resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Student not found."); return; }
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
                Integer deleteId = RequestValidation.positiveInt(req.getParameter("id"));
                if (deleteId == null) {
                    resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid student ID.");
                    return;
                }
                dao.delete(deleteId);
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
            String phone = req.getParameter("phone");
            String gender = req.getParameter("gender");

            if (rollNo == null || rollNo.isBlank() || rollNo.trim().length() > 30
                    || firstName == null || firstName.isBlank() || firstName.trim().length() > 60
                    || lastName == null || lastName.isBlank() || lastName.trim().length() > 60
                    || email == null || !email.trim().matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")
                    || email.trim().length() > 150) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Enter a valid roll number, first name, last name, and email address.");
                return;
            }
            if (phone != null && phone.trim().length() > 25) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Phone number must be 25 characters or fewer.");
                return;
            }
            if (gender != null && !gender.isBlank()
                    && !Arrays.asList("Female", "Male", "Non-binary", "Prefer not to say").contains(gender)) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Choose a valid gender option.");
                return;
            }
            if (semesterValue == null || !semesterValue.matches("\\d{1,2}")
                    || Integer.parseInt(semesterValue) < 1 || Integer.parseInt(semesterValue) > 12) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Semester must be between 1 and 12.");
                return;
            }
            if (!Arrays.asList("ACTIVE", "INACTIVE", "GRADUATED").contains(status)) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Choose a valid student status.");
                return;
            }
            Integer studentId = null;
            if (id != null && !id.isBlank()) {
                studentId = RequestValidation.positiveInt(id);
                if (studentId == null) {
                    resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid student ID.");
                    return;
                }
            }
            Integer departmentId = null;
            if (department != null && !department.isBlank()) {
                departmentId = RequestValidation.positiveInt(department);
                if (departmentId == null) {
                    resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Choose a valid department.");
                    return;
                }
            }

            Student s = new Student();
            if (studentId != null) s.setStudentId(studentId);
            s.setRollNo(rollNo.trim());
            s.setFirstName(firstName.trim());
            s.setLastName(lastName.trim());
            s.setEmail(email.trim());
            s.setPhone(phone == null || phone.isBlank() ? null : phone.trim());
            s.setGender(gender == null || gender.isBlank() ? null : gender);
            s.setStatus(status);
            s.setSemester(Integer.parseInt(semesterValue));
            String dob = req.getParameter("dateOfBirth");
            if (dob != null && !dob.isBlank()) {
                try {
                    LocalDate parsedDate = LocalDate.parse(dob);
                    if (parsedDate.isAfter(LocalDate.now())) {
                        resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Date of birth cannot be in the future.");
                        return;
                    }
                    s.setDateOfBirth(Date.valueOf(parsedDate));
                } catch (DateTimeParseException ex) {
                    resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Enter a valid date of birth.");
                    return;
                }
            }
            s.setDepartmentId(departmentId == null ? 0 : departmentId);
            if (s.getStudentId() > 0) dao.update(s); else dao.save(s);
            resp.sendRedirect(req.getContextPath()+"/students?message=saved");
        } catch (Exception e) { throw new ServletException("Unable to save student. Check required fields and unique roll/email.",e); }
    }
}
