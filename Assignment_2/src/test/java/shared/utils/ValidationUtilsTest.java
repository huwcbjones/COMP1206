package shared.utils;

import org.junit.Test;
import shared.exceptions.ValidationFailedException;

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

    @Test
    public void validateUsernameLength() throws Exception {
        boolean passedLengthTest = false;
        try {
            ValidationUtils.validateUsername("as");
        } catch (ValidationFailedException e){
            passedLengthTest = true;
        }
        assertTrue("Username length validation failed!", passedLengthTest);

        ValidationUtils.validateUsername("asa");
    }

    @Test
    public void validateUsernameChars() throws Exception {
        ValidationUtils.validateUsername("abcd1234_4321dcba");
    }

    @Test
    public void validateName() throws Exception {
        ValidationUtils.validateName("Huw");
        ValidationUtils.validateName("Jones");
        ValidationUtils.validateName("Someone-Something");

        boolean passedTest = false;
        try {
            ValidationUtils.validateName("-asd");
        } catch (ValidationFailedException e){
            passedTest = true;
        }
        assertTrue(passedTest);

        passedTest = false;
        try {
            ValidationUtils.validateName("ASD-123");
        } catch (ValidationFailedException e){
            passedTest = true;
        }
        assertTrue(passedTest);
    }
}