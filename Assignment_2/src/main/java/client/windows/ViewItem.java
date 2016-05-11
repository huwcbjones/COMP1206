package client.windows;

import client.Client;
import client.components.WindowPanel;
import shared.Item;
import shared.Packet;
import shared.PacketType;
import shared.events.PacketListener;
import shared.utils.ReplyWaiter;

import javax.swing.*;
import java.util.UUID;

/**
 * View Item Panel
 *
 * @author Huw Jones
 * @since 20/04/2016
 */
public class ViewItem extends WindowPanel {
    private Item item;

    public ViewItem() {
        super("");
        Client.addPacketListener(new PacketHandler());
    }

    public boolean setItem(UUID itemID){
        ReplyWaiter handler = new ReplyWaiter(Client.getConfig().getTimeout()) {
            @Override
            public void packetReceived(Packet packet) {
                switch (packet.getType()) {
                    case ITEM:
                        this.waiter.replyReceived();
                        ViewItem.this.setItem((Item)packet.getPayload());
                        break;
                }
                waiter.replyReceived();
            }
        };
        Client.addPacketListener(handler);
        Client.sendPacket(new Packet<>(PacketType.FETCH_ITEM, itemID));

        handler.getWaiter().waitForReply();
        Client.removePacketListener(handler);

        if(handler.getWaiter().isReplyTimedOut()){
            return false;
        }
        return true;
    }

    public void setItem(Item item){
        this.item = item;
        this.setTitle(item.getTitle());
    }

    /**
     * Gets the default button for the panel
     *
     * @return Default button
     */
    @Override
    public JButton getDefaultButton() {
        return null;
    }

    private class PacketHandler implements PacketListener {
        @Override
        public void packetReceived(Packet packet) {
            switch(packet.getType()){
                case ITEM:
                    ViewItem.this.setItem((Item)packet.getPayload());
                    break;
            }
        }
    }
}
