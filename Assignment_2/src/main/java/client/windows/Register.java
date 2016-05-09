package client.windows;

import client.Client;
import client.components.WindowPanel;
import client.events.RegisterListener;
import client.utils.Server;
import shared.User;
import shared.components.HintPasswordFieldUI;
import shared.components.HintTextFieldUI;
import shared.components.JLinkLabel;
import shared.exceptions.ValidationFailedException;
import shared.utils.ValidationUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Arrays;

import static shared.utils.ValidationUtils.VALIDATION_FAIL;
import static shared.utils.ValidationUtils.VALIDATION_SUCCESS;

/**
 * Registration Panel
 *
 * @author Huw Jones
 * @since 25/04/2016
 */
public class Register extends WindowPanel {

    private JLabel label_username;
    private JTextField text_username;

    private JLabel label_firstName;
    private JTextField text_firstName;

    private JLabel label_lastName;
    private JTextField text_lastName;

    private JLabel label_password;
    private JPasswordField text_password;

    private JLabel label_passwordConfirm;
    private JPasswordField text_passwordConfirm;

    private JLabel label_server;
    private JComboBox<Server> combo_server;

    public JButton btn_register;
    public JLinkLabel btn_back;

    private RegisterListener registerListener;

    public Register() {
        super("Register");
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

        this.label_firstName = new JLabel("First Name", JLabel.LEADING);
        this.label_firstName.setLabelFor(this.label_firstName);
        c.insets = new Insets(3, 0, 3, 0);
        c.gridy = row;
        this.add(this.label_firstName, c);
        row++;

        this.text_firstName = new JTextField();
        this.text_firstName.setUI(new HintTextFieldUI("First Name", true));
        c.gridy = row;
        c.insets = new Insets(0, 0, 6, 0);
        this.add(this.text_firstName, c);
        row++;

        this.label_lastName = new JLabel("Last Name", JLabel.LEADING);
        this.label_lastName.setLabelFor(this.label_lastName);
        c.insets = new Insets(3, 0, 3, 0);
        c.gridy = row;
        this.add(this.label_lastName, c);
        row++;

        this.text_lastName = new JTextField();
        this.text_lastName.setUI(new HintTextFieldUI("Last Name", true));
        c.gridy = row;
        c.insets = new Insets(0, 0, 6, 0);
        this.add(this.text_lastName, c);
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

        this.label_passwordConfirm = new JLabel("Confirm Password", JLabel.LEADING);
        this.label_passwordConfirm.setLabelFor(this.label_passwordConfirm);
        c.insets = new Insets(3, 0, 3, 0);
        c.gridy = row;
        this.add(this.label_passwordConfirm, c);
        row++;

        this.text_passwordConfirm = new JPasswordField();
        this.text_passwordConfirm.setUI(new HintPasswordFieldUI("Confirm Password", true));
        c.insets = new Insets(0, 0, 6, 0);
        c.gridy = row;
        this.add(this.text_passwordConfirm, c);
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

        this.btn_register = new JButton("Register");
        this.btn_register.setMnemonic('r');
        c.insets = new Insets(6, 0, 6, 0);
        c.gridy = row;
        this.add(this.btn_register, c);
        row++;

        this.btn_back = new JLinkLabel("Back to Login", JLabel.LEADING);
        this.btn_back.setFont(this.btn_back.getFont().deriveFont(this.btn_back.getFont().getStyle() | Font.BOLD));
        c.insets = new Insets(6, 0, 6, 0);
        c.gridy = row;
        c.fill = GridBagConstraints.NONE;
        this.add(this.btn_back, c);
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
        this.registerListener = new RegisterHandler();
        this.btn_register.addActionListener(new RegisterBtnClickHandler());

        this.text_username.addFocusListener(new FocusChangeHandler());
        this.text_firstName.addFocusListener(new FocusChangeHandler());
        this.text_lastName.addFocusListener(new FocusChangeHandler());
        this.text_password.addFocusListener(new FocusChangeHandler());
        this.text_passwordConfirm.addFocusListener(new FocusChangeHandler());
        this.combo_server.addActionListener(new ComboServerChangeHandler());
    }
    /**
     * Gets the default button for the panel
     *
     * @return
     */
    @Override
    public JButton getDefaultButton() {
        return this.btn_register;
    }

    //region Utility Methods
    private void setFormEnabledState(boolean state) {
        this.text_username.setEnabled(state);
        this.text_firstName.setEnabled(state);
        this.text_lastName.setEnabled(state);
        this.text_password.setEnabled(state);
        this.text_passwordConfirm.setEnabled(state);
        this.btn_register.setEnabled(state);
        this.btn_back.setEnabled(state);
    }

    private ArrayList<String> validateFields(boolean isFocusChangeValidation) {
        ArrayList<String> errors = new ArrayList<>();
        Component firstWrongField = null;

        // Validate username
        try {
            ValidationUtils.validateUsername(this.text_username.getText());
            ValidationUtils.setValidation(this.text_username, VALIDATION_SUCCESS);
        } catch (ValidationFailedException e) {
            if (firstWrongField == null) firstWrongField = this.text_username;
            errors.add(e.getMessage());
            ValidationUtils.setValidation(this.text_username, VALIDATION_FAIL);
        }

        // Validate First Name
        try {
            ValidationUtils.validateName(this.text_firstName.getText());
            ValidationUtils.setValidation(this.text_firstName, VALIDATION_SUCCESS);
        } catch (ValidationFailedException e) {
            if (firstWrongField == null) firstWrongField = this.text_firstName;
            errors.add(e.getMessage());
            ValidationUtils.setValidation(this.text_firstName, VALIDATION_FAIL);
        }

        // Validate Last Name
        try {
            ValidationUtils.validateName(this.text_lastName.getText());
            ValidationUtils.setValidation(this.text_lastName, VALIDATION_SUCCESS);
        } catch (ValidationFailedException e) {
            if (firstWrongField == null) firstWrongField = this.text_lastName;
            errors.add(e.getMessage());
            ValidationUtils.setValidation(this.text_lastName, VALIDATION_FAIL);
        }

        // Validate Password
        try {
            ValidationUtils.validatePassword(this.text_password.getPassword());
            ValidationUtils.setValidation(this.text_password, VALIDATION_SUCCESS);
        } catch (ValidationFailedException e) {
            if (firstWrongField == null) firstWrongField = this.text_password;
            errors.add(e.getMessage());
            ValidationUtils.setValidation(this.text_password, VALIDATION_FAIL);
        }

        // Validate Password
        if (Arrays.equals(this.text_password.getPassword(), this.text_passwordConfirm.getPassword())) {
            ValidationUtils.setValidation(this.text_passwordConfirm, VALIDATION_SUCCESS);
        } else {
            if (firstWrongField == null) firstWrongField = this.text_passwordConfirm;
            errors.add("Passwords do not match.");
            ValidationUtils.setValidation(this.text_passwordConfirm, VALIDATION_FAIL);
        }

        if (firstWrongField != null && !isFocusChangeValidation) {
            firstWrongField.requestFocus();
        }

        return errors;
    }
    private void clearFields() {
        this.text_username.setText("");
        this.text_firstName.setText("");
        this.text_lastName.setText("");
        this.text_password.setText("");
        this.text_passwordConfirm.setText("");
    }

    //endregion
    private class ComponentHandler extends ComponentAdapter {
        /**
         * Invoked when the component has been made invisible.
         *
         * @param e
         */
        @Override
        public void componentHidden(ComponentEvent e) {
            Authenticate.setUsername(Register.this.text_username.getText());
            Register.this.text_password.setText("");
            Register.this.text_passwordConfirm.setText("");
        }

        /**
         * Invoked when the component has been made visible.
         *
         * @param e
         */
        @Override
        public void componentShown(ComponentEvent e) {
            Register.this.text_username.setText(Authenticate.getUsername());
            if(Register.this.text_username.getText().equals("")){
                Register.this.text_username.requestFocus();
                return;
            }
            if(Register.this.text_firstName.getText().equals("")){
                Register.this.text_firstName.requestFocus();
                return;
            }
            if(Register.this.text_lastName.getText().equals("")){
                Register.this.text_lastName.requestFocus();
                return;
            }
            Register.this.text_password.requestFocus();

            // Refresh list of servers
            Register.this.combo_server.removeAllItems();
            for (Server s : Client.getConfig().getServers()) {
                Register.this.combo_server.addItem(s);
            }
            Register.this.combo_server.setSelectedIndex(0);
        }
    }

    private class RegisterBtnClickHandler implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            ArrayList<String> errors = Register.this.validateFields(false);
            if (errors.size() != 0) {
                ValidationUtils.showValidationMessage(Register.this, errors);
                return;
            }
            Register.this.setFormEnabledState(false);
            Client.addRegisterListener(Register.this.registerListener);

            if(Client.getConfig().getSelectedServer() == null){
                Register.this.combo_server.setSelectedIndex(0);
            }

            Client.register(new User(
                    Register.this.text_username.getText(),
                    Register.this.text_firstName.getText(),
                    Register.this.text_lastName.getText()
                ),
                Register.this.text_password.getPassword(),
                Register.this.text_passwordConfirm.getPassword()
            );
        }
    }

    private class RegisterHandler implements RegisterListener {

        /**
         * Fired when a user successfully registers
         *
         * @param user Registered user
         */
        @Override
        public void registerSuccess(User user) {
            Client.removeRegisterListener(Register.this.registerListener);
            Register.this.setFormEnabledState(true);
            Register.this.clearFields();
            Register.this.text_username.setText(user.getUsername());
            SwingUtilities.invokeLater( () ->
                JOptionPane.showMessageDialog(
                    Register.this,
                    "Hi " + user.getFullName() + ",\n" +
                        "You've successfully registered for Biddr on " + Client.getConfig().getSelectedServer().getName() + ".\n" +
                        "Click OK to continue to login to Biddr.\n\n" +
                        "Just to confirm, your login details are as follows:\n" +
                        "\t- Username: " + user.getUsername() + "\n" +
                        "\t- Password: As Provided",
                    "Registration Succeeded!",
                    JOptionPane.INFORMATION_MESSAGE)
            );
        }

        /**
         * Fired when a user fails to register
         *
         * @param reason Reason why registration failed
         */
        @Override
        public void registerFail(String reason) {
            Client.removeRegisterListener(Register.this.registerListener);
            Register.this.setFormEnabledState(true);
            SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(
                Register.this,
                "We're sorry, but we failed to register your account.\nReason: " + reason,
                "Registration Failed",
                JOptionPane.ERROR_MESSAGE
            ));
        }
    }

    private class FocusChangeHandler extends FocusAdapter {
        @Override
        public void focusLost(FocusEvent e) {
            Register.this.validateFields(true);
        }
    }

    /**
     * Sets the selected server
     */
    private class ComboServerChangeHandler implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            Server selectedServer = (Server) Register.this.combo_server.getSelectedItem();
            if(selectedServer == null) return;
            if (selectedServer.equals(Client.getConfig().getSelectedServer())) return;
            if (Client.isConnected()) {
                Client.disconnect();
            }

            Client.getConfig().setSelectedServer(selectedServer);
        }
    }
}
