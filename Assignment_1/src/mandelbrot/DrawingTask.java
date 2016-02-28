package mandelbrot;

import utils.ColouredPixel;
import utils.Complex;
import utils.ImageSegment;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.concurrent.Callable;

/**
 * Processes Render for Image segment
 *
 * @author Huw Jones
 * @since 27/02/2016
 */
public abstract class DrawingTask implements Callable<ImageSegment> {

    protected DrawingManagementThread drawingManagementThread;
    protected Rectangle2D bounds;
    protected int maxIterations;
    protected BufferedImage image;

    public DrawingTask(DrawingManagementThread t, Rectangle2D bounds, int maxIterations) {
        this.drawingManagementThread = t;
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
    public final ImageSegment call() throws Exception {
        image = new BufferedImage(getImageWidth(), getImageHeight(), BufferedImage.TYPE_INT_RGB);
        ColouredPixel pixel;
        for (int y = 0; y < getImageHeight(); y++) {
            for (int x = 0; x < getImageWidth(); x++) {
                Point2D absP = new Point2D.Double(x + bounds.getX(), y + bounds.getY());
                Point2D relP = new Point2D.Double(x, y);

                Complex c = drawingManagementThread.getComplexFromPoint(absP);

                pixel = doPixelCalculation(relP, c);
                paintPixel(pixel);
            }
        }

        adjustImage();

        return new ImageSegment(image, bounds);
    }

    protected int getImageHeight(){
        return (int)this.bounds.getHeight();
    }
    protected int getImageWidth(){
        return (int)this.bounds.getWidth();
    }
    protected void adjustImage(){
    }

    protected abstract ColouredPixel doPixelCalculation(Point2D point, Complex complex);

    private void paintPixel(Point2D p, Color c) {
        int x = Double.valueOf(p.getX()).intValue();
        int y = Double.valueOf(p.getY()).intValue();

        this.image.setRGB(x, y, c.getRGB());
    }

    private void paintPixel(ColouredPixel p) {
        int x = Double.valueOf(p.getPoint().getX()).intValue();
        int y = Double.valueOf(p.getPoint().getY()).intValue();
        this.image.setRGB(x, y, p.getColour().getRGB());
    }
}
