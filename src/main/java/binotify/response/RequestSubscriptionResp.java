package binotify.response;

public class RequestSubscriptionResp extends BaseResponse {
    public String creatorId;
    public String subscriberId;
    public String status;

    public RequestSubscriptionResp(boolean success, String message, String creatorId, String subscriberId, String status) {
        super(success, message);
        this.creatorId = creatorId;
        this.subscriberId = subscriberId;
        this.status = status;
    }
}
