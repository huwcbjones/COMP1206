package shared.exceptions;

/**
 * Thrown when the operation could not complete because the client is closed
 *
 * @author Huw Jones
 * @since 24/03/2016
 */
public class AuctionClosedException extends Exception {
    public AuctionClosedException(){
        super("Operation failed, Auction is closed.");
    }
}
