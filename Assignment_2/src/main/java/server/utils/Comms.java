package server.utils;

import server.Server;
import shared.Packet;

import javax.swing.*;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * Server side Communications class
 *
 * @author Huw Jones
 * @since 27/03/2016
 */
public final class Comms extends shared.Comms {
    private final long clientID;

    public Comms (long clientID, Socket socket, ObjectInputStream input, ObjectOutputStream output) {
        super(socket, input, output);
        this.readThread.setName("Comms_Thread_r_" + clientID);
        this.writeThread.setName("Comms_Thread_w_" + clientID);
        this.clientID = clientID;

        this.lastPingTimer = new Timer((int)(Comms.PING_TIMEOUT), e ->{
            log.info("Ping not received in timeout period.");
            this.shutdown();
            fireConnectionClosed("Lost connection to client");});
        this.lastPingTimer.setRepeats(false);
    }

    /**
     * Need to override shared Comms packetReceived method to prevent the server sending a ping back straight away.
     * Instead, we let the ping packet propagate to the server's main packet handler and let that queue a
     * scheduled task to send a new ping packet in 0.95 * TIMEOUT value.
     * We still need to restart the timer so that we know if a client didn't reply to our ping
     *
     * @param packet From client
     */
    @Override
    public void packetReceived(Packet packet) {
        this.lastPingTimer.restart();
    }
}
