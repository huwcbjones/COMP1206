package shared;

import java.io.Serializable;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.UUID;

/**
 * Represents an shared.Item for client.
 *
 * @author Huw Jones
 * @since 24/03/2016
 */
public class Item implements Serializable {

    private UUID itemID;
    private UUID userID;
    private String title;
    private String description;
    private ArrayList<String> keywords;
    private Calendar startTime;
    private Calendar endTime;
    private BigDecimal reservePrice;
    private ArrayList<Bid> bids;
    private Bid topBid;


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
}
