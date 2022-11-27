package binotify.subscription;

import binotify.entity.SubscriptionEntity;
import binotify.request.ApproveOrRejectSubscriptionReq;
import binotify.request.ListRequestSubscriptionReq;
import binotify.request.RequestSubscriptionReq;
import binotify.response.ApproveOrRejectSubscriptionResp;
import binotify.response.ListRequestSubscriptionResp;
import binotify.response.RequestSubscriptionResp;
import jakarta.jws.WebService;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

@WebService(endpointInterface = "binotify.subscription.ISubscription")
public class Subscription implements ISubscription {
    private Connection db_conn;

    public Subscription(Connection db_conn) {
        this.db_conn = db_conn;
    }

    @Override
    public RequestSubscriptionResp requestSubscription(RequestSubscriptionReq reqSub) {
        try {
            Statement statement = this.db_conn.createStatement();
            String sql = "INSERT INTO subscription(creator_id, subscriber_id, status)"
                    + "VALUES ('%s', '%s', '%s')";
            String formattedSql = String.format(sql, reqSub.creatorId, reqSub.subscriberId, "PENDING");
            int count = statement.executeUpdate(formattedSql);
            if (count == 1) {
                return new RequestSubscriptionResp(true, "Added new subscription request with status: PENDING", reqSub.creatorId, reqSub.subscriberId, "PENDING");
            } else {
                return new RequestSubscriptionResp(true, "Subscription request failed", reqSub.creatorId, reqSub.subscriberId, null);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new RequestSubscriptionResp(false, e.getMessage(), null, null, null);
        }
    }

    @Override
    public ApproveOrRejectSubscriptionResp approveOrRejectSubscription(ApproveOrRejectSubscriptionReq appOrRej) {
        try {
            Statement statement = this.db_conn.createStatement();
            String sql =
                    "UPDATE subscription" +
                    "SET status = '%s'" +
                    "WHERE" +
                    "creator_id = '%s" +
                    "AND subscriber_id = '%s";

            String appOrRejString = appOrRej.approve ? "ACCEPTED": "REJECTED";
            String formattedSql = String.format(sql, appOrRejString, appOrRej.creatorId, appOrRej.subscriberId);
            int count = statement.executeUpdate(formattedSql);
            if (count == 1) {
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
        try {
            Statement statement = this.db_conn.createStatement();
            String sql = "SELECT * FROM subscription WHERE status = 'PENDING'";
            ResultSet resultSet = statement.executeQuery(sql);
            List<SubscriptionEntity> listResp = new ArrayList<SubscriptionEntity>();
            while (resultSet.next()) {
                listResp.add(new SubscriptionEntity(
                        resultSet.getString("creator_id"),
                        resultSet.getString("subscriber_id"),
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
}
