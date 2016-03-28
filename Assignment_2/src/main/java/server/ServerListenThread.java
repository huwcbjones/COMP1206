package server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import shared.exceptions.ConnectionFailedException;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Listens for connections on a server socket
 *
 * @author Huw Jones
 * @since 27/03/2016
 */
class ServerListenThread extends Thread {

    protected static final Logger log = LogManager.getLogger(ServerListenThread.class);

    private final Server server;
    private final ServerSocket socket;
    private boolean shouldQuit = false;

    public ServerListenThread (Server server, ServerSocket socket) {
        this(server, socket, "PlainServer");
    }

    protected ServerListenThread (Server server, ServerSocket socket, String name){
        super(name);
        this.server = server;
        this.socket = socket;
    }

    @Override
    public final void run () {
        log.info("Listening for connections on: {}", this.socket.getLocalPort());
        ClientConnection client;
        while (!this.shouldQuit) {
            try {
                long clientID = this.server.getNextClientID();
                client = connectClient(clientID, this.socket.accept());
                this.server.addClient(client);
                client.start();
            } catch (ConnectionFailedException | IOException e) {
                if (!e.getMessage().equals("socket closed")) {
                    log.error(e.getMessage());
                }
            }
        }
    }

    protected ClientConnection connectClient(long clientID, Socket socket) throws ConnectionFailedException {
        return new ClientConnection(clientID, socket);
    }

    public void shutdown () {
        this.shouldQuit = true;
        try {
            this.socket.close();
        } catch (IOException e) {
            log.debug(e);
        }
    }
}