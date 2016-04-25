package client.components;

import shared.Item;
import shared.components.ImagePanel;

import javax.swing.*;
import java.awt.*;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Displays Items in a JPanel
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
        this.setMinimumSize(new Dimension(560, 200));
        this.item = item;
        this.initComponents();
    }

    private void initComponents() {
        this.setBackground(Color.WHITE);
        GridBagConstraints c;

        this.label_title = new JLabel(this.item.getTitle(), JLabel.LEFT);
        this.label_title.setFont(this.label_title.getFont().deriveFont(Font.BOLD, 16f));
        this.label_title.setForeground(Color.BLUE);
        c = new GridBagConstraints();
        c.insets.set(6, 6, 6, 6);
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 0;
        c.gridwidth = 3;
        c.fill = GridBagConstraints.HORIZONTAL;
        this.add(this.label_title, c);

        this.panel_image = new ImagePanel();
        this.panel_image.setPreferredSize(new Dimension(128, 128));
        c = new GridBagConstraints();
        c.insets.set(6, 6, 6, 6);
        c.gridx = 0;
        c.gridy = 1;
       // c.weightx = 1;
        //c.weighty = 1;
        c.gridheight = 3;
        //c.fill = GridBagConstraints.BOTH;
        this.add(this.panel_image, c);

        this.label_keywords = new JLabel("Keywords: " + this.item.getKeywordString(), JLabel.LEFT);
        this.label_keywords.setFont(this.label_keywords.getFont().deriveFont(Font.PLAIN, 12f));
        c = new GridBagConstraints();
        c.insets.set(6, 6, 6, 6);
        c.gridx = 1;
        c.gridy = 1;
        c.weightx = 0.4;
        c.weighty = 0;
        c.gridwidth = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        this.add(this.label_keywords, c);

        this.label_description = new JLabel("<html>" + this.item.getDescription() + "</html>", JLabel.LEFT);
        c = new GridBagConstraints();
        c.insets.set(6, 6, 6, 6);
        c.gridx = 1;
        c.gridy = 2;
        c.weightx = 0;
        c.weighty = 0;
        c.gridwidth = 1;
        c.gridheight = 2;
        this.add(this.label_description, c);

        this.label_topBid = new JLabel("Top Bid: " + NumberFormat.getCurrencyInstance(Locale.UK).format(0), JLabel.LEFT);
        if(this.item.getNumberOfBids() != 0){
            this.label_topBid.setText("Top Bid: " + this.item.getTopBid().getPriceString());
        }
        c = new GridBagConstraints();
        c.insets.set(6, 6, 6, 6);
        c.gridx = 2;
        c.gridy = 1;
        c.weightx = 0.3;
        c.weighty = 0;
        this.add(this.label_topBid, c);

        this.label_reserve = new JLabel("Reserve: "+ this.item.getReserveString(), JLabel.LEFT);
        c = new GridBagConstraints();
        c.insets.set(6, 6, 6, 6);
        c.gridx = 2;
        c.gridy = 2;
        c.weightx = 0.3;
        c.weighty = 0;
        this.add(this.label_reserve, c);
    }
}
