package edu.college.sms.servlet;

import edu.college.sms.dao.CourseDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.Arrays;

@WebServlet("/courses")
public class CourseServlet extends HttpServlet {
    private final CourseDAO dao=new CourseDAO();
    @Override protected void doGet(HttpServletRequest req,HttpServletResponse resp)throws ServletException,IOException{
        try{
            String action=req.getParameter("action");
            if("add".equals(action)||"edit".equals(action)){
                if("edit".equals(action)){
                    String raw=req.getParameter("id");
                    if(raw==null||!raw.matches("\\d+")){resp.sendError(400,"Invalid course ID.");return;}
                    var course=dao.findById(Integer.parseInt(raw));
                    if(course==null){resp.sendError(404,"Course not found.");return;}
                    req.setAttribute("course",course);
                }
                req.setAttribute("departments",dao.departments());req.setAttribute("faculty",dao.faculty());
                req.getRequestDispatcher("/course-form.jsp").forward(req,resp);return;
            }
            req.setAttribute("courses",dao.findAll(req.getParameter("q")));
            req.getRequestDispatcher("/courses.jsp").forward(req,resp);
        }catch(Exception e){throw new ServletException("Unable to load courses.",e);}
    }
    @Override protected void doPost(HttpServletRequest req,HttpServletResponse resp)throws ServletException,IOException{
        req.setCharacterEncoding("UTF-8");
        try{
            String action=req.getParameter("action");
            if("delete".equals(action)){
                String raw=req.getParameter("id");if(raw==null||!raw.matches("\\d+")){resp.sendError(400,"Invalid course ID.");return;}
                dao.delete(Integer.parseInt(raw));resp.sendRedirect(req.getContextPath()+"/courses?message=deleted");return;
            }
            String id=req.getParameter("courseId"),code=req.getParameter("courseCode"),name=req.getParameter("courseName");
            String creditValue=req.getParameter("credits"),dept=req.getParameter("departmentId"),faculty=req.getParameter("facultyId");
            if(code==null||code.isBlank()||code.trim().length()>20||name==null||name.isBlank()||name.trim().length()>120){
                resp.sendError(400,"Enter a course code and course name within the allowed length.");return;
            }
            if(creditValue==null||!creditValue.matches("\\d{1,2}")||Integer.parseInt(creditValue)<1||Integer.parseInt(creditValue)>10){
                resp.sendError(400,"Credits must be between 1 and 10.");return;
            }
            if(id!=null&&!id.isBlank()&&!id.matches("\\d+")){resp.sendError(400,"Invalid course ID.");return;}
            if(dept!=null&&!dept.isBlank()&&!dept.matches("\\d+")||faculty!=null&&!faculty.isBlank()&&!faculty.matches("\\d+")){
                resp.sendError(400,"Choose a valid department and faculty member.");return;
            }
            Integer departmentId=dept==null||dept.isBlank()?null:Integer.valueOf(dept);
            Integer facultyId=faculty==null||faculty.isBlank()?null:Integer.valueOf(faculty);
            if(id==null||id.isBlank())dao.save(code.trim(),name.trim(),Integer.parseInt(creditValue),departmentId,facultyId);
            else dao.update(Integer.parseInt(id),code.trim(),name.trim(),Integer.parseInt(creditValue),departmentId,facultyId);
            resp.sendRedirect(req.getContextPath()+"/courses?message=saved");
        }catch(Exception e){throw new ServletException("Unable to save course. Check duplicate course codes and selected records.",e);}
    }
}
