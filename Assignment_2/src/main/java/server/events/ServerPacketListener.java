package server.events;

import server.ServerComms.ClientConnection;
import shared.Packet;

import java.util.EventListener;

/**
 * * Event Handler for receiving server packets
 *
 * @author Huw Jones
 * @since 28/03/2016
 */
public interface ServerPacketListener extends EventListener {

    void packetReceived(ClientConnection client, Packet packet);
}
