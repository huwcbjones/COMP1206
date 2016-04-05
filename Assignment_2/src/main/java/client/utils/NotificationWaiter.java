package client.utils;

import client.Client;

/**
 * Allows waiting for a response from server
 *
 * @author Huw Jones
 * @since 05/04/2016
 */
public class NotificationWaiter extends shared.utils.NotificationWaiter {
    @Override
    public void waitForReply() {
        super.waitForReply(Client.getConfig().getTimeout());
    }
}
