package client.windows;

import client.Client;
import client.components.ItemPanel;
import client.components.WindowPanel;
import shared.*;
import shared.components.ItemList;
import shared.events.PacketListener;
import shared.utils.ReplyWaiter;
import shared.utils.UUIDUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.*;

import static shared.SearchOptions.Direction.DESC;
import static shared.SearchOptions.Sort.TIME;


/**
 * View User panel
 *
 * @author Huw Jones
 * @since 20/04/2016
 */
public class ViewUser extends WindowPanel {
    private User user;

    private final Set<Long> searchIDs = new HashSet<>();
    private final LinkedHashMap<UUID, Item> items = new LinkedHashMap<>();

    private JPanel panel_details;
    private JPanel panel_items;
    private JPanel panel_bids;

    private JLabel content_id;
    private JLabel content_username;
    private JLabel content_firstName;
    private JLabel content_lastName;
    private JLabel content_joined;

    private ItemList<Item> content_items;

    public ViewUser() {
        super("");
        this.initComponents();
        this.initEventListeners();
    }

    private void initComponents() {
        this.setLayout(new GridBagLayout());
        this.setBorder(new EmptyBorder(0, 8, 0, 8));

        GridBagConstraints c;
        //region Panels
        this.panel_details = new JPanel(new GridBagLayout());
        this.panel_details.setOpaque(false);
        c = new GridBagConstraints();
        c.weightx = 0.2;
        c.weighty = 0.4;
        c.insets = new Insets(16, 16, 16, 16);
        c.gridx = 0;
        c.gridy = 0;
        c.fill = GridBagConstraints.BOTH;
        this.add(this.panel_details, c);

        this.panel_items = new JPanel(new GridBagLayout());
        this.panel_items.setOpaque(false);
        c = new GridBagConstraints();
        c.weightx = 0.8;
        c.weighty = 1;
        c.insets = new Insets(16, 16, 16, 16);
        c.gridx = 1;
        c.gridy = 0;
        c.gridheight = 2;
        c.fill = GridBagConstraints.BOTH;
        this.add(this.panel_items, c);

        this.panel_bids = new JPanel(new GridBagLayout());
        this.panel_bids.setOpaque(false);
        c = new GridBagConstraints();
        c.weightx = 0.2;
        c.weighty = 0.6;
        c.insets = new Insets(16, 16, 16, 16);
        c.gridx = 0;
        c.gridy = 1;
        c.fill = GridBagConstraints.BOTH;
        this.add(this.panel_bids, c);
        //endregion

        //region Details Panel
        this.panel_details.setLayout(new BorderLayout());

        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        JLabel labelDetails = new JLabel("Details", JLabel.LEADING);
        labelDetails.setFont(labelDetails.getFont().deriveFont(16f));
        titlePanel.add(labelDetails, BorderLayout.CENTER);
        titlePanel.add(new JSeparator(JSeparator.HORIZONTAL), BorderLayout.PAGE_END);
        this.panel_details.add(titlePanel, BorderLayout.PAGE_START);

        JPanel container = new JPanel(new GridBagLayout());
        container.setOpaque(false);

        int row = 0;
        JLabel label_uniqueID = new JLabel("User ID:", JLabel.TRAILING);
        label_uniqueID.setFont(label_uniqueID.getFont().deriveFont(12f));
        c = new GridBagConstraints();
        c.gridy = row;
        c.gridx = 0;
        c.weightx = 0.3;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(2, 4, 2, 6);
        container.add(label_uniqueID, c);

        this.content_id = new JLabel("", JLabel.LEADING);
        this.content_id.setFont(this.content_id.getFont().deriveFont(12f));
        c.gridx = 1;
        c.weightx = 0.7;
        c.insets = new Insets(2, 6, 2, 4);
        container.add(this.content_id, c);
        row++;

        JLabel label_username = new JLabel("Username:", JLabel.TRAILING);
        label_username.setFont(label_username.getFont().deriveFont(12f));
        c = new GridBagConstraints();
        c.gridy = row;
        c.gridx = 0;
        c.weightx = 0.3;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(2, 4, 2, 6);
        container.add(label_username, c);

        this.content_username = new JLabel("", JLabel.LEADING);
        this.content_username.setFont(this.content_username.getFont().deriveFont(12f));
        c.gridx = 1;
        c.weightx = 0.7;
        c.insets = new Insets(2, 6, 2, 4);
        container.add(this.content_username, c);
        row++;

        JLabel labelFirstName = new JLabel("First Name:", JLabel.TRAILING);
        labelFirstName.setFont(labelFirstName.getFont().deriveFont(12f));
        c = new GridBagConstraints();
        c.gridy = row;
        c.gridx = 0;
        c.weightx = 0.3;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(2, 4, 2, 6);
        container.add(labelFirstName, c);

        this.content_firstName = new JLabel("", JLabel.LEADING);
        this.content_firstName.setFont(this.content_firstName.getFont().deriveFont(12f));
        c.gridx = 1;
        c.weightx = 0.7;
        c.insets = new Insets(2, 6, 2, 4);
        container.add(this.content_firstName, c);
        row++;

        JLabel labelLastName = new JLabel("Last Name:", JLabel.TRAILING);
        labelLastName.setFont(labelLastName.getFont().deriveFont(12f));
        c = new GridBagConstraints();
        c.gridy = row;
        c.gridx = 0;
        c.weightx = 0.3;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(2, 4, 2, 6);
        container.add(labelLastName, c);

        this.content_lastName = new JLabel("", JLabel.LEADING);
        this.content_lastName.setFont(this.content_lastName.getFont().deriveFont(12f));
        c.gridx = 1;
        c.weightx = 0.7;
        c.insets = new Insets(2, 6, 2, 4);
        container.add(this.content_lastName, c);
        row++;

        JLabel labelJoined = new JLabel("Date Joined:", JLabel.TRAILING);
        labelJoined.setFont(labelJoined.getFont().deriveFont(12f));
        c = new GridBagConstraints();
        c.gridy = row;
        c.gridx = 0;
        c.weightx = 0.3;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(2, 4, 2, 6);
        container.add(labelJoined, c);

        this.content_joined = new JLabel("", JLabel.LEADING);
        this.content_joined.setFont(this.content_joined.getFont().deriveFont(12f));
        c.gridx = 1;
        c.weightx = 0.7;
        c.insets = new Insets(2, 6, 2, 4);
        container.add(this.content_joined, c);
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

        //region Item Panel
        this.panel_items.setLayout(new BorderLayout());
        titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);

        JLabel labelItems = new JLabel("Items", JLabel.LEADING);
        labelItems.setFont(labelDetails.getFont().deriveFont(16f));
        titlePanel.add(labelItems, BorderLayout.CENTER);
        titlePanel.add(new JSeparator(JSeparator.HORIZONTAL), BorderLayout.PAGE_END);
        this.panel_items.add(titlePanel, BorderLayout.PAGE_START);

        this.content_items = new ItemList<Item>() {
            @Override
            public Component drawItem(Item item) {
                return new ItemPanel(item);
            }
        };

        JScrollPane scroller = new JScrollPane(this.content_items);
        scroller.setOpaque(false);
        scroller.setBorder(new EmptyBorder(4, 8, 2, 8));
        scroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroller.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scroller.getVerticalScrollBar().setUnitIncrement(8);
        this.panel_items.add(scroller, BorderLayout.CENTER);
        //endregion

        //region Bids Panel
        this.panel_bids.setLayout(new BorderLayout());
        titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);

        JLabel labelBids = new JLabel("Bids", JLabel.LEADING);
        labelBids.setFont(labelDetails.getFont().deriveFont(16f));
        titlePanel.add(labelBids, BorderLayout.CENTER);
        titlePanel.add(new JSeparator(JSeparator.HORIZONTAL), BorderLayout.PAGE_END);
        this.panel_bids.add(titlePanel, BorderLayout.PAGE_START);

        /*this.model_bid = new BidTableModel();
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

        this.panel_bids.add(scroller, BorderLayout.CENTER);*/
        //endregion
    }

    private void initEventListeners() {
        Client.addPacketListener(new PacketHandler());
    }


    public boolean setUser(UUID userID) {
        if (userID == null) return false;
        ReplyWaiter handler = new ReplyWaiter(Client.getConfig().getTimeout()) {
            @Override
            public void packetReceived(Packet packet) {
                switch (packet.getType()) {
                    case USER:
                        if (((User) packet.getPayload()).getUniqueID().equals(userID)) {
                            this.waiter.replyReceived();
                            SwingUtilities.invokeLater(() -> ViewUser.this.setUser((User) packet.getPayload()));
                        }
                        break;
                }
            }
        };

        Client.addPacketListener(handler);
        Client.sendPacket(new Packet<>(PacketType.FETCH_USER, userID));

        handler.getWaiter().waitForReply();
        return !handler.getWaiter().isReplyTimedOut();
    }

    public void setUser(User user) {
        this.user = user;
        SearchOptions search = new SearchOptions(TIME, DESC, "", this.user.getUniqueID(), new Keyword(-1, ""), new Timestamp(0), new Timestamp(Long.MAX_VALUE), BigDecimal.ZERO, false, true);
        synchronized (this.searchIDs){
            this.searchIDs.add(search.getSearchID());
        }
        Client.sendPacket(new Packet<>(PacketType.SEARCH, search));
        SwingUtilities.invokeLater(() -> {
            // Title
            this.setTitle(user.getFullName());
            Main.getMain().updateTitle();

            this.content_id.setText(UUIDUtils.UUIDToBase64String(user.getUniqueID()));
            this.content_username.setText(user.getUsername());
            this.content_firstName.setText(user.getFirstName());
            this.content_lastName.setText(user.getLastName());
            this.content_joined.setText(user.getJoinedString());

        });
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

    private void displayItems() {
        this.content_items.removeAllElements();
        this.content_items.addAllElements(new ArrayList<>(this.items.values()));
    }

    private class PacketHandler implements PacketListener {
        @Override
        public void packetReceived(Packet packet) {
            switch(packet.getType()){
                case SEARCH_RESULTS:
                    SearchResults results = (SearchResults)packet.getPayload();
                    synchronized (ViewUser.this.searchIDs){
                        if(!ViewUser.this.searchIDs.contains(results.getSearchID())){
                            return;
                        }
                        ViewUser.this.searchIDs.remove(results.getSearchID());
                    }
                    ViewUser.this.items.clear();
                    results.getItems().forEach(item -> ViewUser.this.items.put(item.getID(), item));
                    SwingUtilities.invokeLater(ViewUser.this::displayItems);
                    break;
            }
        }
    }
}
