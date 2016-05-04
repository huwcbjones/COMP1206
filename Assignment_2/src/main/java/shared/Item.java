package shared;

import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Represents an Item for client.
 *
 * @author Huw Jones
 * @since 24/03/2016
 */
public class Item implements Serializable {

    protected final UUID itemID;
    protected final UUID userID;
    protected final String title;
    protected final String description;
    protected final HashSet<String> keywords;
    protected final Timestamp startTime;
    protected final Timestamp endTime;
    protected final BigDecimal reservePrice;
    protected final ArrayList<? extends Bid> bids;
    protected final BufferedImage image;
    protected Bid topBid;

    public Item(UUID itemID, UUID userID, String title, String description, Set<String> keywords, Timestamp startTime, Timestamp endTime, BigDecimal reservePrice, ArrayList<? extends Bid> bids, BufferedImage image) {
        this.itemID = itemID;
        this.userID = userID;
        this.title = title;
        this.description = description;
        this.keywords = new HashSet<>(keywords);
        this.startTime = startTime;
        this.endTime = endTime;
        this.reservePrice = reservePrice;
        this.bids = bids;
        this.image = image;
        this.setTopBid();
    }

    private void setTopBid() {
        for (Bid b : this.bids) {
            if (this.topBid == null) {
                this.topBid = b;
            } else if (this.topBid.compareTo(b) > 0) {
                this.topBid = b;
            }
        }
    }

    /**
     * Gets the UUID of the item
     *
     * @return UUID of item
     */
    public UUID getID() {
        return this.itemID;
    }

    /**
     * Gets the UUID of the User who placed this item for auction
     *
     * @return UUID of seller
     */
    public UUID getUserID() {
        return this.userID;
    }


    /**
     * Gets the item title
     *
     * @return title
     */
    public String getTitle() {
        return this.title;
    }

    /**
     * Returns an ArrayList of keywords for the item
     *
     * @return Keywords
     */
    public Set<String> getKeywords() {
        return this.keywords;
    }

    /**
     * Returns a CSV string of keywords
     *
     * @return String of keywords
     */
    public String getKeywordString() {
        return String.join(", ", keywords);
    }

    /**
     * Gets the top Bid
     *
     * @return Top Bid
     */
    public Bid getTopBid() {
        return this.topBid;
    }

    public ArrayList<? extends Bid> getBids() { return this.bids; }

    /**
     * Gets the number of bids on an item
     *
     * @return NUmber of bids
     */
    public int getNumberOfBids() {
        return this.bids.size();
    }

    /**
     * Gets the reserve price
     *
     * @return Reserve Price
     */
    public BigDecimal getReserve() {
        return this.reservePrice;
    }

    /**
     * Gets the reserve price formatted as a currency
     *
     * @return String, reserve price in format £x.xx
     */
    public String getReserveString() {
        return NumberFormat.getCurrencyInstance(Locale.UK).format(this.reservePrice);
    }

    /**
     * Gets the description
     *
     * @return Description of item
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * Gets the item's image
     *
     * @return BufferedImage, item image
     */
    public BufferedImage getImage() {
        return this.image;
    }

    /**
     * Gets the item's start time
     *
     * @return Start time
     */
    public Timestamp getStartTime() {
        return this.startTime;
    }

    public String getStartTimeString() {
        return getStartTimeString("HH:mm:ss dd/MM/yyyy");
    }

    public String getStartTimeString(String format) {
        return new SimpleDateFormat(format).format(this.getStartTime());
    }

    /**
     * Gets the item's end time
     *
     * @return End time
     */
    public Timestamp getEndTime() {
        return this.endTime;
    }

    public String getEndTimeString() {
        return getEndTimeString("HH:mm:ss dd/MM/yyyy");
    }

    public String getEndTimeString(String format) {
        return new SimpleDateFormat(format).format(this.getEndTime());
    }

    /**
     * Returns the number of seconds until the auction is due to start.
     * Or 0, if the auction should have already started
     *
     * @return Number of seconds
     */
    public long getTimeUntilStart() {
        // Get difference between now and start time
        Timestamp now = new Timestamp(new Date().getTime());
        long difference = this.startTime.getTime() - now.getTime();

        // return difference, or 0 if difference < 0
        return (difference < 0) ? 0 : difference;
    }

    /**
     * Returns the number of seconds until the auction is due to end.
     * Or 0, if the auction should have already ended
     *
     * @return Number of seconds
     */
    public long getTimeUntilEnd() {
        // Get difference between now and start time
        Timestamp now = new Timestamp(new Date().getTime());
        long difference = this.endTime.getTime() - now.getTime();

        // return difference, or 0 if difference < 0
        return (difference < 0) ? 0 : difference;
    }

    /**
     * Returns whether the auction has ended or not
     *
     * @return True if the auction has ended
     */
    public boolean isAuctionEnded() {
        return this.getTimeUntilStart() == 0 && this.getTimeUntilEnd() == 0;
    }

    /**
     * Creates an ItemBuilder instance
     *
     * @return A new ItemBuilder instance.
     */
    public static ItemBuilder createBuilder() {
        return new ItemBuilder();
    }
}
