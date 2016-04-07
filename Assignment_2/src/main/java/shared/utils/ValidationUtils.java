package shared.utils;

import shared.exceptions.PasswordNotStrongEnoughException;

import java.util.Arrays;

/**
 * Utilities for field Validation
 *
 * @author Huw Jones
 * @since 26/03/2016
 */
public class ValidationUtils {

    public static void validatePassword (char[] password) throws PasswordNotStrongEnoughException {
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
        throw new PasswordNotStrongEnoughException();
    }
}
