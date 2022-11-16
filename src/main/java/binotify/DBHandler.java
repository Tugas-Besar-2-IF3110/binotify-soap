package binotify;

import java.sql.*;

public class DBHandler {
    private Connection connection;

    public DBHandler() {
        try {
            System.out.println("Connecting to MySQL Database");
            this.connection = DriverManager.getConnection(
                System.getProperty("DB_URL"), 
                System.getProperty("DB_USERNAME"), 
                System.getProperty("DB_PASSWORD")
            );
            System.out.println("Database connected!");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error on connecting to database");
        }
    }

    public Connection getConnection() {
        return this.connection;
    }
}
