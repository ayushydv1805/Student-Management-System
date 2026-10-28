package edu.college.sms.security;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

@WebFilter("/*")
public class AdminAuthFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req=(HttpServletRequest)request;
        HttpServletResponse resp=(HttpServletResponse)response;
        String path=req.getRequestURI().substring(req.getContextPath().length());

        // Render's health probe must not need admin credentials; public CSS contains no private data.
        if ("/health".equals(path) || path.startsWith("/assets/")) {
            chain.doFilter(request,response);
            return;
        }

        String expectedUser=System.getenv("SMS_ADMIN_USER");
        String expectedPassword=System.getenv("SMS_ADMIN_PASSWORD");
        if (expectedUser==null || expectedUser.isBlank() || expectedPassword==null || expectedPassword.isBlank()) {
            resp.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                "Admin access is not configured. Set SMS_ADMIN_USER and SMS_ADMIN_PASSWORD.");
            return;
        }

        String header=req.getHeader("Authorization");
        if (header!=null && header.startsWith("Basic ")) {
            try {
                String decoded=new String(Base64.getDecoder().decode(header.substring(6).trim()),StandardCharsets.UTF_8);
                int separator=decoded.indexOf(':');
                if (separator>=0) {
                    String username=decoded.substring(0,separator);
                    String password=decoded.substring(separator+1);
                    if (constantTimeEquals(expectedUser,username) && constantTimeEquals(expectedPassword,password)) {
                        chain.doFilter(request,response);
                        return;
                    }
                }
            } catch (IllegalArgumentException ignored) {
                // Malformed base64 is treated as a failed login.
            }
        }
        resp.setHeader("WWW-Authenticate","Basic realm=\"CampusDesk Admin\", charset=\"UTF-8\"");
        resp.setHeader("Cache-Control","no-store");
        resp.sendError(HttpServletResponse.SC_UNAUTHORIZED,"Administrator sign-in required.");
    }

    private boolean constantTimeEquals(String expected,String actual) {
        return MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8),actual.getBytes(StandardCharsets.UTF_8));
    }
}
