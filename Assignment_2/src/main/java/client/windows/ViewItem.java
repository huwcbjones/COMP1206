package client.windows;

import client.Client;
import client.components.WindowPanel;
import shared.Item;
import shared.Packet;
import shared.PacketType;
import shared.User;
import shared.components.ImagePanel;
import shared.components.JLinkLabel;
import shared.events.PacketListener;
import shared.utils.ImageUtils;
import shared.utils.ReplyWaiter;
import shared.utils.TimeUtils;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.UUID;

/**
 * View Item Panel
 *
 * @author Huw Jones
 * @since 20/04/2016
 */
public class ViewItem extends WindowPanel {
    private Item item;

    private ImagePanel panel_image;
    private JPanel panel_details;
    private JPanel panel_bids;
    private JPanel panel_seller;
    private JPanel panel_description;

    private JLinkLabel link_seller;
    private JLabel content_keywords;
    private JLabel content_timeRemaining;
    private JLabel content_reserve;
    private JLabel content_topBid;
    private JFormattedTextField text_bid;
    private JButton btn_bid;
    private JLabel content_description;

    private Timer updateTimer;


    public ViewItem() {
        super("");
        this.initComponents();
        this.initEventListeners();
    }

    private void initEventListeners() {
        Client.addPacketListener(new PacketHandler());
        this.link_seller.addActionListener(e -> {

            Main.getMain().changePanel(Main.PANEL_VIEWUSER);
        });
        this.updateTimer = new Timer(1000, e -> {
            if (this.item == null) return;

            if (this.item.getAuctionStatus() == Item.AUCTION_STARTED) {
                this.content_timeRemaining.setText(TimeUtils.getTimeString(this.item.getTimeUntilEnd(), true));
            } else {
                this.content_timeRemaining.setText("Auction Closed");
            }

            this.content_timeRemaining.setForeground(TimeUtils.getTimeColour(this.item.getTimeUntilEnd()));
        });
        this.updateTimer.setRepeats(true);
        this.updateTimer.start();
        this.panel_image.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                if(ViewItem.this.item == null) return;

                ImagePanel panel = ViewItem.this.panel_image;
                if (ViewItem.this.item.getImage() != null) {
                    panel.setImage(ImageUtils.getScaledImage(ViewItem.this.item.getImage(), panel.getWidth(), panel.getHeight()), true);
                } else {
                    panel.setImage(null, true);
                }
            }
        });
    }

    private void initComponents() {
        this.setLayout(new GridBagLayout());
        this.setBorder(new EmptyBorder(0, 8, 0, 8));

        GridBagConstraints c;
        //region Panels
        this.panel_image = new ImagePanel();
        this.panel_image.setBorder(new CompoundBorder(new LineBorder(Color.LIGHT_GRAY, 1), new EmptyBorder(16, 16, 16, 16)));
        c = new GridBagConstraints();
        c.weighty = c.weightx = 0.4;
        c.insets = new Insets(16, 16, 16, 16);
        c.gridx = 0;
        c.gridy = 0;
        c.fill = GridBagConstraints.BOTH;
        this.add(this.panel_image, c);

        this.panel_details = new JPanel(new GridBagLayout());
        this.panel_details.setOpaque(false);
        c = new GridBagConstraints();
        c.weightx = 0.3;
        c.weighty = 0.5;
        c.insets = new Insets(16, 16, 16, 16);
        c.gridx = 1;
        c.gridy = 0;
        c.fill = GridBagConstraints.BOTH;
        this.add(this.panel_details, c);

        this.panel_bids = new JPanel(new GridBagLayout());
        this.panel_bids.setOpaque(false);
        this.panel_bids.setBorder(new CompoundBorder(new LineBorder(Color.LIGHT_GRAY, 1), new EmptyBorder(16, 16, 16, 16)));
        c = new GridBagConstraints();
        c.weightx = 0.3;
        c.weighty = 0.5;
        c.gridx = 2;
        c.gridy = 0;
        c.insets = new Insets(16, 16, 16, 16);
        c.fill = GridBagConstraints.BOTH;
        this.add(this.panel_bids, c);

        this.panel_description = new JPanel(new GridBagLayout());
        this.panel_description.setOpaque(false);
        c = new GridBagConstraints();
        c.weightx = 0.4;
        c.weighty = 0.6;
        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 2;
        c.insets = new Insets(16, 16, 16, 16);
        c.fill = GridBagConstraints.BOTH;
        this.add(this.panel_description, c);

        this.panel_seller = new JPanel(new GridBagLayout());
        this.panel_seller.setOpaque(false);
        this.panel_seller.setBorder(new CompoundBorder(new LineBorder(Color.LIGHT_GRAY, 1), new EmptyBorder(16, 16, 16, 16)));
        c = new GridBagConstraints();
        c.weightx = 0.3;
        c.weighty = 0.5;
        c.gridx = 2;
        c.gridy = 1;
        c.insets = new Insets(16, 16, 16, 16);
        c.fill = GridBagConstraints.BOTH;
        this.add(this.panel_seller, c);
        //endregion

        //region Details Panel
        this.panel_details.setLayout(new BorderLayout());

        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        JLabel labelDetails = new JLabel("Auction Details", JLabel.LEADING);
        labelDetails.setFont(labelDetails.getFont().deriveFont(16f));
        titlePanel.add(labelDetails, BorderLayout.CENTER);
        titlePanel.add(new JSeparator(JSeparator.HORIZONTAL), BorderLayout.PAGE_END);
        this.panel_details.add(titlePanel, BorderLayout.PAGE_START);

        JPanel container = new JPanel(new GridBagLayout());
        container.setOpaque(false);

        int row = 0;
        JLabel label_keywords = new JLabel("Keywords:", JLabel.TRAILING);
        label_keywords.setFont(label_keywords.getFont().deriveFont(12f));
        c = new GridBagConstraints();
        c.gridy = row;
        c.gridx = 0;
        c.weightx = 0.3;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(2, 4, 2, 6);
        container.add(label_keywords, c);

        this.content_keywords = new JLabel("", JLabel.LEADING);
        this.content_keywords.setFont(this.content_keywords.getFont().deriveFont(12f));
        c.gridx = 1;
        c.weightx = 0.7;
        c.insets = new Insets(2, 6, 2, 4);
        container.add(this.content_keywords, c);
        row++;

        JLabel label_timeRemaining = new JLabel("Time Remaining:", JLabel.TRAILING);
        label_timeRemaining.setFont(label_timeRemaining.getFont().deriveFont(12f));
        c = new GridBagConstraints();
        c.gridy = row;
        c.gridx = 0;
        c.weightx = 0.3;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(2, 4, 2, 6);
        container.add(label_timeRemaining, c);

        this.content_timeRemaining = new JLabel("", JLabel.LEADING);
        this.content_timeRemaining.setFont(this.content_timeRemaining.getFont().deriveFont(Font.PLAIN, 12f));
        c.gridx = 1;
        c.weightx = 0.7;
        c.insets = new Insets(2, 6, 2, 4);
        container.add(this.content_timeRemaining, c);
        row++;

        JLabel label_topBid = new JLabel("Top Bid:", JLabel.TRAILING);
        label_topBid.setFont(label_topBid.getFont().deriveFont(12f));
        c = new GridBagConstraints();
        c.gridy = row;
        c.gridx = 0;
        c.weightx = 0.3;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(2, 4, 2, 6);
        container.add(label_topBid, c);

        this.content_topBid = new JLabel("-", JLabel.LEADING);
        this.content_topBid.setFont(this.content_topBid.getFont().deriveFont(14f));
        c.gridx = 1;
        c.weightx = 0.7;
        c.insets = new Insets(2, 6, 2, 4);
        container.add(this.content_topBid, c);
        row++;

        JLabel label_reserve = new JLabel("Reserve Price:", JLabel.TRAILING);
        label_reserve.setFont(label_reserve.getFont().deriveFont(12f));
        c = new GridBagConstraints();
        c.gridy = row;
        c.gridx = 0;
        c.weightx = 0.3;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(2, 4, 2, 6);
        container.add(label_reserve, c);

        this.content_reserve = new JLabel("Reserve:", JLabel.LEADING);
        this.content_reserve.setFont(this.content_reserve.getFont().deriveFont(12f));
        c.gridx = 1;
        c.weightx = 0.7;
        c.insets = new Insets(2, 6, 2, 4);
        container.add(this.content_reserve, c);
        row++;

        NumberFormatter currencyFormat = new NumberFormatter(NumberFormat.getCurrencyInstance(Locale.UK));
        currencyFormat.setAllowsInvalid(false);
        this.text_bid = new JFormattedTextField(currencyFormat);
        this.text_bid.setValue(0.0);
        c.gridy = row;
        c.gridx = 0;
        c.gridwidth = 2;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(2, 32, 2, 32);
        container.add(this.text_bid, c);
        row++;

        this.btn_bid = new JButton("Place Bid");
        this.btn_bid.setMnemonic('b');
        c.gridy = row;
        c.gridx = 0;
        c.gridwidth = 2;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(2, 32, 2, 32);
        container.add(this.btn_bid, c);
        row++;

        JPanel padder = new JPanel();
        padder.setOpaque(false);
        c = new GridBagConstraints();
        c.gridy = row;
        c.gridx = 0;
        c.gridwidth = 2;
        c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;
        container.add(padder, c);

        this.panel_details.add(container, BorderLayout.CENTER);
        //endregion

        //region Seller Panel
        this.panel_seller.setLayout(new BorderLayout());

        titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);

        JLabel labelSeller = new JLabel("Seller Details", JLabel.LEADING);
        labelDetails.setFont(labelDetails.getFont().deriveFont(16f));
        titlePanel.add(labelSeller, BorderLayout.CENTER);
        titlePanel.add(new JSeparator(JSeparator.HORIZONTAL), BorderLayout.PAGE_END);
        this.panel_seller.add(titlePanel, BorderLayout.PAGE_START);

        container = new JPanel(new GridBagLayout());
        container.setOpaque(false);

        row = 0;
        this.link_seller = new JLinkLabel("", JLinkLabel.LEADING);
        this.link_seller.setFont(this.link_seller.getFont().deriveFont(Font.BOLD, 14f));
        c = new GridBagConstraints();
        c.gridy = row;
        c.gridx = 0;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(2, 8, 2, 8);
        container.add(this.link_seller, c);
        row++;

        padder = new JPanel();
        padder.setOpaque(false);
        c = new GridBagConstraints();
        c.gridy = row;
        c.gridx = 0;
        c.gridwidth = 2;
        c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;
        container.add(padder, c);

        this.panel_seller.add(container, BorderLayout.CENTER);
        //endregion

        //region Description Panel
        this.panel_description.setLayout(new BorderLayout());
        this.panel_description.getInsets().set(8, 8, 8, 8);
        JLabel descriptionLabel = new JLabel("Description", JLabel.LEADING);
        descriptionLabel.setFont(descriptionLabel.getFont().deriveFont(16f));

        container = new JPanel(new BorderLayout());
        container.add(descriptionLabel, BorderLayout.CENTER);
        container.setOpaque(false);
        container.add(new JSeparator(JSeparator.HORIZONTAL), BorderLayout.PAGE_END);

        this.panel_description.add(container, BorderLayout.PAGE_START);

        this.content_description = new JLabel("", JLabel.LEADING);
        this.content_description.setVerticalAlignment(JLabel.TOP);
        this.panel_description.add(this.content_description, BorderLayout.CENTER);
        //endregion

    }

    public boolean setItem(UUID itemID) {
        ReplyWaiter handler = new ReplyWaiter(Client.getConfig().getTimeout()) {
            @Override
            public void packetReceived(Packet packet) {
                switch (packet.getType()) {
                    case ITEM:
                        this.waiter.replyReceived();
                        ViewItem.this.setItem((Item) packet.getPayload());
                        break;
                }
            }
        };
        Client.addPacketListener(handler);
        Client.sendPacket(new Packet<>(PacketType.FETCH_ITEM, itemID));

        handler.getWaiter().waitForReply();
        Client.removePacketListener(handler);

        if (handler.getWaiter().isReplyTimedOut()) {
            return false;
        }

        handler = new ReplyWaiter(Client.getConfig().getTimeout()) {
            @Override
            public void packetReceived(Packet packet) {
                switch (packet.getType()) {
                    case USER:
                        if (((User) packet.getPayload()).getUniqueID().equals(item.getUserID())) {
                            this.waiter.replyReceived();
                            SwingUtilities.invokeLater(() -> ViewItem.this.setUser((User) packet.getPayload()));
                        }
                        break;
                }
            }
        };

        Client.addPacketListener(handler);
        Client.sendPacket(new Packet<>(PacketType.FETCH_USER, item.getUserID()));

        handler.getWaiter().waitForReply();
        return !handler.getWaiter().isReplyTimedOut();

    }

    public void setItem(Item item) {
        this.item = item;

        SwingUtilities.invokeLater(() -> {
            // Title
            this.setTitle(item.getTitle());
            Main.getMain().updateTitle(this);

            if (this.item.getImage() != null) this.panel_image.setImage(this.item.getImage());
            this.panel_image.repaint();

            // Set keywords
            this.content_keywords.setText(this.item.getKeywordString());

            // Set time remaining
            if (this.item.getAuctionStatus() == Item.AUCTION_STARTED) {
                this.content_timeRemaining.setText(TimeUtils.getTimeString(this.item.getTimeUntilEnd(), true));
            } else {
                this.content_timeRemaining.setText("Auction Closed");
            }
            this.content_timeRemaining.setForeground(TimeUtils.getTimeColour(this.item.getTimeUntilEnd()));

            // Set reserve
            this.content_reserve.setText(this.item.getReserveString());

            if (this.item.getNumberOfBids() == 0) {
                this.content_topBid.setText("-");
            } else {
                this.content_topBid.setText(item.getTopBid().getPriceString());
            }

            this.content_description.setText("<html>" + this.item.getDescription() + "</html>");

            this.getRootPane().revalidate();
            this.getRootPane().repaint();
        });
    }

    private void setUser(User user) {
        this.link_seller.setText(user.getFullName() + " (" + user.getUsername() + ")");
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

    private class PacketHandler implements PacketListener {
        @Override
        public void packetReceived(Packet packet) {
            switch (packet.getType()) {
                case ITEM:
                    SwingUtilities.invokeLater(() -> ViewItem.this.setItem((Item) packet.getPayload()));
                    break;

                case USER:
                    SwingUtilities.invokeLater(() -> ViewItem.this.setUser((User) packet.getPayload()));
            }
        }
    }
}
