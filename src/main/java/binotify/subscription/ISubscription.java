package binotify.subscription;

import binotify.request.*;
import binotify.response.ApproveOrRejectSubscriptionResp;
import binotify.response.ListRequestSubscriptionResp;
import binotify.response.RequestSubscriptionResp;
import binotify.response.ValidateSubscriptionResp;
import jakarta.jws.WebMethod;
import jakarta.jws.WebParam;
import jakarta.jws.WebService;
import jakarta.jws.soap.SOAPBinding;

@WebService
@SOAPBinding(style = SOAPBinding.Style.DOCUMENT)
public interface ISubscription {
    @WebMethod
    public RequestSubscriptionResp requestSubscription(
            @WebParam(name="request") RequestSubscriptionReq reqSub
    );

    @WebMethod
    public ApproveOrRejectSubscriptionResp approveOrRejectSubscription(
            @WebParam(name="request") ApproveOrRejectSubscriptionReq appOrRej
    );

    @WebMethod
    public ListRequestSubscriptionResp listRequestSubscription(
            @WebParam(name="request") ListRequestSubscriptionReq listReqSub
    );

    @WebMethod
    public ValidateSubscriptionResp validateSubscription(
            @WebParam(name="request") ValidateSubscriptionReq valSub
    );
}
