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
    public MandelbrotRenderManagementThread(Main mainWindow, OpenClThread thread, ImagePanel panel) {
        super(mainWindow, thread, panel, "Mandelbrot");
    }

    /**
     * Loads programs into the OpenCL context
     */
    @Override
    protected void ocl_loadPrograms() {
        super.ocl_loadPrograms();

        // Load both double and float versions
        if (!this.openClThread.loadProgram("mandelbrot_x64", this.getClass().getResourceAsStream("/mandelbrot/opencl/x64/mandelbrot.cl"))) {
            this.config.disableOpenCL();
        }
        if (!this.openClThread.loadProgram("mandelbrot_x32", this.getClass().getResourceAsStream("/mandelbrot/opencl/x32/mandelbrot.cl"))) {
            this.config.disableOpenCL();
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
        if (this.openClThread.useDouble() && this.config.useOpenCL_double()) {
            return this.getX64Kernel(dimension, results);
        } else {
            return this.getX32Kernel(dimension, results);
        }
    }

    private CLKernel getX64Kernel( Dimension dimension, CLBuffer<Integer> results) {
        CLProgram mandelbrot = this.openClThread.getProgram("mandelbrot_x64");
        int iterations = this.iterations;
        double escapeRadius = this.escapeRadiusSquared;
        double[] dimensions = new double[]{dimension.width, dimension.height};
        double[] scales = new double[]{this.xScale, this.yScale};
        double[] shifts = new double[]{this.getShiftX(), this.getShiftY()};
        double scaleFactor = this.getScale();
        double huePrev = this.getImageHue();
        double hueAdj = this.getHue();
        double saturation = this.getSaturation();
        double brightness = this.getBrightness();

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

    private CLKernel getX32Kernel(Dimension dimension, CLBuffer<Integer> results) {
        CLProgram mandelbrot = this.openClThread.getProgram("mandelbrot_x32");
        int iterations = this.iterations;
        float escapeRadius = (float) this.escapeRadiusSquared;
        float[] dimensions = new float[]{(float) dimension.width, (float) dimension.height};
        float[] scales = new float[]{(float) this.xScale, (float) this.yScale};
        float[] shifts = new float[]{(float) this.getShiftX(), (float) this.getShiftY()};
        float scaleFactor = (float) this.getScale();
        float huePrev = this.getImageHue();
        float hueAdj = this.getHue();
        float saturation = this.getSaturation();
        float brightness = this.getBrightness();

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
