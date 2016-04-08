package client.windows;

import client.Client;
import client.events.RegisterListener;
import client.utils.HintTextFieldUI;
import client.utils.SpringUtilities;
import client.utils.WindowTemplate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import shared.User;
import shared.exceptions.ValidationFailedException;
import shared.utils.ValidationUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Register Window
 *
 * @author Huw Jones
 * @since 27/03/2016
 */
public final class Register extends WindowTemplate {

    protected static final Logger log = LogManager.getLogger(Register.class);

    private JPanel panel_form;
    private JButton btn_cancel;
    private JButton btn_register;

    private JLabel label_server;
    private JTextField text_server;
    private JLabel label_validation_server;

    private JLabel label_username;
    private JTextField text_username;
    private JLabel label_validation_username;

    private JLabel label_firstName;
    private JTextField text_firstName;
    private JLabel label_validation_firstName;

    private JLabel label_lastName;
    private JTextField text_lastName;
    private JLabel label_validation_lastName;

    private JLabel label_password;
    private JPasswordField text_password;
    private JLabel label_validation_password;

    private JLabel label_passwordConfirm;
    private JPasswordField text_passwordConfirm;
    private JLabel label_validation_passwordConfirm;

    private RegisterListener registerListener;
    private boolean isRegistering = false;


    public Register() {
        this(null);
    }

    public Register(Window relativeTo) {
        super("Register");
        // Set do nothing as we'll decide what to do when the WindowClosing listener is called
        this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        this.setResizable(false);
        this.setMinimumSize(new Dimension(380, 218));
        this.setMaximumSize(new Dimension(380, 218));
        this.setLocationRelativeTo(relativeTo);

        this.initEventListeners();
    }

    /**
     * Adds Event Listeners
     */
    private void initEventListeners() {
        // Sets default button on enter press
        this.getRootPane().setDefaultButton(this.btn_register);

        // Triggers cancel button event handler when escape button pressed
        KeyStroke escapeKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, true);
        Action escapeAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Register.cancelBtnClickHandler handler = new Register.cancelBtnClickHandler();
                handler.actionPerformed(e);
            }
        };
        this.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escapeKeyStroke, "ESCAPE");
        this.getRootPane().getActionMap().put("ESCAPE", escapeAction);

        // Add cancel/register button listeners
        this.btn_cancel.addActionListener(new cancelBtnClickHandler());
        this.btn_register.addActionListener(new registerBtnClickHandler());

        this.registerListener = new registerListener();

        // Adds window closing listener
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (
                    !(
                        Register.this.text_username.getText().equals("")
                            ^ Register.this.text_firstName.getText().equals("")
                            ^ Register.this.text_lastName.getText().equals("")
                            ^ Register.this.text_password.getPassword().length == 0
                            ^ Register.this.text_passwordConfirm.getPassword().length == 0
                    )
                    ) {
                    int result = JOptionPane.showConfirmDialog(
                        Register.this,
                        "Do you want to cancel registration?",
                        "Cancel Registration?",
                        JOptionPane.OK_CANCEL_OPTION,
                        JOptionPane.QUESTION_MESSAGE
                    );
                    if (result == JOptionPane.CANCEL_OPTION) {
                        Register.this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
                        return;
                    }
                }
                Register.this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
                Client.removeRegisterListener(Register.this.registerListener);
            }
        });

        this.text_username.addFocusListener(new focusChangeListener());
        this.text_firstName.addFocusListener(new focusChangeListener());
        this.text_lastName.addFocusListener(new focusChangeListener());
        this.text_password.addFocusListener(new focusChangeListener());
        this.text_passwordConfirm.addFocusListener(new focusChangeListener());
    }

    @Override
    protected void initComponents() {
        GridBagConstraints c;
        Container container = this.getContentPane();
        container.setLayout(new GridBagLayout());

        this.panel_form = new JPanel(new SpringLayout());
        int row = 0;

        this.label_server = new JLabel("Server", JLabel.LEADING);
        this.panel_form.add(this.label_server);

        this.text_server = new JTextField(this.config.getSelectedServer().getName());
        this.text_server.setEnabled(false);
        this.panel_form.add(this.text_server);

        this.label_validation_server = ValidationUtils.createValidationLabel();
        this.panel_form.add(this.label_validation_server);
        row++;

        this.label_username = new JLabel("Username", JLabel.LEADING);
        this.panel_form.add(this.label_username);

        this.text_username = new JTextField();
        this.text_username.setName("Username");
        this.text_username.setUI(new HintTextFieldUI("Username", true));
        this.panel_form.add(this.text_username);

        this.label_validation_username = ValidationUtils.createValidationLabel();
        this.panel_form.add(this.label_validation_username);
        row++;

        this.label_firstName = new JLabel("First Name", JLabel.LEADING);
        this.panel_form.add(this.label_firstName);

        this.text_firstName = new JTextField();
        this.text_firstName.setName("First Name");
        this.text_firstName.setUI(new HintTextFieldUI("John", true));
        this.panel_form.add(this.text_firstName);

        this.label_validation_firstName = ValidationUtils.createValidationLabel();
        this.panel_form.add(this.label_validation_firstName);
        row++;

        this.label_lastName = new JLabel("Last Name", JLabel.LEADING);
        this.panel_form.add(this.label_lastName);

        this.text_lastName = new JTextField();
        this.text_lastName.setName("Last Name");
        this.text_lastName.setUI(new HintTextFieldUI("Smith", true));
        this.panel_form.add(this.text_lastName);

        this.label_validation_lastName = ValidationUtils.createValidationLabel();
        this.panel_form.add(this.label_validation_lastName);
        row++;

        this.label_password = new JLabel("Password", JLabel.LEADING);
        this.panel_form.add(this.label_password);

        this.text_password = new JPasswordField();
        this.text_password.setName("Password");
        this.panel_form.add(this.text_password);

        this.label_validation_password = ValidationUtils.createValidationLabel();
        this.panel_form.add(this.label_validation_password);
        row++;

        this.label_passwordConfirm = new JLabel("Confirm Password", JLabel.LEADING);
        this.panel_form.add(this.label_passwordConfirm);

        this.text_passwordConfirm = new JPasswordField();
        this.text_passwordConfirm.setName("Password Confirmation");
        this.panel_form.add(this.text_passwordConfirm);

        this.label_validation_passwordConfirm = ValidationUtils.createValidationLabel();
        this.panel_form.add(this.label_validation_passwordConfirm);
        row++;

        SpringUtilities.makeCompactGrid(this.panel_form, row, 3, 0, 6, 6, 3);

        c = new GridBagConstraints();
        c.weightx = 1;
        c.weighty = 1;
        c.gridwidth = 2;
        c.insets.set(3, 3, 3, 3);
        c.fill = GridBagConstraints.BOTH;
        container.add(this.panel_form, c);

        this.btn_cancel = new JButton("Cancel");
        this.btn_cancel.setMnemonic('c');
        c = new GridBagConstraints();
        c.weightx = 0.5;
        c.gridy = 1;
        c.insets.set(3, 6, 3, 3);
        c.fill = GridBagConstraints.BOTH;
        container.add(this.btn_cancel, c);

        this.btn_register = new JButton("Register");
        this.btn_register.setMnemonic('r');
        c = new GridBagConstraints();
        c.weightx = 0.5;
        c.gridx = 1;
        c.gridy = 1;
        c.insets.set(3, 3, 3, 6);
        c.fill = GridBagConstraints.BOTH;
        container.add(this.btn_register, c);
    }

    //region Utility Methods
    private void setFormEnabledState(boolean state) {
        this.text_username.setEnabled(state);
        this.text_firstName.setEnabled(state);
        this.text_lastName.setEnabled(state);
        this.text_password.setEnabled(state);
        this.text_passwordConfirm.setEnabled(state);
        this.btn_register.setEnabled(state);
    }

    private void clearFields() {
        this.text_username.setText("");
        this.text_firstName.setText("");
        this.text_lastName.setText("");
        this.text_password.setText("");
        this.text_passwordConfirm.setText("");
    }
    //endregion

    private ArrayList<String> validateForm() {
        return validateForm(false);
    }

    private ArrayList<String> validateForm(boolean isFocusChangeValidation) {
        ArrayList<String> errors = new ArrayList<>();
        Component firstWrongField = null;

        // Validate username
        try {
            ValidationUtils.validateUsername(this.text_username.getText());
            ValidationUtils.setValidation(this.label_validation_username, null);
        } catch (ValidationFailedException e) {
            if (firstWrongField == null) firstWrongField = this.text_username;
            errors.add(e.getMessage());
            ValidationUtils.setValidation(this.label_validation_username, e.getMessage());
        }

        // Validate First Name
        try {
            ValidationUtils.validateName(this.text_firstName.getText());
            ValidationUtils.setValidation(this.label_validation_firstName, null);
        } catch (ValidationFailedException e) {
            if (firstWrongField == null) firstWrongField = this.text_firstName;
            errors.add(e.getMessage());
            ValidationUtils.setValidation(this.label_validation_firstName, e.getMessage());
        }

        // Validate Last Name
        try {
            ValidationUtils.validateName(this.text_lastName.getText());
            ValidationUtils.setValidation(this.label_validation_lastName, null);
        } catch (ValidationFailedException e) {
            if (firstWrongField == null) firstWrongField = this.text_lastName;
            errors.add(e.getMessage());
            ValidationUtils.setValidation(this.label_validation_lastName, e.getMessage());
        }

        // Validate Password
        try {
            ValidationUtils.validatePassword(this.text_password.getPassword());
            ValidationUtils.setValidation(this.label_validation_password, null);
        } catch (ValidationFailedException e) {
            if (firstWrongField == null) firstWrongField = this.text_password;
            errors.add(e.getMessage());
            ValidationUtils.setValidation(this.label_validation_password, e.getMessage());
        }

        // Validate Password
        if (Arrays.equals(this.text_password.getPassword(), this.text_passwordConfirm.getPassword())) {
            ValidationUtils.setValidation(this.label_validation_passwordConfirm, true);
        } else {
            if (firstWrongField == null) firstWrongField = this.text_passwordConfirm;
            errors.add("Passwords do not match.");
            ValidationUtils.setValidation(this.label_validation_passwordConfirm, false);
        }

        if (firstWrongField != null && !isFocusChangeValidation) {
            firstWrongField.requestFocus();
        }

        return errors;
    }

    /**
     * Closes window when cancel button pressed
     */
    private class cancelBtnClickHandler implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            Register.this.dispatchEvent(new WindowEvent(Register.this, WindowEvent.WINDOW_CLOSING));
        }
    }

    private class registerBtnClickHandler implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            ArrayList<String> errors = Register.this.validateForm();
            if (errors.size() != 0) {
                ValidationUtils.showValidationMessage(Register.this, errors);
                return;
            }
            Register.this.isRegistering = true;
            Register.this.setFormEnabledState(false);
            Client.addRegisterListener(Register.this.registerListener);
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

    private class registerListener implements RegisterListener {

        /**
         * Fired when a user successfully registers
         *
         * @param user Registered user
         */
        @Override
        public void registerSuccess(User user) {
            Register.this.isRegistering = false;
            Client.removeRegisterListener(Register.this.registerListener);
            Register.this.setFormEnabledState(true);
            Register.this.clearFields();
            JOptionPane.showMessageDialog(
                Register.this,
                "Hi " + user.getFullName() + ",\n" +
                    "You've successfully registered for Biddr on " + Register.this.config.getSelectedServer().getName() + ".\n" +
                    "Click OK to continue to login to Biddr.\n\n" +
                    "Just to confirm, your login details are as follows:\n" +
                    "\t- Username: " + user.getUsername() + "\n" +
                    "\t- Password: As Provided",
                "Registration Succeeded!",
                JOptionPane.INFORMATION_MESSAGE
            );
            Register.this.dispatchEvent(new WindowEvent(Register.this, WindowEvent.WINDOW_CLOSING));
        }

        /**
         * Fired when a user fails to register
         *
         * @param reason Reason why registration failed
         */
        @Override
        public void registerFail(String reason) {
            Register.this.isRegistering = false;
            Client.removeRegisterListener(Register.this.registerListener);
            Register.this.setFormEnabledState(true);
            JOptionPane.showMessageDialog(
                Register.this,
                "We're sorry, but we failed to register your account.\nReason: " + reason,
                "Registration Failed",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private class focusChangeListener extends FocusAdapter {
        /**
         * Invoked when a component loses the keyboard focus.
         *
         * @param e
         */
        @Override
        public void focusLost(FocusEvent e) {
            Register.this.validateForm(true);
        }
    }
}
