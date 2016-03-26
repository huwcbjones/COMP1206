package server;

import shared.exceptions.InvalidCredentialException;
import shared.exceptions.PasswordNotStrongEnoughException;
import shared.exceptions.PasswordsDoNotMatchException;
import shared.exceptions.UnsupportedSecurityException;
import shared.utils.StringUtils;
import shared.utils.ValidationUtils;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

/**
 * Server side user
 * For doing server side user actions
 * If a user's details are going to be sent to the client, use a shared.User
 *
 * @author Huw Jones
 * @since 26/03/2016
 */
public class User extends shared.User {
    private byte[] passwordHash;
    private byte[] salt;
    private boolean isLoggedIn = false;

    public User (String firstName, String lastName, byte[] passwordHash, byte[] salt) {
        super(firstName, lastName);
        this.passwordHash = passwordHash;
        this.salt = salt;
    }

    public boolean isAuthenticated (char[] password) throws UnsupportedSecurityException {
        byte[] passwordHash = User.generatePasswordHash(password, this.salt);
        boolean returnValue = Arrays.equals(passwordHash, this.passwordHash);

        Arrays.fill(passwordHash, (byte)0);
        Arrays.fill(password, '\u0000');

        return returnValue;
    }

    public boolean changePassword (char[] oldPassword, char[] newPassword, char[] confirm)
            throws UnsupportedSecurityException, InvalidCredentialException, PasswordsDoNotMatchException, PasswordNotStrongEnoughException {

        // Check old password is correct
        if(!isAuthenticated(oldPassword)){
            Arrays.fill(oldPassword, '\u0000');
            throw new InvalidCredentialException("Current password is incorrect.");
        }

        if(!Arrays.equals(newPassword, oldPassword)){
            Arrays.fill(newPassword, '\u0000');
            Arrays.fill(confirm, '\u0000');
            throw new PasswordsDoNotMatchException();
        }

        if(!ValidationUtils.validatePassword(newPassword)){
            // An exception should be triggered here anyway
            return false;
        }

        return false;
    }

    public static byte[] generatePasswordHash(char[] password, byte[] salt) throws UnsupportedSecurityException {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.update(StringUtils.charsToBytes(password));
            byte[] passwordHash = messageDigest.digest();

            messageDigest.update(saltPassword(passwordHash, salt));
            Arrays.fill(passwordHash, (byte)0);

            return messageDigest.digest();
        } catch (NoSuchAlgorithmException e) {
            throw UnsupportedSecurityException.FromException(e);
        }
    }

    private static byte[] saltPassword(byte[] passwordHash, byte[] salt){
        byte[] saltedHash = new byte[passwordHash.length];

        for(int i = 0; i < saltedHash.length; i++){
            saltedHash[i] = (byte)(passwordHash[i] ^ salt[i]);
        }
        Arrays.fill(passwordHash, (byte) 0);
        return saltedHash;
    }
}
