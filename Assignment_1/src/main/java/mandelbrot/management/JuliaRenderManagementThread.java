package mandelbrot.management;

import com.nativelibs4java.opencl.CLBuffer;
import com.nativelibs4java.opencl.CLKernel;
import com.nativelibs4java.opencl.CLProgram;
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

    protected Complex complex;

    public JuliaRenderManagementThread(Main mainWindow, OpenClRenderThread thread, ImagePanel panel) {
        super(mainWindow, thread, panel, "Julia");
    }

    /**
     * Gets properties for this render
     *
     * @return ImageProperties
     */
    @Override
    protected ImageProperties getRenderProperties() {
        ImageProperties properties = super.getRenderProperties();
        properties.setComplex(complex);
        return properties;
    }

    @Override
    protected Callable<ImageSegment> createTask(Rectangle2D bounds) {
        return new JuliaTask(this, bounds, complex);
    }

    /**
     * Creates the CL Kernel for execution
     *
     * @param dimension Dimensions of image to render
     * @param results   Buffer to put results into
     * @return CLKernel to execute
     */
    protected CLKernel createOpenCLKernel(Dimension dimension, CLBuffer<Integer> results) {
        CLProgram julia = openClRenderThread.getProgram("julia");
        int iterations = this.iterations;
        int escapeRadius = config.getEscapeRadiusSquared();
        float[] complex = new float[]{(float)this.complex.getReal(), (float)this.complex.getImaginary()};
        float[] dimensions = new float[]{(float) dimension.width, (float) dimension.height};
        float[] scales = new float[]{(float) xScale, (float) yScale};
        float[] shifts = new float[]{(float) xShift, (float) yShift};
        float scaleFactor = (float)this.scaleFactor;
        float huePrev = getImageHue();
        float hueAdj = getHue();
        float saturation = getSaturation();
        float brightness = getBrightness();

        return julia.createKernel(
                "julia",
                iterations,
                escapeRadius,
                complex,
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

    @Override
    public void render() {
        this.complex = config.getSelectedPoint();
        super.render();
    }

    @Override
    public double getScale() {
        return 1;
    }

    @Override
    public double getShiftX() {
        return 0;
    }

    @Override
    public double getShiftY() {
        return 0;
    }

    /**
     * Loads programs into the OpenCL context
     */
    @Override
    protected void ocl_loadPrograms() {
        super.ocl_loadPrograms();
        if (!openClRenderThread.loadProgram("julia", this.getClass().getResourceAsStream("/mandelbrot/opencl/julia.cl"))) {
            config.disableOpenCL();
        }
    }
}
