package mandelbrot.management;

import com.nativelibs4java.opencl.CLBuffer;
import com.nativelibs4java.opencl.CLKernel;
import mandelbrot.ConfigManager;
import mandelbrot.render.JuliaTask;
import mandelbrot.Main;
import utils.Complex;
import utils.ImagePanel;
import utils.ImageProperties;
import utils.ImageSegment;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.concurrent.Callable;

/**
 * Julia Drawing Management Thread
 *
 * @author Huw Jones
 * @since 28/02/2016
 */
public class JuliaRenderManagementThread extends RenderManagementThread {

    protected Complex c;

    public JuliaRenderManagementThread(ConfigManager config, OpenClRenderThread thread, ImagePanel panel) {
        super(config, thread, panel, "Julia");
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

    /**
     * Creates the CL Kernel for execution
     *
     * @param dimension Dimensions of image to render
     * @param results   Buffer to put results into
     * @return CLKernel to execute
     */
    @Override
    protected CLKernel createOpenCLKernel(Dimension dimension, CLBuffer<Float> results) {
        return null;
    }


    public void render(Complex c) {
        this.c = c;
        super.render();
    }

    @Override
    protected double getShiftX() {
        return 0;
    }

    @Override
    protected double getShiftY() {
        return 0;
    }

    @Override
    protected double getScaleFactor() {
        return 1;
    }
}
