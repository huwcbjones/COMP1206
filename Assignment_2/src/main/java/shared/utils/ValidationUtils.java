package shared.utils;

import shared.exceptions.ValidationFailedException;

import java.util.Arrays;

/**
 * Utilities for field Validation
 *
 * @author Huw Jones
 * @since 26/03/2016
 */
public final class ValidationUtils {

    public static void validatePassword (char[] password) throws ValidationFailedException {
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
        if(username.length() < 3){
            throw new ValidationFailedException("Usernames must be longer than 3 characters.");
        }
        if(username.matches("[\\w]*")){
            return;
        }
        throw new ValidationFailedException("Usernames are case insensitive and can only contain alphanumeric characters and underscores (_).");
    }

    public static void validateName(String name) throws ValidationFailedException {
        if(name.length() < 3){
            throw new ValidationFailedException("Names must be longer than 3 characters.");
        }
        if(name.matches("^[a-zA-Z]+[a-zA-Z\\-]*$")){
            return;
        }
        throw new ValidationFailedException("Names can only contain letters and hyphens (-).");
    }
}
