package binotify.security;

import jakarta.jws.WebService;

import java.sql.*;

@WebService(endpointInterface = "binotify.security.Security")
public class SecurityImplementation extends Security {
    private Connection db_conn;

    public SecurityImplementation(Connection db_conn) {
        this.db_conn = db_conn;
    }

    @Override
    public String addLogging(String description, String IP, String endpoint) {
        try {
            Statement statement = this.db_conn.createStatement();
            String sql = "INSERT INTO logging(description, IP, endpoint)"
                + "VALUES ('%s', '%s', '%s')";
            String formattedSql = String.format(sql, description, IP, endpoint);
            int count = statement.executeUpdate(formattedSql);
            return "Added new logging with return value: " + count;
        } catch (Exception e) {
            e.printStackTrace();
            return "Something went wrong while adding logging " + e.getMessage();
        }
    }
}
