package client.windows;

import client.components.WindowPanel;
import shared.Item;

import javax.swing.*;

/**
 * View Item Panel
 *
 * @author Huw Jones
 * @since 20/04/2016
 */
public class ViewItem extends WindowPanel {
    private Item item;

    public ViewItem() {
        super("");
    }

    public void setItem(Item item){
        this.setTitle(item.getTitle());
        this.item = item;
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
