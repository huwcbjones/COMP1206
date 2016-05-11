package shared.utils;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * {DESCRIPTION}
 *
 * @author Huw Jones
 * @since 10/05/2016
 */
public class TimeUtilsTest {
    @Test
    public void getTimeString() throws Exception {
        assertEquals("Seconds not equal", "1 seconds", TimeUtils.getTimeString(1, false));
        assertEquals("Minutes not equal", "60 seconds", TimeUtils.getTimeString(60, false));
        assertEquals("Hours not equal", "60 mins", TimeUtils.getTimeString(3600, false));
    }

}