package shared;

import java.awt.image.BufferedImage;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.*;

/**
 * Item Builder class
 *
 * @author Huw Jones
 * @since 20/04/2016
 */
public class ItemBuilder {
    private UUID itemID = null;
    private UUID userID = null;
    private String title = null;
    private String description = null;
    private HashSet<Keyword> keywords = new HashSet<>();
    private Timestamp startTime = null;
    private Timestamp endTime = null;
    private BigDecimal reservePrice = null;
    private ArrayList<Bid> bids = new ArrayList<>();
    private BufferedImage image = null;
    private BufferedImage thumbnail = null;

    public Item getItem() throws IllegalArgumentException {
        if (itemID == null) {
            throw new IllegalArgumentException("ItemID not set.");
        }
        if (userID == null) {
            throw new IllegalArgumentException("UserID not set.");
        }
        if (title == null) {
            throw new IllegalArgumentException("Title not set.");
        }
        if (description == null) {
            throw new IllegalArgumentException("Description not set.");
        }
        if (startTime == null) {
            startTime = new Timestamp(System.currentTimeMillis());
        }
        if (endTime == null) {
            throw new IllegalArgumentException("End time not set.");
        }
        if (reservePrice == null) {
            reservePrice = BigDecimal.ZERO;
        }
        return new Item(itemID, userID, title, description, keywords, startTime, endTime, reservePrice, bids, image, thumbnail);
    }

    public ItemBuilder setID(UUID ID) {
        this.itemID = ID;
        return this;
    }

    public ItemBuilder setUserID(UUID userID) {
        this.userID = userID;
        return this;
    }

    public ItemBuilder setTitle(String title) {
        this.title = title;
        return this;
    }

    public ItemBuilder setDescription(String description) {
        this.description = description;
        return this;
    }

    public ItemBuilder setStartTime(Timestamp startTime) {
        this.startTime = startTime;
        return this;
    }

    public ItemBuilder setEndTime(Timestamp endTime) {
        this.endTime = endTime;
        return this;
    }

    public ItemBuilder setReservePrice(BigDecimal reservePrice) {
        this.reservePrice = reservePrice;
        return this;
    }

    public ItemBuilder addKeyword(Keyword... keyword) {
        this.keywords.addAll(Arrays.asList(keyword));
        return this;
    }

    public ItemBuilder addAllKeywords(Keyword[] keywords) {
        this.keywords.addAll(Arrays.asList(keywords));
        return this;
    }

    public ItemBuilder addAllKeywords(List<Keyword> keywords) {
        this.keywords.addAll(keywords);
        return this;
    }

    public ItemBuilder removeKeyword(Keyword... keyword) {
        this.keywords.removeAll(Arrays.asList(keyword));
        return this;
    }

    public ItemBuilder removeAllKeywords(Keyword[] keywords) {
        this.keywords.removeAll(Arrays.asList(keywords));
        return this;
    }

    public ItemBuilder addBid(Bid bid) {
        this.bids.add(bid);
        return this;
    }

    public ItemBuilder addAllBids(Bid[] bids) {
        this.bids.addAll(Arrays.asList(bids));
        return this;
    }

    public ItemBuilder removeBid(Bid bid) {
        this.bids.remove(bid);
        return this;
    }

    public ItemBuilder removeAllBids(Bid[] bids) {
        this.bids.removeAll(Arrays.asList(bids));
        return this;
    }

    public ItemBuilder setImage(BufferedImage image) {
        this.image = image;
        return this;
    }

    public ItemBuilder setThumbnail(BufferedImage thumbnail) {
        this.thumbnail = thumbnail;
        return this;
    }
}
