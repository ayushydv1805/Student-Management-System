package edu.college.sms.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class Database {
    private Database() {}

    public static Connection getConnection() throws SQLException {
        // Hosted deployments can route SQL through the authenticated Supabase HTTPS gateway.
        String gatewayUrl=System.getenv("SMS_GATEWAY_URL");
        if(gatewayUrl!=null&&!gatewayUrl.isBlank()){
            String token=System.getenv("SMS_GATEWAY_TOKEN");
            if(token==null||token.isBlank())throw new SQLException("SMS_GATEWAY_TOKEN is required when SMS_GATEWAY_URL is configured.");
            return HttpSqlConnection.open(gatewayUrl,token);
        }

        // MySQL remains the local-development default.
        String url=System.getenv().getOrDefault("SMS_DB_URL",
            "jdbc:mysql://localhost:3306/student_management_db?useSSL=false&serverTimezone=UTC");
        String user=System.getenv().getOrDefault("SMS_DB_USER","root");
        String password=System.getenv().getOrDefault("SMS_DB_PASSWORD","");
        return DriverManager.getConnection(url,user,password);
    }

    public static boolean isPostgres(Connection connection) throws SQLException {
        return "PostgreSQL".equalsIgnoreCase(connection.getMetaData().getDatabaseProductName());
    }
}
