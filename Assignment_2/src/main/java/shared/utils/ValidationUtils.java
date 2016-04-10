package shared.utils;

import shared.exceptions.ValidationFailedException;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

/**
 * Utilities for field Validation
 *
 * @author Huw Jones
 * @since 26/03/2016
 */
public final class ValidationUtils {

    public static final String REGEXP_USERNAME = "[\\w]*";
    public static final String REGEXP_NAME = "^[a-zA-Z]+[a-zA-Z\\-]*$";

    public static JLabel createValidationLabel() {
        JLabel label = new JLabel("", JLabel.CENTER);
        label.setPreferredSize(new Dimension(16, 23));
        //label.set
        return label;
    }

    /**
     * Sets the validation label state
     *
     * @param validationLabel Validation Label to set
     * @param state           True = validation success, False = validation failure
     */
    public static void setValidation(JLabel validationLabel, boolean state) {
        if (state) {
        	// Set field to tick mark (Unicode: 2713)
            validationLabel.setText("\u2713");
            validationLabel.setToolTipText("All done");
            validationLabel.setForeground(Color.GREEN);
        } else {
            validationLabel.setText("X");
            validationLabel.setToolTipText("There are errors here.");
            validationLabel.setForeground(Color.RED);
        }
    }

    /**
     * Sets the validation label state
     *
     * @param validationLabel Validation Label to set
     * @param message         Why the validation failed (displayed as a tooltip)
     */
    public static void setValidation(JLabel validationLabel, String message) {
        if (message == null) {
            validationLabel.setText("✓");
            validationLabel.setForeground(Color.GREEN);
            validationLabel.setToolTipText("All done");
        } else {
            validationLabel.setText("X");
            validationLabel.setForeground(Color.RED);
            validationLabel.setToolTipText(message);
        }
    }

    public static void showValidationMessage(Component parent, ArrayList<String> errors) {
        String message = "The form was incorrectly completed. Please correct the following errors:\n";
        Iterator<String> messageIterator = errors.iterator();
        while (messageIterator.hasNext()) {
            message += "\t- " + messageIterator.next();
            if (messageIterator.hasNext()) {
                message += "\n";
            }
        }
        JOptionPane.showMessageDialog(parent,
            message,
            "Validation Error",
            JOptionPane.ERROR_MESSAGE
        );
    }
    public static void validatePassword(char[] password) throws ValidationFailedException {
        validatePassword(password, true);
    }

    public static void validatePassword(char[] password, boolean isRequired) throws ValidationFailedException {
        if(password.length == 0 && isRequired){
            throw new ValidationFailedException("A password is required.");
        }

        boolean lower = false,
            upper = false,
            number = false;
        int length = password.length;

        if (length >= 32) return;

        for (char c : password) {
            lower |= Character.isLowerCase(c);
            upper |= Character.isUpperCase(c);
            number |= Character.isDigit(c);

            if (lower & upper & number) {
                Arrays.fill(password, '\u0000');
                return;
            }
        }

        Arrays.fill(password, '\u0000');

        if (lower && upper && number) {
            return;
        }
        throw new ValidationFailedException("Password must contain lower case, upper case and a number, or be longer than 32 characters.");
    }

    public static void validateUsername(String username) throws ValidationFailedException {
        validateUsername(username, true);
    }

    public static void validateUsername(String username, boolean isRequired) throws ValidationFailedException {
        if(username.equals("") && isRequired){
            throw new ValidationFailedException("Username is required");
        }
        if (username.length() < 3) {
            throw new ValidationFailedException("Usernames must be longer than 3 characters.");
        }
        if (username.matches(REGEXP_USERNAME)) {
            return;
        }
        throw new ValidationFailedException("Usernames are case insensitive and can only contain alphanumeric characters and underscores (_).");
    }

    public static void validateName(String name) throws ValidationFailedException {
        validateName(name, true);
    }
    public static void validateName(String name, boolean isRequired) throws ValidationFailedException {
        if(name.equals("") && isRequired){
            throw new ValidationFailedException("Name is required.");
        }
        if (name.length() < 3) {
            throw new ValidationFailedException("Names must be longer than 3 characters.");
        }
        if (name.matches(REGEXP_NAME)) {
            return;
        }
        throw new ValidationFailedException("Names can only contain letters and hyphens (-).");
    }

    // public static void
}
