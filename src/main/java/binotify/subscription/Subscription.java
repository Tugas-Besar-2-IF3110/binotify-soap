package binotify.subscription;

import binotify.entity.SubscriptionEntity;
import binotify.request.*;
import binotify.response.*;
import binotify.security.Logger;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.xml.ws.developer.JAXWSProperties;
import jakarta.annotation.Resource;
import jakarta.jws.WebService;
import jakarta.mail.*;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import jakarta.xml.ws.WebServiceContext;
import jakarta.xml.ws.handler.MessageContext;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.*;

@WebService(endpointInterface = "binotify.subscription.ISubscription")
public class Subscription implements ISubscription {
    @Resource
    WebServiceContext wsContext;

    private Connection db_conn;
    private Logger logger;

    public Subscription(Connection db_conn) {
        this.db_conn = db_conn;
        this.logger = new Logger(db_conn);
    }

    @Override
    public RequestSubscriptionResp requestSubscription(RequestSubscriptionReq reqSub) {
        LocalDateTime timestamp = LocalDateTime.now();
        MessageContext msgx = wsContext.getMessageContext();
        String IPAddress = this.getRequestIPAddress(msgx);

        RequestSubscriptionReq req = reqSub;
        RequestSubscriptionResp resp = null;

        if (!System.getProperty("BINOTIFY_APP_API_KEY").equals(reqSub.API_KEY)) {
            resp = new RequestSubscriptionResp(false, "Not Authorized", null, null, null);
        } else {
            try {
                Statement statement = this.db_conn.createStatement();
                String sql = "INSERT INTO subscription(creator_id, subscriber_id, status) "
                        + "VALUES (%d, %d, '%s')";
                String formattedSql = String.format(sql, reqSub.creatorId, reqSub.subscriberId, "PENDING");
                int count = statement.executeUpdate(formattedSql);
                if (count == 1) {
                    resp = new RequestSubscriptionResp(true, "Added new subscription request with status: PENDING", reqSub.creatorId, reqSub.subscriberId, "PENDING");
                    notificationEmailAdmin();
                } else {
                    resp = new RequestSubscriptionResp(true, "Subscription request failed", reqSub.creatorId, reqSub.subscriberId, null);
                }
            } catch (Exception e) {
    //            e.printStackTrace();
                resp = new RequestSubscriptionResp(false, e.getMessage(), null, null, null);
            }
        }

        this.logger.generateLogging(timestamp, IPAddress, req, resp);
        return resp;
    }

    @Override
    public ApproveOrRejectSubscriptionResp approveOrRejectSubscription(ApproveOrRejectSubscriptionReq appOrRej) {
        LocalDateTime timestamp = LocalDateTime.now();
        MessageContext msgx = wsContext.getMessageContext();
        String IPAddress = this.getRequestIPAddress(msgx);

        ApproveOrRejectSubscriptionReq req = appOrRej;
        ApproveOrRejectSubscriptionResp resp = null;

        if (!System.getProperty("BINOTIFY_REST_API_KEY").equals(appOrRej.API_KEY)) {
            resp = new ApproveOrRejectSubscriptionResp(false, "Not Authorized", null, null, null);
        } else {
            try {
                Statement statement = this.db_conn.createStatement();
                String sql =
                        "UPDATE subscription " +
                        "SET status = '%s' " +
                        "WHERE " +
                        "creator_id = %d " +
                        "AND subscriber_id = %d";

                String appOrRejString = appOrRej.approve ? "ACCEPTED": "REJECTED";
                String formattedSql = String.format(sql, appOrRejString, appOrRej.creatorId, appOrRej.subscriberId);
                int count = statement.executeUpdate(formattedSql);
                if (count == 1) {
                    boolean successCallback = this.callbackUpdateRequest(appOrRej.creatorId, appOrRej.subscriberId, appOrRejString);

                    String message = appOrRejString + " subscription successfully";
                    if (!successCallback) {
                        message += " but callback failed";
                    } else {
                        sql =
                                "UPDATE subscription " +
                                        "SET is_polled = %s " +
                                        "WHERE " +
                                        "creator_id = %d " +
                                        "AND subscriber_id = %d";

                        formattedSql = String.format(sql, "true", appOrRej.creatorId, appOrRej.subscriberId);
                        count = statement.executeUpdate(formattedSql);

                        if (count == 1) {
                            System.out.println("Update is_polled successful");
                        } else {
                            System.out.println("Update is_polled failed");
                        }
                    }
                    resp = new ApproveOrRejectSubscriptionResp(true, message, appOrRej.creatorId, appOrRej.subscriberId, appOrRejString);
                } else {
                    String message = appOrRejString + " subscription failed";
                    resp = new ApproveOrRejectSubscriptionResp(false, message, appOrRej.creatorId, appOrRej.subscriberId, null);
                }

            } catch (Exception e) {
                e.printStackTrace();
                resp = new ApproveOrRejectSubscriptionResp(false, e.getMessage(), null, null, null);
            }
        }

        this.logger.generateLogging(timestamp, IPAddress, req, resp);
        return resp;
    }

    @Override
    public ListRequestSubscriptionResp listRequestSubscription(ListRequestSubscriptionReq listReqSub) {
        LocalDateTime timestamp = LocalDateTime.now();
        MessageContext msgx = wsContext.getMessageContext();
        String IPAddress = this.getRequestIPAddress(msgx);

        ListRequestSubscriptionReq req = listReqSub;
        ListRequestSubscriptionResp resp = null;

        if (!System.getProperty("BINOTIFY_REST_API_KEY").equals(listReqSub.API_KEY)) {
            resp = new ListRequestSubscriptionResp(false, "Not Authorized", null);
        } else {
            try {
                Statement statement = this.db_conn.createStatement();
                String sql = "SELECT * FROM subscription WHERE status = 'PENDING'";
                ResultSet resultSet = statement.executeQuery(sql);
                List<SubscriptionEntity> listResp = new ArrayList<SubscriptionEntity>();
                while (resultSet.next()) {
                    listResp.add(new SubscriptionEntity(
                            resultSet.getInt("creator_id"),
                            resultSet.getInt("subscriber_id"),
                            resultSet.getString("status")
                    ));
                }
                String message = "Subscription request list fetched successfully";
                resp = new ListRequestSubscriptionResp(true, message, listResp);

            } catch (Exception e) {
                e.printStackTrace();
                resp = new ListRequestSubscriptionResp(false, e.getMessage(), null);
            }
        }

        this.logger.generateLogging(timestamp, IPAddress, req, resp);
        return resp;
    }

    @Override
    public ValidateSubscriptionResp validateSubscription(ValidateSubscriptionReq valSub) {
        LocalDateTime timestamp = LocalDateTime.now();
        MessageContext msgx = wsContext.getMessageContext();
        String IPAddress = this.getRequestIPAddress(msgx);

        ValidateSubscriptionReq req = valSub;
        ValidateSubscriptionResp resp = null;

        if (!System.getProperty("BINOTIFY_REST_API_KEY").equals(valSub.API_KEY)) {
            resp = new ValidateSubscriptionResp(false, "Not Authorized", null, null, null);
        } else {
            try {
                Statement statement = this.db_conn.createStatement();
                String sql = "SELECT * FROM subscription WHERE creator_id = " + valSub.creatorId
                        + " AND subscriber_id = " + valSub.subscriberId;
                ResultSet resultSet = statement.executeQuery(sql);

                SubscriptionEntity subscription = null;
                if (resultSet.next()) {
                    subscription = new SubscriptionEntity(
                            resultSet.getInt("creator_id"),
                            resultSet.getInt("subscriber_id"),
                            resultSet.getString("status")
                    );
                }
                String message = "Subscription validation fetched successfully";
                boolean subscribed = subscription != null && subscription.status.equals("ACCEPTED");

                resp = new ValidateSubscriptionResp(true, message, valSub.creatorId, valSub.subscriberId, subscribed);

            } catch (Exception e) {
                e.printStackTrace();
                resp = new ValidateSubscriptionResp(false, e.getMessage(), null, null, null);
            }
        }

        this.logger.generateLogging(timestamp, IPAddress, req, resp);
        return resp;
    }

    @Override
    public CheckStatusRequestResp checkStatusRequest(CheckStatusRequestReq csr) {
        LocalDateTime timestamp = LocalDateTime.now();
        MessageContext msgx = wsContext.getMessageContext();
        String IPAddress = this.getRequestIPAddress(msgx);

        CheckStatusRequestReq req = csr;
        CheckStatusRequestResp resp = null;

        if (!System.getProperty("BINOTIFY_APP_API_KEY").equals(csr.API_KEY)) {
            resp = new CheckStatusRequestResp(false, "Not Authorized", null);
        } else {
            try {
                Statement statement = this.db_conn.createStatement();
                String query = "SELECT * FROM subscription WHERE is_polled = 0";
                if (csr.subscriberId != null) {
                    query += " AND subscriber_id = " + csr.subscriberId;
                }

                if (csr.creatorId != null) {
                    query += " AND creator_id = " + csr.creatorId;
                }
                ResultSet resultSet = statement.executeQuery(query);
                List<SubscriptionEntity> listResp = new ArrayList<SubscriptionEntity>();
                while (resultSet.next()) {
                    listResp.add(new SubscriptionEntity(
                            resultSet.getInt("creator_id"),
                            resultSet.getInt("subscriber_id"),
                            resultSet.getString("status")
                    ));
                }

                String message = "Check status request list fetched successfully";
                resp = new CheckStatusRequestResp(true, message, listResp);

                String sql =
                        "UPDATE subscription " +
                        "SET is_polled = true " +
                        "WHERE " +
                        "is_polled = false AND status <> 'PENDING'";

                if (csr.subscriberId != null) {
                    sql += " AND subscriber_id = " + csr.subscriberId;
                }

                if (csr.creatorId != null) {
                    sql += " AND creator_id = " + csr.creatorId;
                }

                int count = statement.executeUpdate(sql);

            } catch (Exception e) {
                e.printStackTrace();
                resp = new CheckStatusRequestResp(false, e.getMessage(), null);
            }
        }
        this.logger.generateLogging(timestamp, IPAddress, req, resp);
        return resp;
    }

    public boolean callbackUpdateRequest(int creatorId, int subscriberId, String status) {
        int statusCode = 0;
        try {
            URL url = new URL(System.getProperty("BINOTIFY_APP_CALLBACK_URL"));

            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Authorization", System.getProperty("API_KEY"));

            Map<String, Object> parameters = new HashMap<>();
            parameters.put("creator_id", creatorId);
            parameters.put("subscriber_id", subscriberId);
            parameters.put("status", status);

            con.setDoOutput(true);
            DataOutputStream out = new DataOutputStream(con.getOutputStream());
            out.writeBytes(getFormDataAsString(parameters));
            out.flush();
            out.close();

            con.setConnectTimeout(5000);
            con.setReadTimeout(5000);
            statusCode = con.getResponseCode();

            if (statusCode == 200) {
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(con.getInputStream()));

                String inputLine;
                StringBuffer content = new StringBuffer();
                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine);
                }

                in.close();
                System.out.println(content.toString());
                return true;
            } else if (statusCode == 400) {
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(con.getErrorStream()));

                String inputLine;
                StringBuffer content = new StringBuffer();
                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine);
                }

                in.close();
                System.out.println(content.toString());
                return false;
            }
            return false;

        } catch (Exception e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    public static boolean notificationEmailAdmin() {
        int statusCode = 0;
        try {
            URL url = new URL(System.getProperty("BINOTIFY_REST_ADMIN_URL"));

            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Authorization", "Bearer " + System.getProperty("API_KEY"));

            Map<String, Object> parameters = new HashMap<>();

            con.setDoOutput(true);
            DataOutputStream out = new DataOutputStream(con.getOutputStream());
            out.writeBytes(getFormDataAsString(parameters));
            out.flush();
            out.close();

            con.setConnectTimeout(5000);
            con.setReadTimeout(5000);
            statusCode = con.getResponseCode();
            System.out.println(statusCode);

            if (statusCode >= 200 && statusCode <= 299) {
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(con.getInputStream()));

                String inputLine;
                StringBuffer content = new StringBuffer();
                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine);
                }

                in.close();
                System.out.println(content.toString());

                // Recipient's email ID needs to be mentioned.
                String to = content.toString();

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
                    protected jakarta.mail.PasswordAuthentication getPasswordAuthentication() {
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
                    message.setSubject("Notification!");

                    // Now set the actual message
                    message.setText("There is a new subscription request, please approve or reject!");

                    System.out.println("sending...");
                    // Send message
                    Transport.send(message);
                    System.out.println("Sent message successfully....");
                } catch (MessagingException mex) {
                    mex.printStackTrace();
                }
                return true;
            } else if (statusCode == 400) {
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(con.getErrorStream()));

                String inputLine;
                StringBuffer content = new StringBuffer();
                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine);
                }

                in.close();
                System.out.println(content.toString());
                return false;
            }
            return false;

        } catch (Exception e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    private static String getFormDataAsString(Map<String, Object> formData) {
        StringBuilder formBodyBuilder = new StringBuilder();
        for (Map.Entry<String, Object> singleEntry : formData.entrySet()) {
            if (formBodyBuilder.length() > 0) {
                formBodyBuilder.append("&");
            }
            formBodyBuilder.append(URLEncoder.encode(singleEntry.getKey(), StandardCharsets.UTF_8));
            formBodyBuilder.append("=");
            formBodyBuilder.append(URLEncoder.encode(singleEntry.getValue().toString(), StandardCharsets.UTF_8));
        }
        return formBodyBuilder.toString();
    }

    public String getRequestIPAddress(MessageContext msgx) {
        HttpExchange exchange = (HttpExchange) msgx.get(JAXWSProperties.HTTP_EXCHANGE);
        InetSocketAddress remoteAddress = exchange.getRemoteAddress();
        InetAddress address = remoteAddress.getAddress();
        return address.getHostAddress();
    }
}
