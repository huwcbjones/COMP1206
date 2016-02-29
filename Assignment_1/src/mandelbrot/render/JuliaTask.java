package mandelbrot.render;

import mandelbrot.management.DrawingManagementThread;
import utils.ColouredPixel;
import utils.Complex;
import utils.ImageProperties;

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
    public JuliaTask(DrawingManagementThread t, Rectangle2D bounds, ImageProperties properties, Complex c) {
        super(t, bounds, properties);
        complex = c;
    }

    @Override
    protected ColouredPixel doPixelCalculation(Point2D point, Complex c) {
        int currIteration = 0;
        Complex z = c;
        Complex prevPoint;

        while (z.squareReal() + z.squareImaginary() <= 4 && currIteration < maxIterations) {
            prevPoint = z.clone();
            z = z.square();
            z.add(complex);

            // Apply period detection
            if(z.equals(prevPoint)){
                currIteration = maxIterations;
                break;
            }

            currIteration++;
        }

        Color colour;

        if (currIteration < maxIterations) {
            colour = getHSBColour(currIteration, z);
        } else {
            colour = Color.BLACK;
        }

        return new ColouredPixel(point, colour);
    }
}
