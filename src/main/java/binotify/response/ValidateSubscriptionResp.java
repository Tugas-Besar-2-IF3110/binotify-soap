package binotify.response;

public class ValidateSubscriptionResp extends BaseResponse {
    public Integer creatorId;
    public Integer subscriberId;
    public Boolean subscribed;

    public ValidateSubscriptionResp(boolean success, String message, Integer creatorId, Integer subscriberId, Boolean subscribed) {
        super(success, message);
        this.creatorId = creatorId;
        this.subscriberId = subscriberId;
        this.subscribed = subscribed;
    }
}
