package edu.college.sms.servlet;

import edu.college.sms.dao.EnrollmentDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

@WebServlet("/enrollments")
public class EnrollmentServlet extends HttpServlet {
    private final EnrollmentDAO dao=new EnrollmentDAO();
    @Override protected void doGet(HttpServletRequest req,HttpServletResponse resp)throws ServletException,IOException{
        try{
            String action=req.getParameter("action");
            if("add".equals(action)){
                req.setAttribute("students",dao.students());req.setAttribute("courses",dao.courses());
                req.getRequestDispatcher("/enrollment-form.jsp").forward(req,resp);return;
            }
            req.setAttribute("enrollments",dao.findAll(req.getParameter("q")));
            req.getRequestDispatcher("/enrollments.jsp").forward(req,resp);
        }catch(Exception e){throw new ServletException("Unable to load enrollments.",e);}
    }
    @Override protected void doPost(HttpServletRequest req,HttpServletResponse resp)throws ServletException,IOException{
        req.setCharacterEncoding("UTF-8");
        try{
            String action=req.getParameter("action");
            if("delete".equals(action)){
                String raw=req.getParameter("id");if(raw==null||!raw.matches("\\d+")){resp.sendError(400,"Invalid enrollment ID.");return;}
                dao.delete(Integer.parseInt(raw));resp.sendRedirect(req.getContextPath()+"/enrollments?message=deleted");return;
            }
            String student=req.getParameter("studentId"),course=req.getParameter("courseId"),year=req.getParameter("academicYear"),semester=req.getParameter("semester");
            if(student==null||!student.matches("\\d+")||course==null||!course.matches("\\d+")){
                resp.sendError(400,"Select a student and course.");return;
            }
            if(year==null||!year.matches("20\\d{2}-\\d{2}")||Integer.parseInt(year.substring(5))!=((Integer.parseInt(year.substring(0,4))+1)%100)){
                resp.sendError(400,"Academic year must use the format YYYY-YY, for example 2026-27.");return;
            }
            if(semester==null||!semester.matches("\\d{1,2}")||Integer.parseInt(semester)<1||Integer.parseInt(semester)>12){
                resp.sendError(400,"Semester must be between 1 and 12.");return;
            }
            dao.save(Integer.parseInt(student),Integer.parseInt(course),year,Integer.parseInt(semester));
            resp.sendRedirect(req.getContextPath()+"/enrollments?message=saved");
        }catch(Exception e){throw new ServletException("Unable to save enrollment. The student may already be enrolled in this course for that academic year and semester.",e);}
    }
}
