package mandelbrot.management;

import com.nativelibs4java.opencl.CLBuffer;
import com.nativelibs4java.opencl.CLKernel;
import com.nativelibs4java.opencl.CLProgram;
import mandelbrot.Main;
import mandelbrot.render.MandelbrotTask;
import utils.ImagePanel;
import utils.ImageSegment;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.concurrent.Callable;

/**
 * Management Thread for Mandelbrot Set
 *
 * @author Huw Jones
 * @since 28/02/2016
 */
public class MandelbrotRenderManagementThread extends RenderManagementThread {
    public MandelbrotRenderManagementThread(Main mainWindow, OpenClRenderThread thread, ImagePanel panel) {
        super(mainWindow, thread, panel, "Mandelbrot");
    }

    /**
     * Loads programs into the OpenCL context
     */
    @Override
    protected void ocl_loadPrograms() {
        super.ocl_loadPrograms();

        String arch = "32";
        if (openClRenderThread.useDouble()) arch = "64";

        if (!openClRenderThread.loadProgram("mandelbrot", this.getClass().getResourceAsStream("/mandelbrot/opencl/x" + arch + "/mandelbrot.cl"))) {
            config.disableOpenCL();
        }
    }

    /**
     * Creates the task to dispatch to the workers
     *
     * @param bounds     Bounds of render area
     * @return ImageSegment with result of render
     */
    @Override
    protected Callable<ImageSegment> createTask(Rectangle2D bounds) {
        return new MandelbrotTask(this, bounds);
    }

    /**
     * Creates the CL Kernel for execution
     *
     * @param dimension Dimensions of image to render
     * @param results   Buffer to put results into
     * @return CLKernel to execute
     */
    @Override
    protected CLKernel createOpenCLKernel(Dimension dimension, CLBuffer<Integer> results) {
        CLProgram mandelbrot = openClRenderThread.getProgram("mandelbrot");
        if (openClRenderThread.useDouble()) {
            return getX64Kernel(mandelbrot, dimension, results);
        } else {
            return getX32Kernel(mandelbrot, dimension, results);
        }
    }

    private CLKernel getX64Kernel(CLProgram mandelbrot, Dimension dimension, CLBuffer<Integer> results) {
        int iterations = this.iterations;
        int escapeRadius = config.getEscapeRadiusSquared();
        double[] dimensions = new double[]{dimension.width, dimension.height};
        double[] scales = new double[]{xScale, yScale};
        double[] shifts = new double[]{getShiftX(), getShiftY()};
        double scaleFactor = getScale();
        double huePrev = getImageHue();
        double hueAdj = getHue();
        double saturation = getSaturation();
        double brightness = getBrightness();

        return mandelbrot.createKernel(
                "mandelbrot",
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

    private CLKernel getX32Kernel(CLProgram mandelbrot, Dimension dimension, CLBuffer<Integer> results) {
        int iterations = this.iterations;
        int escapeRadius = config.getEscapeRadiusSquared();
        float[] dimensions = new float[]{(float) dimension.width, (float) dimension.height};
        float[] scales = new float[]{(float) xScale, (float) yScale};
        float[] shifts = new float[]{(float) getShiftX(), (float) getShiftY()};
        float scaleFactor = (float) getScale();
        float huePrev = getImageHue();
        float hueAdj = getHue();
        float saturation = getSaturation();
        float brightness = getBrightness();

        return mandelbrot.createKernel(
                "mandelbrot",
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
