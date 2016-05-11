package server.tasks;

import server.Server;
import server.ServerComms.ClientConnection;
import shared.Packet;
import shared.PacketType;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Handles requests for max reserve price
 *
 * @author Huw Jones
 * @since 10/05/2016
 */
public class ReserveTask extends Task {

    public ReserveTask(ClientConnection client) {
        super("ReserveTask", client);
    }

    /**
     * Executed on task failure
     */
    @Override
    protected void failureAction() {
    }

    @Override
    public void runSafe() throws Exception {
        BigDecimal maxPrice = this.getMaxPrice();
        if(maxPrice == null) return;
        int maxPriceInt = maxPrice.multiply(BigDecimal.valueOf(100)).intValue();
        this.client.sendPacket(new Packet<>(PacketType.RESERVE_RANGE, maxPriceInt));
    }

    private BigDecimal getMaxPrice() {
        String selectSql;
        selectSql = "SELECT MAX(reservePrice) AS maxPrice FROM items";
        Connection c = null;
        PreparedStatement selectQuery = null;

        BigDecimal maxPrice = null;

        try {
            c = Server.getData().getConnection();
            selectQuery = c.prepareStatement(selectSql);
            ResultSet itemResults = selectQuery.executeQuery();

            while (itemResults.next()) {
                maxPrice = itemResults.getBigDecimal("maxPrice");
            }
            itemResults.close();
        } catch (SQLException e) {
            log.debug("SQLState: {}", e.getSQLState());
            log.debug("Error Code: {}", e.getErrorCode());
            log.debug("Message: {}", e.getMessage());
            log.debug("Cause: {}", e.getCause());
            log.error("Failed to load user.", e);
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

        return maxPrice;
    }
}
