package client.windows;

import client.Client;
import client.Config;
import client.events.LoginListener;
import client.utils.ImagePanel;
import client.utils.Server;
import client.utils.SpringUtilities;
import client.utils.WindowTemplate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import shared.User;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * Login window
 *
 * @author Huw Jones
 * @since 27/03/2016
 */
public final class Login extends WindowTemplate {

    private static final Logger log = LogManager.getLogger(Login.class);

    private ImagePanel panel_banner;

    private JPanel panel_fields;
    private JPanel panel_loginButtons;

    private JMenuBar menuBar;
    private JMenu menu_file;
    private JMenuItem menu_file_register;
    private JMenuItem menu_file_exit;
    private JMenu menu_options;
    private JMenuItem menu_options_servers;

    //region panel_fields controls
    private JLabel label_username;
    private JTextField text_username;

    private JLabel label_password;
    private JPasswordField text_password;

    private JLabel label_server;
    private JComboBox<Server> combo_server;
    //endregion

    private JButton btn_cancel;
    private JButton btn_login;

    private final LoginListener loginListener;

    public Login () {
        super("Login");

        // Set frame properties
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.setMinimumSize(new Dimension(280, 218));
        this.setMaximumSize(new Dimension(280, 218));
        this.setLocationRelativeTo(null);
        this.setResizable(false);

        //region Set Enter/Escape key press actions
        this.getRootPane().setDefaultButton(this.btn_login);
        this.btn_cancel.addActionListener(new cancelBtnClickHandler());
        this.btn_login.addActionListener(new loginBtnClickHandler());

        KeyStroke escapeKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, true);
        Action escapeAction = new AbstractAction() {
            @Override
            public void actionPerformed (ActionEvent e) {
                cancelBtnClickHandler handler = new cancelBtnClickHandler();
                handler.actionPerformed(e);
            }
        };
        this.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escapeKeyStroke, "ESCAPE");
        this.getRootPane().getActionMap().put("ESCAPE", escapeAction);
        //endregion

        //region Add Event Handlers
        this.loginListener = new loginHandler();
        this.combo_server.addActionListener(new comboServerChangeHandler());
        this.addWindowListener(new WindowAdapter() {
            /**
             * Invoked when a window is in the process of being closed.
             * The close operation can be overridden at this point.
             *
             * @param e
             */
            @Override
            public void windowClosing (WindowEvent e) {
                Client.removeLoginListener(Login.this.loginListener);
            }
        });
        //endregion
    }

    private void initMainMenu(){
        this.menuBar = new JMenuBar();
        this.menu_file = new JMenu("File");
        this.menu_file.setMnemonic('f');
        this.menu_file_register = new JMenuItem("Register");
        this.menu_file_register.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_MASK));
        this.menu_file_register.getAccessibleContext().setAccessibleDescription("Register for an account.");
        this.menu_file.add(this.menu_file_register);

        this.menu_file.addSeparator();

        this.menu_file_exit = new JMenuItem("Exit");
        this.menu_file_exit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, InputEvent.ALT_MASK));
        this.menu_file.add(this.menu_file_exit);

        this.menuBar.add(this.menu_file);
        this.menu_options = new JMenu("Options");
        this.menu_options.setMnemonic('o');
        this.menuBar.add(this.menu_options);

        this.menu_options_servers = new JMenuItem("Servers");
        this.menu_options_servers.setMnemonic('s');
        this.menu_options.add(this.menu_options_servers);
        this.setJMenuBar(this.menuBar);
    }

    @Override
    protected void initComponents () {
        this.initMainMenu();
        GridBagConstraints c;

        Container container = this.getContentPane();
        container.setLayout(new GridBagLayout());

        this.panel_banner = new ImagePanel();
        try {
            BufferedImage banner = ImageIO.read(Login.class.getResource("/img/biddr_banner_login.png"));
            this.panel_banner.setImage(banner, true);
        } catch (IOException e) {
            log.error("Could not load banner image.");
        }
        c = new GridBagConstraints();
        c.weightx = 1;
        c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;
        container.add(this.panel_banner, c);

        //region panel_fields
        this.panel_fields = new JPanel(new SpringLayout());

        this.label_username = new JLabel("Username", JLabel.LEADING);
        this.panel_fields.add(this.label_username);

        this.text_username = new JTextField();
        this.panel_fields.add(this.text_username);

        this.label_password = new JLabel("Password", JLabel.LEADING);
        this.panel_fields.add(this.label_password);

        this.text_password = new JPasswordField();
        this.panel_fields.add(this.text_password);

        this.label_server = new JLabel("Server", JLabel.LEADING);
        this.panel_fields.add(this.label_server);

        this.combo_server = new JComboBox<>();
        for (Server s : this.config.getServers()) {
            this.combo_server.addItem(s);
        }

        this.panel_fields.add(this.combo_server);

        SpringUtilities.makeCompactGrid(this.panel_fields, 3, 2, 0, 0, 3, 6);

        c = new GridBagConstraints();
        c.gridy = 1;
        c.insets.set(3, 9, 3, 4);
        c.fill = GridBagConstraints.HORIZONTAL;
        container.add(this.panel_fields, c);
        //endregion

        //region panel_loginButtons
        this.panel_loginButtons = new JPanel(new GridBagLayout());
        int row = 0;
        this.btn_cancel = new JButton("Cancel");
        this.btn_cancel.setMnemonic('c');
        c = new GridBagConstraints();
        c.insets.set(3, 6, 3, 3);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridwidth = 2;
        c.weightx = 0.5;
        c.gridx = 0;
        c.gridy = row;
        this.panel_loginButtons.add(this.btn_cancel, c);

        this.btn_login = new JButton("Login");
        this.btn_login.setMnemonic('l');
        c = new GridBagConstraints();
        c.insets.set(3, 3, 3, 6);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridwidth = 2;
        c.weightx = 0.5;
        c.gridx = 2;
        c.gridy = row;
        this.panel_loginButtons.add(this.btn_login, c);
        row++;

        c = new GridBagConstraints();
        c.gridy = 2;
        c.insets.set(3, 0, 3, 0);
        c.fill = GridBagConstraints.HORIZONTAL;
        container.add(this.panel_loginButtons, c);
        //endregion
    }

    private class cancelBtnClickHandler implements ActionListener {
        @Override
        public void actionPerformed (ActionEvent e) {
            Client.removeLoginListener(Login.this.loginListener);
            Login.this.setFormEnabledState(true);
        }
    }

    /**
     * Performs the login
     */
    private class loginBtnClickHandler implements ActionListener {
        @Override
        public void actionPerformed (ActionEvent e) {
            // Disable form so only one login can occur at a time
            Login.this.setFormEnabledState(false);

            Client.addLoginListener(Login.this.loginListener);
            Client.login(Login.this.text_username.getText(), Login.this.text_password.getPassword());
        }
    }

    /**
     * Sets the selected server
     */
    private class comboServerChangeHandler implements ActionListener {
        @Override
        public void actionPerformed (ActionEvent e) {
            Server selectedServer = (Server) Login.this.combo_server.getSelectedItem();
            if (selectedServer.equals(Client.getConfig().getSelectedServer())) return;
            if (Client.isConnected()) {
                Client.disconnect();
            }

            Login.this.config.setSelectedServer(selectedServer);
        }
    }

    private class loginHandler implements LoginListener {

        /**
         * Fired when a successful login occurs
         *
         * @param user User object for current user
         */
        @Override
        public void loginSuccess (User user) {
            Client.removeLoginListener(Login.this.loginListener);
            Login.this.setFormEnabledState(true);
        }

        /**
         * Fire when an unsuccessful login occurs
         *
         * @param message Reason why login failed
         */
        @Override
        public void loginError (String message) {
            Client.removeLoginListener(Login.this.loginListener);
            Login.this.setFormEnabledState(true);
            Login.this.clearFields();
            JOptionPane.showMessageDialog(
                    Login.this,
                    message,
                    "Login Failed",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }


    private void setFormEnabledState (boolean state) {
        this.text_username.setEnabled(state);
        this.text_password.setEnabled(state);
        this.combo_server.setEnabled(state);
        this.btn_login.setEnabled(state);
    }

    private void clearFields () {
        this.text_username.setText("");
        this.text_password.setText("");
    }
}
