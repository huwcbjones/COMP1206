package client.windows;

import client.Client;
import client.Config;
import client.events.LoginEventListener;
import client.utils.ImagePanel;
import client.utils.Server;
import client.utils.SpringUtilities;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import shared.User;
import shared.events.ConnectionListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;

/**
 * Login window
 *
 * @author Huw Jones
 * @since 27/03/2016
 */
public final class Login extends JFrame {

    private static Logger log = LogManager.getLogger(Login.class);

    private Config config;
    private boolean isLoggingIn = false;

    private ImagePanel panel_banner;

    private JPanel panel_fields;
    private JPanel panel_loginButtons;
    private JPanel panel_extraButtons;

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

    private LoginEventListener loginEventListener;

    public Login () {
        super("Login | AuctionSys");
        this.config = Client.getConfig();

        // Set frame properties
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.setMinimumSize(new Dimension(280, 218));
        this.setMaximumSize(new Dimension(280, 218));
        this.setLocationRelativeTo(null);
        this.setResizable(false);

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
            log.debug("Failed to set System Look and Feel. {}", ex.getMessage());
            log.trace(ex);
        }
        // Initialise components
        initComponents();

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
        this.loginEventListener = new loginEventHandler();
        this.combo_server.addActionListener(new comboServerChangeHandler());
        //endregion
    }

    private void initComponents () {
        GridBagConstraints c;

        Container container = this.getContentPane();
        container.setLayout(new GridBagLayout());

        this.panel_banner = new ImagePanel();
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
            if(Login.this.isLoggingIn){
                Client.cancelLogin();
                Login.this.isLoggingIn = false;
                Client.removeLoginListener(Login.this.loginEventListener);
                Login.this.setFormEnabledState(true);
            } else {
                Login.this.dispatchEvent(new WindowEvent(Login.this, WindowEvent.WINDOW_CLOSING));
            }
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
            Login.this.isLoggingIn = true;
            Client.addLoginListener(Login.this.loginEventListener);

            // Run login in new anonymous thread
            // Also uses nice J8 lamba expressions, instead of
            // new Thread(new Runnable(){public void run(){Client.login();}, "Login Thread").start();
            // Well I was going to do it this way, until I realised how useful it would be to pass the username
            // and password to the login function!
            new Thread(() -> {
                Client.login(Login.this.text_username.getText(), Login.this.text_password.getPassword());
            }, "LoginThread").start();
        }
    }

    /**
     * Sets the selected server
     */
    private class comboServerChangeHandler implements ActionListener {
        @Override
        public void actionPerformed (ActionEvent e) {
            Server selectedServer = (Server) Login.this.combo_server.getSelectedItem();
            Login.this.config.setSelectedServer(selectedServer);
        }
    }

    private class loginEventHandler implements LoginEventListener {

        /**
         * Fired when a successful login occurs
         *
         * @param user User object for current user
         */
        @Override
        public void loginSuccess (User user) {
            Login.this.isLoggingIn = false;
            Login.this.setFormEnabledState(true);
            Client.removeLoginListener(Login.this.loginEventListener);
        }

        /**
         * Fire when an unsuccessful login occurs
         *
         * @param message Reason why login failed
         */
        @Override
        public void loginError (String message) {
            Login.this.isLoggingIn = false;
            Login.this.setFormEnabledState(true);
            Login.this.clearFields();
            JOptionPane.showMessageDialog(
                    Login.this,
                    message,
                    "Login Failed",
                    JOptionPane.ERROR_MESSAGE
            );
            Client.removeLoginListener(Login.this.loginEventListener);
        }
    }

    private class connectionEventHandler implements ConnectionListener {
        @Override
        public void connectionClosed (String reason) {
            if(Login.this.isLoggingIn){
                Client.removeLoginListener(Login.this.loginEventListener);
                Login.this.isLoggingIn = false;
            }
            JOptionPane.showMessageDialog(
                    Login.this,
                    "The connection to the server was closed.\nReason: " + reason,
                    "Connection Closed.",
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
