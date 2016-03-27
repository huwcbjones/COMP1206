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
import java.util.Random;

/**
 * Server side user
 * For doing server side user actions
 * If a user's details are going to be sent to the client, use a shared.User
 *
 * @author Huw Jones
 * @since 26/03/2016
 */
final class User extends shared.User {
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

        Arrays.fill(passwordHash, (byte) 0);
        Arrays.fill(password, '\u0000');

        return returnValue;
    }

    public boolean changePassword (char[] oldPassword, char[] newPassword, char[] confirm)
            throws UnsupportedSecurityException, InvalidCredentialException, PasswordsDoNotMatchException, PasswordNotStrongEnoughException {

        // Check old password is correct
        if (!isAuthenticated(oldPassword)) {
            Arrays.fill(oldPassword, '\u0000');
            throw new InvalidCredentialException("Current password is incorrect.");
        }

        if (!Arrays.equals(newPassword, oldPassword)) {
            Arrays.fill(newPassword, '\u0000');
            Arrays.fill(confirm, '\u0000');
            throw new PasswordsDoNotMatchException();
        }

        if (!ValidationUtils.validatePassword(newPassword)) {
            // An exception should be triggered here anyway
            return false;
        }


        return false;
    }

    /**
     * Generates the hash for a password
     * @param password Password to hash
     * @param salt Salt to hash password with
     * @return Password hash
     * @throws UnsupportedSecurityException Thrown if the platform does not support the security requirements
     */
    public static byte[] generatePasswordHash (char[] password, byte[] salt) throws UnsupportedSecurityException {
        try {
            // Get a message digest instance of SHA-256
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");

            // Update the message digest with the password char->bytes
            messageDigest.update(StringUtils.charsToBytes(password));

            // Get the hash of the password
            byte[] passwordHash = messageDigest.digest();

            // Update the message digest with the salted password hash
            messageDigest.update(saltPassword(passwordHash, salt));

            // Clear the passwordHash (for security reasons)
            Arrays.fill(passwordHash, (byte) 0);

            // Get the new hash
            return messageDigest.digest();
        } catch (NoSuchAlgorithmException e) {
            throw UnsupportedSecurityException.FromException(e);
        }
    }

    /**
     * Creates a salt
     * @return A bite of NaCL
     * @throws UnsupportedSecurityException Thrown if platform doesn't support security requirements
     */
    public static byte[] getRandomSalt() throws UnsupportedSecurityException {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");

            // Create some random bytes
            byte[] randomBytes = new byte[32];
            new Random().nextBytes(randomBytes);

            // Hash the random bytes
            md.update(randomBytes);

            // Return a salt
            return md.digest();
        } catch (NoSuchAlgorithmException e) {
            throw UnsupportedSecurityException.FromException(e);
        }
    }

    /**
     * Salts a password
     * @param passwordHash Password hash that needs salting
     * @param salt NaCl to use (sometimes uses KCl)
     * @return Salted password
     */
    private static byte[] saltPassword (byte[] passwordHash, byte[] salt) {
        byte[] saltedHash = new byte[passwordHash.length];

        // Combine the two hashes (as salt is generated from SHA-256 hash)
        for (int i = 0; i < saltedHash.length; i++) {
            saltedHash[i] = (byte) ( passwordHash[i] ^ salt[i] );
        }

        // Clear out the array (#InfoSec)
        Arrays.fill(passwordHash, (byte) 0);
        return saltedHash;
    }
}
