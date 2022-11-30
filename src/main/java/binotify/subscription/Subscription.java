package binotify.subscription;

import binotify.entity.SubscriptionEntity;
import binotify.request.*;
import binotify.response.ApproveOrRejectSubscriptionResp;
import binotify.response.ListRequestSubscriptionResp;
import binotify.response.RequestSubscriptionResp;
import binotify.response.ValidateSubscriptionResp;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import jakarta.jws.WebService;
import jakarta.xml.ws.WebServiceContext;

import java.io.*;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.*;

@WebService(endpointInterface = "binotify.subscription.ISubscription")
public class Subscription implements ISubscription {
    @Resource
    private WebServiceContext ctx;


    private Connection db_conn;

    public Subscription(Connection db_conn) {
        this.db_conn = db_conn;
    }

    @Override
    public RequestSubscriptionResp requestSubscription(RequestSubscriptionReq reqSub) {
        if (!System.getProperty("BINOTIFY_APP_API_KEY").equals(reqSub.API_KEY)) {
            return new RequestSubscriptionResp(false, "Not Authorized", null, null, null);
        }

        try {
            Statement statement = this.db_conn.createStatement();
            String sql = "INSERT INTO subscription(creator_id, subscriber_id, status) "
                    + "VALUES (%d, %d, '%s')";
            String formattedSql = String.format(sql, reqSub.creatorId, reqSub.subscriberId, "PENDING");
            int count = statement.executeUpdate(formattedSql);
            if (count == 1) {
                return new RequestSubscriptionResp(true, "Added new subscription request with status: PENDING", reqSub.creatorId, reqSub.subscriberId, "PENDING");
            } else {
                return new RequestSubscriptionResp(true, "Subscription request failed", reqSub.creatorId, reqSub.subscriberId, null);
            }
        } catch (Exception e) {
//            e.printStackTrace();
            return new RequestSubscriptionResp(false, e.getMessage(), null, null, null);
        }
    }

    @Override
    public ApproveOrRejectSubscriptionResp approveOrRejectSubscription(ApproveOrRejectSubscriptionReq appOrRej) {
        if (!System.getProperty("BINOTIFY_REST_API_KEY").equals(appOrRej.API_KEY)) {
            return new ApproveOrRejectSubscriptionResp(false, "Not Authorized", null, null, null);
        }

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
                }
                return new ApproveOrRejectSubscriptionResp(true, message, appOrRej.creatorId, appOrRej.subscriberId, appOrRejString);
            } else {
                String message = appOrRejString + " subscription failed";
                return new ApproveOrRejectSubscriptionResp(false, message, appOrRej.creatorId, appOrRej.subscriberId, null);
            }

        } catch (Exception e) {
            e.printStackTrace();
            return new ApproveOrRejectSubscriptionResp(false, e.getMessage(), null, null, null);
        }
    }

    @Override
    public ListRequestSubscriptionResp listRequestSubscription(ListRequestSubscriptionReq listReqSub) {
        if (!System.getProperty("BINOTIFY_REST_API_KEY").equals(listReqSub.API_KEY)) {
            return new ListRequestSubscriptionResp(false, "Not Authorized", null);
        }

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
            ListRequestSubscriptionResp resp = new ListRequestSubscriptionResp(true, message, listResp);
            return resp;

        } catch (Exception e) {
            e.printStackTrace();
            ListRequestSubscriptionResp resp = new ListRequestSubscriptionResp(false, e.getMessage(), null);
            return resp;
        }
    }

    @Override
    public ValidateSubscriptionResp validateSubscription(ValidateSubscriptionReq valSub) {
        if (!System.getProperty("BINOTIFY_REST_API_KEY").equals(valSub.API_KEY)) {
            return new ValidateSubscriptionResp(false, "Not Authorized", null, null, null);
        }

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
            boolean subscribed = subscription != null && subscription.status == "ACCEPTED";

            ValidateSubscriptionResp resp = new ValidateSubscriptionResp(true, message, valSub.creatorId, valSub.subscriberId, subscribed);
            return resp;

        } catch (Exception e) {
            e.printStackTrace();
            ValidateSubscriptionResp resp = new ValidateSubscriptionResp(false, e.getMessage(), null, null, null);
            return resp;
        }
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
}
