package shared;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.UUID;

/**
 * Represents a set of search options
 *
 * @author Huw Jones
 * @since 09/05/2016
 */
public class SearchOptions implements Serializable {

    private static final long serialUID = 3456L;
    private final Sort sort;
    private final Direction direction;
    private final String string;
    private final UUID sellerID;
    private final Keyword keyword;
    private final Timestamp startTime;
    private final Timestamp endTime;
    private final BigDecimal reserve;
    private final boolean noBids;
    private final boolean includeClosed;

    public SearchOptions(Sort sort, Direction direction, String string, UUID sellerID, Keyword keyword, Timestamp startTime, Timestamp endTime, BigDecimal reserve, boolean noBids, boolean includeClosed) {
        this.sort = sort;
        this.direction = direction;
        this.string = string;
        this.sellerID = sellerID;
        this.keyword = keyword;
        this.startTime = startTime;
        this.endTime = endTime;
        this.reserve = reserve;
        this.noBids = noBids;
        this.includeClosed = includeClosed;

    }

    public boolean isIncludeClosed() {
        return includeClosed;
    }

    public UUID getSellerID() {
        return sellerID;
    }

    public Sort getSort() {
        return sort;
    }

    public Direction getDirection() {
        return direction;
    }

    public boolean isNoBids() {
        return noBids;
    }

    public BigDecimal getReserve() {
        return reserve;
    }

    public Timestamp getEndTime() {
        return endTime;
    }

    public Timestamp getStartTime() {
        return startTime;
    }

    public String getString() {
        return string;
    }

    public Keyword getKeyword() {
        return keyword;
    }

    public enum Sort {
        BID,
        NUM_BIDS,
        TIME,
        RESERVE
    }

    public enum Direction {
        ASC,
        DESC
    }
}
