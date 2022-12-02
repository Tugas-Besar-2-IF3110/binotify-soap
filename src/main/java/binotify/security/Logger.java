package binotify.security;

import binotify.request.*;
import binotify.response.*;

import java.sql.Connection;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Logger {
    private Connection db_conn;

    public Logger(Connection db_conn) {
        this.db_conn = db_conn;
    }

    public void generateLogging(LocalDateTime timestamp, String IPAddress, BaseRequest req, BaseResponse resp) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        StringBuilder descriptionBuilder = new StringBuilder();
        String endpoint = "";

        if (!resp.success && resp.message.equals("Not Authorized")) {
            descriptionBuilder.append("Request from unknown sources, request rejected");
        } else {
            if (req instanceof RequestSubscriptionReq && resp instanceof RequestSubscriptionResp) {
                RequestSubscriptionReq reqCasted = (RequestSubscriptionReq) req;
                RequestSubscriptionResp respCasted = (RequestSubscriptionResp) resp;
                descriptionBuilder.append("Request subscription for subscriber_id = " + reqCasted.subscriberId + " and creator_id = " + reqCasted.creatorId);
                endpoint = "Request Subscription";

            } else if (req instanceof ListRequestSubscriptionReq && resp instanceof ListRequestSubscriptionResp) {
                ListRequestSubscriptionReq reqCasted = (ListRequestSubscriptionReq) req;
                ListRequestSubscriptionResp respCasted = (ListRequestSubscriptionResp) resp;
                descriptionBuilder.append("Request list of all subscription with status \"PENDING\"");
                endpoint = "List Request Subscription";

            } else if (req instanceof ValidateSubscriptionReq && resp instanceof ValidateSubscriptionResp) {
                ValidateSubscriptionReq reqCasted = (ValidateSubscriptionReq) req;
                ValidateSubscriptionResp respCasted = (ValidateSubscriptionResp) resp;
                descriptionBuilder.append(
                        "Request validate subscription for subscriber_id = " + reqCasted.subscriberId + " and creator_id = " + reqCasted.creatorId
                        + ", validation result is " + respCasted.subscribed
                );
                endpoint = "Validate Subscription";

            } else if (req instanceof ApproveOrRejectSubscriptionReq && resp instanceof ApproveOrRejectSubscriptionResp) {
                ApproveOrRejectSubscriptionReq reqCasted = (ApproveOrRejectSubscriptionReq) req;
                ApproveOrRejectSubscriptionResp respCasted = (ApproveOrRejectSubscriptionResp) resp;
                descriptionBuilder.append(
                        "Request to " + (reqCasted.approve ? "approve" : "reject") + " subscription request for subscriber_id = " + reqCasted.subscriberId + " and creator_id = " + reqCasted.creatorId
                                + ", subscription request " + respCasted.status
                );
                endpoint = "Approve Or Reject Subscription";

            }  else if (req instanceof CheckStatusRequestReq && resp instanceof CheckStatusRequestResp) {
                CheckStatusRequestReq reqCasted = (CheckStatusRequestReq) req;
                CheckStatusRequestResp respCasted = (CheckStatusRequestResp) resp;
                descriptionBuilder.append("Request to check status request request for ");
                if (reqCasted.creatorId != null && reqCasted.subscriberId != null) {
                    descriptionBuilder.append("subscriber_id = " + reqCasted.subscriberId + " and creator_id = " + reqCasted.subscriberId);
                } else if (reqCasted.creatorId != null) {
                    descriptionBuilder.append("creator_id = " + reqCasted.creatorId);
                } else if (reqCasted.subscriberId != null) {
                    descriptionBuilder.append("subscriber_id = " + reqCasted.subscriberId);
                }

                endpoint = "Check Status Request";
            } else {
                descriptionBuilder.append("Invalid request format");
            }

            if (resp.success) {
                descriptionBuilder.append(", successful");
            } else {
                descriptionBuilder.append(", failed");
            }
        }
        try {
            String description = descriptionBuilder.toString();
            String timestampString = dtf.format(timestamp);
            Statement statement = this.db_conn.createStatement();
            String sql = "INSERT INTO logging(description, IP, endpoint, requested_at) "
                    + "VALUES ('%s', '%s', '%s', '%s')";

            String formattedSql = String.format(sql, description, IPAddress, endpoint, timestampString);
            int count = statement.executeUpdate(formattedSql);
            if (count == 1) {
                System.out.println("Logging added");
            } else {
                System.out.println("Failed to add logging");
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Failed to add logging");
        }
    }
}
