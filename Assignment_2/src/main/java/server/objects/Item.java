package server.objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import server.Server;
import server.events.AuctionListener;
import server.exceptions.OperationFailureException;
import shared.Keyword;
import shared.utils.RunnableAdapter;
import shared.utils.UUIDUtils;

import javax.swing.event.EventListenerList;
import java.awt.image.BufferedImage;
import java.math.BigDecimal;
import java.sql.*;
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

    private static final long serialUID = -2134L;

    private EventListenerList listenerList = new EventListenerList();

    public Item(UUID itemID, UUID userID, String title, String description, HashSet<Keyword> keywords, Timestamp startTime, Timestamp endTime, BigDecimal reservePrice, ArrayList<shared.Bid> bids, BufferedImage image, BufferedImage thumbnail) {
        super(itemID, userID, title, description, keywords, startTime, endTime, reservePrice, bids, image, thumbnail);
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
            item.getImage(),
            item.getThumbnail()
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

    /**
     * Gets the User of the seller
     *
     * @return User of the seller
     */
    public User getUser() {
        return Server.getData().getUser(this.getUserID());
    }

    /**
     * Starts an auction
     *
     * @throws OperationFailureException Thrown if the auction cannot be started
     */
    public void startAuction() throws OperationFailureException {
        if (this.getTimeUntilStart() != 0) {
            throw new OperationFailureException("Cannot start auction, auction not due to start for " + this.getTimeUntilStart() + " seconds.");
        }

        if (this.getTimeUntilEnd() == 0) {
            throw new OperationFailureException("Cannot start auction, auction has already ended.");
        }
        this.fireAuctionStart();
    }

    /**
     * Places a bid on this item
     *
     * @param userID Used placing the bid
     * @param price  Price of the bid
     * @throws OperationFailureException Thrown if the bid could not be placed
     */
    public void placeBid(UUID userID, BigDecimal price) throws OperationFailureException {
        if (userID.equals(this.getUserID())) {
            throw new OperationFailureException("Cannot bid on this item.");
        }
        if (this.getReserve().compareTo(price) > 0) {
            throw new OperationFailureException("Bid must be greater than or equal to the reserce price.");
        }
        if (this.bids.size() != 0 && price.compareTo(this.getTopBid().getPrice()) <= 0) {
            throw new OperationFailureException("Bid must be greater than current top bid.");
        }

        UUID bidID = this.addBidToDatabase(userID, price);
        this.loadBid(bidID);
        this.topBid = this.bids.get(bidID);
        this.fireAuctionBid(this.getBid(bidID));
    }

    /**
     * Ends an auction
     *
     * @throws OperationFailureException Thrown if the auction cannot be ended
     */
    public void endAuction() throws OperationFailureException {
        if (this.getTimeUntilStart() != 0) {
            throw new OperationFailureException("Cannot end auction, auction is still due to start in " + this.getTimeUntilStart() + " seconds.");
        }

        if (this.getTimeUntilEnd() != 0) {
            throw new OperationFailureException("Cannot end auction, auction has " + this.getTimeUntilEnd() + " seconds left.");
        }

        this.fireAuctionEnd(this.getAuctionStatus() == AUCTION_WON);
    }

    //region Event Firing
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

    private void fireAuctionBid(shared.Bid bid) {
        Server.dispatchEvent(new RunnableAdapter() {
            @Override
            public void runSafe() {
                Object[] listeners = Item.this.listenerList.getListenerList();
                for (int i = listeners.length - 2; i >= 0; i -= 2) {
                    if (listeners[i] == AuctionListener.class) {
                        ((AuctionListener) listeners[i + 1]).auctionBid(Item.this.itemID, bid.getID());
                    }
                }
            }
        });
    }

    //endregion


    /**
     * Adds a bid to the database
     * @param userID User placing bid
     * @param bid Bid price
     * @return UUID of new bid
     * @throws OperationFailureException Thrown if the addition failed
     */
    private UUID addBidToDatabase(UUID userID, BigDecimal bid) throws OperationFailureException {
        final Logger log = LogManager.getLogger(Item.class);

        ArrayList<UUID> bidIDs = new ArrayList<>();

        Connection c = null;
        PreparedStatement selectBidIDs = null;
        PreparedStatement insertBid = null;

        String selectBidIDsSql = "SELECT bidID FROM bids";
        String insertBidSql = "INSERT INTO bids (bidID, itemID, userID, price, time) VALUES (?, ?, ?, ?, (CAST(strftime('%s', 'now') AS INT)))";

        ResultSet bidIDSet = null;
        boolean wasSuccess = false;
        UUID uniqueID = null;
        try {
            c = Server.getData().getConnection();

            // Create unique Bid ID
            selectBidIDs = c.prepareStatement(selectBidIDsSql);
            bidIDSet = selectBidIDs.executeQuery();

            while (bidIDSet.next()) {
                bidIDs.add(UUIDUtils.BytesToUUID(bidIDSet.getBytes("bidID")));
            }

            do {
                uniqueID = UUID.randomUUID();
            }
            while (bidIDs.contains(uniqueID));

            c.setAutoCommit(false);
            insertBid = c.prepareStatement(insertBidSql);
            insertBid.setBytes(1, UUIDUtils.UUIDToBytes(uniqueID));
            insertBid.setBytes(2, UUIDUtils.UUIDToBytes(this.getID()));
            insertBid.setBytes(3, UUIDUtils.UUIDToBytes(userID));
            insertBid.setBigDecimal(4, bid);
            insertBid.executeUpdate();
            c.commit();
            wasSuccess = true;
        } catch (SQLException e) {
            log.catching(e);
            log.debug("SQLState: {}", e.getSQLState());
            log.debug("Error Code: {}", e.getErrorCode());
            log.debug("Message: {}", e.getMessage());
            log.debug("Cause: {}", e.getCause());
            log.error("Failed to add bid.");
            wasSuccess = false;
        } finally {
            try {
                if (bidIDSet != null) {
                    bidIDSet.close();
                }
                if (selectBidIDs != null) {
                    selectBidIDs.close();
                }
                if (insertBid != null) {
                    insertBid.close();
                }
                if (c != null) {
                    c.setAutoCommit(true);
                    c.close();
                }
            } catch (SQLException suppress) {
                log.trace(suppress);
            }
        }
        if (!wasSuccess) {
            throw new OperationFailureException("Failed to save bid.");
        }
        return uniqueID;
    }

    /**
     * Loads a bid into this item
     * @param bidID ID of bid
     * @throws OperationFailureException Thrown if the bid could not be loaded
     */
    private void loadBid(UUID bidID) throws OperationFailureException {
        final Logger log = LogManager.getLogger(Item.class);

        Connection c = null;
        PreparedStatement selectBid = null;

        String selectBidSql = "SELECT bidID, userID, price, time FROM bids WHERE bidID=?";

        ResultSet bidIDSet = null;
        try {
            c = Server.getData().getConnection();

            // Create unique Bid ID
            selectBid = c.prepareStatement(selectBidSql);
            selectBid.setBytes(1, UUIDUtils.UUIDToBytes(bidID));
            bidIDSet = selectBid.executeQuery();

            while (bidIDSet.next()) {
                Bid bid = new Bid(
                    UUIDUtils.BytesToUUID(bidIDSet.getBytes("bidID")),
                    this.getID(),
                    UUIDUtils.BytesToUUID(bidIDSet.getBytes("userID")),
                    bidIDSet.getBigDecimal("price"),
                    new Timestamp(bidIDSet.getLong("time") * 1000L)
                );
                this.bids.put(bid.getID(), bid);
            }


        } catch (SQLException e) {
            log.catching(e);
            log.debug("SQLState: {}", e.getSQLState());
            log.debug("Error Code: {}", e.getErrorCode());
            log.debug("Message: {}", e.getMessage());
            log.debug("Cause: {}", e.getCause());
            log.error("Failed to load bid.");
        } finally {
            try {
                if (bidIDSet != null) {
                    bidIDSet.close();
                }
                if (selectBid != null) {
                    selectBid.close();
                }
                if (c != null) {
                    c.setAutoCommit(true);
                    c.close();
                }
            } catch (SQLException suppress) {
                log.trace(suppress);
            }
        }
    }

    /**
     * Gets the top Bid
     *
     * @return Top Bid
     */
    @Override
    public Bid getTopBid() {
        shared.Bid bid = super.getTopBid();
        if (bid == null) return null;
        return new Bid(
            bid.getID(),
            bid.getItemID(),
            bid.getUserID(),
            bid.getPrice(),
            bid.getTime()
        );
    }

    /**
     * Creates a shared.Item version (compatible with the client)
     * @return Shared Item
     */
    public shared.Item getClientItem() {
        return Item.getClientItem(this);
    }

    /**
     * Creates a server item from a shared item
     * @param item Shared item
     * @return Server Item
     */
    public static Item createServerItem(shared.Item item) {
        return new Item(item);
    }

    public static shared.Item getClientItem(Item item) {
        return new shared.Item(
            item.getID(),
            item.getUserID(),
            item.getTitle(),
            item.getDescription(),
            item.getKeywords(),
            item.getStartTime(),
            item.getEndTime(),
            item.getReserve(),
            item.getBids(),
            item.getImage(),
            item.getThumbnail()
        );
    }
}
