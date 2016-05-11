package server.tasks;

import server.Server;
import server.ServerComms.ClientConnection;
import server.objects.Bid;
import server.objects.Item;
import server.objects.User;
import shared.Packet;
import shared.PacketType;

import java.util.UUID;

/**
 * Task for ending auctions
 *
 * @author Huw Jones
 * @since 04/05/2016
 */
public class AuctionEndTask extends Task {
    private final UUID itemID;

    public AuctionEndTask(ClientConnection client, UUID itemID) {
        super("AuctionEndTask", client);
        this.itemID = itemID;
    }

    /**
     * Executed on task failure
     */
    @Override
    protected void failureAction() {
        log.fatal("Failed to end auction for Item({})", this.itemID);
    }

    @Override
    public void runSafe() throws Exception {
        Item item = Server.getData().getItem(itemID);

        item.endAuction();

        // Tell logged in clients that the auction has ended
        Server.getServer().broadcastPacket(new Packet<>(PacketType.AUCTION_END, this.itemID), true);

        // Let seller know the auction failed (if logged in)
        if(item.getAuctionStatus() == shared.Item.AUCTION_NO_WINNER && item.getUser().isLoggedIn()){
            item.getUser().getClient().sendPacket(new Packet<>(PacketType.AUCTION_NO_WINNER, itemID));
            return;
        }

        // Notify winner that they won the auction
        Bid winningBid = item.getTopBid();
        User winningUser = winningBid.getUser();

        if(winningUser.isLoggedIn()) {
            winningUser.getClient().sendPacket(new Packet<>(PacketType.AUCTION_WIN, this.itemID));
            if(item.getUser().isLoggedIn()){
                item.getUser().getClient().sendPacket(new Packet<>(PacketType.AUCTION_WIN, itemID));
            }
        }
    }
}
