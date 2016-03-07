package mandelbrot.management;

import com.nativelibs4java.opencl.CLBuffer;
import com.nativelibs4java.opencl.CLKernel;
import mandelbrot.Main;
import mandelbrot.render.BurningShipTask;
import utils.ImagePanel;
import utils.ImageSegment;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.concurrent.Callable;

/**
 * Burning Ship Fractal
 *
 * @author Huw Jones
 * @since 07/03/2016
 */
public class BurningShipManagementThread extends RenderManagementThread {

    public BurningShipManagementThread(Main mainWindow, OpenClThread thread, ImagePanel panel) {
        super(mainWindow, thread, panel, "BurningShip");
    }

    /**
     * Creates the task to dispatch to the workers
     *
     * @param bounds Bounds of render area
     * @return ImageSegment with result of render
     */
    @Override
    protected Callable<ImageSegment> createTask(Rectangle2D bounds) {
        return new BurningShipTask(this, bounds);
    }

    /**
     * Creates the Open CL Kernel for execution
     *
     * @param dimension Dimensions of image to render
     * @param results   Buffer to put results into
     * @return CLKernel to execute
     */
    @Override
    protected CLKernel createOpenCLKernel(Dimension dimension, CLBuffer<Integer> results) {
        return null;
    }
}
