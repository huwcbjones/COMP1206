package client.windows;

import client.components.WindowPanel;

import javax.swing.*;

/**
 * Server Config Panel
 *
 * @author Huw Jones
 * @since 25/04/2016
 */
public class ServerPanel extends WindowPanel {

    public ServerPanel() {
        super("Servers");
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
}
