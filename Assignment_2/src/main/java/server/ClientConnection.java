package server;

import shared.Comms;
import shared.exceptions.ConnectionFailedException;
import shared.utils.Log;

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
public final class ClientConnection extends Thread {

    private final long clientID;
    private final Socket socket;
    private final Comms comms;
    private boolean isConnected = false;

    public ClientConnection(Socket socket, long clientID) throws ConnectionFailedException {
        this.clientID = clientID;
        this.socket = socket;

        Log.Information("New connection from " + this.socket.getInetAddress() + ":" + this.socket.getPort());

        try {
            this.comms = new Comms(
                    clientID,
                    new ObjectInputStream(this.socket.getInputStream()),
                    new ObjectOutputStream(this.socket.getOutputStream())
            );
        } catch (IOException e) {
            throw new ConnectionFailedException("Could not create client connection stream. " + e.getMessage());
        }
    }

    public long getClientID(){
        return this.clientID;
    }
}
