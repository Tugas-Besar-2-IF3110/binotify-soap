package binotify.response;

import binotify.entity.SubscriptionEntity;

import java.sql.ResultSet;
import java.util.List;

public class ListRequestSubscriptionResp extends BaseResponse {
    public List<SubscriptionEntity> list;

    public ListRequestSubscriptionResp(boolean success, String message, List<SubscriptionEntity> list) {
        super(success, message);
        this.list = list;
    }
}
