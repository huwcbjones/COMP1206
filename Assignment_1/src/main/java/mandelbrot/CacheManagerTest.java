package mandelbrot;

import junit.framework.TestCase;
import utils.FractalImage;
import utils.ImageColourProperties;
import utils.ImageProperties;

import java.awt.image.BufferedImage;

/**
 * {DESCRIPTION}
 *
 * @author Huw Jones
 * @since 03/03/2016
 */
public class CacheManagerTest extends TestCase {

    public void testIsCached() throws Exception {
        FractalImage img1 = new FractalImage(1, 1, BufferedImage.TYPE_INT_RGB);
        img1.setProperties(new ImageProperties(1, 1, 100, 1, 0, 0));
        img1.setColourProperties(new ImageColourProperties(0, 1, 1));

        FractalImage img2 = new FractalImage(1, 1, BufferedImage.TYPE_INT_RGB);
        img2.setProperties(new ImageProperties(1, 1, 100, 1, 0, 0));
        img2.setColourProperties(new ImageColourProperties(0.1f, 1, 1));

        CacheManager cache = new CacheManager();
        cache.cacheImage(img1);
        cache.cacheImage(img2);

        assertTrue(cache.isCached(new ImageProperties(1, 1, 100, 1, 0, 0), new ImageColourProperties(0, 1, 1)));
    }

    public void testGetImage() throws Exception {
        FractalImage img1 = new FractalImage(1, 1, BufferedImage.TYPE_INT_RGB);
        img1.setProperties(new ImageProperties(1, 1, 100, 1, 0, 0));
        img1.setColourProperties(new ImageColourProperties(0, 1, 1));

        FractalImage img2 = new FractalImage(1, 1, BufferedImage.TYPE_INT_RGB);
        img2.setProperties(new ImageProperties(1, 1, 100, 1, 0, 0));
        img2.setColourProperties(new ImageColourProperties(0.1f, 1, 1));

        CacheManager cache = new CacheManager();
        cache.cacheImage(img1);
        cache.cacheImage(img2);

        assertTrue(img2 == cache.getImage(new ImageProperties(1, 1, 100, 1, 0, 0), new ImageColourProperties(0.1f, 1, 1)));
    }
}