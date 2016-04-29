package client.windows;

import client.Client;
import client.components.WindowPanel;
import client.events.LoginAdapter;
import client.utils.Server;
import shared.User;
import shared.components.HintPasswordFieldUI;
import shared.components.HintTextFieldUI;
import shared.components.JLinkLabel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

/**
 * Login Panel
 *
 * @author Huw Jones
 * @since 25/04/2016
 */
public class Login extends WindowPanel {

    //region panel_fields controls
    private JLabel label_username;
    private JTextField text_username;

    private JLabel label_password;
    private JPasswordField text_password;

    private JLabel label_server;
    private JComboBox<Server> combo_server;
    //endregion

    public JLinkLabel btn_register;
    public JButton btn_login;

    private LoginHandler loginListener = new LoginHandler();

    public Login() {
        super("Login");
        this.initComponents();
        this.initEventListeners();
    }

    private void initComponents(){
        this.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.FIRST_LINE_START;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        int row = 0;

        //region Fields
        this.label_username = new JLabel("Username", JLabel.LEADING);
        this.label_username.setLabelFor(this.text_username);
        c.insets = new Insets(3, 0, 3, 0);
        c.gridy = row;
        this.add(this.label_username, c);
        row++;

        this.text_username = new JTextField();
        this.text_username.setUI(new HintTextFieldUI("Username", true));
        c.gridy = row;
        c.insets = new Insets(0, 0, 6, 0);
        this.add(this.text_username, c);
        row++;

        this.label_password = new JLabel("Password", JLabel.LEADING);
        this.label_password.setLabelFor(this.text_password);
        c.insets = new Insets(3, 0, 3, 0);
        c.gridy = row;
        this.add(this.label_password, c);
        row++;

        this.text_password = new JPasswordField();
        this.text_password.setUI(new HintPasswordFieldUI("Password", true));
        c.insets = new Insets(0, 0, 6, 0);
        c.gridy = row;
        this.add(this.text_password, c);
        row++;

        this.label_server = new JLabel("Server", JLabel.LEADING);
        this.label_server.setLabelFor(this.combo_server);
        c.insets = new Insets(3, 0, 3, 0);
        c.gridy = row;
        this.add(this.label_server, c);
        row++;

        this.combo_server = new JComboBox<>();
        for (Server s : Client.getConfig().getServers()) {
            this.combo_server.addItem(s);
        }
        c.insets = new Insets(0, 0, 6, 0);
        c.gridy = row;
        this.add(this.combo_server, c);
        row++;
        //endregion

        //region Buttons

        this.btn_login = new JButton("Login");
        this.btn_login.setMnemonic('l');
        c.insets = new Insets(6, 0, 6, 0);
        c.gridy = row;
        this.add(this.btn_login, c);
        row++;

        this.btn_register = new JLinkLabel("Register for Biddr", JLabel.LEADING);
        this.btn_register.setFont(this.btn_register.getFont().deriveFont(this.btn_register.getFont().getStyle() | Font.BOLD));
        c.insets = new Insets(6, 0, 6, 0);
        c.gridy = row;
        c.fill = GridBagConstraints.NONE;
        this.add(this.btn_register, c);
        row++;

        JPanel padder = new JPanel();
        padder.setBackground(Color.WHITE);
        c.gridy = row;
        c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;
        this.add(padder, c);
        //endregion
    }

    private void initEventListeners(){
        this.addComponentListener(new ComponentHandler());
        this.btn_login.addActionListener(new LoginBtnClickHandler());
        this.combo_server.addActionListener(new ComboServerChangeHandler());
    }
    /**
     * Gets the default button for the panel
     *
     * @return Default Button
     */
    @Override
    public JButton getDefaultButton() {
        return this.btn_login;
    }

    private class ComponentHandler extends ComponentAdapter {
        /**
         * Invoked when the component has been made invisible.
         *
         * @param e
         */
        @Override
        public void componentHidden(ComponentEvent e) {
            Authenticate.setUsername(Login.this.text_username.getText());
            Login.this.text_password.setText("");
        }

        /**
         * Invoked when the component has been made visible.
         *
         * @param e
         */
        @Override
        public void componentShown(ComponentEvent e) {
            Login.this.text_username.setText(Authenticate.getUsername());
            if(!Login.this.text_username.getText().equals("")){
                Login.this.text_password.requestFocus();
            }

            // Refresh list of servers
            Login.this.combo_server.removeAllItems();
            for (Server s : Client.getConfig().getServers()) {
                Login.this.combo_server.addItem(s);
            }
        }
    }

    //region Utility Methods
    private void setFormEnabledState(boolean state) {
        this.text_username.setEnabled(state);
        this.text_password.setEnabled(state);
        this.combo_server.setEnabled(state);
        this.btn_login.setEnabled(state);
        this.btn_register.setEnabled(state);
    }

    private void clearFields() {
        this.text_username.setText("");
        this.text_password.setText("");
        this.text_username.requestFocus();
    }
    //endregion

    /**
     * Performs the login
     */
    private class LoginBtnClickHandler implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            // Disable form so only one login can occur at a time
            Login.this.setFormEnabledState(false);

            Client.addLoginListener(Login.this.loginListener);
            Client.login(Login.this.text_username.getText(), Login.this.text_password.getPassword());
        }
    }
    /**
     * Handles LoginEvents
     */
    private class LoginHandler extends LoginAdapter {

        /**
         * Fired when a successful login occurs
         *
         * @param user User object for current user
         */
        @Override
        public void loginSuccess(User user) {
            Client.removeLoginListener(Login.this.loginListener);
            Login.this.setFormEnabledState(true);
            Login.this.clearFields();
        }

        /**
         * Fire when an unsuccessful login occurs
         *
         * @param message Reason why login failed
         */
        @Override
        public void loginFail(String message) {
            Client.removeLoginListener(Login.this.loginListener);
            Login.this.setFormEnabledState(true);
            Login.this.clearFields();
            SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(
                Login.this,
                message,
                "Login Failed",
                JOptionPane.ERROR_MESSAGE
            ));
        }
    }

    /**
     * Sets the selected server
     */
    private class ComboServerChangeHandler implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            Server selectedServer = (Server) Login.this.combo_server.getSelectedItem();
            if(selectedServer == null) return;
            if (selectedServer.equals(Client.getConfig().getSelectedServer())) return;
            if (Client.isConnected()) {
                Client.disconnect();
            }

            Client.getConfig().setSelectedServer(selectedServer);
        }
    }
}
