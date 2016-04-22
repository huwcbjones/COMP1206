package shared;

import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Locale;
import java.util.UUID;

/**
 * Represents an shared.Item for client.
 *
 * @author Huw Jones
 * @since 24/03/2016
 */
public class Item implements Serializable {

    private final UUID itemID;
    private final UUID userID;
    private final String title;
    private final String description;
    private final HashSet<String> keywords;
    private final Timestamp startTime;
    private final Timestamp endTime;
    private final BigDecimal reservePrice;
    private final ArrayList<Bid> bids;
    private final BufferedImage image;
    private Bid topBid;

    public Item(UUID itemID, UUID userID, String title, String description, HashSet<String> keywords, Timestamp startTime, Timestamp endTime, BigDecimal reservePrice, ArrayList<Bid> bids, BufferedImage image) {
        this.itemID = itemID;
        this.userID = userID;
        this.title = title;
        this.description = description;
        this.keywords = keywords;
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
    public ArrayList<String> getKeywords() {
        // Maintain immutability by returning a separate ArrayList instance
        return new ArrayList<>(keywords);
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

    /**
     * Gets the number of bids on an item
     *
     * @return NUmber of bids
     */
    public int getNumberOfBids() {
        return this.bids.size();
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
     * Creates an ItemBuilder instance
     *
     * @return A new ItemBuilder instance.
     */
    public static ItemBuilder createBuilder() {
        return new ItemBuilder();
    }
}
