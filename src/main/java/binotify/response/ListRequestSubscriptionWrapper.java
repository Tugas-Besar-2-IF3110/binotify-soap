package binotify.response;

import binotify.entity.SubscriptionEntity;

import java.util.ArrayList;
import java.util.List;

public class ListRequestSubscriptionWrapper {
    public ListRequestSubscriptionWrapper(List<SubscriptionEntity> elements) {
        this.elements = elements;
    }

    public List<SubscriptionEntity> elements = new ArrayList<SubscriptionEntity>();
}
