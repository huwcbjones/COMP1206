package server.tasks;

import server.ClientConnection;
import server.Server;
import server.exceptions.OperationFailureException;
import shared.Item;
import shared.Keyword;
import shared.Packet;
import shared.PacketType;
import shared.exceptions.ValidationFailedException;
import shared.utils.UUIDUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.UUID;

/**
 * Auctions a new item
 *
 * @author Huw Jones
 * @since 22/04/2016
 */
public class NewAuctionTask extends Task {
    private final Item item;

    public NewAuctionTask(ClientConnection client, Item item) {
        super("NewAuction", client);
        this.item = item;
    }

    /**
     * Performs the Worker Task
     */
    @Override
    public void runSafe() {
        try {
            validateFields();
            UUID itemID = this.addItemToDatabase();
            Server.getData().processItemImage(itemID, this.item.getImage());
            log.info("New item ({}) added to database!", itemID);
            this.client.sendPacket(new Packet<>(PacketType.CREATE_ITEM_SUCCESS, itemID));
            Server.getData().loadItem(itemID);
        } catch (ValidationFailedException | OperationFailureException e) {
            log.error(e.getMessage());
            this.client.sendPacket(new Packet<>(PacketType.CREATE_ITEM_FAIL, e.getMessage()));
        }
    }

    /**
     * Executed on task failure
     */
    @Override
    protected void failureAction() {
        this.client.sendPacket(new Packet<>(PacketType.CREATE_ITEM_FAIL, "There was an error creating the auction for that item."));
    }

    private UUID addItemToDatabase() throws OperationFailureException {
        UUID uniqueID;
        do {
            uniqueID = UUID.randomUUID();
        } while (Server.getData().itemExists(uniqueID));


        Connection c = null;
        PreparedStatement insertItem = null;
        PreparedStatement insertKeywords = null;
        String insertSql = "INSERT INTO items (itemID, userID, title, description, startTime, endTime, reservePrice) VALUES (?, ?, ?, ?, ?, ?, ?)";
        String insertKeywordsSql = "INSERT INTO item_keywords (itemID, keywordID) VALUES (?, ?)";
        boolean wasSuccess = false;
        try {
            c = Server.getData().getConnection();
            c.setAutoCommit(false);
            insertItem = c.prepareStatement(insertSql);
            insertKeywords = c.prepareStatement(insertKeywordsSql);
            insertItem.setBytes(1, UUIDUtils.UUIDToBytes(uniqueID));
            insertItem.setBytes(2, UUIDUtils.UUIDToBytes(this.item.getUserID()));
            insertItem.setString(3, this.item.getTitle());
            insertItem.setString(4, this.item.getDescription());
            insertItem.setLong(5, this.item.getStartTime().getTime() / 1000L);
            insertItem.setLong(6, this.item.getEndTime().getTime() / 1000L);
            insertItem.setBigDecimal(7, this.item.getReserve());
            insertItem.executeUpdate();

            insertKeywords.setBytes(1, UUIDUtils.UUIDToBytes(uniqueID));
            for (Keyword k : item.getKeywords()) {
                insertKeywords.setInt(2, k.getKeywordID());
                insertKeywords.addBatch();
            }
            ArrayList<Integer> result = new ArrayList<>();
            for (int r : insertKeywords.executeBatch()) {
                result.add(r);
            }
            if(result.parallelStream().filter(i -> i == PreparedStatement.EXECUTE_FAILED).count() != 0){
                log.error("Failed to link keywords.");
                c.rollback();
            } else {
                c.commit();
                wasSuccess = true;
            }
        } catch (SQLException e) {
            log.debug(e);
            log.debug("SQLState: {}", e.getSQLState());
            log.debug("Error Code: {}", e.getErrorCode());
            log.debug("Message: {}", e.getMessage());
            log.debug("Cause: {}", e.getCause());
            wasSuccess = false;
        } finally {
            try {
                if (insertKeywords != null) {
                    insertKeywords.close();
                }
                if (insertItem != null) {
                    insertItem.close();
                }
                if (c != null) {
                    c.setAutoCommit(true);
                    c.close();
                }
            } catch (SQLException suppress) {
                log.trace(suppress);
            }
        }
        if (!wasSuccess) {
            throw new OperationFailureException("Failed to add item. A server error occurred.");
        }

        return uniqueID;
    }

    private void validateFields() throws ValidationFailedException {

    }
}
