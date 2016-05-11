package shared;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Represents an Item for client.
 *
 * @author Huw Jones
 * @since 24/03/2016
 */
public class Item implements Serializable {

    public static final int AUCTION_NOT_STARTED = 0;
    public static final int AUCTION_STARTED = 1;
    public static final int AUCTION_WON = 2;
    public static final int AUCTION_NO_WINNER = 3;

    protected final UUID itemID;
    protected final UUID userID;
    protected final String title;
    protected final String description;
    protected final HashSet<Keyword> keywords;
    protected final Timestamp startTime;
    protected final Timestamp endTime;
    protected final BigDecimal reservePrice;
    protected final HashMap<UUID, Bid> bids =  new HashMap<>();
    protected final byte[] image;
    protected final byte[] thumbnail;
    protected Bid topBid;

    public Item(UUID itemID, UUID userID, String title, String description, Set<Keyword> keywords, Timestamp startTime, Timestamp endTime, BigDecimal reservePrice, ArrayList<Bid> bids, BufferedImage image, BufferedImage thumbnail) {
        this.itemID = itemID;
        this.userID = userID;
        this.title = title;
        this.description = description;
        this.keywords = new HashSet<>(keywords);
        this.startTime = startTime;
        this.endTime = endTime;
        this.reservePrice = reservePrice;
        bids.stream().forEach(bid -> this.bids.put(bid.getID(), bid));
        this.image = this.imageToBytes(image);
        this.thumbnail = this.imageToBytes(thumbnail);
        this.setTopBid();
    }

    private void setTopBid() {
        for (Bid b : this.bids.values()) {
            if (this.topBid == null) {
                this.topBid = b;
            } else if (this.topBid.compareTo(b) < 0) {
                this.topBid = b;
            }
        }
    }

    /**
     * Converts a BufferedImage (not serializable) to a byte array (serializable)
     *
     * @param image Buffered Image
     * @return byte array
     */
    private byte[] imageToBytes(BufferedImage image) {
        if (image == null) {
            return new byte[0];
        }
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", output);
            output.flush();
            return output.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return new byte[0];
        }
    }

    /**
     * Converts a byte array (serializable) to a BufferedImage (not serializable)
     *
     * @param bytes byte array
     * @return Buffered Image
     */
    private BufferedImage bytesToImage(byte[] bytes) {
        try (ByteArrayInputStream input = new ByteArrayInputStream(bytes)) {
            return ImageIO.read(input);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Gets the UUID of the item
     *
     * @return UUID of item
     */
    public UUID getID() {
        return this.itemID;
    }

    /**
     * Gets the UUID of the User who placed this item for auction
     *
     * @return UUID of seller
     */
    public UUID getUserID() {
        return this.userID;
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
    public Set<Keyword> getKeywords() {
        return this.keywords;
    }

    /**
     * Returns a CSV string of keywords
     *
     * @return String of keywords
     */
    public String getKeywordString() {
        List<String> keywords = new ArrayList<>(this.keywords.size());
        // Stream, keywords, select toString value, collect into a list, add to keywords
        keywords.addAll(this.keywords.stream().filter(item -> item != null).map(Keyword::toString).collect(Collectors.toList()));
        return String.join(", ", keywords);
    }

    /**
     * Gets the top Bid
     *
     * @return Top Bid
     */
    public Bid getTopBid() {
        return this.topBid;
    }

    public ArrayList<Bid> getBids() {
        return new ArrayList<>(this.bids.values());
    }

    public Bid getBid(UUID bidID){
        if(this.bids.containsKey(bidID)){
            return this.bids.get(bidID);
        } else {
            return null;
        }
    }

    /**
     * Gets the number of bids on an item
     *
     * @return NUmber of bids
     */
    public int getNumberOfBids() {
        return this.bids.size();
    }

    /**
     * Gets the reserve price
     *
     * @return Reserve Price
     */
    public BigDecimal getReserve() {
        return this.reservePrice;
    }

    /**
     * Gets the reserve price formatted as a currency
     *
     * @return String, reserve price in format £x.xx
     */
    public String getReserveString() {
        return NumberFormat.getCurrencyInstance(Locale.UK).format(this.reservePrice);
    }

    /**
     * Gets the description
     *
     * @return Description of item
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * Gets the item's image
     *
     * @return BufferedImage, item image
     */
    public BufferedImage getImage() {
        return this.bytesToImage(this.image);
    }

    /**
     * Gets the item's thumbanil image
     *
     * @return BufferedImage, thumbanil image
     */
    public BufferedImage getThumbnail() {
        return this.bytesToImage(this.thumbnail);
    }

    /**
     * Gets the item's start time
     *
     * @return Start time
     */
    public Timestamp getStartTime() {
        return this.startTime;
    }

    public String getStartTimeString() {
        return getStartTimeString("HH:mm:ss dd/MM/yyyy");
    }

    public String getStartTimeString(String format) {
        return new SimpleDateFormat(format).format(this.getStartTime());
    }

    /**
     * Gets the item's end time
     *
     * @return End time
     */
    public Timestamp getEndTime() {
        return this.endTime;
    }

    public String getEndTimeString() {
        return getEndTimeString("HH:mm:ss dd/MM/yyyy");
    }

    public String getEndTimeString(String format) {
        return new SimpleDateFormat(format).format(this.getEndTime());
    }

    /**
     * Returns the number of seconds until the auction is due to start.
     * Or 0, if the auction should have already started
     *
     * @return Number of seconds
     */
    public long getTimeUntilStart() {
        // Get difference between now and start time
        Timestamp now = new Timestamp(new Date().getTime());
        long difference = this.startTime.getTime() - now.getTime();

        // return difference, or 0 if difference < 0
        return (difference < 0) ? 0 : difference;
    }

    /**
     * Returns the number of seconds until the auction is due to end.
     * Or 0, if the auction should have already ended
     *
     * @return Number of seconds
     */
    public long getTimeUntilEnd() {
        // Get difference between now and start time
        Timestamp now = new Timestamp(new Date().getTime());
        long difference = this.endTime.getTime() - now.getTime();

        // return difference, or 0 if difference < 0
        return (difference < 0) ? 0 : difference;
    }

    /**
     * Returns whether the auction has ended or not
     *
     * @return True if the auction has ended
     */
    public boolean isAuctionEnded() {
        return this.getTimeUntilStart() == 0 && this.getTimeUntilEnd() == 0;
    }

    /**
     * Returns an int representing auction status
     *
     * @return Int
     */
    public int getAuctionStatus() {
        if (this.getTimeUntilStart() != 0) {
            return AUCTION_NOT_STARTED;
        } else if (this.getTimeUntilStart() == 0 && this.getTimeUntilEnd() != 0) {
            return AUCTION_STARTED;
        } else {
            if (this.bids.size() == 0 || this.getTopBid().getPrice().compareTo(this.getReserve()) < 0) {
                return AUCTION_NO_WINNER;
            } else {
                return AUCTION_WON;
            }
        }
    }

    /**
     * Creates an ItemBuilder instance
     *
     * @return A new ItemBuilder instance.
     */
    public static ItemBuilder createBuilder() {
        return new ItemBuilder();
    }
}
