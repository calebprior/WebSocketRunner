package WebSocketRunner;

import org.json.JSONObject;

import javax.websocket.DeploymentException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

/**
 * Created by Caleb Prior on 24-Oct-16.
 */
public class WebSocketEndPoint {

    private String url;
    private int port;
    private WebSocketClient client;
    private boolean closed = true;

    private MessageHandler messageHandler;
    private CloseHandler closeHandler;
    private ErrorHandler errorHandler;
    private InitialMessageHandler initialMessageHandler;
    private Map<String, List<String>> headers;

    WebSocketEndPoint(String url, int port,  MessageHandler messageHandler, CloseHandler closeHandler, InitialMessageHandler initialMessageHandler, ErrorHandler errorHandler){
        this.url = url;
        this.port = port;
        this.messageHandler = messageHandler;
        this.closeHandler = closeHandler;
        this.errorHandler = errorHandler;
        this.initialMessageHandler = initialMessageHandler;
    }

    /**
     * Set the headers to use
     *
     * @param headers Headers to use
     */
    public void setHeaders(Map<String, List<String>> headers) {
        this.headers = headers;
    }

    /**
     * Initiate a connection to a webSocket endpoint using a client
     *
     * @throws URISyntaxException If the URI could not be found
     */
    void connect() throws URISyntaxException, IOException, DeploymentException {
        createWebSocketClient();
        setupHandlers();
        setupHeaders();
        client.connect();
        closed = false;
    }

    /**
     * Creates the webSocket client
     *
     * @throws URISyntaxException If endpoint could not be found
     */
    private void createWebSocketClient() throws URISyntaxException {
        URI uriNewPort = this.setUriPort(new URI(url), port);
        this.client = new WebSocketClient(uriNewPort);
    }

    /**
     * Set the port for the uri
     *
     * @param uri  Uri to set
     * @param port Port to use
     * @return The new URI
     */
    private URI setUriPort(URI uri, int port) {
        try {
            return new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(), port, uri.getPath(), uri.getQuery(), uri.getFragment());
        } catch (URISyntaxException e) {
            return uri;
        }
    }

    /**
     * Setup the handlers for the client
     */
    private void setupHandlers() {
        this.client.addInitialMessageHandler(initialMessageHandler);
        this.client.addMessageHandler(this::baseOnMessage);

        this.client.addCloseHandler(code -> {
            this.closed = true;
            closeHandler.handleClose(code);
        });

        this.client.addErrorHandler(throwable -> {
            this.closed = true;
            errorHandler.handleError(throwable);
        });
    }

    /**
     * Setup the headers before use
     */
    private void setupHeaders() {
        if(headers != null) {
            client.setHeaders(headers);
        }
    }

    /**
     * Handles the incoming messages
     *
     * @param message Incoming message
     * @throws Exception If processing failed
     */
    private void baseOnMessage(String message) throws Exception {
        try {
            messageHandler.handleMessage(message);
        } catch (Exception e) {
            closed = true;
            throw e;
        }
    }

    /**
     * Send a message through the client, with the userAgent field
     *
     * @param message Message to send
     */
    void send(JSONObject message) {
        client.sendMessage(message.toString());
    }

    /**
     * Returns whether the client is finished and closed
     *
     * @return Whether the client is finished
     */
    boolean isClosed() {
        return closed || client.isClosed();
    }

    /**
     * Close to client with the expected code
     *
     * @param closeCode Close Code
     */
    public void close(int closeCode) throws IOException {
        try {
            client.close(closeCode);
        } catch (Exception e) {
            closed = true;
            throw e;
        }
    }
}
