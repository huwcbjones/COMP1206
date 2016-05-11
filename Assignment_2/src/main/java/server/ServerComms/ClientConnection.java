package server.ServerComms;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import server.Server;
import server.events.ServerPacketListener;
import server.objects.User;
import shared.Packet;
import shared.events.ConnectionAdapter;
import shared.events.ConnectionListener;
import shared.events.PacketListener;
import shared.exceptions.ConnectionFailedException;

import javax.swing.event.EventListenerList;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * Handles a client on the server, wrapper class for Comms
 *
 * @author Huw Jones
 * @since 27/03/2016
 */
public final class ClientConnection extends ConnectionAdapter implements PacketListener {

    private static final Logger log = LogManager.getLogger(ClientConnection.class);

    private final EventListenerList listenerList = new EventListenerList();

    private final long clientID;
    private final Comms comms;
    private final boolean isSecureConnection;
    private boolean isConnected = false;
    private User user = null;

    public ClientConnection(long clientID, Socket socket) throws ConnectionFailedException {
        this(clientID, socket, false);
    }

    public ClientConnection(long clientID, Socket socket, boolean isSecureConnection) throws ConnectionFailedException {
        this.clientID = clientID;
        this.isSecureConnection = isSecureConnection;

        log.info("New {}client connection, from {}:{}", (isSecureConnection ? "secure " : ""), this.clientID, socket.getInetAddress(), socket.getPort());

        try {
            ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());

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
        log.info("Client({}) successfully connected!", this.clientID);
        this.isConnected = true;
        this.comms.addMessageListener(this);
        this.comms.sendMessage(Packet.Ping());
    }

    /**
     * Gets the client ID for this client
     *
     * @return Client ID
     */
    public long getClientID() {
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

    /**
     * Returns whether or not this client is connected
     *
     * @return True if client is connected
     */
    public boolean isConnected() {
        return this.isConnected;
    }

    /**
     * Gets whether a user is logged in or not.
     *
     * @return Returns true is user is logged in
     */
    public boolean isUserLoggedIn() {
        return this.user != null;
    }

    /**
     * Gets the user currently logged in on this connection, or null if no user is logged in
     *
     * @return User, or null if no user.
     */
    public User getUser() {
        return this.user;
    }

    /**
     * Sets the user logged in on this connection
     *
     * @param user User
     */
    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return this.clientID + " (" + this.comms.getConnectionDetails() + ")";
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
    public void packetReceived(Packet packet) {
        Object[] listeners = this.listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == ServerPacketListener.class) {
                ((ServerPacketListener) listeners[i + 1]).packetReceived(this, packet);
            }
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
        this.listenerList.add(ServerPacketListener.class, listener);
    }

    /**
     * Removes a ServerPacketListener from this client
     *
     * @param listener Listener to remove
     */
    public void removeServerPacketListener(ServerPacketListener listener) {
        this.listenerList.add(ServerPacketListener.class, listener);
    }

    /**
     * Adds a ConnectionListener to this client
     *
     * @param listener Listener to add
     */
    public void addConnectionListener(ConnectionListener listener) {
        this.comms.addConnectionListener(listener);
    }

    /**
     * Removes a ConnectionListener from this client
     *
     * @param listener Listener to remove
     */
    public void removeConnectionListener(ConnectionListener listener) {
        this.comms.addConnectionListener(listener);
    }

    /**
     * Sends a packet to the client
     *
     * @param packet Packet to send
     */
    public void sendPacket(Packet packet) {
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
        log.info("Client({}), connection closed: {}", this.clientID, reason);
        this.isConnected = false;
        Server.getServer().removeClient(this);
        this.closeConnection();
    }

    public void closeConnection() {
        log.info("Disconnecting Client({}).", this.clientID);
        this.comms.removeConnectionListener(this);
        this.comms.shutdown();
    }
    //endregion
}
