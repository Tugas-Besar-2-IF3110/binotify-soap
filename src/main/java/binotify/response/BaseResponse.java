package binotify.response;

public class BaseResponse {
    public boolean success;
    public String message;

    public BaseResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
}
