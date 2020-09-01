/*
    HTSP Message Library
    Author: Waldo Theron
    Version: 0.1
 */

public class ConnectionTest {
    private Thread connectionThread;

    public static void main(String... args)
    {
        ConnectionInfo connectionInfo = new ConnectionInfo("home.theron.co.nz", 9982, "shield", "shield", "IPTV CLIENT", "");
        HTSPSerializer htspSerializer = new HTSPSerializer();
        HTSPMessageHandler htspMessageHandler = new HTSPMessageHandler();
        SocketIOHandler socketIOHandler = new SocketIOHandler(htspSerializer, htspMessageHandler);

        AuthListener authListener = new AuthListener(htspMessageHandler, connectionInfo);
        Connection connection = new Connection(connectionInfo, socketIOHandler);
        InputHandler inputHandler = new InputHandler(htspMessageHandler);

        htspMessageHandler.setConnection(connection);
        htspMessageHandler.addMessageListener(authListener);

        Thread connectionThread = new Thread(connection);
        connectionThread.start();

        Thread inputThread = new Thread(inputHandler);


        HTSPMessage message = new HTSPMessage();

        message.put("method", "hello");
        message.put("htspversion", 23);
        message.put("clientname", connectionInfo.getClientName());
        message.put("clientversion", connectionInfo.getClientVersion());

        htspMessageHandler.sendMessage(message);

        inputThread.start();
    }
}
