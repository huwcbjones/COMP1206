package server.tasks;

import server.ServerComms.ClientConnection;
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

    /**
     * Performs the Worker Task
     */
    @Override
    public void runSafe() {
        if(!this.client.isConnected()) return;
        this.client.sendPacket(Packet.Ping());
    }

    /**
     * Executed on task failure
     */
    @Override
    protected void failureAction() {

    }
}
