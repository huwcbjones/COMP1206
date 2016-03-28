package server;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import server.events.ServerPacketListener;
import server.utils.Comms;
import shared.Packet;
import shared.PacketType;
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
public final class ClientConnection implements PacketListener {

    private static final Logger log = LogManager.getLogger(ClientConnection.class);

    private ArrayList<ServerPacketListener> packetListeners;

    private final long clientID;
    private final Socket socket;
    private final Comms comms;
    private boolean isConnected = false;

    public ClientConnection (long clientID, Socket socket) throws ConnectionFailedException {
        this.clientID = clientID;
        this.socket = socket;
        this.packetListeners = new ArrayList<>();

        log.info("New connection from {}:{}", this.socket.getInetAddress(), this.socket.getPort());

        try {
            ObjectOutputStream outputStream = new ObjectOutputStream(this.socket.getOutputStream());
            ObjectInputStream inputStream = new ObjectInputStream(this.socket.getInputStream());

            this.comms = new Comms(
                    clientID,
                    socket,
                    inputStream,
                    outputStream
            );
            this.comms.start();
        } catch (IOException e) {
            log.debug(e);
            throw new ConnectionFailedException("Could not create client connection stream. " + e.getMessage());
        }
    }

    public boolean connect(){
        Object waitForHello = new Object();
        this.comms.addMessageListener((packet -> {
            synchronized (waitForHello) {
                isConnected = true;
                waitForHello.notify();
            }
        }));
        synchronized (waitForHello){
            try {
                log.info("Waiting for HELLO...");
                waitForHello.wait(5 * 1000);
                log.info("HELLO Client #{}", this.clientID);
            } catch (InterruptedException e) {
                log.debug(e);
            }
        }
        if(!isConnected){
            return false;
        }
        this.comms.sendMessage(new Packet<>(PacketType.HELLO, "hello"));
        return true;
    }

    public void closeConnection () {
        try {
            socket.close();
        } catch (IOException e) {
            log.debug(e);
        }
    }

    public long getClientID () {
        return this.clientID;
    }

    @Override
    public void packetReceived (Packet packet) {
        firePacketReceivedEvent(packet);
    }

    public void addServerPacketListener(ServerPacketListener listener){
        this.packetListeners.add(listener);
    }

    public void removeServerPacketListener(ServerPacketListener listener){
        this.packetListeners.remove(listener);
    }

    private void firePacketReceivedEvent (Packet packet) {
        for (ServerPacketListener l : this.packetListeners) {
            l.packetRecieved(this, packet);
        }
    }

    /**
     * Sends a packet to the client
     * @param packet Packet to send
     */
    public void sendMessage(Packet packet){
        this.comms.sendMessage(packet);
    }
}
