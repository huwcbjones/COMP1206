package server;

import com.djdch.log4j.StaticShutdownCallbackRegistry;
import nl.jteam.tls.StrongTls;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import server.events.LoginListener;
import server.events.ServerListener;
import server.events.ServerPacketListener;
import server.exceptions.OperationFailureException;
import server.objects.User;
import server.tasks.PacketHandler;
import shared.Packet;
import shared.exceptions.ConfigLoadException;
import shared.utils.RunnableAdapter;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.swing.event.EventListenerList;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Auction Server Daemon
 *
 * @author Huw Jones
 * @since 24/03/2016
 */
public final class Server {

    private static final Logger log = LogManager.getLogger(Server.class);

    /**
     * Static instance of Config
     */
    private static final Config config = new Config();

    /**
     * Static instance of data persistence layer
     */
    private static final DataPersistence data = new DataPersistence();
    private static final LoginHandler userLoginHandler = new LoginHandler();
    /**
     * Static instance of this
     */
    private static Server server;
    /**
     * Server WorkerPool for running tasks
     */
    private static WorkerPool workPool;

    static {
        System.setProperty("log4j.shutdownCallbackRegistry", "com.djdch.log4j.StaticShutdownCallbackRegistry");
    }

    /**
     * Client HashMap
     */
    private final HashMap<Long, ClientConnection> clients;
    //region Event Management
    private final EventListenerList listeners = new EventListenerList();
    private final PacketTaskHandler packetTaskHandler = new PacketTaskHandler();
    private AtomicLong clientConnectionCounter = new AtomicLong(0);
    private ServerListenThread plainSocket;
    //endregion
    private ServerListenThread secureSocket;
    private boolean isRunning = false;

    public Server() {
        clients = new HashMap<>();
        Server.server = this;
        Runtime.getRuntime().addShutdownHook(new shutdownThread());
    }

    public void setConfigFile(String file) {
        config.setConfigFile(file);
    }

    public void setDataDirectory(String dir) {
        config.setDataStore(dir);
    }

    public void testConfig() {
        try {
            config.loadConfig();
        } catch (ConfigLoadException e) {
            System.err.println("Failed to load config.");
            System.err.println(e.getMessage());
        }
        System.out.print(config.getConfig());
    }

    //region Starting Server

    /**
     * Runs the server.
     */
    public void run() {
        this.run(false);
    }

    /**
     * Runs the server
     *
     * @param createDatabase Will create the database if it does not exist
     */
    public void run(boolean createDatabase) {
        new Thread(() -> {
            this.startServer(createDatabase);
        }, "ServerStart").start();
    }

    private void startServer(boolean createDatabase) {
        this.fireServerStarting();
        log.info("Starting server...");

        this.loadConfig();
        try {
            this.loadData(createDatabase);
        } catch (OperationFailureException e) {
            log.fatal(e.getMessage());
            this.fireServerShutdown();
            return;
        }
        this.startWorkers();

        try {
            this.createSockets();
        } catch (IOException | OperationFailureException e) {
            log.error("Failed to create socket: {}", e.getMessage());
            this.fireServerShutdown();
            return;
        }

        this.plainSocket.start();
        if (Server.config.isSecureConnectionEnabled()) {
            this.secureSocket.start();
        }
        this.isRunning = true;
        this.fireServerStarted();
    }

    /**
     * Loads the server config
     */
    private void loadConfig() {
        log.info("Loading config...");

        try {
            config.loadConfig();
        } catch (ConfigLoadException e) {
            log.error("Failed to load config. Quitting!");
            System.exit(1);
        }
    }

    /**
     * Loads the data layer
     *
     * @param createDatabase Will create the database if it doesn't exist
     * @throws OperationFailureException Thrown if there was an error loading data
     */
    private void loadData(boolean createDatabase) throws OperationFailureException {
        Server.data.loadData(createDatabase);
    }

    /**
     * Starts the Worker Pool
     */
    private void startWorkers() {
        log.info("Starting workers...");
        Server.workPool = new WorkerPool(Server.config.getWorkers());
    }

    /**
     * Creates the server sockets to listen to connections
     *
     * @throws IOException               On Socket Error
     * @throws OperationFailureException On Server Error
     */
    private void createSockets() throws IOException, OperationFailureException {

        log.info("Starting plain socket...");
        // Create plain ServerSocket (no encryption)

        ServerSocket plainSocket = new ServerSocket(Server.config.getPlainPort());
        this.plainSocket = new ServerListenThread(this, plainSocket);
        log.info("Plain socket started successfully!");

        // Check the config to see if we are listening on a secure socket
        if (!Server.config.isSecureConnectionEnabled()) return;

        log.info("Starting secure socket...");

        // Set the JVM SSL Keystore
        File keyStore = Server.config.getKeyStore();
        if (keyStore == null) {
            throw new OperationFailureException("Could not find server keystore.");
        }
        log.debug("Using key-store: {}", keyStore.getAbsolutePath());
        System.setProperty("javax.net.ssl.keyStore", keyStore.getAbsolutePath());
        System.setProperty("javax.net.ssl.keyStorePassword", "fkZC17Az8f6Cuqd1bgnimMnAnhwiEm0GCly4T1sB8zmV2iCrxUyuCI1JcFznokQ98T4LS3e8ZoX6DUi7");

        try {
            // Create SLLServerSocket
            SSLServerSocketFactory sslSocketFactory = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
            SSLServerSocket sslSocket = (SSLServerSocket) sslSocketFactory.createServerSocket(Server.config.getSecurePort());
            sslSocket.setUseClientMode(false);

            // Enable strong protocols
            sslSocket.setEnabledProtocols(StrongTls.intersection(sslSocket.getSupportedProtocols(), StrongTls.ENABLED_PROTOCOLS));
            log.debug("Enabled Protocols: ");
            for (String protocol : sslSocket.getEnabledProtocols()) {
                log.debug("\t- {}", protocol);
            }

            // Enable stong cipher suites
            sslSocket.setEnabledCipherSuites(StrongTls.intersection(sslSocket.getSupportedCipherSuites(), StrongTls.ENABLED_CIPHER_SUITES));
            log.debug("Enabled Cipher Suites: ");
            for (String cipherSuite : sslSocket.getEnabledCipherSuites()) {
                log.debug("\t- {}", cipherSuite);
            }

            // Start Listen Thread
            this.secureSocket = new SecureServerListenThread(this, sslSocket);
        } catch (SocketException e) {
            log.debug(e);
            throw new OperationFailureException("Possible key-store location problem?");
        }
        log.info("Secure socket started successfully!");
    }

    //endregion
    //region Shutting Server Down
    public void shutdownServer() {
        new shutdownThread().start();
    }

    private void doShutdown() {
        if (!this.isRunning) return;

        log.info("Server shutting down...");
        this.fireServerShuttingDown();


        // Only send disconnects if there are clients connected
        if (this.clients.size() != 0) {
            log.info("Sending disconnect to clients...");
            ArrayList<ClientConnection> clients = new ArrayList<>(this.clients.values());
            for (ClientConnection client : clients) {
                client.closeConnection();
            }
        }

        if (this.plainSocket != null || this.secureSocket != null) {
            log.info("Closing sockets...");
            this.plainSocket.shutdown();

            // Only shutdown secureSocket if it is enabled
            if (config.isSecureConnectionEnabled() && this.secureSocket != null) {
                this.secureSocket.shutdown();
            }


        }

        if (Server.workPool != null) {
            Server.workPool.shutdown();
        }

        log.info("Saving data...");
        this.saveState();

        log.info("Server safely shut down!");
        StaticShutdownCallbackRegistry.invoke();
        this.isRunning = false;
        this.fireServerShutdown();
    }

    public void saveState() {

    }
    //endregion

    void addClient(ClientConnection clientConnection) {
        this.clients.put(clientConnection.getClientID(), clientConnection);
        clientConnection.addServerPacketListener(this.packetTaskHandler);
    }

    void removeClient(ClientConnection clientConnection) {
        this.clients.remove(clientConnection.getClientID());
        clientConnection.removeServerPacketListener(this.packetTaskHandler);
    }

    //region Get Methods

    /**
     * Gets the next ClientID
     *
     * @return Next ClientID
     */
    long getNextClientID() {
        return this.clientConnectionCounter.getAndIncrement();
    }

    /**
     * Fires server starting event
     */
    private void fireServerStarting() {
        Server.dispatchEvent(new RunnableAdapter() {
            @Override
            public void runSafe() {
                Object[] listeners = Server.this.listeners.getListenerList();
                for (int i = listeners.length - 2; i >= 0; i -= 2) {
                    if (listeners[i] == ServerListener.class) {
                        ((ServerListener) listeners[i + 1]).serverStarting();
                    }
                }
            }
        });
    }

    /**
     * Fires server started event
     */
    private void fireServerStarted() {
        Server.dispatchEvent(new RunnableAdapter() {
            @Override
            public void runSafe() {
                Object[] listeners = Server.this.listeners.getListenerList();
                for (int i = listeners.length - 2; i >= 0; i -= 2) {
                    if (listeners[i] == ServerListener.class) {
                        ((ServerListener) listeners[i + 1]).serverStarted();
                    }
                }
            }
        });
    }

    /**
     * Fires server shutting down event
     */
    private void fireServerShuttingDown() {
        Server.dispatchEvent(new RunnableAdapter() {
            @Override
            public void runSafe() {
                Object[] listeners = Server.this.listeners.getListenerList();
                for (int i = listeners.length - 2; i >= 0; i -= 2) {
                    if (listeners[i] == ServerListener.class) {
                        ((ServerListener) listeners[i + 1]).serverShuttingDown();
                    }
                }
            }
        });
    }

    /**
     * Fires server shutdown event
     */
    private void fireServerShutdown() {
        Server.dispatchEvent(new RunnableAdapter() {
            @Override
            public void runSafe() {
                Object[] listeners = Server.this.listeners.getListenerList();
                for (int i = listeners.length - 2; i >= 0; i -= 2) {
                    if (listeners[i] == ServerListener.class) {
                        ((ServerListener) listeners[i + 1]).serverShutdown();
                    }
                }
            }
        });
    }

    /**
     * Gets the Server instance from a static context
     *
     * @return Server instance
     */
    public static Server getServer() {
        return Server.server;
    }
    //endregion

    //region Event Handling and Dispatching

    /**
     * Gets the config instance from a static context
     *
     * @return Config instance
     */
    public static Config getConfig() {
        return Server.config;
    }

    /**
     * Gets the DataPersistence instance from a static context
     *
     * @return DataPersistence instance
     */
    public static DataPersistence getData() {
        return Server.data;
    }

    /**
     * Gets the WorkerPool instance from a static context
     *
     * @return WorkerPoll instance
     */
    public static WorkerPool getWorkerPool() {
        return Server.workPool;
    }

    public static LoginHandler getLoginEventHandler() {
        return Server.userLoginHandler;
    }

    /**
     * Dispatches an event handler in the EDT (any worker pool thread).
     * Or, if the server isn't running, just run the event handler in the current thread.
     *
     * @param event Event to dispatch
     */
    public static void dispatchEvent(RunnableAdapter event) {
        if (Server.getServer().isRunning && Server.workPool.isRunning()) {
            Server.workPool.dispatchEvent(event);
        } else {
            event.run();
        }
    }

    /**
     * Adds a Login listener to this server instance
     *
     * @param listener LoginListener
     */
    public static void addLoginListener(LoginListener listener) {
        Server.getServer().listeners.add(LoginListener.class, listener);
    }

    /**
     * Removes a Login listener to this server instance
     *
     * @param listener LoginListener
     */
    public static void removeLoginListener(LoginListener listener) {
        Server.getServer().listeners.remove(LoginListener.class, listener);
    }

    /**
     * Adds a Server listener to this server instance
     *
     * @param listener ServerListener
     */
    public static void addServerListener(ServerListener listener) {
        Server.getServer().listeners.add(ServerListener.class, listener);
    }

    /**
     * Removes a Server listener to this server instance
     *
     * @param listener ServerListener
     */
    public static void removeServerListener(ServerListener listener) {
        Server.getServer().listeners.remove(ServerListener.class, listener);
    }

    //endregion

    //region Event Handler Classes

    /**
     * Handles Users logging in and out
     */
    private static class LoginHandler implements LoginListener {

        /**
         * Occurs when a user logs in
         *
         * @param user User that logged in
         */
        @Override
        public void userLoggedIn(User user) {
            Server.dispatchEvent(new RunnableAdapter() {
                @Override
                public void runSafe() {
                    Object[] listeners = Server.getServer().listeners.getListenerList();
                    for (int i = listeners.length - 2; i >= 0; i -= 2) {
                        if (listeners[i] == LoginListener.class) {
                            ((LoginListener) listeners[i + 1]).userLoggedIn(user);
                        }
                    }
                }

            });
        }

        /**
         * Occurs when a user logs out
         *
         * @param user User that logged in
         */
        @Override
        public void userLoggedOut(User user, ClientConnection clientID) {
            log.info("User ({}) logged out on Client({})", user.getUniqueID(), clientID.getClientID());
            Server.dispatchEvent(new RunnableAdapter() {
                @Override
                public void runSafe() {
                    Object[] listeners = Server.getServer().listeners.getListenerList();
                    for (int i = listeners.length - 2; i >= 0; i -= 2) {
                        if (listeners[i] == LoginListener.class) {
                            ((LoginListener) listeners[i + 1]).userLoggedOut(user, clientID);
                        }
                    }
                }
            });
        }
    }

    /**
     * Runs the server shutdown in its own thread
     */
    private class shutdownThread extends Thread {
        public shutdownThread() {
            super("ServerShutdown");
        }

        @Override
        public void run() {
            doShutdown();
        }
    }

    /**
     * Handles Server Packets
     * Queues PacketHandler to run in worker pool
     */
    private class PacketTaskHandler implements ServerPacketListener {

        @Override
        public void packetReceived(ClientConnection client, Packet packet) {
            Server.workPool.queueTask(new PacketHandler(client, packet));
        }
    }

    //endregion
}
