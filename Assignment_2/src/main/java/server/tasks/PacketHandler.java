package server.tasks;

import server.ClientConnection;
import server.Server;
import server.utils.Comms;
import shared.*;
import shared.utils.RunnableAdapter;

/**
 * Handles Packets
 *
 * @author Huw Jones
 * @since 27/03/2016
 */
public final class PacketHandler extends RunnableAdapter {

    private final ClientConnection client;
    private final Packet packet;

    public PacketHandler (ClientConnection client, Packet packet) {
        this.client = client;
        this.packet = packet;
    }

    @Override
    public void runSafe () {
        switch(this.packet.getType()){
            case HELLO:
                this.client.sendPacket(new Packet<>(PacketType.HELLO, "hello"));
                break;
            case PING:
                // Send the packet back slightly earlier to prevent accidental timeouts because
                // of the physical time it takes to send the TCP packet.
                Server.getWorkerPool().scheduleTask(new PingPongTask(this.client), (int)(Comms.PING_TIMEOUT * 0.95));
                break;
            case LOGIN:
                Server.getWorkerPool().queueTask(new LoginTask(this.client, (char[][])packet.getPayload()));
                break;
            case LOGOUT:
                Server.getWorkerPool().queueTask(new LogoutTask(this.client));
                break;
            case REGISTER:
                Server.getWorkerPool().queueTask(new RegisterTask(this.client, (RegisterUser)packet.getPayload()));
                break;
            case CREATE_ITEM:
                Server.getWorkerPool().queueTask(new NewAuctionTask(this.client, (Item)packet.getPayload()));
                break;
            case FETCH_KEYWORDS:
                Server.getWorkerPool().queueTask(new KeywordTask(this.client));
                break;
            case FETCH_RESERVE_RANGE:
                Server.getWorkerPool().queueTask(new ReserveTask(this.client));
                break;
            case SEARCH:
                Server.getWorkerPool().queueTask(new SearchTask(this.client, (SearchOptions)packet.getPayload()));
                break;
            default:
                this.client.sendPacket(Packet.wasOK(false));
                break;
        }
    }

    @Override
    public String toString() {
        return "PacketHandler";
    }
}
