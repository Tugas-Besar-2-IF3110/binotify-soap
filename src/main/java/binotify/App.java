package binotify;

import java.io.*;
import java.sql.Connection;

import binotify.subscription.Subscription;
import io.github.cdimascio.dotenv.Dotenv;
import jakarta.xml.ws.Endpoint;

import java.util.Properties;

import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

public class App 
{
    public static void main( String[] args ) throws IOException, InterruptedException {
        Dotenv.configure().systemProperties().load();
        DBHandler db = new DBHandler();
        Connection db_conn = db.getConnection();

        // Recipient's email ID needs to be mentioned.
        String to = "kareldavid9@gmail.com";

        // Sender's email ID needs to be mentioned
        String from = System.getProperty("MAIL_USERNAME");

        // Assuming you are sending email from through gmails smtp
        String host = "smtp.gmail.com";

        // Get system properties
        Properties properties = System.getProperties();

        // Setup mail server
        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.port", "465");
        properties.put("mail.smtp.ssl.enable", "true");
        properties.put("mail.smtp.auth", "true");

        // Get the Session object.// and pass username and password
        Session session = Session.getInstance(properties, new jakarta.mail.Authenticator() {

            protected PasswordAuthentication getPasswordAuthentication() {

                return new PasswordAuthentication(System.getProperty("MAIL_USERNAME"), System.getProperty("MAIL_PASSWORD"));

            }

        });

        // Used to debug SMTP issues
        session.setDebug(true);

        try {
            // Create a default MimeMessage object.
            MimeMessage message = new MimeMessage(session);

            // Set From: header field of the header.
            message.setFrom(new InternetAddress(from));

            // Set To: header field of the header.
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));

            // Set Subject: header field
            message.setSubject("This is the Subject Line!");

            // Now set the actual message
            message.setText("This is actual message");

            System.out.println("sending...");
            // Send message
            Transport.send(message);
            System.out.println("Sent message successfully....");
        } catch (MessagingException mex) {
            mex.printStackTrace();
        }

        Endpoint.publish(System.getProperty("BASE_URL") + "/subscription", new Subscription(db_conn));
    }


}
