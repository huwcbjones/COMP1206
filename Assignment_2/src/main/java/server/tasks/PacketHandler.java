package server.tasks;

import server.ClientConnection;
import server.Server;
import server.utils.Comms;
import shared.Packet;
import shared.PacketType;

/**
 * Handles Packets
 *
 * @author Huw Jones
 * @since 27/03/2016
 */
public final class PacketHandler implements Runnable {

    private final ClientConnection client;
    private final Packet packet;

    public PacketHandler (ClientConnection client, Packet packet) {
        this.client = client;
        this.packet = packet;
    }

    @Override
    public void run () {
        switch(this.packet.getType()){
            case HELLO:
                this.client.sendPacket(new Packet<>(PacketType.HELLO, "hello"));
                break;
            case PING:
                // Send the packet back slightly earlier to prevent accidental timeouts because
                // of the physical time it takes to send the TCP packet.
                Server.getWorkerPool().scheduleTask(new PingPongTask(this.client), (int)(Comms.PING_TIMEOUT * 0.95));
                break;
            default:
                this.client.sendPacket(Packet.wasOK(false));
                break;
        }
    }
}
