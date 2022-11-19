package binotify;

import java.sql.Connection;

import binotify.security.SecurityImplementation;
import io.github.cdimascio.dotenv.Dotenv;
import jakarta.xml.ws.Endpoint;

public class App 
{
    public static void main( String[] args )
    {
        Dotenv.configure().systemProperties().load();
        DBHandler db = new DBHandler();
        Connection db_conn = db.getConnection();
        Endpoint.publish(System.getProperty("BASE_URL") + "/logging", new SecurityImplementation(db_conn));
    }
}
