package client;

import client.events.LoginEventListener;
import client.utils.Server;
import client.windows.Login;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import server.exceptions.ConfigLoadException;
import shared.Comms;
import shared.Packet;
import shared.PacketType;
import shared.User;
import shared.events.PacketListener;
import shared.exceptions.ConnectionFailedException;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Auction Client
 *
 * @author Huw Jones
 * @since 24/03/2016
 */
public final class Client {

    private static Logger log = LogManager.getLogger(Client.class);
    private static final Config config = new Config();
    private static User user;
    private static boolean isConnected = false;
    private static boolean isLoggingIn = false;
    private static boolean cancelLogin = false;

    private static ArrayList<LoginEventListener> loginEventListeners = new ArrayList<>();

    private static Comms comms;

    private static final Object waitForReply = new Object();
    private static boolean replyTimedOut = true;

    public Client () {
        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown, "ShutdownThread"));
        try {
            Client.config.loadConfig();
        } catch (ConfigLoadException e) {
            log.warn(e.getMessage());
        }
        Login loginWindow = new Login();

        loginWindow.setVisible(true);
    }

    public static Config getConfig () {
        return Client.config;
    }

    private void shutdown () {
        log.info("Shutting down client...");
        if (Client.isConnected) {
            Client.comms.shutdown();
        }
    }

    public static void login(String username, char[] password) {
        try {
            if (!Client.isConnected) {
                Client.connect();
            }
            if (Client.cancelLogin) {
                Client.cancelLogin = false;
                return;
            }
            Client.comms.sendMessage(new Packet<>(PacketType.LOGIN, new char[][]{username.toCharArray(), password}));
            Client.waitForReply(5  * 1000);
            if(Client.replyTimedOut){
                fireLoginFailHandler("Request timed out.");
            }
        } catch (ConnectionFailedException e) {
            log.error("Failed to connect to server! {}", e.getMessage());
        }
    }

    public static void connect () throws ConnectionFailedException {
        Server server = Client.config.getSelectedServer();
        log.info("Connecting to {} ({} on {})...", server.getName(), server.getAddress(), server.getPort());
        try {
            Socket plainSocket = new Socket(server.getAddress(), server.getPort());
            ObjectOutputStream outputStream = new ObjectOutputStream(plainSocket.getOutputStream());
            ObjectInputStream inputStream = new ObjectInputStream(plainSocket.getInputStream());

            Client.comms = new Comms(plainSocket, inputStream, outputStream);
            Client.comms.start();
            replyHandler helloHandler = new replyHandler();
            Client.comms.addMessageListener(helloHandler);

            log.info("Saying HELLO to the server...");
            Client.comms.sendMessage(new Packet<>(PacketType.HELLO, "hello"));
            Client.waitForReply(5 * 1000);
            Client.comms.removeMessageListener(helloHandler);
            if (Client.replyTimedOut) {
                Client.comms.shutdown();
                throw new ConnectionFailedException("Handshake was unsuccessful.");
            }

            Client.comms.sendMessage(new Packet<>(PacketType.VERSION, Config.VERSION));

        } catch (IOException e) {
            log.debug(e);
            throw new ConnectionFailedException(e.getMessage());
        }
    }

    /**
     * Adds a login listener to the Client
     *
     * @param listener Listener to add
     */
    public static void addLoginListener (LoginEventListener listener) {
        Client.loginEventListeners.add(listener);
    }

    /**
     * Removes a login listener to the Client
     *
     * @param listener Listener to remove
     */
    public static void removeLoginListener (LoginEventListener listener) {
        Client.loginEventListeners.remove(listener);
    }

    public static void cancelLogin () {
        if (Client.isLoggingIn) {
            Client.cancelLogin = true;
        }
    }

    private static void fireLoginFailHandler (String message) {
        // This gets around concurrent modification exceptions if the listener removes itself when being called
        ArrayList<LoginEventListener> listeners = new ArrayList<>();
        listeners.addAll(Client.loginEventListeners);
        for (LoginEventListener l : listeners) {
            l.loginError(message);
        }
    }

    private static void fireLoginSuccessHandler (User user) {
        // This gets around concurrent modification exceptions if the listener removes itself when being called
        ArrayList<LoginEventListener> listeners = new ArrayList<>();
        listeners.addAll(Client.loginEventListeners);
        for (LoginEventListener l : listeners) {
            l.loginSuccess(user);
        }
    }

    /**
     * Causes the thread this method is called in to wait, until the timeout occurs, or we are notified of a message
     *
     * @param timeout How long to wait before timing out
     */
    private static void waitForReply(int timeout) {
        synchronized (Client.waitForReply) {
            try {
                // Change reply timed out to true, if we received a reply, the handling code should change this to false
                Client.replyTimedOut = true;

                log.info("Waiting for server reply...");
                Client.waitForReply.wait(timeout);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Unblocks a waitForReply
     */
    private static void replyReceived() {
        // Change reply timed out to false as we didn't time out
        Client.replyTimedOut = false;
        synchronized (Client.waitForReply) {
            Client.waitForReply.notify();
        }
    }

    private static class replyHandler implements PacketListener {
        @Override
        public void packetReceived (Packet packet) {
            switch (packet.getType()) {
                case HELLO:
                    log.info("Server says: {}", packet.getPayload());
                    Client.replyReceived();
                    break;
                case LOGIN_SUCCESS:
                    Client.fireLoginSuccessHandler((User) packet.getPayload());
                    Client.replyReceived();
                    break;
                case LOGIN_FAIL:
                    Client.fireLoginFailHandler((String)packet.getPayload());
                    Client.replyReceived();
                    break;
            }
        }
    }
}
