package shared.utils;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * {DESCRIPTION}
 *
 * @author Huw Jones
 * @since 26/03/2016
 */
public class ValidationUtilsTest {

    @Test
    public void validatePassword () throws Exception {
        assertTrue(ValidationUtils.validatePassword(new char[]{'A', 'b', 'c', 'd', '1', '2', '3', '$' }));
        assertTrue(ValidationUtils.validatePassword("1234567890abcdef1234567890abcdef".toCharArray()));
    }
}