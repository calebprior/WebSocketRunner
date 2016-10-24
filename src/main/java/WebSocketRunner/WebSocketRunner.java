package WebSocketRunner;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

/**
 * Created by Caleb Prior on 24-Oct-16.
 */
public class WebSocketRunner implements MessageHandler, InitialMessageHandler, ErrorHandler, CloseHandler {

    private WebSocketEndPoint webSocketEndPoint;

    private int messageCount = 0;
    private List<MessageRunner> messageRunners = null;
    private JSONObject initialMessage;

    private boolean error = false;
    private ErrorHandler errorHandler;
    private boolean finished = false;
    private boolean expectedConnectionFailure = false;
    private Integer expectedCloseCode = null;

    public WebSocketRunner(String url, int port) throws URISyntaxException {
        this.webSocketEndPoint = new WebSocketEndPoint(url, port, this, this, this, this);
        this.messageCount = 0;
    }

    /**
     * Set the list of message runner
     *
     * @param messageRunners List of messageRunners
     * @return Self
     */
    public WebSocketRunner setMessageHandlers(List<MessageRunner> messageRunners) {
        this.messageRunners = messageRunners;
        return this;
    }

    /**
     * Set the headers for every connection
     *
     * @param headers Headers to use
     * @return Self
     */
    public WebSocketRunner setHeaders(Map<String, List<String>> headers) {
        this.webSocketEndPoint.setHeaders(headers);
        return this;
    }

    /**
     * Set the error handler
     *
     * @param handler Error handler
     * @return Self
     */
    public WebSocketRunner setErrorHandler(ErrorHandler handler){
        this.errorHandler = handler;
        return this;
    }

    /**
     * Set the initial message
     *
     * @param initialMessage The message
     * @return Self
     */
    public WebSocketRunner setInitialMessage(JSONObject initialMessage) {
        this.initialMessage = initialMessage;
        return this;
    }

    /**
     * Set the expected close code
     *
     * @param code Expected code
     * @return Self
     */
    public WebSocketRunner setExpectedCloseCode(int code) {
        this.expectedCloseCode = code;
        return this;
    }

    /**
     * Sets the expected connection failure condition
     *
     * @param expected Whether or not a connection failure is expected on connect
     * @return Self
     */
    public WebSocketRunner setExpectedConnectionFailure(boolean expected) {
        this.expectedConnectionFailure = expected;
        return this;
    }

    /**
     * Execute the query using the message handlers, and await expected close code
     *
     * @return Self
     * @throws Exception   If error occurs
     */
    public WebSocketRunner run() throws Exception {
        try {
            webSocketEndPoint.connect();
            waitTilFinished();
            return this;

        } catch (Exception e) {
            if (expectedConnectionFailure) {
                finished = true;
                error = false;
                return this;
            } else {
                finished = true;
                error = true;
                throw e;
            }
        }
    }

    /**
     * Waits until the endpoint is finished or an error occurred
     *
     * @throws Exception If error occurs
     */
    private void waitTilFinished() throws Exception {
        while (!webSocketEndPoint.isClosed() && !error && !finished) {
            Thread.sleep(100);
        }

        if(!webSocketEndPoint.isClosed()){
            webSocketEndPoint.close(1000);
        }
    }

    /**
     * Send a message to the endpoint, with a userAgent field
     *
     * @param message Message to send
     */
    public void send(JSONObject message) {
        webSocketEndPoint.send(message);
    }

    /**
     * Close the underlying socket
     *
     * @param closeCode Code to close on
     */
    public void close(int closeCode) {
        try {
            setExpectedCloseCode(closeCode);
            webSocketEndPoint.close(closeCode);
        } catch (Exception e) {
            e.printStackTrace();
            error = true;
        }
    }

    @Override
    public void handleMessage(String message) throws Exception {
        try {
            if (messageRunners.size() > messageCount && messageRunners.get(messageCount) != null) {
                // Parse message into String, String map for easy processing
                Gson gson = new Gson();
                Type type = new TypeToken<Map<String, String>>() {}.getType();
                Map<String, String> messageMap = gson.fromJson(message, type);

                messageRunners.get(messageCount).handle(messageMap, this);
            } else {
                error = true;
                finished = true;
            }

            messageCount++;

        } catch (Exception e) {
            error = true;
            throw e;
        }
    }

    @Override
    public void handleClose(int code) throws Exception {
        if (code != expectedCloseCode) {
            error = true;
        } else {
            finished = true;
        }

        if(code != expectedCloseCode) {
            throw new Exception("Expected " + expectedCloseCode + " but got " + code);
        }
    }

    @Override
    public void handleInitialMessage() throws Exception {
        if (initialMessage != null) {
            send(initialMessage);
        }
    }

    @Override
    public void handleError(Throwable throwable) {
        if(errorHandler != null) {
            errorHandler.handleError(throwable);
        }
    }

    /**
     * Assert that no errors occurred
     */
    public void assertOk() throws Exception {
        if(error) {
            throw new Exception("An error occurred");
        }
    }
}
