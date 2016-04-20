package shared;

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
    private Bid topBid;

    public Item(UUID itemID, UUID userID, String title, String description, HashSet<String> keywords, Timestamp startTime, Timestamp endTime, BigDecimal reservePrice, ArrayList<Bid> bids) {
        this.itemID = itemID;
        this.userID = userID;
        this.title = title;
        this.description = description;
        this.keywords = keywords;
        this.startTime = startTime;
        this.endTime = endTime;
        this.reservePrice = reservePrice;
        this.bids = bids;
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

    public Bid getTopBid() {
        return this.topBid;
    }

    public int getNumberOfBids() {
        return this.bids.size();
    }

    public String getReserveString() {
        return NumberFormat.getCurrencyInstance(Locale.UK).format(this.reservePrice);
    }

    public String getDescription() {
        return this.description;
    }

    public static ItemBuilder createBuilder(){
        return new ItemBuilder();
    }
}
