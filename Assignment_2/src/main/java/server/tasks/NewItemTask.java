package server.tasks;

import server.ClientConnection;
import shared.Item;
import shared.Packet;
import shared.PacketType;

/**
 * Auctions a new item
 *
 * @author Huw Jones
 * @since 22/04/2016
 */
public class NewItemTask extends Task {
    private final Item item;
    public NewItemTask(ClientConnection client, Item item) {
        super("NewItem", client);
        this.item = item;
    }

    /**
     * Performs the Worker Task
     */
    @Override
    public void runSafe() {

    }

    /**
     * Executed on task failure
     */
    @Override
    protected void failureAction() {
        this.client.sendPacket(new Packet<>(PacketType.CREATE_ITEM_FAIL, "There was an error creating the auction for that item."));
    }
}
