package mandelbrot.render;

import mandelbrot.management.RenderManagementThread;
import utils.ColouredPixel;
import utils.Complex;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

/**
 * Mandelbrot Set Worker Task
 *
 * @author Huw Jones
 * @since 28/02/2016
 */
public class MandelbrotTask extends RenderTask {

    private boolean halfHeight = true;

    public MandelbrotTask(RenderManagementThread t, Rectangle2D bounds) {
        super(t, bounds);

        // If y shift is 0, we can effectively use half height optimisation
        halfHeight = (mgmtThread.getShiftY() == 0);
    }

    /**
     * Make sure image is half height so we only calculate top half
     * Mandelbrot set is symmetric in the x axis
     * @return Half image height
     */
    @Override
    protected int getImageHeight() {
        if (halfHeight) {
            return super.getImageHeight() / 2 + 2;
        } else {
            return super.getImageHeight();
        }
    }

    /**
     * Since image is half height, we need to flip the existing image and paint it below
     */
    @Override
    protected void adjustImage(){
        if (!halfHeight) return;

        BufferedImage newImage = new BufferedImage(image.getWidth(), image.getHeight() * 2, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = newImage.createGraphics();

        // Draw original image
        g.drawImage(image, 0, 0, null);

        // Flip image upside down (reflect in x = 0)
        AffineTransform at = new AffineTransform();
        at.concatenate(AffineTransform.getScaleInstance(1 ,-1));
        at.concatenate(AffineTransform.getTranslateInstance(0, -image.getHeight()));
        g.transform(at);

        // Adjust image positioning so we don't get any black lines
        g.drawImage(image, 0, 2 -image.getHeight(), null);
        g.dispose();

        image = newImage;
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
        Complex z = complex.clone();
        Complex prevPoint;

        while (z.squareReal() + z.squareImaginary() <= escapeRadiusSquared && currIteration < maxIterations) {
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
