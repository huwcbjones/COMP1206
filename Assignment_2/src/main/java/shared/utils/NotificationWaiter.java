package shared.utils;

/**
 * Allows waiting for a response from server/client
 *
 * @author Huw Jones
 * @since 31/03/2016
 */
public class NotificationWaiter {

    private final Object waitForReply = new Object();
    private boolean replyTimedOut = true;

    /**
     * Unblocks a waitForReply
     */
    public void replyReceived() {
        // Change reply timed out to false as we didn't time out
        this.replyTimedOut = false;
        synchronized (this.waitForReply) {
            this.waitForReply.notify();
        }
    }

    public void waitForReply(int timeout) {
        synchronized (this.waitForReply) {
            try {
                // Change reply timed out to true, if we received a reply, the handling code should change this to false
                this.replyTimedOut = true;
                this.waitForReply.wait(timeout);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean isReplyTimedOut() {
        return this.replyTimedOut;
    }
}
