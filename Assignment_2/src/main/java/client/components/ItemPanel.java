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

    private final Item item;
    private ImagePanel panel_image;
    private JLabel label_title;
    private JLabel label_keywords;
    private JLabel label_description;
    private JLabel label_topBid;
    private JLabel label_reserve;

    public ItemPanel(Item item) {
        super(new GridBagLayout());
        this.item = item;
        this.initComponents();
    }

    private void initComponents() {
        GridBagConstraints c;

        this.panel_image = new ImagePanel();
        c = new GridBagConstraints();
        c.insets.set(6, 6, 6, 6);
        c.weightx = 0.25;
        c.weighty = 1;
        c.gridheight = 3;
        this.add(this.panel_image, c);

        this.label_title = new JLabel(this.item.getTitle());
        c = new GridBagConstraints();
        c.insets.set(6, 6, 6, 6);
        c.gridx = 1;
        c.gridy = 0;
        c.weightx = 0.375;
        c.weighty = 0.25;
        this.add(this.label_title, c);

        this.label_keywords = new JLabel(this.item.getKeywordString());
        c = new GridBagConstraints();
        c.insets.set(6, 6, 6, 6);
        c.gridx = 1;
        c.gridy = 1;
        c.weightx = 0.375;
        c.weighty = 0.25;
        this.add(this.label_keywords, c);

        this.label_topBid = new JLabel(this.item.getTopBid().getPriceString());
        c = new GridBagConstraints();
        c.insets.set(6, 6, 6, 6);
        c.gridx = 2;
        c.gridy = 0;
        c.weightx = 0.375;
        c.weighty = 0.25;
        this.add(this.label_topBid, c);

        this.label_reserve = new JLabel(this.item.getReserveString());
        c = new GridBagConstraints();
        c.insets.set(6, 6, 6, 6);
        c.gridx = 2;
        c.gridy = 1;
        c.weightx = 0.375;
        c.weighty = 0.25;
        this.add(this.label_reserve, c);

        this.label_description = new JLabel(this.item.getDescription());
        c = new GridBagConstraints();
        c.insets.set(6, 6, 6, 6);
        c.gridx = 1;
        c.gridy = 2;
        c.weightx = 0.75;
        c.weighty = 0.5;
        this.add(this.label_description, c);
    }
}
