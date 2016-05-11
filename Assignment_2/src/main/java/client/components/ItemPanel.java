package client.components;

import client.windows.Main;
import shared.Item;
import shared.components.ImagePanel;
import shared.utils.TimeUtils;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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
    private JLabel label_timeRemaining;
    private JLabel label_keywords;
    private JLabel label_description;
    private JLabel label_topBid;
    private JLabel label_reserve;

    public ItemPanel(Item item) {
        super(new GridBagLayout());
        this.setMinimumSize(new Dimension(560, 200));
        this.item = item;
        this.initComponents();
        this.addMouseListener(new MouseHandler());
        this.addMouseMotionListener(new MouseHandler());
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

        this.panel_image = new ImagePanel(this.item.getThumbnail());
        this.panel_image.setBorder(new CompoundBorder(new LineBorder(Color.LIGHT_GRAY, 1), new EmptyBorder(8, 8, 8, 8)));
        this.panel_image.setPreferredSize(new Dimension(128, 128));
        c = new GridBagConstraints();
        c.insets.set(6, 6, 6, 6);
        c.gridx = 0;
        c.gridy = 1;
        c.gridheight = 4;
        this.add(this.panel_image, c);

        this.label_timeRemaining = new JLabel("Time Remaining: " + TimeUtils.getTimeString(this.item.getTimeUntilEnd(), true), JLabel.LEFT);
        this.label_timeRemaining.setFont(this.label_timeRemaining.getFont().deriveFont(Font.PLAIN, 12f));
        this.label_timeRemaining.setForeground(TimeUtils.getTimeColour(this.item.getTimeUntilEnd()));
        c = new GridBagConstraints();
        c.insets.set(6, 6, 6, 6);
        c.gridx = 1;
        c.gridy = 1;
        c.weightx = 0.4;
        c.weighty = 0;
        c.gridwidth = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        this.add(this.label_timeRemaining, c);

        this.label_keywords = new JLabel("Keywords: " + this.item.getKeywordString(), JLabel.LEFT);
        this.label_keywords.setFont(this.label_keywords.getFont().deriveFont(Font.PLAIN, 12f));
        c = new GridBagConstraints();
        c.insets.set(6, 6, 6, 6);
        c.gridx = 1;
        c.gridy = 2;
        c.weightx = 0.4;
        c.weighty = 0;
        c.gridwidth = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        this.add(this.label_keywords, c);

        this.label_description = new JLabel("<html>" + this.item.getDescription() + "</html>", JLabel.LEADING);
        c = new GridBagConstraints();
        c.insets.set(6, 6, 6, 6);
        c.gridx = 1;
        c.gridy = 3;
        c.weightx = 0;
        c.weighty = 0;
        c.gridwidth = 1;
        c.gridheight = 2;
        this.add(this.label_description, c);

        this.label_topBid = new JLabel("Top Bid: " + NumberFormat.getCurrencyInstance(Locale.UK).format(0), JLabel.LEFT);
        if(this.item.getNumberOfBids() != 0){
            this.label_topBid.setText("Top Bid: " + this.item.getTopBid().getPriceString());
        }
        this.label_topBid.setFont(this.label_topBid.getFont().deriveFont(Font.BOLD, 14f));
        c = new GridBagConstraints();
        c.insets.set(6, 6, 6, 6);
        c.gridx = 2;
        c.gridy = 1;
        c.weightx = 0.3;
        c.weighty = 0;
        this.add(this.label_topBid, c);

        this.label_reserve = new JLabel("Reserve: "+ this.item.getReserveString(), JLabel.LEFT);
        this.label_reserve.setFont(this.label_reserve.getFont().deriveFont(Font.PLAIN, 14f));
        c = new GridBagConstraints();
        c.insets.set(6, 6, 6, 6);
        c.gridx = 2;
        c.gridy = 2;
        c.weightx = 0.3;
        c.weighty = 0;
        this.add(this.label_reserve, c);

        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 3;
        c.weighty = 0;
        c.weightx = 1;
        c.gridwidth = 3;
        this.add(new JSeparator(JSeparator.HORIZONTAL), c);
    }

    @Override
    protected void paintComponent(Graphics g) {
        // Update time values, then repaint
        if(this.item.getAuctionStatus() == Item.AUCTION_STARTED) {
            this.label_timeRemaining.setText("Time Remaining: " + TimeUtils.getTimeString(this.item.getTimeUntilEnd(), true));
        } else {
            this.label_timeRemaining.setText("Auction Closed");
        }

        this.label_timeRemaining.setForeground(TimeUtils.getTimeColour(this.item.getTimeUntilEnd()));
        super.paintComponent(g);
    }

    private class MouseHandler extends MouseAdapter {

        @Override
        public void mouseEntered(MouseEvent e) {
            ItemPanel.this.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            ItemPanel.this.setBackground(UIManager.getColor("List.selectionBackground"));
        }

        @Override
        public void mouseExited(MouseEvent e) {
            ItemPanel.this.setCursor(Cursor.getDefaultCursor());
            ItemPanel.this.setBackground(Color.WHITE);
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            Main.getMain().displayItem(ItemPanel.this.item.getID());
        }
    }
}
