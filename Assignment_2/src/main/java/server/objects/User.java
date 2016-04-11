package server.objects;

import server.exceptions.OperationFailureException;
import shared.exceptions.InvalidCredentialException;
import shared.exceptions.PasswordsDoNotMatchException;
import shared.exceptions.ValidationFailedException;
import shared.utils.StringUtils;
import shared.utils.ValidationUtils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Random;
import java.util.UUID;

/**
 * Server side user
 * For doing server side user actions
 * If a user's details are going to be sent to the client, use a shared.User
 *
 * @author Huw Jones
 * @since 26/03/2016
 */
public final class User extends shared.User {
    private final byte[] passwordHash;
    private final byte[] salt;
    private boolean isLoggedIn = false;

    public User(UUID uniqueID, String username, String firstName, String lastName, byte[] passwordHash, byte[] salt) {
        super(uniqueID, username, firstName, lastName);
        this.passwordHash = passwordHash;
        this.salt = salt;
    }

    public User(String username, String firstName, String lastName, byte[] passwordHash, byte[] salt) {
        super(username, firstName, lastName);
        this.passwordHash = passwordHash;
        this.salt = salt;
    }

    /**
     * Returns whether a user is logged in or not
     *
     * @return True if user is logged in
     */
    public boolean isLoggedIn() {
        return this.isLoggedIn;
    }

    /**
     * Returns a shared user without the server side gubbins
     *
     * @return shared.User
     */
    public shared.User getSharedUser() {
        return new shared.User(this.getUniqueID(), this.getUsername(), this.getFirstName(), this.getLastName());
    }

    public boolean changePassword(char[] oldPassword, char[] newPassword, char[] confirm)
        throws OperationFailureException, InvalidCredentialException, PasswordsDoNotMatchException, ValidationFailedException {

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

        ValidationUtils.validatePassword(newPassword);

        return false;
    }

    public boolean isAuthenticated(char[] password) throws OperationFailureException {
        byte[] passwordHash = User.generatePasswordHash(password, this.salt);
        boolean returnValue = Arrays.equals(passwordHash, this.passwordHash);

        Arrays.fill(passwordHash, (byte) 0);
        Arrays.fill(password, '\u0000');

        return returnValue;
    }

    public void login(char[] password) throws InvalidCredentialException, OperationFailureException {
        if (!this.isAuthenticated(password)) {
            throw new InvalidCredentialException("Invalid username/password.");
        }
        this.isLoggedIn = true;
    }

    /**
     * Generates the hash for a password
     *
     * @param password Password to hash
     * @param salt     Salt to hash password with
     * @return Password hash
     * @throws OperationFailureException Thrown if the platform does not support the security requirements
     */
    public static byte[] generatePasswordHash(char[] password, byte[] salt) throws OperationFailureException {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");

            // Get the hash of the password
            byte[] passwordHash = messageDigest.digest(StringUtils.charsToBytes(password));

            // Clear the password (for security reasons)
            Arrays.fill(password, '\u0000');

            // Update the message digest with the salted password hash
            byte[] saltedHash = saltPassword(passwordHash, salt);

            // Clear the passwordHash (for security reasons)
            Arrays.fill(passwordHash, (byte) 0);

            // Get the new hash
            messageDigest.reset();
            return messageDigest.digest(saltedHash);
        } catch (NoSuchAlgorithmException ex) {
            throw new OperationFailureException("Server configuration error. Security options not supported.");
        }
    }

    /**
     * Salts a password
     *
     * @param passwordHash Password hash that needs salting
     * @param salt         NaCl to use (sometimes uses KCl)
     * @return Salted password
     */
    public static byte[] saltPassword(byte[] passwordHash, byte[] salt) {
        byte[] saltedHash = new byte[passwordHash.length];
        // Combine the two hashes (as salt is generated from SHA-256 hash)
        for (int i = 0; i < saltedHash.length; i++) {
            saltedHash[i] = (byte) (passwordHash[i] ^ salt[i]);
        }

        // Clear out the array (#InfoSec)
        Arrays.fill(passwordHash, (byte) 0);
        return saltedHash;
    }

    /**
     * Creates a salt
     *
     * @return A bite of NaCL
     */
    public static byte[] getRandomSalt() {
        byte[] bytes = new byte[32];
        new Random().nextBytes(bytes);
        return bytes;
    }
}
