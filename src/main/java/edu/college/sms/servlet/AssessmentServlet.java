package edu.college.sms.servlet;

import edu.college.sms.dao.AcademicDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.math.BigDecimal;

@WebServlet("/assessments")
public class AssessmentServlet extends HttpServlet {
    private final AcademicDAO dao=new AcademicDAO();
    @Override protected void doGet(HttpServletRequest req,HttpServletResponse resp)throws ServletException,IOException{
        try{
            if("marks".equals(req.getParameter("action"))){String raw=req.getParameter("id");if(raw==null||!raw.matches("\\d+")){resp.sendError(400,"Invalid assessment ID.");return;}int id=Integer.parseInt(raw);var assessment=dao.assessment(id);if(assessment==null){resp.sendError(404,"Assessment not found.");return;}req.setAttribute("assessment",assessment);req.setAttribute("students",dao.marks(id));req.getRequestDispatcher("/marks.jsp").forward(req,resp);return;}
            req.setAttribute("assessments",dao.assessments(req.getParameter("q")));req.setAttribute("courses",dao.courses());req.getRequestDispatcher("/assessments.jsp").forward(req,resp);
        }catch(Exception e){throw new ServletException("Unable to load assessments.",e);}
    }
    @Override protected void doPost(HttpServletRequest req,HttpServletResponse resp)throws ServletException,IOException{
        req.setCharacterEncoding("UTF-8");String action=req.getParameter("action");
        try{
            if("saveMarks".equals(action)){
                String aid=req.getParameter("assessmentId"),sid=req.getParameter("studentId"),value=req.getParameter("marks");
                if(aid==null||!aid.matches("\\d+")||sid==null||!sid.matches("\\d+")||value==null||!value.matches("\\d{1,6}(\\.\\d{1,2})?")){resp.sendError(400,"Enter valid marks.");return;}
                BigDecimal marks=new BigDecimal(value);if(marks.signum()<0){resp.sendError(400,"Marks cannot be negative.");return;}
                dao.saveMarks(Integer.parseInt(aid),Integer.parseInt(sid),marks);resp.sendRedirect(req.getContextPath()+"/assessments?action=marks&id="+aid+"&message=saved");return;
            }
            String course=req.getParameter("courseId"),name=req.getParameter("assessmentName"),max=req.getParameter("maxMarks"),date=req.getParameter("assessmentDate");
            if(course==null||!course.matches("\\d+")||name==null||name.isBlank()||name.trim().length()>100||max==null||!max.matches("\\d{1,4}(\\.\\d{1,2})?")){resp.sendError(400,"Enter a course, assessment name and positive maximum marks.");return;}
            BigDecimal maximum=new BigDecimal(max);if(maximum.signum()<=0||maximum.compareTo(new BigDecimal("9999.99"))>0){resp.sendError(400,"Maximum marks must be greater than zero and no more than 9999.99.");return;}
            if(date!=null&&!date.isBlank())java.time.LocalDate.parse(date);
            dao.saveAssessment(Integer.parseInt(course),name.trim(),maximum,date);resp.sendRedirect(req.getContextPath()+"/assessments?message=saved");
        }catch(java.time.DateTimeException e){resp.sendError(400,"Use a valid assessment date.");}
        catch(Exception e){throw new ServletException("Unable to save academic record. Verify the course and marks are valid.",e);}
    }
}
