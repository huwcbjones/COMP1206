package server.tasks;

import server.ClientConnection;
import server.Server;
import shared.Packet;
import shared.PacketType;

import java.util.UUID;

/**
 * Task for Starting Auctions
 *
 * @author Huw Jones
 * @since 03/05/2016
 */
public class AuctionStartTask extends Task {
    private final UUID itemID;

    public AuctionStartTask(ClientConnection client, UUID itemID) {
        super("AuctionStartTask", client);
        this.itemID = itemID;
    }

    /**
     * Executed on task failure
     */
    @Override
    protected void failureAction() {
        log.fatal("Failed to start auction for Item({})", this.itemID);
    }

    @Override
    public void runSafe() throws Exception {
        log.info("Auction for Item({}) started!", this.itemID);
        Server.getData().getItem(itemID).startAuction();
        Server.getServer().broadcastPacket(new Packet<>(PacketType.AUCTION_START, itemID));
    }
}
