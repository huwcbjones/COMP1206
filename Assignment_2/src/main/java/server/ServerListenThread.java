package server;

import shared.exceptions.ConnectionFailedException;
import shared.utils.Log;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * Listens for connections on a server socket
 *
 * @author Huw Jones
 * @since 27/03/2016
 */
public final class ServerListenThread extends Thread {

    private final Server server;
    private final ServerSocket socket;
    private boolean shouldQuit = false;

    public ServerListenThread(Server server, ServerSocket socket){
        super("ServerSocketListener_" + socket.getLocalPort());
        this.server = server;
        this.socket = socket;
    }

    @Override
    public void run(){
        ClientConnection client;
        while (!this.shouldQuit) {
            try {
                long clientID = this.server.getNextClientID();
                client = new ClientConnection(this.socket.accept(), clientID);

                this.server.addClient(client);

                client.start();
            } catch (ConnectionFailedException | IOException e) {
                Log.Error(e.getMessage());
            }
        }
    }
}
