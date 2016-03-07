package mandelbrot.management;

import com.nativelibs4java.opencl.CLBuffer;
import com.nativelibs4java.opencl.CLKernel;
import com.nativelibs4java.opencl.CLProgram;
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
     * Loads programs into the OpenCL context
     */
    @Override
    protected void ocl_loadPrograms() {
        super.ocl_loadPrograms();

        // Load both double and float versions
        if (!openClThread.loadProgram("burningShip_x64", this.getClass().getResourceAsStream("/mandelbrot/opencl/x64/burningShip.cl"))) {
            config.disableOpenCL();
        }
        if (!openClThread.loadProgram("burningShip_x32", this.getClass().getResourceAsStream("/mandelbrot/opencl/x32/burningShip.cl"))) {
            config.disableOpenCL();
        }
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
        if (openClThread.useDouble() && config.useOpenCL_double()) {
            return getX64Kernel(dimension, results);
        } else {
            return getX32Kernel(dimension, results);
        }
    }

    private CLKernel getX64Kernel( Dimension dimension, CLBuffer<Integer> results) {
        CLProgram burningShip = openClThread.getProgram("burningShip_x64");
        int iterations = this.iterations;
        double escapeRadius = escapeRadiusSquared;
        double[] dimensions = new double[]{dimension.width, dimension.height};
        double[] scales = new double[]{xScale, yScale};
        double[] shifts = new double[]{getShiftX(), getShiftY()};
        double scaleFactor = getScale();
        double huePrev = getImageHue();
        double hueAdj = getHue();
        double saturation = getSaturation();
        double brightness = getBrightness();

        return burningShip.createKernel(
                "burningShip",
                iterations,
                escapeRadius,
                dimensions,
                scales,
                shifts,
                scaleFactor,
                huePrev,
                hueAdj,
                saturation,
                brightness,
                results
        );
    }

    private CLKernel getX32Kernel(Dimension dimension, CLBuffer<Integer> results) {
        CLProgram burningShip = openClThread.getProgram("burningShip_x32");
        int iterations = this.iterations;
        float escapeRadius = (float)escapeRadiusSquared;
        float[] dimensions = new float[]{(float) dimension.width, (float) dimension.height};
        float[] scales = new float[]{(float) xScale, (float) yScale};
        float[] shifts = new float[]{(float) getShiftX(), (float) getShiftY()};
        float scaleFactor = (float) getScale();
        float huePrev = getImageHue();
        float hueAdj = getHue();
        float saturation = getSaturation();
        float brightness = getBrightness();

        return burningShip.createKernel(
                "burningShip",
                iterations,
                escapeRadius,
                dimensions,
                scales,
                shifts,
                scaleFactor,
                huePrev,
                hueAdj,
                saturation,
                brightness,
                results
        );
    }
}
