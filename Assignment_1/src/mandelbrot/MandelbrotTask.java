package mandelbrot;

import utils.ColouredPixel;
import utils.Complex;
import utils.ImageSegment;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

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

    /**
     * Make sure image is half height so we only calculate top half
     * Mandelbrot set is symmetric in the x axis
     * @return Half image height
     */
    @Override
    protected int getImageHeight() {
        return super.getImageHeight()/2;
    }

    /**
     * Since image is half height, we need to flip the existing image and paint it below
     */
    @Override
    protected void adjustImage(){
        BufferedImage newImage = new BufferedImage(image.getWidth(), image.getHeight() * 2, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = newImage.createGraphics();

        g.drawImage(image, 0, 0, null);
        AffineTransform at = new AffineTransform();
        at.concatenate(AffineTransform.getScaleInstance(1 ,-1));
        at.concatenate(AffineTransform.getTranslateInstance(0, -image.getHeight()));
        g.transform(at);
        g.drawImage(image, 0, -image.getHeight(), null);
        g.dispose();

        image = newImage;
    }

    @Override
    protected ColouredPixel doPixelCalculation(Point2D point, Complex c) {
        int currIteration = 0;
        Complex z = c.clone();
        Complex prevPoint;

        while (z.squareReal() + z.squareImaginary() <= 4 && currIteration < maxIterations) {
            prevPoint = z.clone();
            z = z.square();
            z.add(c);

            // Apply period detection
            if(z.equals(prevPoint)){
                currIteration = maxIterations;
                break;
            }

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
