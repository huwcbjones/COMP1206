package server.utils;

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
    }
}
