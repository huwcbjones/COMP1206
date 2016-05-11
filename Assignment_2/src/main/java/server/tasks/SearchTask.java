package server.tasks;

import server.ClientConnection;
import server.Server;
import server.exceptions.OperationFailureException;
import shared.Item;
import shared.Packet;
import shared.PacketType;
import shared.SearchOptions;
import shared.utils.UUIDUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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
        itemIDs.stream().forEach(itemID -> items.add(Server.getData().getItem(itemID)));

        // Filter ended auctions
        List<Item> itemArray = items.stream().filter(item -> item.getAuctionStatus() == Item.AUCTION_STARTED).collect(Collectors.toList());

        // Send results to client
        this.client.sendPacket(new Packet<>(PacketType.SEARCH_RESULTS, itemArray.toArray(new Item[itemArray.size()])));
    }

    private List<UUID> getItemIDs(){
        String selectSql;
        List<UUID> itemIDs = new ArrayList<>();

        if(this.options == null) return itemIDs;

        long startTime = System.currentTimeMillis();

        // TODO: 11/05/2016 Make this filter dates properly
        selectSql =
                "SELECT items.itemID, COUNT(bidID) AS bidNum FROM items " +
                "LEFT JOIN bids ON bids.itemID = items.itemID " +
                "WHERE " +
                "(? = '' OR (title LIKE ? OR items.description LIKE ?)) AND " +
                "(startTime BETWEEN ? AND ? OR endTime BETWEEN ? AND ?) AND " +
                "(reservePrice >= ?) " +
                "GROUP BY items.itemID " +
                "HAVING (bidNum = 0 OR NOT ?) ";

        // Set order
        switch (options.getSort()) {
            case BID:
                selectSql += "ORDER BY bids.price " + options.getDirection().toString();
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
            selectQuery = c.prepareStatement(selectSql);

            // Convert Java Timestamp to Unix Timestamp (Java includes ms, so divide by 1000)
            long from = this.options.getStartTime().getTime() / 1000L;
            long to = this.options.getEndTime().getTime() / 1000L;

            selectQuery.setString(1, "%" + this.options.getString() + "%");
            selectQuery.setString(2, "%" + this.options.getString() + "%");
            selectQuery.setString(3, "%" + this.options.getString() + "%");

            selectQuery.setLong(4, from);
            selectQuery.setLong(5, to);
            selectQuery.setLong(6, from);
            selectQuery.setLong(7, to);

            selectQuery.setBigDecimal(8, this.options.getReserve());

            selectQuery.setBoolean(9, this.options.isNoBids());

            ResultSet itemResults = selectQuery.executeQuery();

            while (itemResults.next()) {
                itemIDs.add(UUIDUtils.BytesToUUID(itemResults.getBytes("itemID")));
            }

            itemResults.close();
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
