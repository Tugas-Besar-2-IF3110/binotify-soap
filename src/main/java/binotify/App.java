package binotify;

import java.io.*;
import java.sql.Connection;

import binotify.subscription.Subscription;
import io.github.cdimascio.dotenv.Dotenv;
import jakarta.xml.ws.Endpoint;

public class App 
{
    public static void main( String[] args ) throws IOException, InterruptedException {
        Dotenv.configure().systemProperties().load();
        DBHandler db = new DBHandler();
        Connection db_conn = db.getConnection();

        Endpoint.publish(System.getProperty("BASE_URL") + "/subscription", new Subscription(db_conn));
    }


}
