package shared.events;

import shared.Packet;

import java.util.EventListener;

/**
 * Event Handler for receiving packets
 *
 * @author Huw Jones
 * @since 26/03/2016
 */
public interface PacketListener extends EventListener {

    void packetReceived (Packet packet);
}
