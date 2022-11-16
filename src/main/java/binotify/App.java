package binotify;

import binotify.security.SecurityImplementation;
import io.github.cdimascio.dotenv.Dotenv;
import jakarta.xml.ws.Endpoint;

public class App 
{
    public static void main( String[] args )
    {
        Dotenv.configure().systemProperties().load();
        Endpoint.publish(System.getProperty("BASE_URL") + "/logging", new SecurityImplementation());
    }
}
