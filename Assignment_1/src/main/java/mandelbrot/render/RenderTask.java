package mandelbrot.render;

import mandelbrot.management.RenderManagementThread;
import utils.ColouredPixel;
import utils.Complex;
import utils.ImageProperties;
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

    protected RenderManagementThread renderManagementThread;
    protected Rectangle2D bounds;
    protected int maxIterations;
    protected ImageProperties properties;
    protected BufferedImage image;

    protected Point2D absolutePoint;
    protected Point2D relativePoint;

    public RenderTask(RenderManagementThread t, Rectangle2D bounds, ImageProperties properties) {
        this.renderManagementThread = t;
        this.bounds = bounds;
        this.maxIterations = properties.getIterations();
        this.properties = properties;
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

                Complex c = renderManagementThread.getComplexFromPoint(absolutePoint);

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
        return Color.getHSBColor(getHue(iterations, z), 1, 1);
    }

    protected float getHue(int iterations, Complex z){
        return properties.getHue() + (float)(iterations + 1 - Math.log(Math.log(Math.sqrt(z.modulusSquared()))) / Math.log(2)) / 100f;
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
