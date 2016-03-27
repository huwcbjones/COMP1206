package server;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import shared.Comms;
import shared.exceptions.ConnectionFailedException;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * Handles a client on the server
 *
 * @author Huw Jones
 * @since 27/03/2016
 */
final class ClientConnection extends Thread {

    private static final Logger log = LogManager.getLogger(ClientConnection.class);

    private final long clientID;
    private final Socket socket;
    private final Comms comms;
    private boolean isConnected = false;

    public ClientConnection (long clientID, Socket socket) throws ConnectionFailedException {
        this.clientID = clientID;
        this.socket = socket;

        log.info("New connection from {}:{}", this.socket.getInetAddress(), this.socket.getPort());

        try {
            this.comms = new Comms(
                    clientID,
                    new ObjectInputStream(this.socket.getInputStream()),
                    new ObjectOutputStream(this.socket.getOutputStream())
            );
        } catch (IOException e) {
            log.debug(e);
            throw new ConnectionFailedException("Could not create client connection stream. " + e.getMessage());
        }
    }

    public void closeConnection () {
        try {
            socket.close();
        } catch (IOException e) {

        }
    }

    public long getClientID () {
        return this.clientID;
    }
}
