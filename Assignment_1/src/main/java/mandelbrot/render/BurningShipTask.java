package mandelbrot.render;

import mandelbrot.management.RenderManagementThread;
import utils.ColouredPixel;
import utils.Complex;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

/**
 * Worker unit for BUrning Ship Fractal
 *
 * @author Huw Jones
 * @since 07/03/2016
 */
public class BurningShipTask extends RenderTask {

    public BurningShipTask(RenderManagementThread t, Rectangle2D bounds) {
        super(t, bounds);
    }

    /**
     * Worker Unit for individual pixels
     *
     * @param point   Point on worker unit
     * @param complex Complex for point
     * @return ColouredPixel for that pixel
     */
    @Override
    protected ColouredPixel doPixelCalculation(Point2D point, Complex complex) {
        int currIteration = 0;
        Complex z = new Complex(0, 0);
        Complex prevPoint;

        while (z.squareReal() + z.squareImaginary() <= this.escapeRadiusSquared && currIteration < this.maxIterations) {
            z = new Complex(Math.abs(z.getReal()), Math.abs(z.getImaginary()));

            prevPoint = z.clone();
            z = z.square();
            z.add(complex);

            // Apply period detection
            if(z.equals(prevPoint)){
                currIteration = this.maxIterations;
                break;
            }

            currIteration++;
        }

        Color colour;

        if (currIteration < this.maxIterations) {
            colour = this.getHSBColour(currIteration, z);
        } else {
            colour = Color.BLACK;
        }

        return new ColouredPixel(point, colour);
    }
}
