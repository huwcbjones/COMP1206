package client.windows;

import client.Client;
import client.components.WindowPanel;
import client.components.WindowTemplate;
import client.events.LoginAdapter;
import shared.Item;
import shared.Packet;
import shared.events.ConnectionAdapter;
import shared.events.PacketListener;
import shared.utils.RunnableAdapter;
import shared.utils.UUIDUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;
import java.util.UUID;

/**
 * Main Application Window
 *
 * @author Huw Jones
 * @since 27/03/2016
 */
public final class Main extends WindowTemplate {

    public final static String PANEL_SEARCH = "SearchItems";
    public final static String PANEL_NEWITEM = "NewAuction";
    public final static String PANEL_VIEWITEM = "ViewItem";
    public final static String PANEL_VIEWUSER = "ViewUser";
    private static Main main;
    private boolean promptExit = true;
    private JPanel panel_title;
    private JLabel label_title;
    private JPanel panel_cards;
    private HashMap<String, WindowPanel> panels = new HashMap<>();
    private WindowPanel activePanel = null;
    private SearchItems panel_search;
    private NewAuction panel_newAuction;
    private ViewItem panel_viewItem;
    private ViewUser panel_viewUser;
    private JMenuBar menuBar;
    private JMenu menu_file;
    private JMenuItem menu_file_logout;
    private JMenuItem menu_file_exit;
    private JMenu menu_items;
    private JMenuItem menu_items_search;
    private JMenuItem menu_items_new;
    private JMenuItem menu_items_fromID;
    private JMenuItem menu_items_myItems;
    private JMenuItem menu_items_myBids;
    private JMenu menu_help;
    private JMenuItem menu_help_about;

    public Main() {
        super("Home");
        Main.main = this;
        this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        this.setMinimumSize(new Dimension(800, 600));

        this.panels.put(PANEL_SEARCH, this.panel_search);
        this.panels.put(PANEL_NEWITEM, this.panel_newAuction);
        this.panels.put(PANEL_VIEWITEM, this.panel_viewItem);
        this.panels.put(PANEL_VIEWUSER, this.panel_viewUser);
        this.changePanel(PANEL_SEARCH);
    }

    /**
     * Initialises the GUI components
     */
    @Override
    protected void initComponents() {
        this.initMainMenu();
        this.initPanels();
        initEventListeners();
    }

    /**
     * Sets the title for this frame to the specified string.
     *
     * @param title the title to be displayed in the frame's border.
     *              A <code>null</code> value
     *              is treated as an empty string, "".
     * @see #getTitle
     */
    @Override
    public void setTitle(String title) {
        super.setTitle(title);
        this.label_title.setText(title);
    }

    private void initPanels() {
        this.createTitlePanel();
        this.panel_cards = new JPanel(new CardLayout());

        this.panel_search = new SearchItems();
        this.panel_cards.add(panel_search, PANEL_SEARCH);

        this.panel_newAuction = new NewAuction();
        this.panel_cards.add(panel_newAuction, PANEL_NEWITEM);

        this.panel_viewItem = new ViewItem();
        this.panel_cards.add(panel_viewItem, PANEL_VIEWITEM);

        this.panel_viewUser = new ViewUser();
        this.panel_cards.add(panel_viewUser, PANEL_VIEWUSER);

        this.add(this.panel_cards, BorderLayout.CENTER);
    }

    private void initMainMenu() {
        this.menuBar = new JMenuBar();

        //region File
        this.menu_file = new JMenu("File");
        this.menu_file.setMnemonic('f');

        this.menu_file_logout = new JMenuItem("Logout");
        this.menu_file_logout.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.CTRL_MASK));
        this.menu_file.add(this.menu_file_logout);

        this.menu_file.addSeparator();

        this.menu_file_exit = new JMenuItem("Exit");
        this.menu_file_exit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, InputEvent.ALT_MASK));
        this.menu_file.add(this.menu_file_exit);

        this.menuBar.add(this.menu_file);
        //endregion

        //region Items
        this.menu_items = new JMenu("Items");
        this.menu_items.setMnemonic('i');

        this.menu_items_new = new JMenuItem("New Item");
        this.menu_items_new.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_MASK));
        this.menu_items_new.setName(PANEL_NEWITEM);
        this.menu_items.add(this.menu_items_new);

        this.menu_items.addSeparator();

        this.menu_items_search = new JMenuItem("Search");
        this.menu_items_search.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK));
        this.menu_items_new.setName(PANEL_SEARCH);
        this.menu_items.add(this.menu_items_search);

        this.menu_items_fromID = new JMenuItem("From ID");
        this.menu_items_fromID.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_MASK));
        this.menu_items.add(this.menu_items_fromID);

        this.menu_items.addSeparator();

        this.menu_items_myItems = new JMenuItem("My Items");
        this.menu_items_myItems.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, InputEvent.CTRL_MASK));
        this.menu_items_myItems.setName(PANEL_VIEWUSER);
        this.menu_items.add(this.menu_items_myItems);

        this.menu_items_myBids = new JMenuItem("My Bids");
        this.menu_items_myBids.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B, InputEvent.CTRL_MASK));
        this.menu_items_myBids.setName(PANEL_SEARCH);
        this.menu_items.add(this.menu_items_myBids);

        this.menuBar.add(this.menu_items);
        //endregion

        //region Help
        this.menu_help = new JMenu("Help");
        this.menu_help.setMnemonic('h');

        this.menu_help_about = new JMenuItem("About");
        this.menu_help_about.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
        this.menu_help.add(this.menu_help_about);

        this.menuBar.add(this.menu_help);
        //endregion

        this.setJMenuBar(this.menuBar);
    }

    private void createTitlePanel() {
        this.panel_title = new JPanel(new BorderLayout());
        this.panel_title.setBackground(Color.WHITE);
        this.panel_title.setBorder(new EmptyBorder(new Insets(6, 6, 6, 6)));
        this.label_title = new JLabel(this.getTitle(), JLabel.LEADING);
        this.label_title.setFont(this.label_title.getFont().deriveFont(18f));
        this.panel_title.add(this.label_title, BorderLayout.CENTER);
        this.add(this.panel_title, BorderLayout.PAGE_START);
    }

    private void initEventListeners() {
        this.addWindowListener(new WindowClosingHandler());
        Client.addLoginListener(new LoginAdapter() {
            @Override
            public void logout() {
                Main.this.promptExit = false;
                Main.this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
                Main.this.dispatchEvent(new WindowEvent(Main.this, WindowEvent.WINDOW_CLOSING));
                Client.removeLoginListener(this);
            }
        });
        Client.addConnectionListener(new ConnectionAdapter() {
            @Override
            public void connectionClosed(String reason) {
                Main.this.promptExit = false;
                Main.this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
                Main.this.dispatchEvent(new WindowEvent(Main.this, WindowEvent.WINDOW_CLOSING));
            }
        });

        this.menu_file_logout.addActionListener(e ->
            SwingUtilities.invokeLater(() -> {
                int result = JOptionPane.showConfirmDialog(Main.this,
                    "Are you sure you want to log out?",
                    "Logout?",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE
                );
                if (result == JOptionPane.OK_OPTION) {
                    Client.logout();
                }
            })
        );
        this.menu_file_exit.addActionListener(e -> this.dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING)));
        this.menu_items_new.addActionListener(e -> this.changePanel(PANEL_NEWITEM));
        this.menu_items_search.addActionListener(e -> this.changePanel(PANEL_SEARCH));
        this.menu_items_fromID.addActionListener(new GetItemByID());
        this.menu_items_myItems.addActionListener(e -> this.displayUser(Client.getUser().getUniqueID()));
        Client.addPacketListener(new PacketHandler());
    }

    /**
     * Sets the displayed panel
     *
     * @param panelID The panel that should be changed to
     */
    public void changePanel(String panelID) {
        CardLayout layout = (CardLayout) this.panel_cards.getLayout();
        layout.show(this.panel_cards, panelID);
        this.activePanel = this.panels.get(panelID);
        updateTitle();
        this.getRootPane().setDefaultButton(this.activePanel.getDefaultButton());
    }

    public void showNotification(String title, String msg, int type, RunnableAdapter action) {
        int result = JOptionPane.showConfirmDialog(this, msg, title, JOptionPane.YES_NO_OPTION, type);
        if (result == JOptionPane.YES_OPTION) {
            action.run();
        }
    }

    public void updateTitle() {
        this.setTitle(this.activePanel.getTitle());
    }

    public void displayItem(UUID itemID) {
        if (this.panel_viewItem.setItem(itemID)) {
            this.changePanel(PANEL_VIEWITEM);
        } else {
            JOptionPane.showMessageDialog(
                Main.this,
                "Failed to load that item from the server.",
                "Failed to load item.",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }

    public void displayUser(UUID userID) {
        if (this.panel_viewUser.setUser(userID)) {
            this.changePanel(PANEL_VIEWUSER);
        } else {
            JOptionPane.showMessageDialog(
                Main.this,
                "Failed to load that user from the server.",
                "Failed to load user.",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private class GetItemByID implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String itemID = JOptionPane.showInputDialog(Main.this, "Please enter the item's ID code.\nE.g.: JbxroUMLRoKzG9SzBn1d6Q==");
            UUID uuid = UUIDUtils.Base64StringToUUID(itemID);
            Main.getMain().displayItem(uuid);
        }
    }

    public static Main getMain() {
        return Main.main;
    }

    private class WindowClosingHandler extends WindowAdapter {
        /**
         * Invoked when a window is in the process of being closed.
         * The close operation can be overridden at this point.
         *
         * @param e
         */
        @Override
        public void windowClosing(WindowEvent e) {
            if (!promptExit) {
                return;
            }
            int result = JOptionPane.showConfirmDialog(Main.this,
                "Are you sure you want to exit Biddr?",
                "Exit Biddr?",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE
            );
            if (result == JOptionPane.OK_OPTION) {
                Main.this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            } else {
                Main.this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
            }
        }
    }

    private class PacketHandler implements PacketListener {
        @Override
        public void packetReceived(Packet packet) {
            switch (packet.getType()) {
                case CREATE_ITEM_SUCCESS:
                    SwingUtilities.invokeLater(() -> Main.this.changePanel(PANEL_SEARCH));
                    break;
                case AUCTION_WIN:
                    Item item = (Item) packet.getPayload();
                    if (item.getUserID().equals(Client.getUser().getUniqueID())) {
                        SwingUtilities.invokeLater(() -> Main.getMain().showNotification(
                            "Auction Won!",
                            "An item you auctioned has been sold!\nDo you want to view that item?",
                            JOptionPane.INFORMATION_MESSAGE,
                            new RunnableAdapter() {
                                @Override
                                public void runSafe() throws Exception {
                                    Main.getMain().displayItem(item.getID());
                                }
                            }
                        ));

                    } else {
                        SwingUtilities.invokeLater(() -> Main.getMain().showNotification(
                            "Auction Won!",
                            "Congratulations, you've won an auction!\nDo you want to view that item?",
                            JOptionPane.INFORMATION_MESSAGE,
                            new RunnableAdapter() {
                                @Override
                                public void runSafe() throws Exception {
                                    Main.getMain().displayItem(item.getID());
                                }
                            }
                        ));
                    }
                    break;

            }
        }
    }
}
