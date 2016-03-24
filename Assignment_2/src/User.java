import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.UUID;

/**
 * Represents a User
 *
 * @author Huw Jones
 * @since 24/03/2016
 */
public class User {
    private UUID userID;
    private String firstName;
    private String lastName;
    private byte[] password;

    public User(String firstName, String lastName, byte[] password){
        this.firstName = StringUtils.CapitaliseString(firstName);
        this.lastName = StringUtils.CapitaliseString(lastName);
        this.password = password;
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

    public boolean isAuthenticated(String password) throws UnsupportedSecurityException {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.update(password.getBytes("UTF-8"));
            byte[] passwordHash = messageDigest.digest();

            // Have to use Arrays.equals instead of == as arrays are objects
            return Arrays.equals(this.password, passwordHash);
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            throw UnsupportedSecurityException.FromException(e);
        }
    }
}
