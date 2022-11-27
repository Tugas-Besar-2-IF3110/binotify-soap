package binotify.response;

public class ApproveOrRejectSubscriptionResp extends BaseResponse {
    public String creatorId;
    public String subscriberId;
    public String status;

    public ApproveOrRejectSubscriptionResp(boolean success, String message, String creatorId, String subscriberId, String status) {
        super(success, message);
        this.creatorId = creatorId;
        this.subscriberId = subscriberId;
        this.status = status;
    }
}
