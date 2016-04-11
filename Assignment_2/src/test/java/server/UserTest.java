package server;

import server.objects.User;

import java.security.MessageDigest;

import static org.junit.Assert.assertTrue;

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
        md.update("blah".getBytes());
        byte[] salt = md.digest();

        byte[] pwd = User.generatePasswordHash(password.toCharArray(), salt);

        User testUser = new User("TestUser", "Test", "User", pwd, salt);
        assertTrue("Passwords don't match", testUser.isAuthenticated(password.toCharArray()));
    }
}