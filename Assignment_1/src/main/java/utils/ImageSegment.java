package utils;

import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

/**
 * Represents an Image Segment
 *
 * @author Huw Jones
 * @since 28/02/2016
 */
public class ImageSegment {
    private BufferedImage image;
    private Rectangle2D bounds;


    public ImageSegment(BufferedImage image, Rectangle2D bounds) {
        this.image = image;
        this.bounds = bounds;
    }

    public Rectangle2D getBounds() {
        return this.bounds;
    }

    public BufferedImage getImage() {
        return this.image;
    }
}
