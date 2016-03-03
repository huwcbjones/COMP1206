package utils;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * BufferedImage but for Fractals
 *
 * @author Huw Jones
 * @since 29/02/2016
 */
public class FractalImage extends BufferedImage {

    private ImageColourProperties colourProperties;
    private ImageProperties properties;

    /**
     * Constructs a <code>BufferedImage</code> of one of the predefined
     * image types.  The <code>ColorSpace</code> for the image is the
     * default sRGB space.
     *
     * @param width     width of the created image
     * @param height    height of the created image
     * @param imageType type of the created image
     * @see #TYPE_INT_RGB
     * @see #TYPE_INT_ARGB
     * @see #TYPE_INT_ARGB_PRE
     * @see #TYPE_INT_BGR
     * @see #TYPE_3BYTE_BGR
     * @see #TYPE_4BYTE_ABGR
     * @see #TYPE_4BYTE_ABGR_PRE
     * @see #TYPE_BYTE_GRAY
     * @see #TYPE_USHORT_GRAY
     * @see #TYPE_BYTE_BINARY
     * @see #TYPE_BYTE_INDEXED
     * @see #TYPE_USHORT_565_RGB
     * @see #TYPE_USHORT_555_RGB
     */
    public FractalImage(int width, int height, int imageType) {
        super(width, height, imageType);
        this.properties = new ImageProperties(width, height);
        this.colourProperties = new ImageColourProperties();
    }

    public static FractalImage fromBufferedImage(BufferedImage image) {
        FractalImage img = new FractalImage(image.getWidth(), image.getHeight(), image.getType());
        Graphics2D g = img.createGraphics();
        g.drawImage(image, 0, 0, null);
        g.dispose();
        return img;
    }

    public ImageColourProperties getColourProperties() {
        return colourProperties;
    }

    public void setColourProperties(ImageColourProperties colourProperties) {
        this.colourProperties = colourProperties;
    }

    public ImageProperties getProperties() {
        return properties;
    }

    public void setProperties(ImageProperties properties) {
        this.properties = properties;
    }
}
