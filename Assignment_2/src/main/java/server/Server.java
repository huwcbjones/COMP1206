package server;

import com.djdch.log4j.StaticShutdownCallbackRegistry;
import nl.jteam.tls.StrongTls;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import server.events.LoginListener;
import server.events.ServerListener;
import server.events.ServerPacketListener;
import server.exceptions.OperationFailureException;
import server.tasks.PacketHandler;
import shared.Packet;
import shared.exceptions.ConfigLoadException;

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
    private static final Config config = new Config();
    private static final DataPersistence data = new DataPersistence();
    private static Server server;
    private static WorkerPool workPool;
    private final PacketTaskHandler packetTaskHandler = new PacketTaskHandler();
    private final HashMap<Long, ClientConnection> clients;
    private AtomicLong clientConnectionCounter = new AtomicLong(0);
    private ServerListenThread plainSocket;
    private ServerListenThread secureSocket;
    private final EventListenerList listeners = new EventListenerList();

    private boolean isRunning = false;

    static {
        System.setProperty("log4j.shutdownCallbackRegistry", "com.djdch.log4j.StaticShutdownCallbackRegistry");
    }

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

    public void run() {
        this.run(false);
    }

    public void run(boolean createDatabase) {
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

    private void loadConfig() {
        log.info("Loading config...");

        try {
            config.loadConfig();
        } catch (ConfigLoadException e) {
            log.error("Failed to load config. Quitting!");
            System.exit(1);
        }
    }

    private void loadData(boolean createDatabase) throws OperationFailureException {
        Server.data.loadData(createDatabase);
    }

    private void startWorkers() {
        log.info("Starting workers...");
        Server.workPool = new WorkerPool(Server.config.getWorkers());
    }

    private void createSockets() throws IOException, OperationFailureException {
        log.info("Starting plain socket...");
        ServerSocket plainSocket = new ServerSocket(Server.config.getPlainPort());
        this.plainSocket = new ServerListenThread(this, plainSocket);
        log.info("Plain socket started successfully!");

        // Check the config to see if we are listening on a secure socket
        if (!Server.config.isSecureConnectionEnabled()) return;

        log.info("Starting secure socket...");
        File keyStore = Server.config.getKeyStore();
        if (keyStore == null) {
            throw new OperationFailureException("Could not find server keystore.");
        }
        log.debug("Using key-store: {}", keyStore.getAbsolutePath());
        System.setProperty("javax.net.ssl.keyStore", keyStore.getAbsolutePath());
        System.setProperty("javax.net.ssl.keyStorePassword", "fkZC17Az8f6Cuqd1bgnimMnAnhwiEm0GCly4T1sB8zmV2iCrxUyuCI1JcFznokQ98T4LS3e8ZoX6DUi7");

        try {
            SSLServerSocketFactory sslSocketFactory = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
            SSLServerSocket sslSocket = (SSLServerSocket) sslSocketFactory.createServerSocket(Server.config.getSecurePort());
            sslSocket.setUseClientMode(false);
            sslSocket.setEnabledProtocols(StrongTls.intersection(sslSocket.getSupportedProtocols(), StrongTls.ENABLED_PROTOCOLS));
            log.debug("Enabled Protocols: ");
            for (String protocol : sslSocket.getEnabledProtocols()) {
                log.debug("\t- {}", protocol);
            }
            sslSocket.setEnabledCipherSuites(StrongTls.intersection(sslSocket.getSupportedCipherSuites(), StrongTls.ENABLED_CIPHER_SUITES));
            log.debug("Enabled Cipher Suites: ");
            for (String cipherSuite : sslSocket.getEnabledCipherSuites()) {
                log.debug("\t- {}", cipherSuite);
            }
            this.secureSocket = new SecureServerListenThread(this, sslSocket);
        } catch (SocketException e) {
            log.debug(e);
            throw new OperationFailureException("Possible key-store location problem?");
        }
        log.info("Secure socket started successfully!");
    }

    public void shutdownServer() {
        new shutdownThread().run();
    }

    private void doShutdown(){
        if(!this.isRunning) return;

        log.info("Server shutting down...");
        this.fireServerShuttingDown();


        // Only send disconnects if there are clients connected
        if (this.clients.size() != 0) {
            log.info("Sending disconnect to clients...");
            ArrayList<ClientConnection> clients = new ArrayList<>(this.clients.values());
            for(ClientConnection client : clients){
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

    protected long getNextClientID() {
        return this.clientConnectionCounter.getAndIncrement();
    }

    void addClient(ClientConnection clientConnection) {
        this.clients.put(clientConnection.getClientID(), clientConnection);
        clientConnection.addServerPacketListener(this.packetTaskHandler);
    }

    void removeClient(ClientConnection clientConnection) {
        this.clients.remove(clientConnection.getClientID());
        clientConnection.removeServerPacketListener(this.packetTaskHandler);
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
     * Dispatches an event handler in the EDT (any worker pool thread)
     *
     * @param event Event to dispatch
     */
    public static void dispatchEvent(Runnable event){
        if(server.isRunning) {
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
    public static void addLoginListener(LoginListener listener){
        Server.getServer().listeners.add(LoginListener.class, listener);
    }

    /**
     * Removes a Login listener to this server instance
     *
     * @param listener LoginListener
     */
    public static void removeLoginListener(LoginListener listener){
        Server.getServer().listeners.remove(LoginListener.class, listener);
    }

    /**
     * Adds a Server listener to this server instance
     *
     * @param listener ServerListener
     */
    public static void addServerListener(ServerListener listener){
        Server.getServer().listeners.add(ServerListener.class, listener);
    }

    /**
     * Removes a Server listener to this server instance
     *
     * @param listener ServerListener
     */
    public static void removeServerListener(ServerListener listener){
        Server.getServer().listeners.remove(ServerListener.class, listener);
    }

    private void fireServerStarting(){
        Server.dispatchEvent(() ->{
            Object[] listeners = this.listeners.getListenerList();
            for (int i = listeners.length - 2; i >= 0; i -= 2) {
                if (listeners[i] == ServerListener.class) {
                    ((ServerListener) listeners[i + 1]).serverStarting();
                }
            }
        });
    }

    private void fireServerStarted(){
        Server.dispatchEvent(() ->{
            Object[] listeners = this.listeners.getListenerList();
            for (int i = listeners.length - 2; i >= 0; i -= 2) {
                if (listeners[i] == ServerListener.class) {
                    ((ServerListener) listeners[i + 1]).serverStarted();
                }
            }
        });
    }

    private void fireServerShuttingDown(){
        Server.dispatchEvent(() ->{
            Object[] listeners = this.listeners.getListenerList();
            for (int i = listeners.length - 2; i >= 0; i -= 2) {
                if (listeners[i] == ServerListener.class) {
                    ((ServerListener) listeners[i + 1]).serverShuttingDown();
                }
            }
        });
    }

    private void fireServerShutdown(){
        Server.dispatchEvent(() ->{
            Object[] listeners = this.listeners.getListenerList();
            for (int i = listeners.length - 2; i >= 0; i -= 2) {
                if (listeners[i] == ServerListener.class) {
                    ((ServerListener) listeners[i + 1]).serverShutdown();
                }
            }
        });
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

    private class PacketTaskHandler implements ServerPacketListener {

        @Override
        public void packetReceived(ClientConnection client, Packet packet) {
            Server.workPool.queueTask(new PacketHandler(client, packet));
        }
    }
}
