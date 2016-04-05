package client;

import client.events.LoginListener;
import client.utils.ConnectHandler;
import client.utils.NotificationWaiter;
import client.utils.Server;
import client.windows.Login;
import nl.jteam.tls.StrongTls;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import server.exceptions.ConfigLoadException;
import shared.Comms;
import shared.Packet;
import shared.PacketType;
import shared.User;
import shared.events.ConnectionListener;
import shared.events.PacketListener;
import shared.exceptions.ConnectionFailedException;
import shared.exceptions.ConnectionUpgradeException;
import shared.utils.ReplyWaiter;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;

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

    private static ArrayList<LoginListener> loginListeners = new ArrayList<>();
    private static ArrayList<ConnectionListener> connectionListeners = new ArrayList<>();

    private static Comms comms;

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

    /**
     * Connects to the server
     */
    public static void connect() {
        Server server = Client.config.getSelectedServer();
        log.info("Connecting to {} ({} on {})...", server.getName(), server.getAddress(), server.getPort());
        try {
            // Create socket
            Socket plainSocket = new Socket(server.getAddress(), server.getPort());
            ObjectOutputStream outputStream = new ObjectOutputStream(plainSocket.getOutputStream());
            ObjectInputStream inputStream = new ObjectInputStream(plainSocket.getInputStream());

            // Create comms class
            Client.comms = new Comms(plainSocket, inputStream, outputStream);
            Client.comms.start();

            ConnectHandler connectHandler = new ConnectHandler();
            try {
                connectHandler.connect();
            } catch (ConnectionUpgradeException e) {
                // Terminate plain socket
                Client.comms.shutdown();
                server = Client.config.getSelectedServer();

                // Create SSLSocket
                SSLSocketFactory sf = ((SSLSocketFactory) SSLSocketFactory.getDefault());
                SSLSocket secureSocket = (SSLSocket)sf.createSocket(server.getAddress(), server.getPort());
                secureSocket.setUseClientMode(true);
                secureSocket.setEnabledProtocols(StrongTls.intersection(secureSocket.getSupportedProtocols(), StrongTls.ENABLED_PROTOCOLS));
                log.debug("Enabled Protocols: ");
                for (String protocol : secureSocket.getEnabledProtocols()) {
                    log.debug("\t- {}", protocol);
                }
                secureSocket.setEnabledCipherSuites(StrongTls.intersection(secureSocket.getSupportedCipherSuites(), StrongTls.ENABLED_CIPHER_SUITES));
                log.debug("Enabled Cipher Suites: ");
                for (String cipherSuite : secureSocket.getEnabledCipherSuites()) {
                    log.debug("\t- {}", cipherSuite);
                }
                secureSocket.startHandshake();
                outputStream = new ObjectOutputStream(secureSocket.getOutputStream());
                inputStream = new ObjectInputStream(secureSocket.getInputStream());

                // Create comms class
                Client.comms = new Comms(secureSocket, inputStream, outputStream);
                Client.comms.start();
            }
            Client.isConnected = true;

        } catch (ConnectionFailedException | IOException e) {
            log.info("Connection failed. {}", e.getMessage());
            log.debug(e);
            fireConnectionFailed(e.getMessage());
        }
    }

    /**
     * Log the client into the server
     *
     * @param username username for client
     * @param password password for client
     */
    public static void login(String username, char[] password) {
        if (!Client.isConnected) {
            Client.connect();
        }
        if (Client.cancelLogin || !Client.isConnected) {
            Client.cancelLogin = false;
            return;
        }
        NotificationWaiter waiter = new NotificationWaiter();

        // Create an anonymous waiter to wait for servers reply to the login message
        ReplyWaiter handler = new ReplyWaiter(waiter) {
            @Override
            public void packetReceived(Packet packet) {
                switch (packet.getType()) {
                    case LOGIN_SUCCESS:
                        // Set the client's user details
                        Client.user = (User) packet.getPayload();
                        Client.fireLoginSuccessHandler((User) packet.getPayload());
                        break;
                    case LOGIN_FAIL:
                        Client.fireLoginFailHandler((String) packet.getPayload());
                        break;
                }
                waiter.replyReceived();
            }
        };

        Client.comms.addMessageListener(handler);
        Client.comms.sendMessage(new Packet<>(PacketType.LOGIN, new char[][]{username.toCharArray(), password}));
        Arrays.fill(password, '\u0000'); // Clear password

        waiter.waitForReply(5 * 1000);
        if (waiter.isReplyTimedOut()) {
            // Need to handle timeouts because the fail event only fires if we get a LOGIN_FAIL from the server
            fireLoginFailHandler("Request timed out.");
        }
    }

    //region Send/Receive Packets

    /**
     * Sends a packet
     * @param packet
     */
    public static void sendPacket(Packet packet){
        Client.comms.sendMessage(packet);
    }
    //endregion

    //region Event Trigger Methods

    /**
     * Fires LoginSuccess Event
     *
     * @param user User details of user that logged in
     */
    private static void fireLoginSuccessHandler(User user) {
        // This gets around concurrent modification exceptions if the listener removes itself when being called
        ArrayList<LoginListener> listeners = new ArrayList<>();
        listeners.addAll(Client.loginListeners);
        for (LoginListener l : listeners) {
            l.loginSuccess(user);
        }
    }

    /**
     * Fires LoginFailed Event
     *
     * @param message Reason why login failed
     */
    private static void fireLoginFailHandler(String message) {
        // This gets around concurrent modification exceptions if the listener removes itself when being called
        ArrayList<LoginListener> listeners = new ArrayList<>();
        listeners.addAll(Client.loginListeners);
        for (LoginListener l : listeners) {
            l.loginError(message);
        }
    }

    /**
     * Fires ConnectionFailed Event
     *
     * @param reason Reason why connection failed
     */
    private static void fireConnectionFailed(String reason) {
        ArrayList<ConnectionListener> listeners = new ArrayList<>();
        listeners.addAll(Client.connectionListeners);
        for (ConnectionListener l : listeners) {
            l.connectionFailed(reason);
        }
    }

    public static void cancelLogin() {
        if (Client.isLoggingIn) {
            Client.cancelLogin = true;
        }
    }

    /**
     * Fires ConnectionClosed Event
     *
     * @param reason Reason why connection was closed
     */
    private static void fireConnectionClosed(String reason) {
        ArrayList<ConnectionListener> listeners = new ArrayList<>();
        listeners.addAll(Client.connectionListeners);
        for (ConnectionListener l : listeners) {
            l.connectionClosed(reason);
        }
    }

    /**
     * Fires ConnectionSucceeded Event
     */
    private static void fireConnectionSuccess() {
        ArrayList<ConnectionListener> listeners = new ArrayList<>();
        listeners.addAll(Client.connectionListeners);
        listeners.forEach(ConnectionListener::connectionSucceeded);
    }

    //endregion

    //region Add/Remove Event Handlers

    /**
     * Adds a packet listener to the communications handler
     *
     * @param listener Listener to add
     */
    public static void addPacketListener(PacketListener listener) {
        Client.comms.addMessageListener(listener);
    }

    /**
     * Removes a packet listener to the communications handler
     *
     * @param listener Listener to remove
     */
    public static void removePacketListener(PacketListener listener) {
        Client.comms.removeMessageListener(listener);
    }

    /**
     * Adds a login listener to the Client
     *
     * @param listener Listener to add
     */
    public static void addLoginListener(LoginListener listener) {
        Client.loginListeners.add(listener);
    }

    /**
     * Removes a login listener to the Client
     *
     * @param listener Listener to remove
     */
    public static void removeLoginListener(LoginListener listener) {
        Client.loginListeners.remove(listener);
    }

    /**
     * Adds a connection listener to the Client
     *
     * @param listener Listener to add
     */
    public static void addConnectionListener(ConnectionListener listener) {
        Client.connectionListeners.add(listener);
    }

    /**
     * Removes a connection listener to the Client
     *
     * @param listener Listener to remove
     */
    public static void removeConnectionListener(ConnectionListener listener) {
        Client.connectionListeners.remove(listener);
    }

    //endregion
}
