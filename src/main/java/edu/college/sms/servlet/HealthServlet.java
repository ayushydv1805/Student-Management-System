package edu.college.sms.servlet;

import edu.college.sms.config.Database;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@WebServlet("/health")
public class HealthServlet extends HttpServlet {
    @Override protected void doGet(HttpServletRequest req,HttpServletResponse resp)throws IOException{
        resp.setContentType("text/plain; charset=UTF-8");
        resp.setHeader("Cache-Control","no-store");
        try(Connection c=Database.getConnection();
            PreparedStatement p=c.prepareStatement("SELECT 1");
            ResultSet r=p.executeQuery()){
            if(!r.next())throw new java.sql.SQLException("Database health query returned no row.");
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write("ok");
        }catch(Exception e){
            getServletContext().log("CampusDesk database health check failed.",e);
            resp.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            resp.getWriter().write("database unavailable");
        }
    }
}
