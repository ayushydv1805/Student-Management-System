package edu.college.sms.servlet;

import edu.college.sms.dao.AcademicDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

@WebServlet("/performance")
public class PerformanceServlet extends HttpServlet {
    private final AcademicDAO dao=new AcademicDAO();
    @Override protected void doGet(HttpServletRequest req,HttpServletResponse resp)throws ServletException,IOException{
        try{req.setAttribute("students",dao.performance(req.getParameter("q")));req.getRequestDispatcher("/performance.jsp").forward(req,resp);}
        catch(Exception e){throw new ServletException("Unable to load performance report.",e);}
    }
}
