package shared.utils;

import shared.events.PacketListener;

/**
 * Abstract class that provides Notification Waiter on top of a Packet Listener
 *
 * @author Huw Jones
 * @since 04/04/2016
 */
public abstract class ReplyWaiter implements PacketListener {
    protected final NotificationWaiter waiter;

    public ReplyWaiter(NotificationWaiter waiter){
        this.waiter = waiter;
    }
}
