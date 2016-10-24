package WebSocketRunner;

/**
 * Created by Caleb Prior on 24-Oct-16.
 */
public interface CloseHandler {

    void handleClose(int code) throws Exception;

}
