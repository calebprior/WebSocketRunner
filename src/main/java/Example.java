import WebSocketRunner.WebSocketRunner;
import org.json.JSONObject;

import java.util.*;

/**
 * Created by Caleb Prior on 24-Oct-16.
 */
public class Example {
    private static final String ENDPOINT_URL = "ws://endpoint/url";
    private static final int PORT = 80;


    public static void main(String[] args) throws Exception {
        Thread test = getTest();
        test.start();

        while (test.isAlive()) {
            Thread.sleep(100);
        }
    }

    private static Thread getTest() {
        return new Thread(() -> {
            try {
                new WebSocketRunner(ENDPOINT_URL, PORT)
                        .setHeaders(getHeaders())
                        .setMessageHandlers(Arrays.asList(
                                (message, runner) -> {
                                    System.out.println(message);
                                },
                                (message, runner) -> {
                                    System.out.println(message);

                                    JSONObject req = new JSONObject();
                                    req.put("test", "message");
                                    runner.send(req);
                                },
                                (message, runner) -> {
                                    System.out.println(message);
                                }
                        ))
                        .setExpectedCloseCode(1000)
                        .run()
                        .assertOk();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private static Map<String, List<String>> getHeaders(){
        Map<String, List<String>> headers = new HashMap<>();
        headers.put("test-header", Collections.singletonList("header"));
        return headers;
    }
}
