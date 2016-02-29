package mandelbrot.render;

import mandelbrot.management.DrawingManagementThread;
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
public class JuliaTask extends RenderTask {
    Complex complex;
    public JuliaTask(DrawingManagementThread t, Rectangle2D bounds, int maxIterations, Complex c) {
        super(t, bounds, maxIterations);
        complex = c;
    }

    @Override
    protected ColouredPixel doPixelCalculation(Point2D point, Complex c) {
        int currIteration = 0;
        Complex z = c;

        while (z.squareReal() + z.squareImaginary() <= 4 && currIteration < maxIterations) {
            z = z.square();
            z.add(complex);
            currIteration++;
        }

        Color colour;

        if (currIteration < maxIterations) {
            double fraction = (currIteration + 1 - Math.log(Math.log(Math.sqrt(z.modulusSquared()))) / Math.log(2)) / 100f;
            colour = Color.getHSBColor(Double.valueOf(fraction).floatValue(), 1, 1);
        } else {
            colour = Color.BLACK;
        }

        return new ColouredPixel(point, colour);
    }
}
