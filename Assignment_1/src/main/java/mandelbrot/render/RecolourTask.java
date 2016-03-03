package mandelbrot.render;

import mandelbrot.management.RenderManagementThread;
import utils.ColouredPixel;
import utils.Complex;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

/**
 * Tints an image
 *
 * @author Huw Jones
 * @since 29/02/2016
 */
public class RecolourTask extends RenderTask {

    private float originalHue;
    private float originalSaturation;
    private float originalBrightness;

    public RecolourTask(RenderManagementThread t, Rectangle2D bounds) {
        super(t, bounds);
        originalHue = t.getImage().getColourProperties().getHue();
        originalSaturation = t.getImage().getColourProperties().getSaturation();
        originalBrightness = t.getImage().getColourProperties().getBrightness();
    }

    @Override
    protected ColouredPixel doPixelCalculation (Point2D point, Complex complex) {
        float[] hsb = new float[3];
        int rgb = mgmtThread.getImage().getRGB((int)absolutePoint.getX(), (int)absolutePoint.getY());

        hsb = Color.RGBtoHSB((rgb >> 16) & 0x000000FF, (rgb >>8 ) & 0x000000FF, (rgb) & 0x000000FF, hsb);
        hsb[0] += mgmtThread.getHue() - originalHue;
        hsb[1] += mgmtThread.getSaturation() - originalSaturation;
        hsb[2] += mgmtThread.getBrightness() - originalBrightness;
        if(rgb == Color.black.getRGB()) {
            hsb[1] = 0;
            hsb[2] = 0;
        }
        return new ColouredPixel(point, Color.getHSBColor(hsb[0], hsb[1], hsb[2]));
    }
}
