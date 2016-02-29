package mandelbrot.render;

import mandelbrot.management.RenderManagementThread;
import utils.ColouredPixel;
import utils.Complex;
import utils.ImageProperties;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

/**
 * Tints an image
 *
 * @author Huw Jones
 * @since 29/02/2016
 */
public class TintTask extends RenderTask {

    private float originalTint;

    public TintTask (RenderManagementThread t, Rectangle2D bounds, ImageProperties properties) {
        super(t, bounds, properties);
        originalTint = t.getImage().getTint();
    }

    @Override
    protected ColouredPixel doPixelCalculation (Point2D point, Complex complex) {
        float[] hsb = new float[3];
        int rgb = renderManagementThread.getImage().getRGB((int)absolutePoint.getX(), (int)absolutePoint.getY());

        Color.RGBtoHSB((rgb >> 16) & 0xff, (rgb >> 8) & 0xff , rgb & 0xff, hsb);
        return new ColouredPixel(point, Color.getHSBColor(properties.getTint() + hsb[0] -  originalTint, hsb[1], hsb[2]));
    }
}
