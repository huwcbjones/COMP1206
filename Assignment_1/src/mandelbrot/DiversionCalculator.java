package mandelbrot;

import utils.Complex;
import utils.ImageSegment;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.concurrent.Callable;

/**
 * {DESCRIPTION}
 *
 * @author Huw Jones
 * @since 27/02/2016
 */
public class DiversionCalculator implements Callable<ImageSegment> {

    private DrawingThread drawingThread;
    private Rectangle2D bounds;
    private int maxIterations;
    private BufferedImage image;

    public DiversionCalculator(DrawingThread t, Rectangle2D bounds, int maxIterations) {
        this.drawingThread = t;
        this.bounds = bounds;
        this.maxIterations = maxIterations;
    }

    /**
     * Computes a result, or throws an exception if unable to do so.
     *
     * @return computed result
     * @throws Exception if unable to compute a result
     */
    @Override
    public ImageSegment call() throws Exception {
        image = new BufferedImage((int) bounds.getWidth(), (int) bounds.getHeight(), BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < bounds.getHeight(); y++) {
            for (int x = 0; x < bounds.getWidth(); x++) {

                Point2D absP = new Point2D.Double(x + bounds.getX(), y + bounds.getY());
                Point2D relP = new Point2D.Double(x, y);

                Complex c = drawingThread.getComplexFromPoint(absP);
                int currIteration = 0;
                Complex z = new Complex(c.getReal(), c.getImaginary());

                while (z.modulusSquared() <= 4 && currIteration < maxIterations) {
                    z = z.square();
                    z.add(c);
                    currIteration++;
                }
                if (currIteration < maxIterations) {
                    paintPixel(relP, Color.getHSBColor(currIteration / 100f, 1, 1));
                } else {
                    paintPixel(relP, Color.BLACK);
                }
            }
        }
        return new ImageSegment(image, bounds);
    }

    private void paintPixel(Point2D p, Color c) {
        int x = Double.valueOf(p.getX()).intValue();
        int y = Double.valueOf(p.getY()).intValue();

        this.image.setRGB(x, y, c.getRGB());
    }
}
