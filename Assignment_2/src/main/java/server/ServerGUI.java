package server;

import server.events.LoginListener;
import server.events.ServerAdapter;
import server.events.ServerListener;
import server.objects.User;
import server.utils.JTextAreaAppender;
import shared.Item;
import shared.components.JTextAreaOutputStream;
import shared.utils.WindowTemplate;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.PrintStream;

/**
 * Server GUI interface
 *
 * @author Huw Jones
 * @since 22/04/2016
 */
public final class ServerGUI extends WindowTemplate {

    private static final String WINDOW_TITLE = "Biddr Server Control Panel";
    private final Server server = new Server();
    private JPanel panel_GUI;
    private JPanel panel_controls;
    private JPanel panel_results;
    private JPanel panel_users;
    private JPanel panel_items;
    private JPanel panel_console;
    private JButton btn_start;
    private JButton btn_stop;
    private JButton btn_results;
    private JList<User> list_users;
    private DefaultListModel<User> lm_users;
    private JList<Item> list_items;
    private DefaultListModel<Item> lm_items;
    private JTextArea text_console;

    private ServerListener serverListener = new ServerHandler();
    private LoginListener userListener = new UserHandler();

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

    private void redirectConsole(){
        PrintStream textStream = new PrintStream(new JTextAreaOutputStream(this.text_console));
        System.setOut(textStream);
        System.setErr(textStream);
    }

    /**
     * Initialises the GUI components
     */
    @Override
    protected void initComponents() {
        this.panel_GUI = new JPanel(new GridBagLayout());
        GridBagConstraints c;
        BorderLayout borderLayout;

        //region Controls
        borderLayout = new BorderLayout();
        this.panel_controls = new JPanel(borderLayout);
        this.panel_controls.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Controls"));

        JPanel panel_controls_inner = new JPanel(new GridLayout(1, 3));

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

        this.panel_controls.add(panel_controls_inner, BorderLayout.CENTER);

        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 3;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        this.panel_GUI.add(this.panel_controls, c);
        //endregion

        //region Results
        borderLayout = new BorderLayout();
        this.panel_results = new JPanel(borderLayout);
        this.panel_results.setPreferredSize(new Dimension(260, 240));
        this.panel_results.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Auction Results"));

        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 1;
        c.weightx = 1;
        c.weighty = 0.4;
        c.fill = GridBagConstraints.BOTH;
        this.panel_GUI.add(this.panel_results, c);
        //endregion

        //region Items
        borderLayout = new BorderLayout();
        this.panel_items = new JPanel(borderLayout);
        this.panel_items.setPreferredSize(new Dimension(260, 240));
        this.panel_items.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Items in Auction"));

        this.lm_items = new DefaultListModel<>();
        this.list_items = new JList<>(this.lm_items);
        this.list_items.setDragEnabled(false);
        this.list_items.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.list_items.setLayoutOrientation(JList.VERTICAL);

        this.panel_items.add(this.list_items, BorderLayout.CENTER);

        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 1;
        c.weightx = 1;
        c.weighty = 0.4;
        c.fill = GridBagConstraints.BOTH;
        this.panel_GUI.add(this.panel_items, c);
        //endregion

        //region Users
        borderLayout = new BorderLayout();
        this.panel_users = new JPanel(borderLayout);
        this.panel_users.setPreferredSize(new Dimension(260, 240));
        this.panel_users.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Logged in Users"));

        this.lm_users = new DefaultListModel<>();
        this.list_users = new JList<>(this.lm_users);
        this.list_users.setDragEnabled(false);
        this.list_users.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.list_users.setLayoutOrientation(JList.VERTICAL);

        this.panel_users.add(this.list_users, BorderLayout.CENTER);
        c = new GridBagConstraints();
        c.gridx = 2;
        c.gridy = 1;
        c.weightx = 1;
        c.weighty = 0.4;
        c.fill = GridBagConstraints.BOTH;
        this.panel_GUI.add(this.panel_users, c);
        //endregion

        //region Console
        borderLayout = new BorderLayout();
        this.panel_console = new JPanel(borderLayout);
        this.panel_console.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Console Log"));

        this.text_console = new JTextArea();
        this.text_console.setEditable(false);
        this.text_console.setFont(this.text_console.getFont().deriveFont(11f));
        JTextAreaAppender.addTextArea(this.text_console);
        this.panel_console.add(new JScrollPane(this.text_console), BorderLayout.CENTER);

        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 2;
        c.gridwidth = 3;
        c.weightx = 1;
        c.weighty = 0.6;
        c.fill = GridBagConstraints.BOTH;
        this.panel_GUI.add(this.panel_console, c);
        //endregion

        this.setContentPane(this.panel_GUI);
    }

    private void initEventListeners() {
        this.btn_start.addActionListener(e -> server.run());
        this.btn_stop.addActionListener(e -> server.shutdownServer());
        this.addWindowListener(new WindowHandler());
        Server.addServerListener(this.serverListener);
        Server.addLoginListener(this.userListener);
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
            SwingUtilities.invokeLater(() -> ServerGUI.this.lm_users.addElement(user));
        }

        /**
         * Fired when a user logs out of the server
         *
         * @param user User
         */
        @Override
        public void userLoggedOut(User user) {
            SwingUtilities.invokeLater(() -> ServerGUI.this.lm_users.removeElement(user));
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
