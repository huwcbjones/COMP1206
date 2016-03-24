import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * Represents a Bid on an Item
 *
 * @author Huw Jones
 * @since 24/03/2016
 */
public class Bid implements Serializable {

    private BigDecimal bidPrice;
    private UUID itemID;
    private UUID userID;
}
