package shared;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import shared.events.PacketListener;
import shared.exceptions.VersionMismatchException;

import java.io.*;
import java.util.ArrayList;

/**
 * Communications Thread
 *
 * @author Huw Jones
 * @since 26/03/2016
 */
public class Comms extends Thread {

    private static final Logger log = LogManager.getLogger(Comms.class);

    private ArrayList<PacketListener> listeners;

    private final ObjectInputStream input;
    private final ObjectOutputStream output;

    private boolean shouldQuit = false;

    public Comms (ObjectInputStream input, ObjectOutputStream output) {
        super("Comms_Thread");
        listeners = new ArrayList<>();
        this.input = input;
        this.output = output;
    }

    /**
     * Adds a <code>PacketListener</code> to this Comms instance.
     *
     * @param listener the <code>PacketListener</code> to add
     */
    public void addMessageListener (PacketListener listener) {
        this.listeners.add(listener);
    }

    /**
     * Removes an <code>PacketListener</code> from this spinner.
     *
     * @param listener the <code>PacketListener</code> to remove
     */
    public void removeMessageListener (PacketListener listener) {
        this.listeners.remove(listener);
    }

    /**
     * Sends a message
     *
     * @param packet
     */
    public void sendMessage (Packet packet) {
        try {
            output.writeObject(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Receives a message
     */
    public Packet receiveMessage () throws VersionMismatchException, IOException {
        try {
            return (Packet) input.readObject();
        } catch (ClassNotFoundException e) {
            throw new VersionMismatchException();
        }
    }

    @Override
    public void run () {
        Packet packet;
        while (!shouldQuit) {
            try {
                packet = (Packet) input.readObject();
                for (PacketListener l : this.listeners) {
                    l.packetReceived(packet);
                }
            } catch (EOFException e){
                log.error("Stream was closed.");
                this.shouldQuit = true;
            } catch (IOException | ClassNotFoundException e) {
                if(e.getMessage().toLowerCase().equals("socket closed")){
                    this.shouldQuit = true;
                    continue;
                }
                log.warn("Exception whilst reading packet. {}", e.getMessage());
                log.debug(e);
            }
        }
    }

    public void shutdown (){
        this.shouldQuit = true;
        try {
            this.input.close();
            this.output.close();
        } catch (IOException e) {
            log.debug(e);
        }
    }
}
