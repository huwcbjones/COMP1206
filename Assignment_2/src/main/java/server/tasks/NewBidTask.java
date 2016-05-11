package server.tasks;

import server.Server;
import server.ServerComms.ClientConnection;
import server.exceptions.OperationFailureException;
import server.objects.Item;
import shared.Bid;
import shared.Packet;
import shared.PacketType;
import shared.Pair;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Processes new bids
 *
 * @author Huw Jones
 * @since 11/05/2016
 */
public class NewBidTask extends Task {

    private final Pair<UUID, BigDecimal> bid;
    public NewBidTask(ClientConnection client, Bid bid){
        super("NewBidTask", client);
        this.bid = new Pair<>(bid.getItemID(), bid.getPrice());
    }
    /**
     * Executed on task failure
     */
    @Override
    protected void failureAction() {
        this.client.sendPacket(new Packet<>(PacketType.PLACE_BID_FAIL));
    }

    @Override
    public void runSafe() throws Exception {
        Item item = Server.getData().getItem(this.bid.getKey());
        if(item.getReserve().compareTo(this.bid.getValue()) > 0){
            this.client.sendPacket(new Packet<>(PacketType.PLACE_BID_FAIL, "Bid must be greater than or equal to the reserve price."));
            return;
        }
        if(item.getTopBid() != null && this.bid.getValue().compareTo(item.getTopBid().getPrice()) <= 0){
            this.client.sendPacket(new Packet<>(PacketType.PLACE_BID_FAIL, "Bid is less than or equal to current top bid."));
            return;
        }

        try {
            item.placeBid(this.client.getUser().getUniqueID(), this.bid.getValue());
            this.client.sendPacket(new Packet<>(PacketType.PLACE_BID_SUCCESS));
        } catch (OperationFailureException ex){
            this.client.sendPacket(new Packet<>(PacketType.PLACE_BID_FAIL, ex.getMessage()));
        }
    }
}
