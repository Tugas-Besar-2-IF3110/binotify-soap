package binotify;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.Connection;
import java.util.LinkedHashMap;
import java.util.Map;

import binotify.subscription.Subscription;
import binotify.test.Demo;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.cdimascio.dotenv.Dotenv;
import jakarta.xml.ws.Endpoint;

public class App 
{
    public static void main( String[] args ) throws IOException, InterruptedException {
        Dotenv.configure().systemProperties().load();
        DBHandler db = new DBHandler();
        Connection db_conn = db.getConnection();

//        Endpoint.publish(System.getProperty("BASE_URL") + "/logging", new SecurityImplementation(db_conn));
        Endpoint.publish(System.getProperty("BASE_URL") + "/test", new Demo(db_conn));
        Endpoint.publish(System.getProperty("BASE_URL") + "/subscription", new Subscription(db_conn));

    }

}
