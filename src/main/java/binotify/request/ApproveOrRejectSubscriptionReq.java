package binotify.request;

public class ApproveOrRejectSubscriptionReq extends BaseRequest {
    public int creatorId;
    public int subscriberId;
    public boolean approve;
}
