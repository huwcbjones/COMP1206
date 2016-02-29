package utils;

import junit.framework.TestCase;

/**
 * {DESCRIPTION}
 *
 * @author Huw Jones
 * @since 29/02/2016
 */
public class ImagePropertiesTest extends TestCase {

    public void testEquals() throws Exception {
        ImageProperties p1 = new ImageProperties(10, 1, 0, 0, 0);
        ImageProperties p2 = new ImageProperties(10, 1.1, 0, 0, 0);

        assertEquals(p1.equals(p2), false);
    }
    public void testHashCode() throws Exception {
        ImageProperties p1 = new ImageProperties(10, 1, 0, 0, 0);
        ImageProperties p2 = new ImageProperties(10, 1.1, 0, 0, 0);
        boolean result = p1.hashCode() != p2.hashCode();
        assertEquals(result, true);
    }
}