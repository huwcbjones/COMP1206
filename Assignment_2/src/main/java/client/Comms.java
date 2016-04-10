package client;

import shared.Packet;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * {DESCRIPTION}
 *
 * @author Huw Jones
 * @since 10/04/2016
 */
public class Comms extends shared.Comms {
    public Comms(Socket socket, ObjectInputStream input, ObjectOutputStream output) {
        super(socket, input, output);
    }

    @Override
    public void packetReceived(Packet packet) {
        super.packetReceived(packet);
    }
}
