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

    protected final RenderManagementThread mgmtThread;
    protected final Rectangle2D bounds;
    protected final int maxIterations;
    protected final double escapeRadiusSquared;
    protected BufferedImage image;

    protected Point2D absolutePoint;
    protected Point2D relativePoint;

    public RenderTask(RenderManagementThread t, Rectangle2D bounds) {
        this.mgmtThread = t;
        this.bounds = bounds;
        this.maxIterations = mgmtThread.getIterations();
        this.escapeRadiusSquared = mgmtThread.getEscapeRadiusSquared();
    }

    /**
     * Computes a result, or throws an exception if unable to do so.
     *
     * @return computed result
     * @throws Exception if unable to compute a result
     */
    @Override
    public ImageSegment call() throws Exception {
        // Create image segment
        image = new BufferedImage(getImageWidth(), getImageHeight(), BufferedImage.TYPE_INT_RGB);
        ColouredPixel pixel;
        // Loop through row by row
        for (int y = 0; y < getImageHeight(); y++) {
            for (int x = 0; x < getImageWidth(); x++) {
                // Get points (relative to image segment, for painting and absolute for Image Panel)
                absolutePoint = new Point2D.Double(x + bounds.getX(), y + bounds.getY());
                relativePoint = new Point2D.Double(x, y);

                Complex c = mgmtThread.getComplexFromPoint(absolutePoint);

                // Perform calculation
                pixel = doPixelCalculation(relativePoint, c);

                paintPixel(pixel);
            }
        }

        // Perform final image adjustment
        adjustImage();

        return new ImageSegment(image, bounds);
    }

    protected int getImageHeight(){
        return (int)this.bounds.getHeight();
    }
    protected int getImageWidth(){
        return (int)this.bounds.getWidth();
    }

    /**
     * If the image segment needs adjusting (e.g.: half height optimisation), override this method
     * and perform it here.
     */
    protected void adjustImage(){
    }

    /**
     * Gets HSB colour from iterations and a Complex, z.
     * @param iterations Number of iterations it took
     * @param z Final complex, z
     * @return Color of complex
     */
    protected Color getHSBColour(int iterations, Complex z){
        return Color.getHSBColor(getHue(iterations, z), mgmtThread.getSaturation(), mgmtThread.getBrightness());
    }

    /**
     * Performs colour smoothing
     * See <a href="https://en.wikipedia.org/wiki/Mandelbrot_set#Continuous_.28smooth.29_coloring">Continuous Smooth Colouring</a>
     * @param iterations Number of iterations it took
     * @param z Final complex, z
     * @return Hue for complex
     */
    protected float getHue(int iterations, Complex z){
        // sqrt of inner term removed using log simplification rules. log(x^(1/2)) = (1/2)*log(x) = log(x) / 2
        double log_z = Math.log((z.getReal() * z.getReal()) + (z.getImaginary() * z.getImaginary())) / 2.0d;
        double nu = Math.log( log_z / M_LN2_F ) / M_LN2_F;
        return mgmtThread.getHue() + (iterations + 1 - (float)nu ) / 110f;
    }

    /**
     * Worker Unit for individual pixels
     * @param point Point on worker unit
     * @param complex Complex for point
     * @return ColouredPixel for that pixel
     */
    protected abstract ColouredPixel doPixelCalculation(Point2D point, Complex complex);

    /**
     * Paints a coloured pixel on the image segment
     * @param p Pixel to paint
     */
    private void paintPixel(ColouredPixel p) {
        // Get x/y values
        int x = Double.valueOf(p.getPoint().getX()).intValue();
        int y = Double.valueOf(p.getPoint().getY()).intValue();

        this.image.setRGB(x, y, p.getColour().getRGB());
    }
}
