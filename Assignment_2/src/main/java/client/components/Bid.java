package client.components;

import client.Client;
import shared.Packet;
import shared.PacketType;
import shared.User;
import shared.utils.ReplyWaiter;
import shared.utils.UUIDUtils;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.UUID;

/**
 * Client Bid
 *
 * @author Huw Jones
 * @since 12/05/2016
 */
public class Bid extends shared.Bid {
    private User user = null;

    public Bid(UUID bidID, UUID itemID, UUID userID, BigDecimal bidPrice, Timestamp time) {
        super(bidID, itemID, userID, bidPrice, time);
    }
    public Bid(shared.Bid bid){
        this(bid.getID(), bid.getItemID(), bid.getUserID(), bid.getPrice(), bid.getTime());
    }

    public String getUserString(){
        if(Client.getUser().getUniqueID().equals(this.getUserID())){
            return "You";
        } else {
            ReplyWaiter handler = new ReplyWaiter(Client.getConfig().getTimeout()) {
                @Override
                public void packetReceived(Packet packet) {
                    switch (packet.getType()) {
                        case USER:
                            User user = (User)packet.getPayload();
                            if(Bid.this.getUserID().equals(user.getUniqueID())){
                                this.waiter.replyReceived();
                                Bid.this.user = user;
                            }
                            break;
                    }
                }
            };
            Client.addPacketListener(handler);
            Client.sendPacket(new Packet<>(PacketType.FETCH_USER, getUserID()));

            handler.getWaiter().waitForReply();
            Client.removePacketListener(handler);

            if (handler.getWaiter().isReplyTimedOut() && user == null) {
                return UUIDUtils.UUIDToBase64String(this.getUserID());
            } else {
                return this.user.getUsername();
            }
        }
    }
}
