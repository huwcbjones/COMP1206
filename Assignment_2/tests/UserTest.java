import java.security.MessageDigest;

import static org.junit.Assert.*;

/**
 * {DESCRIPTION}
 *
 * @author Huw Jones
 * @since 24/03/2016
 */
public class UserTest {

    @org.junit.Test
    public void isAuthenticated() throws Exception {
        String password = "testPassword";

        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(password.getBytes("UTF-8"));
        byte[] pwd = md.digest();
        User testUser = new User("Test", "User", pwd);
        assertTrue("Passwords don't match", testUser.isAuthenticated(password));
    }
}