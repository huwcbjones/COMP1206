package server.utils;

import server.Server;

/**
 * Allows waiting for a response from client
 *
 * @author Huw Jones
 * @since 05/04/2016
 */
public class NotificationWaiter extends shared.utils.NotificationWaiter {
    @Override
    public void waitForReply() {
        super.waitForReply(Server.getConfig().getTimeout());
    }
}
