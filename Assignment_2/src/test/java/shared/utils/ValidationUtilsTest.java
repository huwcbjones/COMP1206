package shared.utils;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * {DESCRIPTION}
 *
 * @author Huw Jones
 * @since 26/03/2016
 */
public class ValidationUtilsTest {

    @Test
    public void validatePassword () throws Exception {
        ValidationUtils.validatePassword(new char[]{'A', 'b', 'c', 'd', '1', '2', '3', '$'});
        ValidationUtils.validatePassword("1234567890abcdef1234567890abcdef".toCharArray());
        assertTrue(true);
    }
}