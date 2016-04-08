package shared;

import shared.utils.StringUtils;

import java.io.Serializable;
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

    public User(UUID uniqueID, String username, String firstName, String lastName) {
        this.uniqueID = uniqueID;
        this.username = username;
        this.firstName = StringUtils.CapitaliseString(firstName);
        this.lastName = StringUtils.CapitaliseString(lastName);
    }
    public User(String username, String firstName, String lastName) {
        this(UUID.nameUUIDFromBytes(username.getBytes()), username, firstName, lastName);
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
}
