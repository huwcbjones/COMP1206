package server.events;

import server.ClientConnection;
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
            default:
                this.client.sendPacket(Packet.wasOK(false));
                break;
        }
    }
}
