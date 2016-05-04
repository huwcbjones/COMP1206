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

    public ReplyWaiter(int timeout){
        this.waiter = new NotificationWaiter(){
            @Override
            public void waitForReply() {
                super.waitForReply(timeout);
            }
        };
    }

    public NotificationWaiter getWaiter(){
        return this.waiter;
    }

    public ReplyWaiter(NotificationWaiter waiter){
        this.waiter = waiter;
    }
}
