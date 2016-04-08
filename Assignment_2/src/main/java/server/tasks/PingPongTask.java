package server.tasks;

import server.ClientConnection;
import shared.Packet;

/**
 * Manages server/client keep alive
 *
 * @author Huw Jones
 * @since 07/04/2016
 */
public class PingPongTask extends Task {

    public PingPongTask(ClientConnection client){
        super("ClientPing", client);
    }

    @Override
    public void run() {
        if(!this.client.isConnected()) return;
        this.client.sendPacket(Packet.Ping());
    }
}
