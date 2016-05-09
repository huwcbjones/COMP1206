package server.events;

import java.util.UUID;

/**
 * Adapter for AuctionListener
 *
 * @author Huw Jones
 * @since 09/05/2016
 */
public class AuctionAdapter implements AuctionListener {
    /**
     * Fired when an auction starts
     *
     * @param itemID ID of Item
     */
    @Override
    public void auctionStart(UUID itemID) {
    }

    /**
     * Fired when an auction ends
     *
     * @param itemID ID of Item
     * @param wasWon Boolean, true if auction was won, false if it wasn't
     */
    @Override
    public void auctionEnd(UUID itemID, boolean wasWon) {
    }

    /**
     * Fired when a bid is placed on an auction
     *
     * @param itemID ID of Item
     * @param bidID  ID of Bid
     */
    @Override
    public void auctionBid(UUID itemID, UUID bidID) {
    }
}
