package shared;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * Represents a set of search options
 *
 * @author Huw Jones
 * @since 09/05/2016
 */
public class SearchOptions implements Serializable {

    private static final long serialUID = 3456L;

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

    private final Sort sort;
    private final Direction direction;
    private final String string;
    private final Timestamp startTime;
    private final Timestamp endTime;
    private final BigDecimal reserve;
    private final boolean noBids;

    public SearchOptions(Sort sort, Direction direction, String string, Timestamp startTime, Timestamp endTime, BigDecimal reserve, boolean noBids) {
        this.sort = sort;
        this.direction = direction;
        this.string = string;
        this.startTime = startTime;
        this.endTime = endTime;
        this.reserve = reserve;
        this.noBids = noBids;
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
}
