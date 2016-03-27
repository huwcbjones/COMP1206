import javax.swing.*;
import client.Client;

/**
 * Auction Client Bootstrapper
 *
 * @author Huw Jones
 * @since 24/03/2016
 */
public class auction {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Client c = new Client();
        });
    }
}
