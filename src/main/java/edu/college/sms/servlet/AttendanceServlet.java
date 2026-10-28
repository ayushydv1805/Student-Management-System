package edu.college.sms.servlet;

import edu.college.sms.dao.AcademicDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.time.LocalDate;

@WebServlet("/attendance")
public class AttendanceServlet extends HttpServlet {
    private final AcademicDAO dao=new AcademicDAO();
    @Override protected void doGet(HttpServletRequest req,HttpServletResponse resp)throws ServletException,IOException{
        String date=req.getParameter("date");
        if(date==null||date.isBlank())date=LocalDate.now().toString();
        try {
            date=LocalDate.parse(date).toString();
            req.setAttribute("selectedDate",date);
            req.setAttribute("records",dao.attendance(date,req.getParameter("q")));
            req.getRequestDispatcher("/attendance.jsp").forward(req,resp);
        } catch(java.time.format.DateTimeParseException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST,"Enter a valid attendance date in YYYY-MM-DD format.");
        } catch(Exception e) {
            throw new ServletException("Unable to load attendance records.",e);
        }
    }
    @Override protected void doPost(HttpServletRequest req,HttpServletResponse resp)throws ServletException,IOException{
        req.setCharacterEncoding("UTF-8");String date=req.getParameter("date"),id=req.getParameter("enrollmentId"),status=req.getParameter("status");
        try{
            Integer enrollmentId=RequestValidation.positiveInt(id);
            if(date==null||!date.matches("\\d{4}-\\d{2}-\\d{2}")||enrollmentId==null||!java.util.Arrays.asList("PRESENT","ABSENT","LATE","EXCUSED").contains(status)){resp.sendError(400,"Provide a valid date, enrollment and attendance status.");return;}
            LocalDate.parse(date);dao.saveAttendance(enrollmentId,date,status);resp.sendRedirect(req.getContextPath()+"/attendance?date="+date+"&message=saved");
        }catch(java.time.DateTimeException e){resp.sendError(400,"Invalid attendance date.");}
        catch(Exception e){throw new ServletException("Unable to save attendance.",e);}
    }
}
