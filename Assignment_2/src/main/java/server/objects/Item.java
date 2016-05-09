package server.objects;

import server.Server;
import server.events.AuctionListener;
import server.events.LoginListener;
import server.exceptions.OperationFailureException;
import shared.Keyword;
import shared.utils.RunnableAdapter;

import javax.swing.event.EventListenerList;
import java.awt.image.BufferedImage;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.UUID;

/**
 * Represents
 *
 * @author Huw Jones
 * @since 23/04/2016
 */
public class Item extends shared.Item {

    private EventListenerList listenerList = new EventListenerList();

    public Item(UUID itemID, UUID userID, String title, String description, HashSet<Keyword> keywords, Timestamp startTime, Timestamp endTime, BigDecimal reservePrice, ArrayList<? extends Bid> bids, BufferedImage image) {
        super(itemID, userID, title, description, keywords, startTime, endTime, reservePrice, bids, image);
        this.addAuctionListener(Server.getAuctionEventHandler());
    }

    private Item(shared.Item item) {
        super(
            item.getID(),
            item.getUserID(),
            item.getTitle(),
            item.getDescription(),
            item.getKeywords(),
            item.getStartTime(),
            item.getEndTime(),
            item.getReserve(),
            item.getBids(),
            item.getImage()
        );
        this.addAuctionListener(Server.getAuctionEventHandler());
    }

    /**
     * Adds an AuctionListener to this Item
     *
     * @param listener Listener to add
     */
    public void addAuctionListener(AuctionListener listener) {
        this.listenerList.add(AuctionListener.class, listener);
    }

    /**
     * Removes an AuctionListener on this Item
     *
     * @param listener Listener to remove
     */
    public void removeAuctionListener(AuctionListener listener) {
        this.listenerList.remove(AuctionListener.class, listener);
    }

    public User getUser() {
        return Server.getData().getUser(this.getUserID());
    }

    public void startAuction() throws OperationFailureException {
        if(this.getTimeUntilStart() != 0){
            throw new OperationFailureException("Cannot start auction, auction not due to start for " + this.getTimeUntilStart() + " seconds.");
        }

        if(this.getTimeUntilEnd() == 0){
            throw new OperationFailureException("Cannot start auction, auction has already ended.");
        }
        this.fireAuctionStart();
    }

    public void endAuction() throws OperationFailureException {
        if(this.getTimeUntilStart() != 0){
            throw new OperationFailureException("Cannot end auction, auction is still due to start in " + this.getTimeUntilStart() + " seconds.");
        }

        if(this.getTimeUntilEnd() != 0){
            throw new OperationFailureException("Cannot end auction, auction has " + this.getTimeUntilEnd() + " seconds left.");
        }

        this.fireAuctionEnd(this.getAuctionStatus() == AUCTION_WON);
    }

    private void fireAuctionStart() {
        Server.dispatchEvent(new RunnableAdapter() {
            @Override
            public void runSafe() {
                Object[] listeners = Item.this.listenerList.getListenerList();
                for (int i = listeners.length - 2; i >= 0; i -= 2) {
                    if (listeners[i] == AuctionListener.class) {
                        ((AuctionListener) listeners[i + 1]).auctionStart(Item.this.itemID);
                    }
                }
            }
        });
    }

    private void fireAuctionEnd(boolean wasWon) {
        Server.dispatchEvent(new RunnableAdapter() {
            @Override
            public void runSafe() {
                Object[] listeners = Item.this.listenerList.getListenerList();
                for (int i = listeners.length - 2; i >= 0; i -= 2) {
                    if (listeners[i] == AuctionListener.class) {
                        ((AuctionListener) listeners[i + 1]).auctionEnd(Item.this.itemID, wasWon);
                    }
                }
            }
        });
    }

    private void fireAuctionBid(Bid bid) {
        Server.dispatchEvent(new RunnableAdapter() {
            @Override
            public void runSafe() {
                Object[] listeners = Item.this.listenerList.getListenerList();
                for (int i = listeners.length - 2; i >= 0; i -= 2) {
                    if (listeners[i] == LoginListener.class) {
                        ((AuctionListener) listeners[i + 1]).auctionBid(Item.this.itemID, bid.getID());
                    }
                }
            }
        });
    }

    /**
     * Gets the top Bid
     *
     * @return Top Bid
     */
    @Override
    public Bid getTopBid() {
        return (Bid) super.getTopBid();
    }

    public static Item createServerItem(shared.Item item) {
        return new Item(item);
    }
}
