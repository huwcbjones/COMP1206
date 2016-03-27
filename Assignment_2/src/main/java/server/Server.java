package server;

import server.exceptions.ConfigLoadException;
import shared.utils.Log;

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
        Log.Information("Starting server...");
        this.loadConfig();
        this.loadData();
        try {
            this.createSockets();
        } catch (IOException e) {
            Log.Fatal("Failed to create socket: " + e.getMessage());
        }

        this.plainSocket.start();
        if (this.config.isSecureConnectionEnabled()) {
            this.secureSocket.start();
        }
    }

    public void testConfig () {
        this.loadConfig();
    }

    private void loadConfig () {
        Log.Information("Loading config...");

        try {
            config.loadConfig();
        } catch (ConfigLoadException e) {
            Log.Fatal("Failed to load config. Quitting!");
        }
    }

    private void loadData () {
        Log.Information("Loading data...");
    }

    private void createSockets () throws IOException {
        ServerSocket plainSocket = new ServerSocket(this.config.getPlainPort());
        this.plainSocket = new ServerListenThread(this, plainSocket);
        Log.Information("Unencrypted socket started successfully!");

        // Check the config to see if we are listening on a secure socket
        if (!this.config.isSecureConnectionEnabled()) return;

        SSLServerSocketFactory sslSocketFactory = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
        ServerSocket sslSocket = sslSocketFactory.createServerSocket(this.config.getSecurePort());
        this.secureSocket = new ServerListenThread(this, sslSocket);
    }

    protected long getNextClientID () {
        return this.clientConnectionCounter++;
    }

    protected void addClient (ClientConnection clientConnection) {
        this.clients.put(clientConnection.getClientID(), clientConnection);
    }
}
