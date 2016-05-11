package server;

import server.ServerComms.ClientConnection;
import server.components.AuctionResultTableModel;
import server.components.ItemTableModel;
import server.components.UserTableModel;
import server.events.AuctionListener;
import server.events.LoginListener;
import server.events.ServerAdapter;
import server.events.ServerListener;
import server.objects.Item;
import server.objects.User;
import server.utils.JTextAreaAppender;
import shared.components.JTextAreaOutputStream;
import shared.utils.WindowTemplate;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.UUID;

/**
 * Server GUI interface
 *
 * @author Huw Jones
 * @since 22/04/2016
 */
public final class ServerGUI extends WindowTemplate {

    private static final String WINDOW_TITLE = "Biddr Server Control Panel";
    // Make sure text area appender is reference so it gets compiled
    private static final JTextAreaAppender J_TEXT_AREA_APPENDER = null;
    private final Server server = new Server();
    private JPanel panel_GUI;
    private JPanel panel_controls;
    private JSplitPane panel_split;
    private JPanel panel_tables;
    private JPanel panel_results;
    private JPanel panel_users;
    private JPanel panel_items;
    private JPanel panel_console;
    private JButton btn_start;
    private JButton btn_stop;
    private JButton btn_results;
    private JLabel label_time;
    private JTable table_users;
    private UserTableModel model_users;
    private JTable table_items;
    private ItemTableModel model_items;
    private AuctionResultTableModel model_results;
    private JTable table_results;
    private JTextArea text_console;

    private Timer clock;

    private ServerListener serverListener = new ServerHandler();
    private LoginListener userListener = new UserHandler();
    private AuctionListener auctionListener = new AuctionHandler();

    public ServerGUI() {
        super();
        this.redirectConsole();
        this.setMinimumSize(new Dimension(800, 600));
        this.pack();
        this.setVisible(true);
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.setTitle(WINDOW_TITLE + " [STOPPED]");

        this.initEventListeners();
    }

    private void redirectConsole() {
        PrintStream textStream = new PrintStream(new JTextAreaOutputStream(this.text_console));
        System.setOut(textStream);
        System.setErr(textStream);
    }

    /**
     * Initialises the GUI components
     */
    @Override
    protected void initComponents() {
        this.panel_GUI = new JPanel(new BorderLayout());
        BorderLayout borderLayout;

        this.panel_split = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        this.panel_split.setResizeWeight(0.6d);
        this.panel_split.setDividerSize(5);
        this.panel_split.setContinuousLayout(true);

        this.panel_tables = new JPanel(new GridLayout(1, 3));
        //region Controls
        borderLayout = new BorderLayout();
        this.panel_controls = new JPanel(borderLayout);
        this.panel_controls.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Controls"));

        JPanel panel_controls_inner = new JPanel(new GridLayout(1, 4));

        this.btn_start = new JButton("Start");
        this.btn_start.setMnemonic('s');
        panel_controls_inner.add(this.btn_start);

        this.btn_stop = new JButton("Stop");
        this.btn_stop.setMnemonic('p');
        this.btn_stop.setEnabled(false);
        panel_controls_inner.add(this.btn_stop);

        this.btn_results = new JButton("Get Auction Results");
        this.btn_results.setMnemonic('r');
        this.btn_results.setEnabled(false);
        panel_controls_inner.add(this.btn_results);

        this.label_time = new JLabel("", JLabel.CENTER);
        this.label_time.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
        this.setTime();
        panel_controls_inner.add(this.label_time);

        this.panel_controls.add(panel_controls_inner, BorderLayout.CENTER);

        this.panel_GUI.add(this.panel_controls, BorderLayout.PAGE_START);
        //endregion

        //region Results
        borderLayout = new BorderLayout();
        this.panel_results = new JPanel(borderLayout);
        this.panel_results.setPreferredSize(new Dimension(260, 240));
        this.panel_results.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Auction Results"));

        this.model_results = new AuctionResultTableModel();
        this.table_results = new JTable(this.model_results);
        this.table_results.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        this.table_results.setShowGrid(false);
        this.table_results.setShowHorizontalLines(false);
        this.table_results.setShowVerticalLines(true);
        this.table_results.setRowMargin(0);
        this.table_results.setIntercellSpacing(new Dimension(1, 1));
        this.table_results.setFillsViewportHeight(true);
        this.table_results.setRowSorter(new TableRowSorter<>(this.model_results));
        this.panel_results.add(new JScrollPane(this.table_results));

        this.panel_tables.add(this.panel_results);
        //endregion

        //region Items
        borderLayout = new BorderLayout();
        this.panel_items = new JPanel(borderLayout);
        this.panel_items.setPreferredSize(new Dimension(260, 240));
        this.panel_items.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Items in Auction"));

        this.model_items = new ItemTableModel();
        this.table_items = new JTable(this.model_items);
        this.table_items.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        this.table_items.setShowGrid(false);
        this.table_items.setShowHorizontalLines(false);
        this.table_items.setShowVerticalLines(true);
        this.table_items.setRowMargin(0);
        this.table_items.setIntercellSpacing(new Dimension(1, 1));
        this.table_items.setFillsViewportHeight(true);
        this.table_items.setRowSorter(new TableRowSorter<>(this.model_items));
        this.panel_items.add(new JScrollPane(this.table_items));

        this.panel_tables.add(this.panel_items);
        //endregion

        //region Users
        borderLayout = new BorderLayout();
        this.panel_users = new JPanel(borderLayout);
        this.panel_users.setPreferredSize(new Dimension(260, 240));
        this.panel_users.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Logged in Users"));

        this.model_users = new UserTableModel();
        this.table_users = new JTable(this.model_users);
        this.table_users.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        this.table_users.setShowGrid(false);
        this.table_users.setShowHorizontalLines(false);
        this.table_users.setShowVerticalLines(true);
        this.table_users.setRowMargin(0);
        this.table_users.setIntercellSpacing(new Dimension(1, 1));
        this.table_users.setFillsViewportHeight(true);
        this.table_users.setRowSorter(new TableRowSorter<>(this.model_users));
        this.panel_users.add(new JScrollPane(this.table_users), BorderLayout.CENTER);

        this.panel_tables.add(this.panel_users);
        //endregion

        this.panel_split.add(this.panel_tables);
        //region Console
        borderLayout = new BorderLayout();
        this.panel_console = new JPanel(borderLayout);
        this.panel_console.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Console Log"));

        this.text_console = new JTextArea();
        this.text_console.setEditable(false);
        this.text_console.setFont(this.text_console.getFont().deriveFont(11f));
        JTextAreaAppender.addTextArea(this.text_console);
        this.panel_console.add(new JScrollPane(this.text_console), BorderLayout.CENTER);

        this.panel_split.add(this.panel_console);
        //endregion

        this.panel_GUI.add(this.panel_split, BorderLayout.CENTER);
        this.setContentPane(this.panel_GUI);
    }

    private void initEventListeners() {
        this.btn_start.addActionListener(e -> {
            this.text_console.setText("");
            server.run(true);
        });

        this.btn_stop.addActionListener(e -> server.shutdownServer());
        this.addWindowListener(new WindowHandler());
        Server.addServerListener(this.serverListener);
        Server.addLoginListener(this.userListener);
        Server.addAuctionListener(this.auctionListener);

        this.clock = new Timer(1000, e -> SwingUtilities.invokeLater(this::setTime));
        this.clock.setRepeats(true);
        this.clock.start();
    }

    private void setTime() {
        this.label_time.setText(new SimpleDateFormat("HH:mm:ss dd/MM/yyyy").format(Calendar.getInstance().getTime()));
    }

    private class ServerHandler extends ServerAdapter {

        /**
         * Fired when server starts up
         */
        @Override
        public void serverStarting() {
            SwingUtilities.invokeLater(() -> {
                ServerGUI.this.setTitle(WINDOW_TITLE + " [STARTING]");
                ServerGUI.this.btn_start.setEnabled(false);
                ServerGUI.this.btn_stop.setEnabled(false);
                ServerGUI.this.btn_results.setEnabled(false);
            });
        }

        /**
         * Fired when server starts
         */
        @Override
        public void serverStarted() {
            SwingUtilities.invokeLater(() -> {
                ServerGUI.this.setTitle(WINDOW_TITLE + " [RUNNING]");
                ServerGUI.this.btn_start.setEnabled(false);
                ServerGUI.this.btn_stop.setEnabled(true);
                ServerGUI.this.btn_results.setEnabled(true);
            });
        }

        /**
         * Fired when server initiates shutdown
         */
        @Override
        public void serverShuttingDown() {
            SwingUtilities.invokeLater(() -> {
                ServerGUI.this.setTitle(WINDOW_TITLE + " [STOPPING]");
                ServerGUI.this.btn_start.setEnabled(false);
                ServerGUI.this.btn_stop.setEnabled(false);
                ServerGUI.this.btn_results.setEnabled(false);
                ServerGUI.this.table_items.removeAll();
                ServerGUI.this.table_results.removeAll();
                ServerGUI.this.table_users.removeAll();
            });
        }

        /**
         * Fired when server shuts down
         */
        @Override
        public void serverShutdown() {
            SwingUtilities.invokeLater(() -> {
                ServerGUI.this.setTitle(WINDOW_TITLE + " [STOPPED]");
                ServerGUI.this.btn_start.setEnabled(true);
                ServerGUI.this.btn_stop.setEnabled(false);
                ServerGUI.this.btn_results.setEnabled(false);
                ServerGUI.this.table_results.removeAll();
                ServerGUI.this.table_items.removeAll();
                ServerGUI.this.table_users.removeAll();
            });
        }

        /**
         * Fired when the server fails to start
         *
         * @param reason Why the server failed to start
         */
        @Override
        public void serverStartFail(String reason) {
            SwingUtilities.invokeLater(() -> {
                ServerGUI.this.setTitle(WINDOW_TITLE + " [STOPPED]");
                ServerGUI.this.btn_start.setEnabled(true);
                ServerGUI.this.btn_stop.setEnabled(false);
                ServerGUI.this.btn_results.setEnabled(false);
                JOptionPane.showMessageDialog(ServerGUI.this,
                    "Server failed to start.\n" + reason,
                    "Server Failed to Start!",
                    JOptionPane.ERROR_MESSAGE
                );
            });
        }
    }

    private class UserHandler implements LoginListener {

        /**
         * Fired when a user logs into the server
         *
         * @param user User
         */
        @Override
        public void userLoggedIn(User user) {
            SwingUtilities.invokeLater(() -> ServerGUI.this.model_users.add(user));
        }

        /**
         * Fired when a user logs out of the server
         *
         * @param user User
         */
        @Override
        public void userLoggedOut(User user, ClientConnection clientID) {
            log.trace("User Logged Out.");
            SwingUtilities.invokeLater(() -> ServerGUI.this.model_users.remove(user));
        }
    }

    private class AuctionHandler implements AuctionListener {
        /**
         * Fired when an auction starts
         *
         * @param itemID ID of Item
         */
        @Override
        public void auctionStart(UUID itemID) {
            SwingUtilities.invokeLater(() -> ServerGUI.this.model_items.add(Server.getData().getItem(itemID)));
        }

        /**
         * Fired when an auction ends
         *
         * @param itemID ID of Item
         */
        @Override
        public void auctionEnd(UUID itemID, boolean wasWon) {
            SwingUtilities.invokeLater(() -> {
                Item item = Server.getData().getItem(itemID);
                ServerGUI.this.model_items.remove(item);
                if (wasWon) {
                    ServerGUI.this.model_results.add(item);
                }
            });
        }

        /**
         * Fired when a bid is placed on an auction
         *
         * @param itemID ID of Item
         * @param bidID  ID of Bid
         */
        @Override
        public void auctionBid(UUID itemID, UUID bidID) {
            SwingUtilities.invokeLater(() -> ServerGUI.this.model_items.remove(Server.getData().getItem(itemID)));
            SwingUtilities.invokeLater(() -> ServerGUI.this.model_items.add(Server.getData().getItem(itemID)));
        }
    }

    private class WindowHandler extends WindowAdapter {
        /**
         * Invoked when a window is in the process of being closed.
         * The close operation can be overridden at this point.
         *
         * @param e
         */
        @Override
        public void windowClosing(WindowEvent e) {
            Server.removeServerListener(ServerGUI.this.serverListener);
            Server.removeLoginListener(ServerGUI.this.userListener);
        }
    }
}
