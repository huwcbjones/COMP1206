import server.ServerGUI;

import javax.swing.*;

/**
 * Auction Server GUI entry point
 *
 * @author Huw Jones
 * @since 07/04/2016
 */
public class BiddrServer {

    public static void main(String[] args){
        SwingUtilities.invokeLater(() ->{
            ServerGUI s = new ServerGUI();
        });
    }
}
