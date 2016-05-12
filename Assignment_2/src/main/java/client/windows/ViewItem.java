package client.windows;

import client.Client;
import client.components.Bid;
import client.components.BidTableModel;
import client.components.WindowPanel;
import shared.Item;
import shared.Packet;
import shared.PacketType;
import shared.User;
import shared.components.ImagePanel;
import shared.components.JLinkLabel;
import shared.events.PacketListener;
import shared.utils.*;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableRowSorter;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.*;

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
    private JPanel panel_description;

    private JLabel content_keywords;
    private JLabel content_timeRemaining;
    private JLabel content_reserve;
    private JLabel content_topBid;
    private JLabel label_winner;
    private JLinkLabel content_winner;
    private JFormattedTextField text_bid;
    private JButton btn_bid;
    private JTextField content_itemID;
    private JLabel content_description;
    //region Bids
    private BidTableModel model_bid;
    private JTable content_bids;
    //endregion
    //region Seller
    private JLinkLabel link_seller;
    //endregion
    private Timer updateTimer;


    public ViewItem() {
        super("");
        this.initComponents();
        this.initEventListeners();
    }

    private void initEventListeners() {
        Client.addPacketListener(new PacketHandler());
        this.link_seller.addActionListener(e -> Main.getMain().displayUser(this.item.getUserID()));
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
                if (ViewItem.this.item == null) return;

                ImagePanel panel = ViewItem.this.panel_image;
                if (ViewItem.this.item.getImage() != null) {
                    panel.setImage(ImageUtils.getScaledImage(ViewItem.this.item.getImage(), panel.getWidth(), panel.getHeight()), true);
                } else {
                    panel.setImage(null, true);
                }
            }
        });
        this.btn_bid.addActionListener(e -> {
            try {
                BigDecimal bid = NumberUtils.currencyToBigDecimal(this.text_bid.getText(), NumberFormat.getCurrencyInstance(Locale.UK));
                if (this.item.getReserve().compareTo(bid) > 0) {
                    JOptionPane.showMessageDialog(this, "Bid must be more than, or equal to the reserve price.", "Bid not high enough!", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                if (this.item.getTopBid() != null && bid.compareTo(this.item.getTopBid().getPrice()) <= 0) {
                    JOptionPane.showMessageDialog(this, "Bid is not greater than current top bid.", "Bid not high enough!", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                if (JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to place a bid of " + NumberFormat.getCurrencyInstance(Locale.UK).format(bid) + " on this item?" +
                        "\nBids cannot be reversed and you are committing to purchasing this item from the seller",
                    "Place bid?",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE
                ) != JOptionPane.OK_OPTION) {
                    return;
                }
                Client.sendPacket(new Packet<>(
                    PacketType.PLACE_BID,
                    new Bid(new UUID(0, 0), this.item.getID(), Client.getUser().getUniqueID(), bid, new Timestamp(Calendar.getInstance().getTime().getTime())))
                );
            } catch (ParseException e1) {
                JOptionPane.showMessageDialog(this, "Invalid bid format. Failed to understand bid price.", "Invalid Bid!", JOptionPane.WARNING_MESSAGE);
            }
        });
        this.content_winner.addActionListener(e -> Main.getMain().displayUser(this.item.getTopBid().getUserID()));
    }

    private void initComponents() {
        this.setLayout(new GridBagLayout());
        this.setBorder(new EmptyBorder(0, 8, 0, 8));

        GridBagConstraints c;
        //region Panels
        this.panel_image = new ImagePanel();
        this.panel_image.setBorder(new LineBorder(Color.LIGHT_GRAY, 1));
        c = new GridBagConstraints();
        c.weightx = 0.4;
        c.weighty = 0.5;
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

        this.panel_bids = new JPanel(new BorderLayout());
        this.panel_bids.setOpaque(false);
        c = new GridBagConstraints();
        c.gridheight = 2;
        c.weightx = 0.2;
        c.weighty = 1;
        c.gridx = 2;
        c.gridy = 0;
        c.insets = new Insets(16, 16, 16, 16);
        c.fill = GridBagConstraints.BOTH;
        this.add(this.panel_bids, c);

        this.panel_description = new JPanel(new GridBagLayout());
        this.panel_description.setOpaque(false);
        c = new GridBagConstraints();
        c.weightx = 0.7;
        c.weighty = 0.5;
        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 2;
        c.insets = new Insets(16, 16, 16, 16);
        c.fill = GridBagConstraints.BOTH;
        this.add(this.panel_description, c);
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

        JLabel label_seller = new JLabel("Seller:", JLabel.TRAILING);
        label_seller.setFont(label_timeRemaining.getFont().deriveFont(12f));
        c = new GridBagConstraints();
        c.gridy = row;
        c.gridx = 0;
        c.weightx = 0.3;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(2, 4, 2, 6);
        container.add(label_seller, c);

        this.link_seller = new JLinkLabel("", JLinkLabel.LEADING);
        this.link_seller.setFont(this.link_seller.getFont().deriveFont(Font.BOLD, 12f));
        c.gridx = 1;
        c.weightx = 0.7;
        c.insets = new Insets(2, 6, 2, 4);
        container.add(this.link_seller, c);
        row++;

        this.label_winner = new JLabel("Winner:", JLabel.TRAILING);
        this.label_winner.setFont(this.label_winner.getFont().deriveFont(12f));
        this.label_winner.setVisible(false);
        c = new GridBagConstraints();
        c.gridy = row;
        c.gridx = 0;
        c.weightx = 0.3;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(2, 4, 2, 6);
        container.add(this.label_winner, c);

        this.content_winner = new JLinkLabel("", JLabel.LEADING);
        this.content_winner.setFont(this.content_winner.getFont().deriveFont(Font.BOLD, 12f));
        this.content_winner.setVisible(false);
        c.gridx = 1;
        c.weightx = 0.7;
        c.insets = new Insets(2, 6, 2, 4);
        container.add(this.content_winner, c);
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

        this.content_reserve = new JLabel("", JLabel.LEADING);
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

        JLabel label_itemID = new JLabel("Item ID:", JLabel.TRAILING);
        c = new GridBagConstraints();
        c.gridy = row;
        c.gridx = 0;
        c.weightx = 0.3;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(8, 6, 2, 4);
        container.add(label_itemID, c);

        this.content_itemID = new JTextField("");
        this.content_itemID.setEditable(false);
        c.gridy = row;
        c.gridx = 1;
        c.weightx = 0.7;
        c.insets = new Insets(8, 4, 2, 32);
        container.add(this.content_itemID, c);
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

        //region Bids Panel
        titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);

        JLabel labelBids = new JLabel("Bids", JLabel.LEADING);
        labelBids.setFont(labelDetails.getFont().deriveFont(16f));
        titlePanel.add(labelBids, BorderLayout.CENTER);
        titlePanel.add(new JSeparator(JSeparator.HORIZONTAL), BorderLayout.PAGE_END);
        this.panel_bids.add(titlePanel, BorderLayout.PAGE_START);

        this.model_bid = new BidTableModel();
        this.content_bids = new JTable(this.model_bid);
        this.content_bids.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        this.content_bids.setShowGrid(false);
        this.content_bids.setShowHorizontalLines(false);
        this.content_bids.setShowVerticalLines(false);
        this.content_bids.setRowMargin(0);
        this.content_bids.setIntercellSpacing(new Dimension(4, 2));
        this.content_bids.setFillsViewportHeight(true);
        this.content_bids.setRowSorter(new TableRowSorter<>(this.model_bid));
        this.content_bids.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.content_bids.setDragEnabled(false);

        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(JLabel.RIGHT);
        this.content_bids.getColumnModel().getColumn(1).setCellRenderer(rightRenderer);
        this.content_bids.getColumnModel().getColumn(2).setCellRenderer(rightRenderer);

        JScrollPane scroller = new JScrollPane(this.content_bids);
        scroller.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroller.setBorder(new EmptyBorder(2, 2, 2, 2));

        this.panel_bids.add(scroller, BorderLayout.CENTER);
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
        if(itemID == null) return false;
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
            Main.getMain().updateTitle();

            if (this.item.getImage() != null) this.panel_image.setImage(this.item.getImage(), true);

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
                this.text_bid.setValue(this.item.getReserve());
            } else {
                this.text_bid.setValue(this.item.getTopBid().getPrice());
            }

            if (this.item.getNumberOfBids() == 0) {
                this.content_topBid.setText("-");
            } else {
                this.content_topBid.setText(item.getTopBid().getPriceString());
            }

            this.content_itemID.setText(UUIDUtils.UUIDToBase64String(this.item.getID()));

            this.content_description.setText("<html>" + this.item.getDescription() + "</html>");

            boolean canBid = !this.item.getUserID().equals(Client.getUser().getUniqueID()) && this.item.getAuctionStatus() == Item.AUCTION_STARTED;
            this.text_bid.setEnabled(canBid);
            this.btn_bid.setEnabled(canBid);

            this.model_bid.removeAll();
            ArrayList<Bid> bids = new ArrayList<>();
            this.item.getBids().forEach(bid -> bids.add(new Bid(bid)));
            Collections.sort(bids, Collections.reverseOrder());
            this.model_bid.add(bids);

            if (!canBid && this.item.getAuctionStatus() == Item.AUCTION_WON) {
                this.content_winner.setText(new Bid(this.item.getTopBid()).getUser().getFullName());
                this.content_winner.setVisible(true);
                this.label_winner.setVisible(true);
                this.text_bid.setVisible(false);
                this.btn_bid.setVisible(false);
            } else {
                this.content_winner.setText("");
                this.content_winner.setVisible(false);
                this.label_winner.setVisible(false);
                this.text_bid.setVisible(true);
                this.btn_bid.setVisible(true);
            }

            this.getRootPane().revalidate();
            this.getRootPane().repaint();
        });
    }

    private void setUser(User user) {
        if (this.item.getUserID().equals(user.getUniqueID())) {
            this.link_seller.setText(user.getFullName());
        }
    }

    /**
     * Gets the default button for the panel
     *
     * @return Default button
     */
    @Override
    public JButton getDefaultButton() {
        return this.btn_bid;
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
                    break;

                case PLACE_BID_FAIL:
                    SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(ViewItem.this, "Sorry, we failed to place that bid.\nReason: " + packet.getPayload(), "Bid Failed!", JOptionPane.ERROR_MESSAGE));
                    break;

                case PLACE_BID_SUCCESS:
                    SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(ViewItem.this, "Bid successfully placed!", "Bid Placed!", JOptionPane.INFORMATION_MESSAGE));
                    break;

                case BID:
                    Bid bid = new Bid((shared.Bid) packet.getPayload());
                    // If the bid is for the current loaded item, reload that item from the server.
                    if (ViewItem.this.item != null && bid.getItemID().equals(ViewItem.this.item.getID())) {
                        Client.sendPacket(new Packet<>(PacketType.FETCH_ITEM, bid.getItemID()));
                    }
                    break;
            }
        }
    }
}
