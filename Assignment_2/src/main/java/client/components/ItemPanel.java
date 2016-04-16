package client.components;

import client.utils.ImagePanel;
import shared.Item;

import javax.swing.*;
import java.awt.*;

/**
 * {DESCRIPTION}
 *
 * @author Huw Jones
 * @since 12/04/2016
 */
public class ItemPanel extends JPanel {

    private ImagePanel panel_image;
    private JLabel label_title;
    private JLabel labelcatagories;
    private JLabel label_description;
    private JLabel label_topBid;
    private JLabel label_reserve;

    private final Item item;

    public ItemPanel(Item item) {
        super(new GridBagLayout());
        this.item = item;
        this.initComponents();
    }

    private void initComponents(){
        GridBagConstraints c = new GridBagConstraints();
        c.insets.set(6, 6, 6, 6);
        c.weightx = 0.3;
        c.weighty = 1;

        //this.
    }
}
