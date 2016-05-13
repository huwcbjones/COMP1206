package shared;

import shared.utils.StringUtils;

import java.io.Serializable;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.UUID;

/**
 * Represents a shared.User
 *
 * @author Huw Jones
 * @since 24/03/2016
 */
public class User implements Serializable {

    public static final long serialUID = 1L;

    private final String username;
    private final String firstName;
    private final String lastName;
    private final UUID uniqueID;
    private final Timestamp joined;

    public User(UUID uniqueID, String username, String firstName, String lastName, Timestamp joined) {
        this.uniqueID = uniqueID;
        this.username = username;
        this.firstName = StringUtils.CapitaliseString(firstName);
        this.lastName = StringUtils.CapitaliseString(lastName);
        this.joined = joined;
    }
    public User(String username, String firstName, String lastName, Timestamp joined) {
        this(UUID.nameUUIDFromBytes(username.getBytes()), username, firstName, lastName, joined);
    }

    public String getUsername() {
        return this.username;
    }

    public String getFirstName() {
        return this.firstName;
    }

    public String getLastName() {
        return this.lastName;
    }

    public String getFullName() {
        return this.firstName + " " + this.lastName;
    }

    public UUID getUniqueID() {
        return this.uniqueID;
    }

    public Timestamp getJoined() { return this.joined; }

    public String getJoinedString() { return this.getJoinedString("dd/MM/yyyy"); }

    public String getJoinedString(String format){
        return new SimpleDateFormat(format).format(this.getJoined());
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof  User)){
            return false;
        }
        User user = (User)obj;
        return user.getUniqueID().equals(this.uniqueID);
    }
}
