package binotify.response;

public class ApproveOrRejectSubscriptionResp extends BaseResponse {
    public Integer creatorId;
    public Integer subscriberId;
    public String status;

    public ApproveOrRejectSubscriptionResp(boolean success, String message, Integer creatorId, Integer subscriberId, String status) {
        super(success, message);
        this.creatorId = creatorId;
        this.subscriberId = subscriberId;
        this.status = status;
    }
}
