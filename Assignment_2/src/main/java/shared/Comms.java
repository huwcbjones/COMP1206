package shared;

import client.Client;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import shared.events.ConnectionListener;
import shared.events.PacketListener;
import shared.exceptions.VersionMismatchException;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;

/**
 * Communications Thread
 *
 * @author Huw Jones
 * @since 26/03/2016
 */
public class Comms extends Thread {

    private static final Logger log = LogManager.getLogger(Comms.class);

    private ArrayList<PacketListener> packetListeners;
    private ArrayList<ConnectionListener> connectionListeners;

    private final Socket socket;

    private final ObjectInputStream input;
    private final ObjectOutputStream output;

    private boolean shouldQuit = false;

    public Comms (Socket socket, ObjectInputStream input, ObjectOutputStream output) {
        super("Comms_Thread");
        this.packetListeners = new ArrayList<>();
        this.connectionListeners = new ArrayList<>();
        this.socket = socket;
        this.input = input;
        this.output = output;
    }

    /**
     * Adds a <code>PacketListener</code> to this Comms instance.
     *
     * @param listener the <code>PacketListener</code> to add
     */
    public void addMessageListener (PacketListener listener) {
        this.packetListeners.add(listener);
    }

    /**
     * Removes an <code>PacketListener</code> from this spinner.
     *
     * @param listener the <code>PacketListener</code> to remove
     */
    public void removeMessageListener (PacketListener listener) {
        this.packetListeners.remove(listener);
    }

    /**
     * Sends a message
     *
     * @param packet
     */
    public void sendMessage (Packet packet) {
        try {
            output.writeObject(packet);
            log.debug("Sent packet. Type: {}", packet.getType().toString());
        } catch (IOException e) {
            log.error("Failed to send packet. Reason: {}", e.getMessage());
            log.debug(e);
        }
    }

    /**
     * Receives a message
     */
    public Packet receiveMessage () throws VersionMismatchException, IOException {
        try {
            return (Packet) input.readObject();
        } catch (ClassNotFoundException | ClassCastException e) {
            throw new VersionMismatchException();
        }
    }

    @Override
    public void run () {
        Packet packet;
        while (!shouldQuit) {
            try {
                packet = (Packet) input.readObject();
                log.debug("Received packet from client. Type: {}", packet.getType().toString());
                for (PacketListener l : this.packetListeners) {
                    l.packetReceived(packet);
                }
            } catch (SocketException e) {
                log.info("Socket was closed.");
                this.fireConnectionClosed("Server closed the connection.");
                this.shutdown();
            } catch (EOFException e) {
                this.fireConnectionClosed("Connection lost.");
                this.shutdown();
            } catch (IOException | ClassNotFoundException e) {
                if (e.getMessage().toLowerCase().equals("socket closed")) {
                    this.fireConnectionClosed("Connection lost.");
                    this.shutdown();
                }
                log.warn("Exception whilst reading packet. {}", e.getMessage());
                log.debug(e);
            }
        }
    }

    public void shutdown () {
        this.shouldQuit = true;
        try {
            this.socket.close();
        } catch (IOException e) {
            log.debug(e);
        }
    }

    private void fireConnectionClosed(String reason){
        for(ConnectionListener l : this.connectionListeners){
            l.connectionClosed(reason);
        }
    }
}
