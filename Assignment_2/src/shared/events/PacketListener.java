package shared.events;

import shared.Packet;

/**
 * Event Handler for receiving packets
 *
 * @author Huw Jones
 * @since 26/03/2016
 */
public interface PacketListener {

    void packetReceived (Packet packet);
}
