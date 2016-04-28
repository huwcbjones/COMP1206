package client.windows;

import client.components.WindowPanel;

import javax.swing.*;

/**
 * New Item Panel
 *
 * @author Huw Jones
 * @since 20/04/2016
 */
public class NewItem extends WindowPanel {
    public NewItem() {
        super("New Item");
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
