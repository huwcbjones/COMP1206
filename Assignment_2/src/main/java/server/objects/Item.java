package server.objects;

import server.Server;
import shared.Keyword;

import java.awt.image.BufferedImage;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.UUID;

/**
 * Represents
 *
 * @author Huw Jones
 * @since 23/04/2016
 */
public class Item extends shared.Item {
    public Item(UUID itemID, UUID userID, String title, String description, HashSet<Keyword> keywords, Timestamp startTime, Timestamp endTime, BigDecimal reservePrice, ArrayList<? extends Bid> bids, BufferedImage image) {
        super(itemID, userID, title, description, keywords, startTime, endTime, reservePrice, bids, image);
    }

    private Item(shared.Item item) {
        super(
            item.getID(),
            item.getUserID(),
            item.getTitle(),
            item.getDescription(),
            item.getKeywords(),
            item.getStartTime(),
            item.getEndTime(),
            item.getReserve(),
            item.getBids(),
            item.getImage()
        );
    }

    public User getUser() {
        return Server.getData().getUser(this.getUserID());
    }

    /**
     * Gets the top Bid
     *
     * @return Top Bid
     */
    @Override
    public Bid getTopBid() {
        return (Bid) super.getTopBid();
    }

    public static Item createServerItem(shared.Item item){
        return new Item(item);
    }
}
