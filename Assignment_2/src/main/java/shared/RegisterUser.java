package shared;

import java.io.Serializable;

/**
 * Object sent when registering users
 *
 * @author Huw Jones
 * @since 08/04/2016
 */
public class RegisterUser extends User implements Serializable {

    public static final long serialUID = 1L;

    private final char[] password;
    private final char[] passwordConfirm;

    public RegisterUser(User user, char[] password, char[] passwordConfirm) {
        super(user.getUsername(), user.getFirstName(), user.getLastName());
        this.password = password;
        this.passwordConfirm = passwordConfirm;
    }

    public RegisterUser(String username, String firstName, String lastName, char[] password, char[] passwordConfirm) {
        super(username, firstName, lastName);
        this.password = password;
        this.passwordConfirm = passwordConfirm;
    }

    public char[] getPassword() {
        return this.password;
    }

    public char[] getPasswordConfirm() {
        return this.passwordConfirm;
    }
}
