package server.tasks;

import server.Server;
import server.ServerComms.ClientConnection;
import shared.Packet;
import shared.PacketType;

import java.util.UUID;

/**
 * {DESCRIPTION}
 *
 * @author Huw Jones
 * @since 11/05/2016
 */
public class FetchItemTask extends Task {

    private final UUID itemID;

    public FetchItemTask(ClientConnection client, UUID itemID) {
        super("FetchItemTask", client);
        this.itemID = itemID;
    }

    /**
     * Executed on task failure
     */
    @Override
    protected void failureAction() {
        this.client.sendPacket(new Packet<>(PacketType.ITEM, null));
    }

    @Override
    public void runSafe() throws Exception {
        this.client.sendPacket(new Packet<>(PacketType.ITEM, Server.getData().getItem(itemID).getClientItem()));
    }
}
