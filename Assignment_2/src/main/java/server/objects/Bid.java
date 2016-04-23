package server.objects;

import server.Server;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.UUID;

/**
 * Server instance of Bid
 *
 * @author Huw Jones
 * @since 23/04/2016
 */
public class Bid extends shared.Bid {
    public Bid(UUID bidID, UUID itemID, UUID userID, BigDecimal bidPrice, Timestamp time) {
        super(bidID, itemID, userID, bidPrice, time);
    }

    public User getUser(){
        return Server.getData().getUser(this.getUserID());
    }
}
