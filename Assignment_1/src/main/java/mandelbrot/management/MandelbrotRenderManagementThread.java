package mandelbrot.management;

import com.nativelibs4java.opencl.CLBuffer;
import com.nativelibs4java.opencl.CLKernel;
import com.nativelibs4java.opencl.CLProgram;
import mandelbrot.ConfigManager;
import mandelbrot.render.MandelbrotTask;
import utils.ImagePanel;
import utils.ImageProperties;
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
    public MandelbrotRenderManagementThread(ConfigManager config, OpenClRenderThread thread, ImagePanel panel) {
        super(config, thread, panel, "Mandelbrot");
    }

    /**
     * Loads programs into the OpenCL context
     */
    @Override
    protected void ocl_loadPrograms() {
        super.ocl_loadPrograms();
        if (!openClRenderThread.loadProgram("mandelbrot", this.getClass().getResourceAsStream("/mandelbrot/mandelbrot.cl"))) {
            this.isOpenClAvailable = false;
        }
    }

    /**
     * Creates the task to dispatch to the workers
     *
     * @param properties Properties of image to render
     * @param bounds     Bounds of render area
     * @return ImageSegment with result of render
     */
    @Override
    protected Callable<ImageSegment> createTask(ImageProperties properties, Rectangle2D bounds) {
        return new MandelbrotTask(this, bounds, properties);
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

        CLProgram mandelbrot = openClRenderThread.getProgram("mandelbrot");
        return mandelbrot.createKernel(
                "mandelbrot",
                iterations,
                config.getEscapeRadiusSquared(),
                new float[]{(float) dimension.width, (float) dimension.height},
                new float[]{(float) xScale, (float) yScale},
                new float[]{(float) xShift, (float) yShift},
                scaleFactor,
                results
        );
    }


}
