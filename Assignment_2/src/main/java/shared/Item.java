package shared;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
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

}
