package server.tasks;

import server.Server;
import server.ServerComms.ClientConnection;
import shared.Packet;
import shared.PacketType;
import shared.UserRequest;

/**
 * Fetches a user the client requests.
 *
 * @author Huw Jones
 * @since 11/05/2016
 */
public class FetchUserTask extends Task {

    private final UserRequest userRequest;

    public FetchUserTask(ClientConnection client, UserRequest userRequest){
        super("FetchUserTask", client);
        this.userRequest = userRequest;
    }
    /**
     * Executed on task failure
     */
    @Override
    protected void failureAction() {
        this.client.sendPacket(new Packet<>(PacketType.USER, new UserRequest(userRequest.getUserID(), userRequest.getRequestID(), null)));
    }

    @Override
    public void runSafe() throws Exception {
        this.client.sendPacket(new Packet<>(
            PacketType.USER,
            new UserRequest(userRequest.getUserID(), userRequest.getRequestID(), Server.getData().getUser(userRequest.getUserID()).getSharedUser())
        ));
    }
}
