package server.tasks;

import server.Server;
import server.ServerComms.ClientConnection;
import shared.Packet;
import shared.PacketType;

import java.util.UUID;

/**
 * Fetches a user the client requests.
 *
 * @author Huw Jones
 * @since 11/05/2016
 */
public class FetchUserTask extends Task {

    private final UUID userID;

    public FetchUserTask(ClientConnection client, UUID userID){
        super("FetchUserTask", client);
        this.userID = userID;
    }
    /**
     * Executed on task failure
     */
    @Override
    protected void failureAction() {
        this.client.sendPacket(new Packet<>(PacketType.USER));
    }

    @Override
    public void runSafe() throws Exception {
        this.client.sendPacket(new Packet<>(PacketType.USER, Server.getData().getUser(userID).getSharedUser()));
    }
}
