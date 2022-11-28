package binotify.response;

import binotify.entity.SubscriptionEntity;

import java.sql.ResultSet;
import java.util.List;

public class ListRequestSubscriptionResp extends BaseResponse {
    public ListRequestSubscriptionWrapper list;

    public ListRequestSubscriptionResp(boolean success, String message, List<SubscriptionEntity> list) {
        super(success, message);
        this.list = new ListRequestSubscriptionWrapper(list);
    }
}
