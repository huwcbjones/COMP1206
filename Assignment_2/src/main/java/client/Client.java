package client;

import client.events.LoginListener;
import client.events.RegisterListener;
import client.utils.ConnectHandler;
import client.utils.NotificationWaiter;
import client.utils.Server;
import client.windows.Login;
import nl.jteam.tls.StrongTls;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import shared.*;
import shared.exceptions.ConfigLoadException;
import shared.events.ConnectionAdapter;
import shared.events.ConnectionListener;
import shared.events.PacketListener;
import shared.exceptions.ConnectionFailedException;
import shared.exceptions.ConnectionUpgradeException;
import shared.utils.ReplyWaiter;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.swing.event.EventListenerList;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Auction Client
 *
 * @author Huw Jones
 * @since 24/03/2016
 */
public final class Client implements ConnectionListener {

    private static final Logger log = LogManager.getLogger(Client.class);
    private static final Config config = new Config();
    private static User user;
    private static boolean isConnected = false;

    private static final EventListenerList listenerList = new EventListenerList();

    private static final ArrayList<LoginListener> loginListeners = new ArrayList<>();
    private static final ArrayList<ConnectionListener> connectionListeners = new ArrayList<>();

    private static Comms comms;
    private static Client client;

    public Client () {
        Client.client = this;
        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown, "ShutdownThread"));
        try {
            Client.config.loadConfig();
        } catch (ConfigLoadException e) {
            log.warn(e.getMessage());
        }
        System.setProperty("javax.net.ssl.trustStore", "config/keys/biddr.jks");
        System.setProperty("javax.net.ssl.trustStorePassword", "fkZC17Az8f6Cuqd1bgnimMnAnhwiEm0GCly4T1sB8zmV2iCrxUyuCI1JcFznokQ98T4LS3e8ZoX6DUi7");

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

    public static void disconnect() {
        if (!Client.isConnected) return;
        Client.comms.shutdown();
        Client.comms = null;
        Client.isConnected = false;
    }

    private static void doConnect() {
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
                log.info("Switching to secure connection...");
                // Terminate plain socket
                Client.comms.shutdown();
                server = Client.config.getSelectedServer();

                // Create SSLSocket
                SSLSocketFactory sf = ((SSLSocketFactory) SSLSocketFactory.getDefault());
                SSLSocket secureSocket = (SSLSocket)sf.createSocket(server.getAddress(), server.getSecurePort());
                secureSocket.setUseClientMode(true);
                secureSocket.setEnabledProtocols(StrongTls.intersection(secureSocket.getSupportedProtocols(), StrongTls.ENABLED_PROTOCOLS));
                log.debug("Enabled Protocols: ");
                for (String protocol : secureSocket.getEnabledProtocols()) {
                    log.debug("\t- {}", protocol);
                }
                //secureSocket.startHandshake();
                outputStream = new ObjectOutputStream(secureSocket.getOutputStream());
                inputStream = new ObjectInputStream(secureSocket.getInputStream());

                // Create comms class
                Client.comms = new Comms(secureSocket, inputStream, outputStream);
                Client.comms.start();

                connectHandler.secureConnect();
            }
            Client.isConnected = true;
            Client.comms.addConnectionListener(Client.client);
            Client.comms.addMessageListener(Client.comms);
            Client.fireConnectionSucceeded();
            log.info("Connection successfully established!");

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
            ConnectionListener listener = new ConnectionAdapter() {
                @Override
                public void connectionSucceeded() {
                    Client.removeConnectionListener(this);
                    Client.login(username, password);
                }

                @Override
                public void connectionFailed(String reason) {
                    Client.removeConnectionListener(this);
                    Client.fireLoginFailHandler(reason);
                }
            };
            Client.addConnectionListener(listener);
            Client.connect();
        } else {
            new Thread(() -> {
                Client.doLogin(username, password);
            }, "LoginThread").start();
        }
    }

    private static void doLogin(String username, char[] password) {
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
                    case NOK:
                        Client.fireLoginFailHandler("Server failed to process request.");
                        break;
                    default:
                        return;
                }
                waiter.replyReceived();
            }
        };

        Client.comms.addMessageListener(handler);
        Client.comms.sendMessage(new Packet<>(PacketType.LOGIN, new char[][]{username.toCharArray(), password}));
        Arrays.fill(password, '\u0000'); // Clear password

        waiter.waitForReply();
        if (waiter.isReplyTimedOut()) {
            // Need to handle timeouts because the fail event only fires if we get a LOGIN_FAIL from the server
            fireLoginFailHandler("Request timed out.");
        }
    }

    public static void register(User user, char[] password, char[] passwordConfirm){
        if (!Client.isConnected) {
            ConnectionListener listener = new ConnectionAdapter() {
                @Override
                public void connectionSucceeded() {
                    Client.removeConnectionListener(this);
                    Client.register(user, password, passwordConfirm);
                }

                @Override
                public void connectionFailed(String reason) {
                    Client.removeConnectionListener(this);
                    Client.fireRegisterFailHandler(reason);
                }
            };
            Client.addConnectionListener(listener);
            Client.connect();
        } else {
            new Thread(() -> {
                Client.doRegister(user, password, passwordConfirm);
            }, "RegisterThread").start();
        }
    }

    private static void doRegister(User user, char[] password, char[] passwordConfirm){
        NotificationWaiter waiter = new NotificationWaiter();

        ReplyWaiter handler=  new ReplyWaiter(waiter) {
            @Override
            public void packetReceived(Packet packet) {
                switch (packet.getType()){
                    case REGISTER_SUCCESS:
                        Client.fireRegisterSuccessHandler((User) packet.getPayload());
                        break;
                    case REGISTER_FAIL:
                        Client.fireRegisterFailHandler((String)packet.getPayload());
                        break;
                    case NOK:
                        Client.fireRegisterFailHandler("Server failed to process request.");
                        break;
                    default:
                        return;
                }
                waiter.replyReceived();
            }
        };

        Client.comms.addMessageListener(handler);
        Client.comms.sendMessage(new Packet<>(PacketType.REGISTER, new RegisterUser(user, password, passwordConfirm)));
        Arrays.fill(password, '\u0000');
        Arrays.fill(passwordConfirm, '\u0000');

        waiter.waitForReply();
        if(waiter.isReplyTimedOut()){

        }
    }

    /**
     * Connects to the server
     */
    public static void connect() {
        new Thread(Client::doConnect, "ConnectionThread").start();
    }

    //region Get Methods
    public static boolean isConnected() {
        return Client.isConnected;
    }
    //endregion

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
     * Fires RegisterSuccess Event
     *
     * @param user User that has registered
     */
    private static void fireRegisterSuccessHandler(User user) {
        Object[] listeners = Client.listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == RegisterListener.class) {
                ((RegisterListener) listeners[i + 1]).registerSuccess(user);
            }
        }
    }

    /**
     * Fires RegisterFailed Event
     *
     * @param message Reason why register failed
     */
    private static void fireRegisterFailHandler(String message) {
        Object[] listeners = Client.listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == RegisterListener.class) {
                ((RegisterListener) listeners[i + 1]).registerFail(message);
            }
        }
    }

    /**
     * Fires LoginSuccess Event
     *
     * @param user User details of user that logged in
     */
    private static void fireLoginSuccessHandler(User user) {
        Object[] listeners = Client.listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == LoginListener.class) {
                ((LoginListener) listeners[i + 1]).loginSuccess(user);
            }
        }
    }

    /**
     * Fires LoginFailed Event
     *
     * @param message Reason why login failed
     */
    private static void fireLoginFailHandler(String message) {
        Object[] listeners = Client.listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == LoginListener.class) {
                ((LoginListener) listeners[i + 1]).loginFail(message);
            }
        }
    }

    /**
     * Fires ConnectionSucceeded Event
     */
    private static void fireConnectionSucceeded() {
        Object[] listeners = Client.listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == ConnectionListener.class) {
                ((ConnectionListener) listeners[i + 1]).connectionSucceeded();
            }
        }
    }

    /**
     * Fires ConnectionFailed Event
     *
     * @param reason Reason why connection failed
     */
    private static void fireConnectionFailed(String reason) {
        Object[] listeners = Client.listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == ConnectionListener.class) {
                ((ConnectionListener) listeners[i + 1]).connectionFailed(reason);
            }
        }
    }

    /**
     * Fires ConnectionClosed Event
     *
     * @param reason Reason why connection was closed
     */
    private static void fireConnectionClosed(String reason) {
        Object[] listeners = Client.listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == ConnectionListener.class) {
                ((ConnectionListener) listeners[i + 1]).connectionClosed(reason);
            }
        }
    }

    /**
     * Fires when the connection succeeds
     */
    @Override
    public void connectionSucceeded() {
        fireConnectionSucceeded();
    }

    /**
     * Fires when the connection fails
     *
     * @param reason Reason why connection failed
     */
    @Override
    public void connectionFailed(String reason) {
        Client.isConnected = false;
        fireConnectionFailed(reason);
    }

    /**
     * Fires when the connection is closed
     *
     * @param reason Reason why the connection is closed
     */
    @Override
    public void connectionClosed(String reason) {
        Client.isConnected = false;
        fireConnectionClosed(reason);
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
     * Adds a Register listener to the Client
     *
     * @param listener Listener to add
     */
    public static void addRegisterListener(RegisterListener listener) {
        Client.listenerList.add(RegisterListener.class, listener);
    }

    /**
     * Removes a Register listener to the Client
     *
     * @param listener Listener to remove
     */
    public static void removeRegisterListener(RegisterListener listener) {
        Client.listenerList.remove(RegisterListener.class, listener);
    }

    /**
     * Adds a login listener to the Client
     *
     * @param listener Listener to add
     */
    public static void addLoginListener(LoginListener listener) {
        Client.listenerList.add(LoginListener.class, listener);
    }

    /**
     * Removes a login listener to the Client
     *
     * @param listener Listener to remove
     */
    public static void removeLoginListener(LoginListener listener) {
        Client.listenerList.remove(LoginListener.class, listener);
    }

    /**
     * Adds a connection listener to the Client
     *
     * @param listener Listener to add
     */
    public static void addConnectionListener(ConnectionListener listener) {
        Client.listenerList.add(ConnectionListener.class, listener);
    }

    /**
     * Removes a connection listener to the Client
     *
     * @param listener Listener to remove
     */
    public static void removeConnectionListener(ConnectionListener listener) {
        Client.listenerList.remove(ConnectionListener.class, listener);
    }
    //endregion
}
