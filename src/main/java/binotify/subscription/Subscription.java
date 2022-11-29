package binotify.subscription;

import binotify.entity.SubscriptionEntity;
import binotify.request.ApproveOrRejectSubscriptionReq;
import binotify.request.ListRequestSubscriptionReq;
import binotify.request.RequestHeader;
import binotify.request.RequestSubscriptionReq;
import binotify.response.ApproveOrRejectSubscriptionResp;
import binotify.response.ListRequestSubscriptionResp;
import binotify.response.RequestSubscriptionResp;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import jakarta.jws.WebService;
import jakarta.xml.ws.WebServiceContext;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
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
                this.callbackUpdateRequest(appOrRej.creatorId, appOrRej.subscriberId, "REJECTED");
                String message = appOrRejString + " subscription successfully";
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

    public void callbackUpdateRequest(int creatorId, int subscriberId, String status) throws IOException, InterruptedException {
        ObjectMapper mapper = new ObjectMapper();
        LinkedHashMap<String, Object> params = new LinkedHashMap<String, Object>();
        params.put("creator_id", creatorId);
        params.put("subscriber_id", subscriberId);
        params.put("status", status);

        String postDataString = mapper.writeValueAsString(params);
        System.out.println(postDataString);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .header("Authorization", System.getProperty("API_KEY"))
                .uri(URI.create(System.getProperty("BINOTIFY_APP_CALLBACK_URL")))
                .POST(HttpRequest.BodyPublishers.ofString(postDataString))
                .build();

        HttpResponse<String> response = client.send(request,
                HttpResponse.BodyHandlers.ofString());

        System.out.println(response.statusCode());
        System.out.println(response.body());
        Map<String,Object> map = mapper.readValue(response.body(), Map.class);
    }
}
