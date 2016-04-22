package server.tasks;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import server.ClientConnection;
import server.Server;
import server.exceptions.OperationFailureException;
import server.objects.User;
import shared.Packet;
import shared.PacketType;
import shared.RegisterUser;
import shared.exceptions.ValidationFailedException;
import shared.utils.UUIDUtils;
import shared.utils.ValidationUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.UUID;

/**
 * Processes user registration
 *
 * @author Huw Jones
 * @since 08/04/2016
 */
public class RegisterTask extends Task {

    private static final Logger log = LogManager.getLogger(RegisterTask.class);
    private RegisterUser user;

    public RegisterTask(ClientConnection client, RegisterUser user) {
        super("RegisterUser", client);
        this.user = user;
    }

    @Override
    protected void doTask() {
        log.trace("password: {} {}", user.getPassword(), user.getPasswordConfirm());
        if (Server.getData().userExists(this.user.getUsername())) {
            log.warn("User tried to register with an in-use username.");
            this.client.sendPacket(new Packet<>(PacketType.REGISTER_FAIL, "A user with that username already exists."));
            return;
        }
        log.trace("Username is available.");
        try {
            validateFields();
            log.debug("Input data validated.");
            UUID newUser = this.addUserToDatabase();
            log.info("New user ({}) added to database!", newUser);
            this.client.sendPacket(new Packet<>(PacketType.REGISTER_SUCCESS, Server.getData().getUser(newUser).getSharedUser()));
        } catch (ValidationFailedException | OperationFailureException e) {
            this.client.sendPacket(new Packet<>(PacketType.REGISTER_FAIL, e.getMessage()));
        }
    }

    @Override
    protected void failureAction() {
        this.client.sendPacket(new Packet<>(PacketType.REGISTER_FAIL, "Server encountered an error processing that request."));
    }

    private UUID addUserToDatabase() throws OperationFailureException {

        UUID uniqueID;
        do {
            uniqueID = UUID.randomUUID();
        }
        while (Server.getData().userExists(uniqueID));

        char[] password = user.getPassword();
        byte[] salt = User.getRandomSalt();
        byte[] passwordHash = User.generatePasswordHash(password, salt);
        byte[] password_bytes = new byte[64];
        System.arraycopy(passwordHash, 0, password_bytes, 0, 32);
        System.arraycopy(salt, 0, password_bytes, 32, 32);

        Connection c = null;
        PreparedStatement insertUser = null;
        String insertUserSql = "INSERT INTO users (userID, username, firstName, lastName, password) VALUES (?, ?, ?, ?, ?)";
        boolean wasSuccess;
        try {
            c = Server.getData().getConnection();
            c.setAutoCommit(false);
            insertUser = c.prepareStatement(insertUserSql);
            insertUser.setBytes(1, UUIDUtils.UUIDToBytes(uniqueID));
            insertUser.setString(2, user.getUsername());
            insertUser.setString(3, user.getFirstName());
            insertUser.setString(4, user.getLastName());
            insertUser.setBytes(5, password_bytes);
            insertUser.executeUpdate();
            c.commit();
            wasSuccess = true;
        } catch (SQLException e) {
            log.debug(e);
            log.debug("SQLState: {}", e.getSQLState());
            log.debug("Error Code: {}", e.getErrorCode());
            log.debug("Message: {}", e.getMessage());
            log.debug("Cause: {}", e.getCause());
            log.error("Failed to add user.");
            wasSuccess = false;
        } finally {
            try {
                if (insertUser != null) {
                    insertUser.close();
                }
                if (c != null) {
                    c.setAutoCommit(true);
                    c.close();
                }
            } catch (SQLException suppress) {
                log.trace(suppress);
            }
        }
        if (!wasSuccess) {
            throw new OperationFailureException("Failed to add user. A server error occurred.");
        }

        return uniqueID;

    }

    private void validateFields() throws ValidationFailedException {
        ValidationUtils.validateUsername(this.user.getUsername());
        ValidationUtils.validateName(this.user.getFirstName());
        ValidationUtils.validateName(this.user.getLastName());
        ValidationUtils.validatePassword(this.user.getPassword());
        if (!Arrays.equals(this.user.getPassword(), this.user.getPasswordConfirm())) {
            throw new ValidationFailedException("Passwords don't match.");
        }
    }
}
