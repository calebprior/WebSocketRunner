package WebSocketRunner;

import java.util.Map;

/**
 * Created by Caleb Prior on 24-Oct-16.
 */
public interface MessageRunner {

    void handle(Map<String, String> message, WebSocketRunner runner);

}
