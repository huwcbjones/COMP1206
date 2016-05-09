package shared;

import org.junit.Test;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

/**
 * Item Test
 *
 * @author Huw Jones
 * @since 09/05/2016
 */
public class ItemTest {
    @Test
    public void getAuctionStatus() throws Exception {
        Calendar startTime = Calendar.getInstance();
        startTime.add(Calendar.SECOND, 1);

        Calendar endTime = Calendar.getInstance();
        endTime.add(Calendar.SECOND, 2);

        ItemBuilder builder = Item.createBuilder();
        builder
            .setID(UUID.randomUUID())
            .setUserID(UUID.randomUUID())
            .setTitle("Test Item")
            .setDescription("")
            .setReservePrice(new BigDecimal("10.00"))
            .setStartTime(new Timestamp(startTime.getTime().getTime()))
            .setEndTime(new Timestamp(endTime.getTime().getTime()));

        Item item = builder.getItem();

        assertEquals("Auction should not have started.", Item.AUCTION_NOT_STARTED, item.getAuctionStatus());

        Thread.sleep(1200);

        assertEquals("Auction should have started.", Item.AUCTION_STARTED, item.getAuctionStatus());

        Thread.sleep(1200);

        assertEquals("Auction should have closed with no winner.", Item.AUCTION_NO_WINNER, item.getAuctionStatus());
    }

}