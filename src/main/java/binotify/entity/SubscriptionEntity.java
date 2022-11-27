package binotify.entity;

public class SubscriptionEntity {
    public String creatorId;
    public String subscriberId;
    public String status;

    public SubscriptionEntity(String creatorId, String subscriberId, String status) {
        this.creatorId = creatorId;
        this.subscriberId = subscriberId;
        this.status = status;
    }
}
