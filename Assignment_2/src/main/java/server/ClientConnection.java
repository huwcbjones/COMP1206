package server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import server.events.ServerPacketListener;
import server.utils.Comms;
import server.utils.ConnectHandler;
import shared.Packet;
import shared.events.ConnectionAdapter;
import shared.events.PacketListener;
import shared.exceptions.ConnectionFailedException;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Handles a client on the server
 *
 * @author Huw Jones
 * @since 27/03/2016
 */
public final class ClientConnection extends ConnectionAdapter implements PacketListener {

    private static final Logger log = LogManager.getLogger(ClientConnection.class);

    private ArrayList<ServerPacketListener> serverPacketListeners;

    private final long clientID;
    private final Socket socket;
    private final Comms comms;
    private final boolean isSecureConnection;
    private boolean isConnected = false;

    public ClientConnection(long clientID, Socket socket) throws ConnectionFailedException {
        this(clientID, socket, false);
    }

    public ClientConnection(long clientID, Socket socket, boolean isSecureConnection) throws ConnectionFailedException {
        this.clientID = clientID;
        this.socket = socket;
        this.isSecureConnection = isSecureConnection;
        this.serverPacketListeners = new ArrayList<>();

        log.info("New {} client connection #{}, from {}:{}", (isSecureConnection?"secure":""), this.clientID, this.socket.getInetAddress(), this.socket.getPort());

        try {
            ObjectOutputStream outputStream = new ObjectOutputStream(this.socket.getOutputStream());
            ObjectInputStream inputStream = new ObjectInputStream(this.socket.getInputStream());

            this.comms = new Comms(
                    clientID,
                    socket,
                    inputStream,
                    outputStream
            );
            this.comms.addConnectionListener(this);
            this.comms.start();
        } catch (IOException e) {
            log.debug(e);
            throw new ConnectionFailedException("Could not create client connection stream. " + e.getMessage());
        }
    }

    public void connect() throws ConnectionFailedException {
        ConnectHandler connectHandler = new ConnectHandler(this);
        connectHandler.connect();
        log.info("Client #{} successfully connected!", this.clientID);
        this.comms.addMessageListener(this);
    }

    public void closeConnection () {
        log.info("Closing client connection...");
        try {
            socket.close();
        } catch (IOException e) {
            log.debug(e);
        }
    }

    /**
     * Gets the client ID for this client
     *
     * @return Client ID
     */
    public long getClientID () {
        return this.clientID;
    }

    /**
     * Returns whether or not this client connection is secure
     *
     * @return True if client connection is secure
     */
    public boolean isSecureConnection() {
        return this.isSecureConnection;
    }

    //region Event Handling
    /**
     * Handle packet received from Comms class.
     * Fire ServerPacketReceived so action can be carried out in server worker thread.
     * Fire PacketReceived so that action can be carried out internally (e.g.: ConnectHandler)
     *
     * @param packet Packet that was received from client
     */
    @Override
    public void packetReceived (Packet packet) {
        ArrayList<ServerPacketListener> serverPacketListeners = new ArrayList<>(this.serverPacketListeners);
        for (ServerPacketListener l : serverPacketListeners) {
            l.packetReceived(this, packet);
        }
    }

    /**
     * Adds a PacketListener to this client
     *
     * @param listener Listener to add
     */
    public void addPacketListener(PacketListener listener) {
        this.comms.addMessageListener(listener);
    }

    /**
     * Removes a PacketListener from this client
     *
     * @param listener Listener to remove
     */
    public void removePacketListener(PacketListener listener) {
        this.comms.removeMessageListener(listener);
    }

    /**
     * Adds a ServerPacketListener to this client
     *
     * @param listener Listener to add
     */
    public void addServerPacketListener(ServerPacketListener listener) {
        this.serverPacketListeners.add(listener);
    }

    /**
     * Removes a ServerPacketListener from this client
     *
     * @param listener Listener to remove
     */
    public void removeServerPacketListener(ServerPacketListener listener) {
        this.serverPacketListeners.remove(listener);
    }

    /**
     * Sends a packet to the client
     * @param packet Packet to send
     */
    public void sendPacket(Packet packet){
        this.comms.sendMessage(packet);
    }

    /**
     * Fires when the connection is closed
     * Subclasses should override this method if they want to listen to this event.
     *
     * @param reason Reason why the connection is closed
     */
    @Override
    public void connectionClosed(String reason) {
        log.info("Client #{}, connection closed: {}", this.clientID, reason);
        Server.getServer().removeClient(this);
        this.closeConnection();
    }
    //endregion
}
