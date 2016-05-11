package server.tasks;

import server.Server;
import server.ServerComms.ClientConnection;
import server.exceptions.OperationFailureException;
import server.objects.User;
import shared.Packet;
import shared.PacketType;
import shared.exceptions.InvalidCredentialException;
import shared.exceptions.ValidationFailedException;
import shared.utils.ValidationUtils;

import java.util.NoSuchElementException;

/**
 * Logs in a user, or not
 *
 * @author Huw Jones
 * @since 11/04/2016
 */
public class LoginTask extends Task {

    private final char[][] details;

    public LoginTask(ClientConnection client, char[][] details) {
        super("Login", client);
        this.details = details;
    }

    @Override
    public void runSafe() {
        if (this.details.length != 2) {
            this.client.sendPacket(new Packet<>(PacketType.LOGIN_FAIL, "A client/server error occurred during login."));
            return;
        }

        String username = new String(this.details[0]);
        char[] password = this.details[1];

        if (username.length() == 0) {
            log.trace("Username was blank.");
            this.client.sendPacket(LoginTask.invalidCredentials());
            return;
        }
        try {
            ValidationUtils.validatePassword(password, true);
        } catch (ValidationFailedException e) {
            this.client.sendPacket(LoginTask.invalidCredentials());
            log.trace("Password failed validation.");
            return;
        }

        try {
            User user = Server.getData().getUser(username);
            if (user != null) {
                log.debug("Authenticating user...");
                user.login(password, this.client);
                log.info("User({}) logged in on Client({})", user.getUniqueID(), this.client.getClientID());
                this.client.sendPacket(new Packet<>(PacketType.LOGIN_SUCCESS, user.getSharedUser()));
                return;
            }
            log.warn("Failed to load user. Is something wrong?");
        } catch (NoSuchElementException | InvalidCredentialException | OperationFailureException e) {
            log.warn("User({}) failed to login. {}", username, e.getMessage());
        }
        this.client.sendPacket(LoginTask.invalidCredentials());
    }

    /**
     * Executed on task failure
     */
    @Override
    protected void failureAction() {
        this.client.sendPacket(LoginTask.invalidCredentials());
    }

    private static Packet invalidCredentials() {
        return new Packet<>(PacketType.LOGIN_FAIL, "Invalid username/password.");
    }
}
