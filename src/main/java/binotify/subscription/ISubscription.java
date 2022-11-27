package binotify.subscription;

import binotify.request.ApproveOrRejectSubscriptionReq;
import binotify.request.ListRequestSubscriptionReq;
import binotify.request.RequestSubscriptionReq;
import binotify.response.ApproveOrRejectSubscriptionResp;
import binotify.response.ListRequestSubscriptionResp;
import binotify.response.RequestSubscriptionResp;
import jakarta.jws.WebMethod;
import jakarta.jws.WebParam;
import jakarta.jws.WebService;
import jakarta.jws.soap.SOAPBinding;

@WebService
@SOAPBinding(style = SOAPBinding.Style.DOCUMENT)
public interface ISubscription {
    @WebMethod
    public RequestSubscriptionResp requestSubscription(@WebParam(name="request") RequestSubscriptionReq reqSub);

    @WebMethod
    public ApproveOrRejectSubscriptionResp approveOrRejectSubscription(@WebParam(name="request") ApproveOrRejectSubscriptionReq appOrRej);

    @WebMethod
    public ListRequestSubscriptionResp listRequestSubscription(@WebParam(name="request") ListRequestSubscriptionReq listReqSub);
}
