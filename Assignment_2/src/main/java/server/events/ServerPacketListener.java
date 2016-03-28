package server.events;

import server.ClientConnection;
import shared.Packet;

/**
 * * Event Handler for receiving server packets
 *
 * @author Huw Jones
 * @since 28/03/2016
 */
public interface ServerPacketListener {

    void packetRecieved(ClientConnection client, Packet packet);
}
