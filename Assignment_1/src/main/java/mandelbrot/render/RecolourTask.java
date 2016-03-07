package mandelbrot.render;

import mandelbrot.management.RenderManagementThread;
import utils.ColouredPixel;
import utils.Complex;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

/**
 * Recolours an image
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
        // Get original values
        this.originalHue = t.getImage().getColourProperties().getHue();
        this.originalSaturation = t.getImage().getColourProperties().getSaturation();
        this.originalBrightness = t.getImage().getColourProperties().getBrightness();
    }

    /**
     * Worker Unit for individual pixels
     * @param point Point on worker unit
     * @param complex Complex for point
     * @return ColouredPixel for that pixel
     */
    @Override
    protected ColouredPixel doPixelCalculation (Point2D point, Complex complex) {
        float[] hsb = new float[3];
        // Get current RGB Colour
        int rgb = this.mgmtThread.getImage().getRGB((int) this.absolutePoint.getX(), (int) this.absolutePoint.getY());

        // Convert RGB to HSB
        hsb = Color.RGBtoHSB(rgb >> 16 & 0x000000FF, rgb >>8 & 0x000000FF, rgb & 0x000000FF, hsb);

        // Add new values and subtract old ones
        hsb[0] += this.mgmtThread.getHue() - this.originalHue;

        // Maintain black
        if(rgb == Color.black.getRGB()) {
            hsb[1] = 0;
            hsb[2] = 0;
        } else {
            hsb[1] += this.mgmtThread.getSaturation() - this.originalSaturation;
            hsb[2] += this.mgmtThread.getBrightness() - this.originalBrightness;
        }

        // Return new pixel, but with adjusted colours
        return new ColouredPixel(point, Color.getHSBColor(hsb[0], hsb[1], hsb[2]));
    }
}
