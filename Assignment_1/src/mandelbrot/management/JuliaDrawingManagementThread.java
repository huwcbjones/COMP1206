package mandelbrot.management;

import mandelbrot.render.JuliaTask;
import mandelbrot.Main;
import utils.Complex;
import utils.ImagePanel;
import utils.ImageProperties;
import utils.ImageSegment;

import java.awt.geom.Rectangle2D;
import java.util.concurrent.Callable;

/**
 * Julia Drawing Management Thread
 *
 * @author Huw Jones
 * @since 28/02/2016
 */
public class JuliaDrawingManagementThread extends DrawingManagementThread {

    protected Complex c;

    public JuliaDrawingManagementThread(Main mainWindow, ImagePanel panel) {
        super(mainWindow, panel, "Julia");
    }

    @Override
    public ImageProperties getImageProperties() {
        ImageProperties img = super.getImageProperties();
        img.setComplex(c);
        return img;
    }

    @Override
    protected Callable<ImageSegment> createTask(ImageProperties properties, Rectangle2D bounds) {
        return new JuliaTask(this, bounds, properties, properties.getComplex());
    }


    public void draw(Complex c) {
        this.c = c;
        super.draw();
    }

    @Override
    protected double getxShift() {
        return 0;
    }

    @Override
    protected double getyShift() {
        return 0;
    }

    @Override
    protected double getScaleFactor() {
        return 1;
    }
}
