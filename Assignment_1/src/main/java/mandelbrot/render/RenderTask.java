package mandelbrot.render;

import mandelbrot.management.RenderManagementThread;
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
public abstract class RenderTask implements Callable<ImageSegment> {

    public static final float M_LN2_F = (float)Math.log(2);

    protected RenderManagementThread mgmtThread;
    protected Rectangle2D bounds;
    protected int maxIterations;
    protected BufferedImage image;

    protected Point2D absolutePoint;
    protected Point2D relativePoint;

    public RenderTask(RenderManagementThread t, Rectangle2D bounds) {
        this.mgmtThread = t;
        this.bounds = bounds;
        this.maxIterations = mgmtThread.getIterations();
    }

    /**
     * Computes a result, or throws an exception if unable to do so.
     *
     * @return computed result
     * @throws Exception if unable to compute a result
     */
    @Override
    public ImageSegment call() throws Exception {
        image = new BufferedImage(getImageWidth(), getImageHeight(), BufferedImage.TYPE_INT_RGB);
        ColouredPixel pixel;
        for (int y = 0; y < getImageHeight(); y++) {
            for (int x = 0; x < getImageWidth(); x++) {
                absolutePoint = new Point2D.Double(x + bounds.getX(), y + bounds.getY());
                relativePoint = new Point2D.Double(x, y);

                Complex c = mgmtThread.getComplexFromPoint(absolutePoint);

                pixel = doPixelCalculation(relativePoint, c);
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

    protected Color getHSBColour(int iterations, Complex z){
        return Color.getHSBColor(getHue(iterations, z), mgmtThread.getSaturation(), mgmtThread.getBrightness());
    }

    protected float getHue(int iterations, Complex z){
        // sqrt of inner term removed using log simplification rules. log(x^(1/2)) = (1/2)*log(x) = log(x) / 2
        double log_z = Math.log((z.getReal() * z.getReal()) + (z.getImaginary() * z.getImaginary())) / 2.0d;
        double nu = Math.log( log_z / M_LN2_F ) / M_LN2_F;
        return mgmtThread.getHue() + (iterations + 1 - (float)nu ) / 110f;
    }

    protected abstract ColouredPixel doPixelCalculation(Point2D point, Complex complex);

    private void paintPixel(ColouredPixel p) {
        int x = Double.valueOf(p.getPoint().getX()).intValue();
        int y = Double.valueOf(p.getPoint().getY()).intValue();
        this.image.setRGB(x, y, p.getColour().getRGB());
    }
}
