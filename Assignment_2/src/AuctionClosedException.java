/**
 * Thrown when the operation could not complete because the auction is closed
 *
 * @author Huw Jones
 * @since 24/03/2016
 */
public class AuctionClosedException extends Exception {
    public AuctionClosedException(){
        super("Operation failed, Auction is closed.");
    }
}
