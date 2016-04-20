package shared;

import java.io.Serializable;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.UUID;

/**
 * Represents a shared.Bid on an shared.Item
 *
 * @author Huw Jones
 * @since 24/03/2016
 */
public class Bid implements Serializable, Comparable<Bid> {

    private BigDecimal bidPrice;
    private UUID itemID;
    private UUID userID;

    @Override
    public int compareTo(Bid o) {
        if (o == null) throw new NullPointerException();
        return this.bidPrice.compareTo(o.bidPrice);
    }

    public String getPriceString() {
        return NumberFormat.getCurrencyInstance(Locale.UK).format(this.bidPrice);
    }

    public BigDecimal getPrice() {
        return this.bidPrice;
    }
}
