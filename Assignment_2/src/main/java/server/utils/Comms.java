package server.utils;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Server side COmmunications class
 *
 * @author Huw Jones
 * @since 27/03/2016
 */
public final class Comms extends shared.Comms {
    private final long clientID;

    public Comms (long clientID, ObjectInputStream input, ObjectOutputStream output) {
        super(input, output);
        this.setName("Comms_Thread_" + clientID);
        this.clientID = clientID;
    }
}
