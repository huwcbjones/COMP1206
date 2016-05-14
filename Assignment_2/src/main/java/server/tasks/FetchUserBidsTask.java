package server.tasks;

import server.Server;
import server.ServerComms.ClientConnection;
import server.exceptions.OperationFailureException;
import shared.Item;
import shared.Packet;
import shared.PacketType;
import shared.utils.UUIDUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.UUID;

/**
 * {DESCRIPTION}
 *
 * @author Huw Jones
 * @since 13/05/2016
 */
public class FetchUserBidsTask extends Task {

    private final UUID userID;

    public FetchUserBidsTask(ClientConnection client, UUID userID) {
        super("FetchUserBidsTask", client);
        this.userID = userID;
    }

    /**
     * Executed on task failure
     */
    @Override
    protected void failureAction() {
        this.client.sendPacket(new Packet<>(PacketType.USERBIDS, new shared.Item[0]));
    }

    @Override
    public void runSafe() throws Exception {
        ArrayList<UUID> itemIDs = this.getItemIDs();

        ArrayList<Item> bidArray = new ArrayList<>(itemIDs.size());

        itemIDs.forEach(itemID -> bidArray.add(Server.getData().getItem(itemID)));

        this.client.sendPacket(new Packet<>(PacketType.USERBIDS, bidArray.toArray(new shared.Item[bidArray.size()])));
    }

    private ArrayList<UUID> getItemIDs() throws OperationFailureException {
        ArrayList<UUID> bids = new ArrayList<>();
        String selectBidsSql = "SELECT itemID, bidID, MAX(price) FROM bids WHERE userID = ? GROUP BY bids.itemID ORDER BY time DESC";
        Connection c = null;
        PreparedStatement selectBids = null;
        boolean wasSuccess = false;
        try {
            c = Server.getData().getConnection();

            selectBids = c.prepareStatement(selectBidsSql);

            selectBids.setBytes(1, UUIDUtils.UUIDToBytes(this.userID));

            ResultSet bidResults = selectBids.executeQuery();
            while (bidResults.next()) {
                bids.add(UUIDUtils.BytesToUUID(bidResults.getBytes("itemID")));
            }
            bidResults.close();
            wasSuccess = true;
        } catch (SQLException e) {
            log.debug("SQLState: {}", e.getSQLState());
            log.debug("Error Code: {}", e.getErrorCode());
            log.debug("Message: {}", e.getMessage());
            log.debug("Cause: {}", e.getCause());
            log.error("Failed to load bids.", e);
        } finally {
            try {
                if (selectBids != null) {
                    selectBids.close();
                }
                if (c != null) {
                    c.close();
                }
            } catch (SQLException suppress) {
                log.trace(suppress);
            }
        }
        if(!wasSuccess) {
            throw new OperationFailureException("Failed to fetch bid list.");
        }
        return bids;
    }
}
