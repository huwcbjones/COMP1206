package server.tasks;

import server.Server;
import server.ServerComms.ClientConnection;
import server.exceptions.OperationFailureException;
import shared.*;
import shared.utils.UUIDUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Searches the database
 *
 * @author Huw Jones
 * @since 10/05/2016
 */
public class SearchTask extends Task {

    private final SearchOptions options;

    public SearchTask(ClientConnection client, SearchOptions options) {
        super("Search Task", client);
        this.options = options;
    }

    /**
     * Executed on task failure
     */
    @Override
    protected void failureAction() {
        this.client.sendPacket(new Packet<>(PacketType.SEARCH_RESULTS, new Item[0]));
    }

    @Override
    public void runSafe() throws Exception {
        List<UUID> itemIDs = this.getItemIDs();

        // Load all applicable items into memory
        itemIDs.stream().forEach(item -> {
            try {
                Server.getData().loadItem(item);
            } catch (OperationFailureException e) {
                log.catching(e);
            }
        });

        // Take items from memory and put them into the ArrayList
        ArrayList<Item> items = new ArrayList<>(itemIDs.size());
        itemIDs.stream().forEach(itemID -> items.add(Server.getData().getItem(itemID).getClientItem()));

        // Send results to client
        this.client.sendPacket(new Packet<>(PacketType.SEARCH_RESULTS, new SearchResults(items, this.options.getSearchID())));
    }

    private List<UUID> getItemIDs(){
        String selectSql;
        List<UUID> itemIDs = new ArrayList<>();

        if(this.options == null) return itemIDs;

        long startTime = System.currentTimeMillis();

        selectSql =
                "SELECT items.itemID, COUNT(bidID) AS bidNum, MAX(bids.price) AS maxBid FROM items " +
                "LEFT JOIN bids ON bids.itemID = items.itemID " +
                "LEFT JOIN item_keywords ON item_keywords.itemID = items.itemID " +
                "WHERE " +
                "(? = '' OR (title LIKE ? OR items.description LIKE ?)) AND " +
                "(startTime BETWEEN ? AND ? OR endTime BETWEEN ? AND ? OR (startTime <= ? AND endTime >=  ?)) AND " +
                "(CAST(strftime('%s', 'now') AS INT) < endTime  OR ?) AND " +
                "(reservePrice >= ?) AND " +
                "(? = -1 OR (SELECT COUNT(keywordID) FROM item_keywords WHERE itemID = items.itemID AND keywordID = ?)) AND " +
                "(NOT ? OR items.userID = ?) " +
                "GROUP BY items.itemID " +
                "HAVING (bidNum = 0 OR NOT ?) ";

        // Set order
        switch (options.getSort()) {
            case BID:
                selectSql += "ORDER BY maxBid " + options.getDirection().toString();
                break;
            case NUM_BIDS:
                selectSql += "ORDER BY COUNT(bids.itemID) " + options.getDirection().toString();
                break;
            case TIME:
                selectSql += "ORDER BY endTime " + options.getDirection().toString();
                break;
            case RESERVE:
                selectSql += "ORDER BY reservePrice " + options.getDirection().toString();
                break;
        }

        Connection c = null;
        PreparedStatement selectQuery = null;

        try {
            c = Server.getData().getConnection();
            if(!c.isClosed()) {
                selectQuery = c.prepareStatement(selectSql);

                // Convert Java Timestamp to Unix Timestamp (Java includes ms, so divide by 1000)
                long from = this.options.getStartTime().getTime() / 1000L;
                long to = this.options.getEndTime().getTime() / 1000L;

                UUID sellerID = this.options.getSellerID();
                boolean restrictSeller = sellerID.getLeastSignificantBits() != 0 && sellerID.getMostSignificantBits() != 0;

                Keyword keyword = this.options.getKeyword();
                int keywordID = -1;
                if (keyword != null) {
                    keywordID = keyword.getKeywordID();
                }

                selectQuery.setString(1, "%" + this.options.getString() + "%");
                selectQuery.setString(2, "%" + this.options.getString() + "%");
                selectQuery.setString(3, "%" + this.options.getString() + "%");

                selectQuery.setLong(4, from);
                selectQuery.setLong(5, to);
                selectQuery.setLong(6, from);
                selectQuery.setLong(7, to);
                selectQuery.setLong(8, from);
                selectQuery.setLong(9, to);

                selectQuery.setBoolean(10, this.options.isIncludeClosed());

                selectQuery.setBigDecimal(11, this.options.getReserve());

                selectQuery.setInt(12, keywordID);
                selectQuery.setInt(13, keywordID);

                selectQuery.setBoolean(14, restrictSeller);
                selectQuery.setBytes(15, UUIDUtils.UUIDToBytes(this.options.getSellerID()));

                selectQuery.setBoolean(16, this.options.isNoBids());

                ResultSet itemResults = selectQuery.executeQuery();

                while (itemResults.next()) {
                    itemIDs.add(UUIDUtils.BytesToUUID(itemResults.getBytes("itemID")));
                }

                itemResults.close();
            }
        } catch (SQLException e) {
            log.debug("SQLState: {}", e.getSQLState());
            log.debug("Error Code: {}", e.getErrorCode());
            log.debug("Message: {}", e.getMessage());
            log.debug("Cause: {}", e.getCause());
            log.error("Failed to search database.", e);
        } finally {
            try {
                if (selectQuery != null) {
                    selectQuery.close();
                }
                if (c != null) {
                    c.close();
                }
            } catch (SQLException suppress) {
                log.trace(suppress);
            }
        }
        long endTime = System.currentTimeMillis();
        log.debug("Search took: {}ms", (endTime - startTime));
        log.debug("Found {} results.", itemIDs.size());
        return itemIDs;
    }
}
