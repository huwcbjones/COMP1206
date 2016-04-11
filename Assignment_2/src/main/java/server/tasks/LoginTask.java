package server.tasks;

import server.ClientConnection;
import server.Server;
import server.exceptions.OperationFailureException;
import server.objects.User;
import shared.Packet;
import shared.PacketType;
import shared.exceptions.InvalidCredentialException;

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
    public void run() {
        try {
            if (this.details.length != 2) {
                this.client.sendPacket(new Packet<>(PacketType.LOGIN_FAIL, "A client/server error occurred during login."));
                return;
            }
            String username = new String(this.details[0]);
            char[] password = this.details[1];

            try {
                User user = Server.getData().getUser(username);
                if (user != null) {
                    log.trace("Authenticating user...");
                    user.login(password);
                    log.info("{} ({}) logged in on Client #{}", user.getUsername(), user.getUniqueID(), this.client.getClientID());
                    this.client.sendPacket(new Packet<>(PacketType.LOGIN_SUCCESS, user.getSharedUser()));
                    return;
                }
                log.warn("Failed to load user. Is something wrong?");
            } catch (NoSuchElementException | InvalidCredentialException | OperationFailureException e) {
                log.warn("User ({}) failed to login. {}", username, e.getMessage());
            }
        } catch (Exception e){
            log.error("Error whilst processing login.", e);
        }
        this.client.sendPacket(new Packet<>(PacketType.LOGIN_FAIL, "Invalid username/password."));
    }
}
