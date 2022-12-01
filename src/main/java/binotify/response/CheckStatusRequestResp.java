package binotify.response;

import binotify.entity.SubscriptionEntity;

import java.util.List;

public class CheckStatusRequestResp extends BaseResponse {
    public List<SubscriptionEntity> list;

    public CheckStatusRequestResp(boolean success, String message, List<SubscriptionEntity> list) {
        super(success, message);
        this.list = list;
    }
}
