package edu.college.sms.servlet;

import edu.college.sms.dao.StudentDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

@WebServlet("/")
public class DashboardServlet extends HttpServlet {
    @Override protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            StudentDAO dao = new StudentDAO();
            request.setAttribute("studentCount", dao.countAll());
            request.setAttribute("departmentCount", dao.countDepartments());
            request.getRequestDispatcher("/index.jsp").forward(request, response);
        } catch (Exception e) {
            throw new ServletException("Unable to load dashboard. Check database configuration.", e);
        }
    }
}