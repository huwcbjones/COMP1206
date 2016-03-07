package mandelbrot.render;

import mandelbrot.management.RenderManagementThread;
import utils.ColouredPixel;
import utils.Complex;
import utils.ImageProperties;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

/**
 * Julia Set Worker Task
 *
 * @author Huw Jones
 * @since 28/02/2016
 */
public class JuliaTask extends RenderTask {

    Complex complex;

    public JuliaTask(RenderManagementThread t, Rectangle2D bounds, Complex complex) {
        super(t, bounds);
        this.complex = complex;
    }

    /**
     * Worker Unit for individual pixels
     * @param point Point on worker unit
     * @param complex Complex for point
     * @return ColouredPixel for that pixel
     */
    @Override
    protected ColouredPixel doPixelCalculation(Point2D point, Complex complex) {
        int currIteration = 0;
        Complex z = complex;
        Complex prevPoint;

        while (z.squareReal() + z.squareImaginary() <= this.escapeRadiusSquared && currIteration < this.maxIterations) {
            prevPoint = z.clone();
            z = z.square();

            // Add the fixed complex
            z.add(this.complex);

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
