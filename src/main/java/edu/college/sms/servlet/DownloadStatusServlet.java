package edu.college.sms.servlet;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@WebServlet("/download/check")
public class DownloadStatusServlet extends HttpServlet {
    private static final HttpClient HTTP = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(15))
        .followRedirects(HttpClient.Redirect.ALWAYS)
        .build();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("text/plain; charset=UTF-8");
        resp.setHeader("Cache-Control", "no-store");
        try {
            String windows = head("https://github.com/ayushydv1805/Student-Management-System/releases/download/campusdesk-v1.0.0/CampusDesk-Setup.exe");
            String android = head("https://github.com/ayushydv1805/Student-Management-System/releases/download/campusdesk-v1.0.0/CampusDesk.apk");
            boolean ok = windows.startsWith("200") && android.startsWith("200");
            resp.setStatus(ok ? HttpServletResponse.SC_OK : HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            resp.getWriter().write("windows=" + windows + "\nandroid=" + android + "\n");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            resp.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            resp.getWriter().write("source check interrupted\n");
        } catch (IOException e) {
            getServletContext().log("CampusDesk installer source check failed.", e);
            resp.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            resp.getWriter().write("installer source unreachable\n");
        }
    }

    private String head(String url) throws IOException, InterruptedException {
        HttpRequest req = HttpRequest.newBuilder(URI.create(url))
            .timeout(Duration.ofSeconds(30))
            .header("User-Agent", "CampusDesk-Download-Health/1.0")
            .method("HEAD", HttpRequest.BodyPublishers.noBody())
            .build();
        HttpResponse<Void> result = HTTP.send(req, HttpResponse.BodyHandlers.discarding());
        return result.statusCode() + " bytes=" + result.headers().firstValue("Content-Length").orElse("unknown");
    }
}
