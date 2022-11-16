package binotify.security;

import jakarta.jws.WebMethod;
import jakarta.jws.WebService;
import jakarta.jws.soap.SOAPBinding;
import jakarta.jws.soap.SOAPBinding.Style;

@WebService
@SOAPBinding(style = Style.DOCUMENT)
public class Security {
    @WebMethod
    public String addLogging(String description, String IP, String endpoint) {return "";}
}
