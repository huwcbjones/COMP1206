package shared;

import java.io.Serializable;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

/**
 * User Request Packet
 *
 * @author Huw Jones
 * @since 13/05/2016
 */
public class UserRequest implements Serializable {

    private static final long serialUID = 50349L;

    private static final AtomicLong requestIDs = new AtomicLong();
    private final long requestID;

    private final UUID userID;
    private User user;

    public UserRequest(UUID userID){
        this.userID = userID;
        this.requestID = requestIDs.getAndIncrement();
    }

    public UserRequest(UUID userID, long requestID, User user){
        this.userID = userID;
        this.requestID = requestID;
        this.user = user;
    }

    public UUID getUserID() {
        return userID;
    }

    public long getRequestID() {
        return requestID;
    }

    public User getUser() {
        return user;
    }
}
