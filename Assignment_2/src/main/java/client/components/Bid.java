package client.components;

import client.Client;
import shared.Packet;
import shared.PacketType;
import shared.User;
import shared.UserRequest;
import shared.utils.ReplyWaiter;

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
    private long requestID;

    public Bid(UUID bidID, UUID itemID, UUID userID, BigDecimal bidPrice, Timestamp time) {
        super(bidID, itemID, userID, bidPrice, time);
    }
    public Bid(shared.Bid bid){
        this(bid.getID(), bid.getItemID(), bid.getUserID(), bid.getPrice(), bid.getTime());
    }

    public User getUser(){
        if(Client.getUser().getUniqueID().equals(this.getUserID())){
            return Client.getUser();
        } else if(this.user == null){
            ReplyWaiter handler = new ReplyWaiter(Client.getConfig().getTimeout()) {
                @Override
                public void packetReceived(Packet packet) {
                    switch (packet.getType()) {
                        case USER:
                            UserRequest userRequest = (UserRequest) packet.getPayload();
                            if(userRequest.getRequestID() == requestID) {
                                User user = userRequest.getUser();
                                if (Bid.this.getUserID().equals(user.getUniqueID())) {
                                    this.waiter.replyReceived();
                                    Bid.this.user = user;
                                }
                            }
                            break;
                    }
                }
            };
            Client.addPacketListener(handler);
            UserRequest userRequest = new UserRequest(getUserID());
            this.requestID = userRequest.getRequestID();
            Client.sendPacket(new Packet<>(PacketType.FETCH_USER, userRequest));

            handler.getWaiter().waitForReply();
            Client.removePacketListener(handler);

            if (handler.getWaiter().isReplyTimedOut() && user == null) {
                return null;
            } else {
                return this.user;
            }
        } else {
            return this.user;
        }
    }
}
