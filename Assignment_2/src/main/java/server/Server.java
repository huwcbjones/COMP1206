package server;

import nl.jteam.tls.StrongTls;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import server.events.PacketHandler;
import server.events.ServerPacketListener;
import server.exceptions.ConfigLoadException;
import shared.Packet;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Auction Server Daemon
 *
 * @author Huw Jones
 * @since 24/03/2016
 */
public final class Server {

    private static final Logger log = LogManager.getLogger(Server.class);

    private boolean shouldQuit = false;

    private long clientConnectionCounter = 0;

    private ServerListenThread plainSocket;
    private ServerListenThread secureSocket;
    private static final Config config = new Config();
    private static Server server;

    private final PacketTaskHandler packetTaskHandler = new PacketTaskHandler();

    private HashMap<Long, ClientConnection> clients;
    private ExecutorService workPool;

    public Server () {
        clients = new HashMap<>();
        Server.server = this;
    }

    public void setConfigFile (String file) {
        config.setConfigFile(file);
    }

    public void setDataDirectory (String dir) {
        config.setDataDirectory(dir);
    }

    public static Config getConfig() {
        return Server.config;
    }

    public void testConfig () {
        try {
            config.loadConfig();
        } catch (ConfigLoadException e) {
            System.err.println("Failed to load config.");
            System.err.println(e);
        }
        System.out.print(config.getConfig());
    }

    private void loadConfig () {
        log.info("Loading config...");

        try {
            config.loadConfig();
        } catch (ConfigLoadException e) {
            log.error("Failed to load config. Quitting!");
            System.exit(1);
        }
    }

    public void run () {
        Runtime.getRuntime().addShutdownHook(new shutdownThread());

        log.info("Starting server...");
        this.loadConfig();
        this.loadData();
        this.startWorkers();

        try {
            this.createSockets();
        } catch (IOException e) {
            log.error("Failed to create socket: {}", e.getMessage());
            return;
        }

        this.plainSocket.start();
        if (Server.config.isSecureConnectionEnabled()) {
            this.secureSocket.start();
        }
    }

    private void loadData () {
        log.info("Loading data...");
    }

    private void startWorkers () {
        log.info("Starting workers...");
        log.debug("New worker pool: " + Server.config.getWorkers());
        this.workPool = Executors.newFixedThreadPool(Server.config.getWorkers());
    }

    private void shutdownServer () {
        log.info("Server shutting down...");

        if (this.plainSocket != null || this.secureSocket != null) {
            log.info("Closing sockets...");
            this.plainSocket.shutdown();

            // Only shutdown secureSocket if it is enabled
            if (config.isSecureConnectionEnabled() && this.secureSocket != null) {
                this.secureSocket.shutdown();
            }

            // Only send disconnects if there are clients connected
            if (this.clients.size() != 0) {
                log.info("Sending disconnect to clients...");
                this.clients.values().forEach(ClientConnection::closeConnection);
            }
        }

        log.info("Saving data...");
        this.saveState();

        log.info("Server safely shut down!");
    }

    public void saveState () {

    }

    protected long getNextClientID () {
        return this.clientConnectionCounter++;
    }

    private void createSockets () throws IOException {
        log.info("Starting plain socket...");
        ServerSocket plainSocket = new ServerSocket(Server.config.getPlainPort());
        this.plainSocket = new ServerListenThread(this, plainSocket);
        log.info("Plain socket started successfully!");

        // Check the config to see if we are listening on a secure socket
        if (!Server.config.isSecureConnectionEnabled()) return;

        log.info("Starting secure socket...");
        System.setProperty("javax.net.ssl.keyStore", "keys/auction");
        System.setProperty("javax.net.ssl.keyStorePassword", "fkZC17Az8f6Cuqd1bgnimMnAnhwiEm0GCly4T1sB8zmV2iCrxUyuCI1JcFznokQ98T4LS3e8ZoX6DUi7");

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
        log.info("Secure socket started successfully!");
    }

    public static Server getServer() {
        return Server.server;
    }

    void addClient(ClientConnection clientConnection) {
        this.clients.put(clientConnection.getClientID(), clientConnection);
        clientConnection.addServerPacketListener(this.packetTaskHandler);
    }

    /**
     * Runs the server shutdown in its own thread
     */
    private class shutdownThread extends Thread {
        public shutdownThread () {
            super("ServerShutdown");
        }

        @Override
        public void run () {
            shutdownServer();
        }
    }

    void removeClient(ClientConnection clientConnection) {
        this.clients.remove(clientConnection.getClientID());
        clientConnection.removeServerPacketListener(this.packetTaskHandler);
    }

    private class PacketTaskHandler implements ServerPacketListener {

        @Override
        public void packetReceived(ClientConnection client, Packet packet) {
            Server.this.workPool.submit(new PacketHandler(client, packet));
        }
    }
}
