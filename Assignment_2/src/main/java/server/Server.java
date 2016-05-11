package server;

import com.djdch.log4j.StaticShutdownCallbackRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import server.ServerComms.ClientConnection;
import server.ServerComms.SecureServerComms;
import server.ServerComms.ServerComms;
import server.events.AuctionListener;
import server.events.LoginListener;
import server.events.ServerListener;
import server.events.ServerPacketListener;
import server.exceptions.OperationFailureException;
import server.objects.User;
import server.tasks.PacketHandler;
import shared.Packet;
import shared.PacketType;
import shared.exceptions.ConfigLoadException;
import shared.utils.RunnableAdapter;

import javax.swing.event.EventListenerList;
import java.util.HashMap;
import java.util.UUID;
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
    private static DataPersistence data;
    private static final LoginHandler userLoginHandler = new LoginHandler();
    private static final AuctionHandler auctionHandler = new AuctionHandler();

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
    private ServerComms plainComms;
    //endregion
    private ServerComms secureComms;
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

        try {
            this.loadConfig();
        } catch (ConfigLoadException e) {
            log.catching(e);
            log.fatal(e.getMessage());
            this.fireServerFailedStart(e.getMessage());
            return;
        }

        this.startWorkers();

        try {
            Server.data = new DataPersistence();
            this.loadData(createDatabase);
        } catch (OperationFailureException e) {
            log.catching(e);
            log.fatal(e.getMessage());
            this.fireServerFailedStart(e.getMessage());
            return;
        }

        try {
            this.createSockets();
        } catch (OperationFailureException e) {
            log.catching(e);
            log.fatal("Failed to create socket: {}", e.getMessage());
            this.fireServerFailedStart(e.getMessage());
            return;
        }

        this.plainComms.start();
        if (Server.config.isSecureConnectionEnabled()) {
            this.secureComms.start();
        }
        this.isRunning = true;
        this.fireServerStarted();
    }

    /**
     * Loads the server config
     */
    private void loadConfig() throws ConfigLoadException {
        log.info("Loading config...");
        config.loadConfig();
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
     * @throws OperationFailureException On Server Error
     */
    private void createSockets() throws OperationFailureException {

        log.info("Starting plain socket...");
        // Create plain ServerSocket (no encryption)

        this.plainComms = new ServerComms(this, Server.config.getPlainPort());
        log.info("Plain socket started successfully!");

        // Check the config to see if we are listening on a secure socket
        if (!Server.config.isSecureConnectionEnabled()) return;

        log.info("Starting secure socket...");

        this.secureComms = new SecureServerComms(this, Server.config.getSecurePort());

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
        log.info("Sending disconnect to clients...");
        this.broadcastPacket(new Packet<>(PacketType.DISCONNECT, "Server shutting down."));

        if (this.plainComms != null || this.secureComms != null) {
            log.info("Closing sockets...");
            this.plainComms.shutdown();

            // Only shutdown secureComms if it is enabled
            if (config.isSecureConnectionEnabled() && this.secureComms != null) {
                this.secureComms.shutdown();
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
        Server.getData().saveData();
    }
    //endregion

    public void addClient(ClientConnection clientConnection) {
        this.clients.put(clientConnection.getClientID(), clientConnection);
        clientConnection.addServerPacketListener(this.packetTaskHandler);
    }

    public void removeClient(ClientConnection clientConnection) {
        this.clients.remove(clientConnection.getClientID());
        clientConnection.removeServerPacketListener(this.packetTaskHandler);
    }

    /**
     * Broadcasts a Packet to all client connections.
     *
     * @param packet Packet to broadcast
     */
    public void broadcastPacket(Packet packet) {
        this.broadcastPacket(packet, false);
    }

    /**
     * Broadcasts a Packet to all client connections.
     *
     * @param packet       Packet to broadcast
     * @param loggedInOnly If true, packet will only be broadcast to logged in users
     */
    public void broadcastPacket(Packet packet, boolean loggedInOnly) {
        // Stream the client connections, filter logged in clients, then send the packet to each of them
        this.clients.values().stream().
            // Filter not(loggedInOnly) OR Client.isLoggedIn
                filter(client -> !loggedInOnly || client.isUserLoggedIn()).
            forEach(client -> client.sendPacket(packet));
    }

    /**
     * Gets the next ClientID
     *
     * @return Next ClientID
     */
    public long getNextClientID() {
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
     * Fires server failed to start event
     */
    private void fireServerFailedStart(String reason) {
        Server.dispatchEvent(new RunnableAdapter() {
            @Override
            public void runSafe() {
                Object[] listeners = Server.this.listeners.getListenerList();
                for (int i = listeners.length - 2; i >= 0; i -= 2) {
                    if (listeners[i] == ServerListener.class) {
                        ((ServerListener) listeners[i + 1]).serverStartFail(reason);
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

    /**
     * Gets the Server's Login Handler
     *
     * @return Login handler
     */
    public static LoginHandler getLoginEventHandler() {
        return Server.userLoginHandler;
    }

    /**
     * Gets the Server's Auction Handler
     *
     * @return Auction handler
     */
    public static AuctionHandler getAuctionEventHandler() {
        return Server.auctionHandler;
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
     * Adds an Auction listener to this server instance
     *
     * @param listener AuctionListener
     */
    public static void addAuctionListener(AuctionListener listener) {
        Server.getServer().listeners.add(AuctionListener.class, listener);
    }

    /**
     * Removes a Auction listener to this server instance
     *
     * @param listener AuctionListener
     */
    public static void removeAuctionListener(AuctionListener listener) {
        Server.getServer().listeners.remove(AuctionListener.class, listener);
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
     * Handles Auction Events
     */
    private static class AuctionHandler implements AuctionListener {

        /**
         * Fired when an auction starts
         *
         * @param itemID ID of Item
         */
        @Override
        public void auctionStart(UUID itemID) {
            log.info("Auction started for Item({})", itemID);
            Server.dispatchEvent(new RunnableAdapter() {
                @Override
                public void runSafe() {
                    Object[] listeners = Server.getServer().listeners.getListenerList();
                    for (int i = listeners.length - 2; i >= 0; i -= 2) {
                        if (listeners[i] == AuctionListener.class) {
                            ((AuctionListener) listeners[i + 1]).auctionStart(itemID);
                        }
                    }
                }
            });
        }

        /**
         * Fired when an auction ends
         *
         * @param itemID ID of Item
         */
        @Override
        public void auctionEnd(UUID itemID, boolean wasWon) {
            log.info("Auction ended for Item({})", itemID);
            Server.dispatchEvent(new RunnableAdapter() {
                @Override
                public void runSafe() {
                    Object[] listeners = Server.getServer().listeners.getListenerList();
                    for (int i = listeners.length - 2; i >= 0; i -= 2) {
                        if (listeners[i] == AuctionListener.class) {
                            ((AuctionListener) listeners[i + 1]).auctionEnd(itemID, wasWon);
                        }
                    }
                }
            });
        }

        /**
         * Fired when a bid is placed on an auction
         *
         * @param itemID ID of Item
         * @param bidID  ID of Bid
         */
        @Override
        public void auctionBid(UUID itemID, UUID bidID) {
            log.info("New Bid({}) on Item({})", bidID, itemID);
            Server.dispatchEvent(new RunnableAdapter() {
                @Override
                public void runSafe() {
                    Object[] listeners = Server.getServer().listeners.getListenerList();
                    for (int i = listeners.length - 2; i >= 0; i -= 2) {
                        if (listeners[i] == AuctionListener.class) {
                            ((AuctionListener) listeners[i + 1]).auctionBid(itemID, bidID);
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
