package binotify.response;

public class RequestSubscriptionResp extends BaseResponse {
    public Integer creatorId;
    public Integer subscriberId;
    public String status;

    public RequestSubscriptionResp(boolean success, String message, Integer creatorId, Integer subscriberId, String status) {
        super(success, message);
        this.creatorId = creatorId;
        this.subscriberId = subscriberId;
        this.status = status;
    }
}
