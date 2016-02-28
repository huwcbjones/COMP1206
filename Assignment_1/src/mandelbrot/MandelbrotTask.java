package mandelbrot;

import utils.ColouredPixel;
import utils.Complex;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

/**
 * {DESCRIPTION}
 *
 * @author Huw Jones
 * @since 28/02/2016
 */
public class MandelbrotTask extends DrawingTask {

    public MandelbrotTask(DrawingManagementThread t, Rectangle2D bounds, int maxIterations) {
        super(t, bounds, maxIterations);
    }

    @Override
    protected ColouredPixel doPixelCalculation(Point2D point, Complex c) {
        int currIteration = 0;
        Complex z = new Complex(c.getReal(), c.getImaginary());

        while (z.modulusSquared() <= 4 && currIteration < maxIterations) {
            z = z.square();
            z.add(c);
            currIteration++;
        }

        Color colour;

        if (currIteration < maxIterations) {
            colour = Color.getHSBColor(currIteration / 100f, 1, 1);
        } else {
            colour = Color.BLACK;
        }

        return new ColouredPixel(point, colour);
    }
}
