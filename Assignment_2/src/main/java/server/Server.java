package server;

import server.exceptions.ConfigLoadException;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import javax.net.ssl.SSLServerSocketFactory;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.HashMap;

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

    private void loadData () {
        log.info("Loading data...");
    }

    private void createSockets () throws IOException {
        ServerSocket plainSocket = new ServerSocket(this.config.getPlainPort());
        this.plainSocket = new ServerListenThread(this, plainSocket);
        log.info("Plain socket started successfully!");

        // Check the config to see if we are listening on a secure socket
        if (!this.config.isSecureConnectionEnabled()) return;

        SSLServerSocketFactory sslSocketFactory = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
        ServerSocket sslSocket = sslSocketFactory.createServerSocket(this.config.getSecurePort());
        this.secureSocket = new SecureServerListenThread(this, sslSocket);
        log.info("Secure socket started successfully!");
    }

    private void shutdownServer () {
        log.info("Server shutting down...");

        log.info("Closing sockets...");
        if (this.plainSocket != null) {
            this.plainSocket.shutdown();
        }
        if (config.isSecureConnectionEnabled() && this.secureSocket != null) {
            this.secureSocket.shutdown();
        }

        log.info("Sending disconnect to clients...");
        this.clients.values().forEach(ClientConnection::closeConnection);

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
    }

    private class shutdownThread extends Thread {
        public shutdownThread () {
            super("ServerShutdown");
        }

        @Override
        public void run () {
            shutdownServer();
        }
    }
}