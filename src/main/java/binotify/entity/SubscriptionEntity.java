package binotify.entity;

public class SubscriptionEntity {
    public int creatorId;
    public int subscriberId;
    public String status;

    public SubscriptionEntity(int creatorId, int subscriberId, String status) {
        this.creatorId = creatorId;
        this.subscriberId = subscriberId;
        this.status = status;
    }
}
