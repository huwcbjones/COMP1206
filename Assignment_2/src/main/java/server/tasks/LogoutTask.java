package server.tasks;

import server.ServerComms.ClientConnection;
import shared.Packet;

/**
 * Logout Task
 *
 * @author Huw Jones
 * @since 24/04/2016
 */
public class LogoutTask extends Task {

    public LogoutTask(ClientConnection client) {
        super("LogoutTask", client);
    }

    /**
     * Executed on task failure
     */
    @Override
    protected void failureAction() {
        this.client.sendPacket(Packet.Logout());
    }

    @Override
    public void runSafe() throws Exception {
        this.client.getUser().logout();
    }
}
