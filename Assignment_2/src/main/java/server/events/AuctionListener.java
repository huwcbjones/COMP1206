package server.events;

import java.util.EventListener;
import java.util.UUID;

/**
 * Event Handler for Auction Events
 *
 * @author Huw Jones
 * @since 09/05/2016
 */
public interface AuctionListener extends EventListener {

    /**
     * Fired when an auction starts
     *
     * @param itemID ID of Item
     */
    void auctionStart(UUID itemID);

    /**
     * Fired when an auction ends
     *
     * @param itemID ID of Item
     * @param wasWon Boolean, true if auction was won, false if it wasn't
     */
    void auctionEnd(UUID itemID, boolean wasWon);

    /**
     * Fired when a bid is placed on an auction
     *
     * @param itemID ID of Item
     * @param bidID  ID of Bid
     */
    void auctionBid(UUID itemID, UUID bidID);

}
