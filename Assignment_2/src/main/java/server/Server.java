package server;

import server.events.PacketHandler;
import server.events.ServerPacketListener;
import server.exceptions.ConfigLoadException;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import shared.Packet;

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
    private Config config;

    private HashMap<Long, ClientConnection> clients;
    private ExecutorService workPool;

    public Server () {
        clients = new HashMap<>();
        config = new Config();
    }

    public void setConfigFile (String file) {
        config.setConfigFile(file);
    }

    public void setDataDirectory (String dir) {
        config.setDataDirectory(dir);
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
        if (this.config.isSecureConnectionEnabled()) {
            this.secureSocket.start();
        }
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

    private void startWorkers () {
        log.info("Starting workers...");
        log.debug("New fixed thread pool: " + this.config.getWorkers());
        this.workPool = Executors.newFixedThreadPool(this.config.getWorkers());
    }

    private void loadData () {
        log.info("Loading data...");
    }

    private void createSockets () throws IOException {
        ServerSocket plainSocket = new ServerSocket(this.config.getPlainPort());
        this.plainSocket = new ServerListenThread(this, plainSocket);
        log.info("Plain socket started successfully!");

        // Check the config to see if we are listening on a secure socket
        if (!this.config.isSecureConnectionEnabled()) return;

        //System.setProperty("javax.net.ssl.keyStore", "");
        //System.setProperty("javax.net.ssl.keyStore", "21234567£$%^&asdfgh");

        SSLServerSocketFactory sslSocketFactory = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
        ServerSocket sslSocket = sslSocketFactory.createServerSocket(this.config.getSecurePort());
        this.secureSocket = new SecureServerListenThread(this, sslSocket);
        log.info("Secure socket started successfully!");
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

    protected void addClient (ClientConnection clientConnection) {
        this.clients.put(clientConnection.getClientID(), clientConnection);
        clientConnection.addServerPacketListener(new packetTaskHandler());
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

    private class packetTaskHandler implements ServerPacketListener {

        @Override
        public void packetRecieved (ClientConnection client, Packet packet) {
            Server.this.workPool.submit(new PacketHandler(client, packet));
        }
    }
}
