package mandelbrot.render;

import mandelbrot.management.DrawingManagementThread;
import utils.ColouredPixel;
import utils.Complex;
import utils.ImageProperties;
import utils.ImageSegment;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

/**
 * Tints an image
 *
 * @author Huw Jones
 * @since 29/02/2016
 */
public class TintTask extends RenderTask {


    public TintTask (DrawingManagementThread t, Rectangle2D bounds, ImageProperties properties) {
        super(t, bounds, properties);
    }

    @Override
    protected ColouredPixel doPixelCalculation (Point2D point, Complex complex) {
        float[] hsb = new float[3];
        int rgb = drawingManagementThread.getImage().getRGB((int)absolutePoint.getX(), (int)absolutePoint.getY());

        Color.RGBtoHSB((rgb >> 16) & 0xff, (rgb >> 8) & 0xff , rgb & 0xff, hsb);
        return new ColouredPixel(point, Color.getHSBColor(properties.getTint() + hsb[0], hsb[1], hsb[2]));
    }
}
