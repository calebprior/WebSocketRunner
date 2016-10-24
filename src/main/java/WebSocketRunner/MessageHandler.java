package WebSocketRunner;

/**
 * Created by Caleb Prior on 24-Oct-16.
 */
public interface MessageHandler {

    void handleMessage(String message) throws Exception;

}
