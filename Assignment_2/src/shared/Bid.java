package shared;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * Represents a shared.Bid on an shared.Item
 *
 * @author Huw Jones
 * @since 24/03/2016
 */
public class Bid implements Serializable {

    private BigDecimal bidPrice;
    private UUID itemID;
    private UUID userID;
}
