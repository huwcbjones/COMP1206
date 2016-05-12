package shared;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.UUID;

/**
 * Represents a shared.Bid on an shared.Item
 *
 * @author Huw Jones
 * @since 24/03/2016
 */
public class Bid implements Serializable, Comparable<Bid> {

    private final UUID bidID;
    private final UUID itemID;
    private final UUID userID;
    private final BigDecimal bidPrice;
    private final Timestamp time;

    public Bid(UUID bidID, UUID itemID, UUID userID, BigDecimal bidPrice, Timestamp time) {
        this.bidID = bidID;
        this.itemID = itemID;
        this.userID = userID;
        this.bidPrice = bidPrice;
        this.time = time;
    }

    /**
     * Gets the UUID of this Bid
     *
     * @return UUID of bid
     */
    public UUID getID() {
        return bidID;
    }

    /**
     * Gets the UUID of the Item the bid was placed on
     *
     * @return UUID of Item
     */
    public UUID getItemID() {
        return itemID;
    }

    /**
     * Gets UUID of User that placed the bid
     *
     * @return UUID of buyer
     */
    public UUID getUserID() {
        return userID;
    }

    /**
     * Gets the time the bid was placed
     *
     * @return Time bid was placed
     */
    public Timestamp getTime() {
        return time;
    }

    public String getTimeString(){
        return getTimeString("HH:mm:ss dd/MM/yyyy");
    }

    public String getTimeString(String format) {
        return new SimpleDateFormat(format).format(this.getTime());
    }

    @Override
    public int compareTo(Bid o) {
        if (o == null) throw new NullPointerException();
        return this.bidPrice.compareTo(o.bidPrice);
    }

    /**
     * Gets the price as a currency formatted string
     *
     * @return Returns price as £x.xx
     */
    public String getPriceString() {
        return NumberFormat.getCurrencyInstance(Locale.UK).format(this.bidPrice);
    }

    /**
     * Gets the price of the bid
     *
     * @return Price of bid
     */
    public BigDecimal getPrice() {
        return this.bidPrice;
    }

}
