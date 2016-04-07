package shared;

import shared.utils.StringUtils;

import java.util.UUID;

/**
 * Represents a shared.User
 *
 * @author Huw Jones
 * @since 24/03/2016
 */
public class User {
    private UUID userID;
    private final String firstName;
    private final String lastName;

    public User(String firstName, String lastName){
        this.firstName = StringUtils.CapitaliseString(firstName);
        this.lastName = StringUtils.CapitaliseString(lastName);
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public UUID getUserID(){
        return this.userID;
    }
}
