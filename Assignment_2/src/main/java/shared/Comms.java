package shared;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import shared.events.ConnectionListener;
import shared.events.PacketListener;
import shared.exceptions.VersionMismatchException;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Communications Thread
 *
 * @author Huw Jones
 * @since 26/03/2016
 */
public class Comms {

    private static final Logger log = LogManager.getLogger(Comms.class);

    private ArrayList<PacketListener> packetListeners;
    private ArrayList<ConnectionListener> connectionListeners;

    protected final CommsReadThread readThread;
    protected final CommsWriteThread writeThread;
    private final Queue<Packet> packetQueue;

    private final Socket socket;

    private final ObjectInputStream input;
    private final ObjectOutputStream output;

    private boolean shouldQuit = false;

    public Comms (Socket socket, ObjectInputStream input, ObjectOutputStream output) {
        this.packetListeners = new ArrayList<>();
        this.connectionListeners = new ArrayList<>();
        this.packetQueue = new LinkedList<>();
        this.socket = socket;
        this.output = output;
        this.input = input;

        readThread = new CommsReadThread();
        writeThread = new CommsWriteThread();
    }

    public void start() {
        log.info("Starting read thread...");
        readThread.start();
        log.info("Starting write thread...");
        writeThread.start();
    }

    /**
     * Adds a <code>ConnectionListener</code> to this Comms instance.
     *
     * @param listener the <code>ConnectionListener</code> to add
     */
    public void addConnectionListener (ConnectionListener listener) {
        this.connectionListeners.add(listener);
    }

    /**
     * Removes an <code>ConnectionListener</code> from this spinner.
     *
     * @param listener the <code>ConnectionListener</code> to remove
     */
    public void removeConnectionListener (ConnectionListener listener) {
        this.connectionListeners.remove(listener);
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
     * @param packet Packet to send
     */
    public void sendMessage (Packet packet) {
        this.packetQueue.add(packet);
        synchronized (this.writeThread) {
            this.writeThread.notify();
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

    public void shutdown () {
        this.shouldQuit = true;
        try {
            this.output.close();
            this.input.close();
            this.socket.close();
        } catch (IOException e) {
            log.debug(e);
        }
    }

    class CommsWriteThread extends Thread {

        CommsWriteThread() {
            super("Comms_Thread_w");
        }

        @Override
        public void run() {
            final Logger log = LogManager.getLogger(CommsWriteThread.class);
            Packet packet;
            while (!shouldQuit) {
                while (Comms.this.packetQueue.size() != 0) {
                    try {
                        packet = Comms.this.packetQueue.poll();
                        if(packet ==  null) continue;
                        output.writeObject(packet);
                        log.debug("Sent packet. Type: {}", packet.getType().toString());
                    } catch (IOException e) {
                        log.error("Failed to send packet. Reason: {}", e.getMessage());
                        log.debug(e);
                    }
                }
                synchronized (this) {
                    try {
                        this.wait();
                    } catch (InterruptedException e) {
                    }
                }
            }
        }
    }

    class CommsReadThread extends Thread {

        CommsReadThread() {
            super("Comms_Thread_r");
        }

        @Override
        public void run() {
            final Logger log = LogManager.getLogger(CommsReadThread.class);
            Packet packet;
            while (!shouldQuit) {
                try {
                    packet = (Packet) input.readObject();
                    log.debug("Received packet. Type: {}", packet.getType().toString());
                    if(packet.getType() == PacketType.DISCONNECT){
                        Comms.this.fireConnectionClosed("Disconnect received.");
                    } else {
                        ArrayList<PacketListener> listeners = new ArrayList<>(Comms.this.packetListeners);
                        for (PacketListener l : listeners) {
                            l.packetReceived(packet);
                        }
                    }
                } catch (SocketException e) {
                    log.info("Socket was closed.");
                    Comms.this.fireConnectionClosed("Connection was closed by remote.");
                    Comms.this.shutdown();
                } catch (EOFException e) {
                    Comms.this.fireConnectionClosed("Connection lost.");
                    Comms.this.shutdown();
                } catch (IOException | ClassNotFoundException | ClassCastException e) {
                    if (e.getMessage().toLowerCase().equals("socket closed")) {
                        Comms.this.fireConnectionClosed("Connection lost.");
                        Comms.this.shutdown();
                    }
                    log.warn("Exception whilst reading packet. {}", e.getMessage());
                    log.debug(e);
                }
            }
        }
    }

    private void fireConnectionClosed(String reason){
        for(ConnectionListener l : this.connectionListeners){
            l.connectionClosed(reason);
        }
    }
}
