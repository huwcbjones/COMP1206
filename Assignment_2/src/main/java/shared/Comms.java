package shared;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import shared.events.ConnectionListener;
import shared.events.PacketListener;
import shared.exceptions.PacketSendFailException;
import shared.exceptions.VersionMismatchException;
import shared.utils.RunnableAdapter;

import javax.swing.*;
import javax.swing.event.EventListenerList;
import java.awt.event.ActionListener;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ConcurrentLinkedQueue;

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
    public static final int PING_TIMEOUT = 30 * 1000;

    protected static final Logger log = LogManager.getLogger(Comms.class);
    private static final DispatchThread dispatchThread = new DispatchThread();

    static {
        dispatchThread.start();
    }

    protected final CommsReadThread readThread;
    protected final CommsWriteThread writeThread;
    private final EventListenerList listenerList = new EventListenerList();
    private final ConcurrentLinkedQueue<Packet> packetQueue;
    private final Socket socket;
    private final ObjectInputStream input;
    private final ObjectOutputStream output;
    protected Timer lastPingTimer;
    protected ActionListener pingListener;
    protected boolean shouldQuit = false;

    public Comms(Socket socket, ObjectInputStream input, ObjectOutputStream output) {
        this.packetQueue = new ConcurrentLinkedQueue<>();
        this.socket = socket;
        this.output = output;
        this.input = input;

        readThread = new CommsReadThread();
        writeThread = new CommsWriteThread();
        this.pingListener = e -> {
            log.info("Ping not received in timeout period.");
            this.shutdown();
            fireConnectionClosed("Lost connection to server");
        };
        this.lastPingTimer = new Timer((int) (Comms.PING_TIMEOUT * 1.05), this.pingListener);
        this.lastPingTimer.setRepeats(false);
    }

    /**
     * Starts the Comms threads
     */
    public final void start() {
        readThread.start();
        writeThread.start();
    }

    public String getConnectionDetails(){
        return socket.getInetAddress() + ":" + socket.getPort();
    }


    /**
     * Shuts down this Comms class
     */
    public void shutdown() {
        if (Comms.this.shouldQuit) return;
        this.shouldQuit = true;

        log.debug("Comms shutdown initiated...");

        new Thread(() -> {
            //final Logger log = LogManager.getLogger(Thread.currentThread().getClass());
            this.lastPingTimer.stop();
            this.lastPingTimer.removeActionListener(this.pingListener);

            try {
                this.readThread.interrupt();
                this.writeThread.interrupt();

                try {
                    this.readThread.join();
                    this.writeThread.join();
                } catch (InterruptedException e) {
                    log.trace(e);
                }
                this.socket.close();
                log.trace("Closed socket.");


            } catch (IOException e) {
                log.debug(e);
            }
            log.info("Comms shutdown!");
        }, "CommsShutdown").start();
    }

    //region Event Handling

    /**
     * Dispatches the event
     *
     * @param event Event to dispatch
     */
    protected void dispatchEvent(RunnableAdapter event) {
        dispatchThread.dispatchEvent(event);
    }

    @Override
    public void packetReceived(Packet packet) {
        if (packet.getType() == PacketType.PING) {
            this.lastPingTimer.restart();
            this.sendMessage(Packet.Ping());
        }
    }

    private void firePacketReceived(Packet packet) {
        this.dispatchEvent(new RunnableAdapter() {
            @Override
            public void runSafe() {
                log.trace("fired->PacketReceived");
                Object[] listeners = Comms.this.listenerList.getListenerList();
                for (int i = listeners.length - 2; i >= 0; i -= 2) {
                    if (listeners[i] == PacketListener.class) {
                        ((PacketListener) listeners[i + 1]).packetReceived(packet);
                    }
                }
            }
        });
    }

    protected final void fireConnectionClosed(String reason) {
        this.dispatchEvent(new RunnableAdapter() {
            @Override
            public void runSafe() {
                log.trace("fired->ConnectionClosed");
                Object[] listeners = Comms.this.listenerList.getListenerList();
                for (int i = listeners.length - 2; i >= 0; i -= 2) {
                    if (listeners[i] == ConnectionListener.class) {
                        ((ConnectionListener) listeners[i + 1]).connectionClosed(reason);
                    }
                }
            }
        });
    }

    /**
     * Adds a <code>ConnectionListener</code> to this Comms instance.
     *
     * @param listener the <code>ConnectionListener</code> to add
     */
    public final void addConnectionListener(ConnectionListener listener) {
        this.listenerList.add(ConnectionListener.class, listener);
    }

    /**
     * Removes an <code>ConnectionListener</code> from this spinner.
     *
     * @param listener the <code>ConnectionListener</code> to remove
     */
    public final void removeConnectionListener(ConnectionListener listener) {
        this.listenerList.remove(ConnectionListener.class, listener);
    }

    /**
     * Adds a <code>PacketListener</code> to this Comms instance.
     *
     * @param listener the <code>PacketListener</code> to add
     */
    public final void addMessageListener(PacketListener listener) {
        this.listenerList.add(PacketListener.class, listener);
    }

    /**
     * Removes an <code>PacketListener</code> from this spinner.
     *
     * @param listener the <code>PacketListener</code> to remove
     */
    public final void removeMessageListener(PacketListener listener) {
        this.listenerList.remove(PacketListener.class, listener);
    }
    //endregion

    //region Mandatory Spec methods

    /**
     * Sends a message
     *
     * @param packet Packet to send
     */
    public final void sendMessage(Packet packet) {
        this.packetQueue.add(packet);
        synchronized (this.writeThread) {
            this.writeThread.notify();
        }
    }

    /**
     * External classes using the Comms API should use sendMessage
     * Writes directly to the output stream.
     * Internal method used to write to the output stream.
     *
     * @param packet
     */
    protected final void sendDirectMessage(Packet packet) throws PacketSendFailException {
        try {
            output.writeObject(packet);
            log.trace("Sent packet. Type: {}", packet.getType().toString());
        } catch (IOException e) {
            log.trace(e);
            throw new PacketSendFailException("Failed to send packet. Type: " + packet.getType().toString() + ". Reason: " + e.getMessage());
        }
    }

    /**
     * Receives a message
     */
    public final Packet receiveMessage() throws VersionMismatchException, IOException {
        try {
            return (Packet) input.readObject();
        } catch (ClassNotFoundException | ClassCastException e) {
            throw new VersionMismatchException();
        }
    }
    //endregion

    private static class DispatchThread extends Thread {
        private ConcurrentLinkedQueue<Runnable> events = new ConcurrentLinkedQueue<>();
        private boolean shouldStop = false;

        public DispatchThread() {
            super("DispatchThread");
            this.setDaemon(true);
        }

        public void dispatchEvent(Runnable event) {
            this.events.add(event);
            synchronized (this) {
                this.notify();
            }
        }

        @Override
        public void run() {
            Runnable event;
            while (!shouldStop) {
                log.trace("Running events");
                while ((event = this.events.poll()) != null) {
                    event.run();
                }
                synchronized (this) {
                    try {
                        this.wait();
                    } catch (InterruptedException shouldQuitNotification) {
                    }
                }
            }
        }

        public void quit() {
            this.shouldStop = true;
            this.interrupt();
        }


    }

    public class CommsWriteThread extends Thread {

        CommsWriteThread() {
            super("CommsThread_w");
        }

        @Override
        public void run() {
            final Logger log = LogManager.getLogger(CommsWriteThread.class);
            Packet packet;

            log.debug("Write thread started!");
            while (!shouldQuit) {
                while ((packet = Comms.this.packetQueue.poll()) != null) {
                    try {
                        Comms.this.sendDirectMessage(packet);
                    } catch (PacketSendFailException e) {
                        log.error(e.getMessage());
                    }
                }
                synchronized (this) {
                    try {
                        this.wait();
                    } catch (InterruptedException threadQuitNotice) {
                        if (shouldQuit) {
                            break;
                        }
                    }
                }
            }

            if (!Comms.this.socket.isClosed()) {
                try {
                    Comms.this.output.flush();
                    log.trace("Flushed output stream.");

                    Comms.this.output.close();
                    log.trace("Closed output stream.");

                } catch (IOException e) {
                    log.trace(e);
                    log.warn("Error flushing and closing output stream. {}", e.getMessage());
                }
            }
            log.debug("Write thread shut down.");
        }
    }

    public class CommsReadThread extends Thread {

        CommsReadThread() {
            super("CommsThread_r");
        }

        @Override
        public void run() {
            final Logger log = LogManager.getLogger(CommsReadThread.class);
            Packet packet;

            log.debug("Read thread started!");
            while (!shouldQuit) {
                try {
                    packet = Comms.this.receiveMessage();

                    log.trace("Received packet. Type: {}", packet.getType().toString());
                    if (packet.getType() == PacketType.DISCONNECT) {
                        Comms.this.fireConnectionClosed("Disconnect received.");
                        Comms.this.shutdown();
                    } else {
                        Comms.this.firePacketReceived(packet);
                    }

                } catch (SocketException e) {
                    log.debug("Socket was closed.");
                    Comms.this.fireConnectionClosed("Connection was closed by remote.");
                    Comms.this.shutdown();
                } catch (EOFException e) {
                    Comms.this.fireConnectionClosed("Connection lost.");
                    Comms.this.shutdown();
                } catch (IOException | VersionMismatchException | ClassCastException e) {
                    if (e.getMessage().toLowerCase().equals("socket closed")) {
                        Comms.this.fireConnectionClosed("Connection lost.");
                        Comms.this.shutdown();
                    }
                    log.warn("Exception whilst reading packet. {}", e.getMessage());
                    log.debug(e);
                }
            }

            try {
                Comms.this.input.close();
                log.trace("Closed input stream.");

            } catch (IOException e) {
                log.trace(e);
                log.warn("Error closing input stream. {}", e.getMessage());
            }

            log.debug("Read thread shut down.");
        }
    }
}
