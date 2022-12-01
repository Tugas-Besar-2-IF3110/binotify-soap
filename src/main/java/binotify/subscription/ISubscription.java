package binotify.subscription;

import binotify.request.*;
import binotify.response.*;
import jakarta.jws.WebMethod;
import jakarta.jws.WebParam;
import jakarta.jws.WebService;
import jakarta.jws.soap.SOAPBinding;

@WebService
@SOAPBinding(style = SOAPBinding.Style.DOCUMENT)
public interface ISubscription {
    @WebMethod(action = "\"requestSubscription\"")
    public RequestSubscriptionResp requestSubscription(
            @WebParam(name="request") RequestSubscriptionReq reqSub
    );

    @WebMethod(action = "\"approveOrRejectSubscription\"")
    public ApproveOrRejectSubscriptionResp approveOrRejectSubscription(
            @WebParam(name="request") ApproveOrRejectSubscriptionReq appOrRej
    );

    @WebMethod(action = "\"listRequestSubscription\"")
    public ListRequestSubscriptionResp listRequestSubscription(
            @WebParam(name="request") ListRequestSubscriptionReq listReqSub
    );

    @WebMethod(action = "\"validateSubscription\"")
    public ValidateSubscriptionResp validateSubscription(
            @WebParam(name="request") ValidateSubscriptionReq valSub
    );

    @WebMethod(action = "\"checkStatusRequest\"")
    public CheckStatusRequestResp checkStatusRequest(
            @WebParam(name="request") CheckStatusRequestReq csr
    );
}
