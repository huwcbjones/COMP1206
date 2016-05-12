package server.tasks;

import server.Server;
import server.ServerComms.ClientConnection;
import server.exceptions.OperationFailureException;
import shared.Packet;
import shared.PacketType;
import shared.User;
import shared.utils.UUIDUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.UUID;


/**
 * Fetches a list of sellers
 *
 * @author Huw Jones
 * @since 12/05/2016
 */
public class FetchSellerList extends Task {

    public FetchSellerList(ClientConnection client) {
        super("FetchSellerListTask", client);
    }

    /**
     * Executed on task failure
     */
    @Override
    protected void failureAction() {
        this.client.sendPacket(new Packet<>(PacketType.SELLER_LIST, new User[0]));
    }

    @Override
    public void runSafe() throws Exception {
        ArrayList<UUID> sellerIDs = this.getSellerList();

        HashSet<server.objects.User> serverUsers = new HashSet<>();
        sellerIDs.forEach(ID -> serverUsers.add(Server.getData().getUser(ID)));

        HashSet<User> users = new HashSet<>();
        serverUsers.stream().filter(item -> item != null).forEach(seller -> users.add(seller.getSharedUser()));

        this.client.sendPacket(new Packet<>(PacketType.SELLER_LIST, users.toArray(new User[users.size()])));
    }

    private ArrayList<UUID> getSellerList() throws OperationFailureException {
        ArrayList<UUID> sellers = new ArrayList<>();
        String selectSellers = "SELECT DISTINCT userID FROM items";
        Connection c = null;
        PreparedStatement selectSeller = null;
        boolean wasSuccess = false;
        try {
            c = Server.getData().getConnection();

            selectSeller = c.prepareStatement(selectSellers);

            ResultSet sellerResults = selectSeller.executeQuery();
            while (sellerResults.next()) {
                sellers.add(UUIDUtils.BytesToUUID(sellerResults.getBytes("userID")));
            }
            sellerResults.close();
            wasSuccess = true;
        } catch (SQLException e) {
            log.debug("SQLState: {}", e.getSQLState());
            log.debug("Error Code: {}", e.getErrorCode());
            log.debug("Message: {}", e.getMessage());
            log.debug("Cause: {}", e.getCause());
            log.error("Failed to load user.", e);
        } finally {
            try {
                if (selectSeller != null) {
                    selectSeller.close();
                }
                if (c != null) {
                    c.close();
                }
            } catch (SQLException suppress) {
                log.trace(suppress);
            }
        }
        if(!wasSuccess) {
            throw new OperationFailureException("Failed to fetch seller list.");
        }
        return sellers;
    }
}
