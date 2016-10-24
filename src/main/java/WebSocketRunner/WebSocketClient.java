package WebSocketRunner;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Caleb Prior on 24-Oct-16.
 */
@ClientEndpoint(configurator = WebSocketClient.class)
public class WebSocketClient extends ClientEndpointConfig.Configurator {

    private Session session = null;
    private URI endpointURI;

    private MessageHandler messageHandler;
    private CloseHandler closeHandler;
    private ErrorHandler errorHandler;
    private InitialMessageHandler initialMessageHandler;

    private static Map<String, List<String>> headers = new HashMap<>();

    public WebSocketClient() {
        //empty default constructor
    }

    WebSocketClient(URI endpointURI) {
        this.endpointURI = endpointURI;
    }

    /**
     * Connect to the endpoint
     */
    public void connect() throws IOException, DeploymentException {
        try {
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            container.connectToServer(this, endpointURI);
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public void beforeRequest(Map<String, List<String>> headers) {
        headers.putAll(this.headers);
    }

    /**
     * Callback hook for Connection open events.
     *
     * @param session the userSession which is opened.
     */
    @OnOpen
    public void onOpen(Session session) throws Exception {
        this.session = session;
        if (initialMessageHandler != null) {
            initialMessageHandler.handleInitialMessage();
        }
    }

    /**
     * Callback hook for Connection close events.
     *
     * @param session - the userSession which is getting closed.
     * @param reason  - the reason for connection close
     */
    @OnClose
    public void onClose(Session session, CloseReason reason) throws Exception{
        if (this.closeHandler != null) {
            this.closeHandler.handleClose(reason.getCloseCode().getCode());
        }
        this.session = null;
    }

    /**
     * Callback hook for Connection Error events
     *
     * @param session - the session the error occurred on
     * @param thr - the error that got thrown
     */
    @OnError
    public void onError(Session session, Throwable thr) {
        if (errorHandler != null){
            errorHandler.handleError(thr);
        }
        this.session = null;
    }

    /**
     * Callback hook for Message Events. This method will be invoked when a client send a message.
     *
     * @param message - The text message
     */
    @OnMessage
    public void onMessage(String message) throws Exception {
        if (this.messageHandler != null) {
            this.messageHandler.handleMessage(message);
        }
    }

    /**
     * register message handler
     *
     * @param msgHandler - handles websocket message event
     */
    void addMessageHandler(MessageHandler msgHandler) {
        this.messageHandler = msgHandler;
    }

    /**
     * Register a close handler
     *
     * @param closeHandler - handles websocket close event
     */
    void addCloseHandler(CloseHandler closeHandler) {
        this.closeHandler = closeHandler;
    }

    /**
     * Add an initialMessage Handler
     *
     * @param handler Message handler
     */
    void addInitialMessageHandler(InitialMessageHandler handler) {
        this.initialMessageHandler = handler;
    }

    /**
     * Add an error handler
     *
     * @param handler Error Handler
     */
    void addErrorHandler(ErrorHandler handler) {
        this.errorHandler = handler;
    }

    /**
     * Set the headers to send with every request
     *
     * @param headers Headers to use
     */
    void setHeaders(Map<String, List<String>> headers) {
        this.headers = headers;
    }

    /**
     * Send a message.
     *
     * @param message - string to be sent
     */
    void sendMessage(String message) {
        this.session.getAsyncRemote().sendText(message);
    }

    /**
     * Returns whether the session is closed
     *
     * @return whether the session is closed
     */
    boolean isClosed() {
        return session == null || !session.isOpen();
    }

    /**
     * Close the socket
     *
     * @param closeCode CloseCode
     */
    public void close(int closeCode) throws IOException {
        session.close(new CloseReason(() -> closeCode, ""));
    }
}

