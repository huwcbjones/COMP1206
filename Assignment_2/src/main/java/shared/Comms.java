package shared;

import client.Client;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import shared.events.ConnectionListener;
import shared.events.PacketListener;
import shared.exceptions.VersionMismatchException;

import javax.swing.*;
import javax.swing.event.EventListenerList;
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
public class Comms implements PacketListener {

    /**
     * Amount of time (in milliseconds) before a ping can timeout
     */
    public static final int PING_TIMEOUT = 60 * 1000;

    protected static final Logger log = LogManager.getLogger(Comms.class);

    protected final EventListenerList listenerList = new EventListenerList();

    protected final CommsReadThread readThread;
    protected final CommsWriteThread writeThread;
    private final Queue<Packet> packetQueue;

    private final Socket socket;

    private final ObjectInputStream input;
    private final ObjectOutputStream output;

    private boolean shouldQuit = false;

    protected Timer lastPingTimer;

    public Comms (Socket socket, ObjectInputStream input, ObjectOutputStream output) {
        this.packetQueue = new LinkedList<>();
        this.socket = socket;
        this.output = output;
        this.input = input;

        readThread = new CommsReadThread();
        writeThread = new CommsWriteThread();
        this.lastPingTimer = new Timer(Comms.PING_TIMEOUT, e ->{
            log.info("Ping not received in timeout period.");
            this.shutdown();
            fireConnectionClosed("Lost connection to server");});
        this.lastPingTimer.setRepeats(false);
    }

    public final void start() {
        log.debug("Starting comms read thread...");
        readThread.start();
        log.debug("Starting comms write thread...");
        writeThread.start();
    }

    //region Event Listening
    /**
     * Adds a <code>ConnectionListener</code> to this Comms instance.
     *
     * @param listener the <code>ConnectionListener</code> to add
     */
    public final void addConnectionListener (ConnectionListener listener) {
        this.listenerList.add(ConnectionListener.class, listener);
    }

    /**
     * Removes an <code>ConnectionListener</code> from this spinner.
     *
     * @param listener the <code>ConnectionListener</code> to remove
     */
    public final void removeConnectionListener (ConnectionListener listener) {
        this.listenerList.remove(ConnectionListener.class, listener);
    }

    /**
     * Adds a <code>PacketListener</code> to this Comms instance.
     *
     * @param listener the <code>PacketListener</code> to add
     */
    public final void addMessageListener (PacketListener listener) {
        this.listenerList.add(PacketListener.class, listener);
    }

    /**
     * Removes an <code>PacketListener</code> from this spinner.
     *
     * @param listener the <code>PacketListener</code> to remove
     */
    public final void removeMessageListener (PacketListener listener) {
        this.listenerList.remove(PacketListener.class, listener);
    }

    protected final void fireConnectionClosed(String reason){
        Object[] listeners = this.listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i]==ConnectionListener.class) {
                ((ConnectionListener)listeners[i+1]).connectionClosed(reason);
            }
        }
    }

    private void firePacketReceived(Packet packet){
        Object[] listeners = this.listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i]==PacketListener.class) {
                ((PacketListener)listeners[i+1]).packetReceived(packet);
            }
        }
    }
    //endregion

    //region Mandatory Spec methods
    /**
     * Sends a message
     *
     * @param packet Packet to send
     */
    public final void sendMessage (Packet packet) {
        this.packetQueue.add(packet);
        synchronized (this.writeThread) {
            this.writeThread.notify();
        }
    }

    /**
     * Receives a message
     */
    public final Packet receiveMessage () throws VersionMismatchException, IOException {
        try {
            return (Packet) input.readObject();
        } catch (ClassNotFoundException | ClassCastException e) {
            throw new VersionMismatchException();
        }
    }
    //endregion

    /**
     * Shuts down this Comms class
     */
    public void shutdown () {
        this.shouldQuit = true;
        try {
            this.output.close();
            this.input.close();
            this.socket.close();
            synchronized (this.readThread){
                this.readThread.notify();
            }
            synchronized (this.writeThread){
                this.writeThread.notify();
            }
        } catch (IOException e) {
            log.debug(e);
        }
    }

    @Override
    public void packetReceived(Packet packet) {
        if(packet.getType() == PacketType.PING){
            this.lastPingTimer.restart();
            this.sendMessage(Packet.Ping());
        }
    }

    public class CommsWriteThread extends Thread {

        CommsWriteThread() {
            super("Comms_Thread_w");
        }

        @Override
        public void run() {
            final Logger log = LogManager.getLogger(CommsWriteThread.class);
            Packet packet;
            while (!shouldQuit) {
                while ((packet = Comms.this.packetQueue.poll()) != null) {
                    try {
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
                    } catch (InterruptedException ignored) {
                    }
                }
            }
        }
    }

    public class CommsReadThread extends Thread {

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
                        Comms.this.firePacketReceived(packet);
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
}
