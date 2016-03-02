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

    protected Complex c;

    public JuliaRenderManagementThread(Main mainWindow, OpenClRenderThread thread, ImagePanel panel) {
        super(mainWindow, thread, panel, "Julia");
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
        CLProgram julia = openClRenderThread.getProgram("julia");
        int iterations = this.iterations;
        int escapeRadius = config.getEscapeRadiusSquared();
        float[] complex = new float[]{(float) c.getReal(), (float) c.getImaginary()};
        float[] dimensions = new float[]{(float) dimension.width, (float) dimension.height};
        float[] scales = new float[]{(float) xScale, (float) yScale};
        float[] shifts = new float[]{(float) xShift, (float) yShift};
        float scaleFactor = (float)this.scaleFactor;

        return julia.createKernel(
                "julia",
                iterations,
                escapeRadius,
                complex,
                dimensions,
                scales,
                shifts,
                scaleFactor,
                results
        );
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

    /**
     * Loads programs into the OpenCL context
     */
    @Override
    protected void ocl_loadPrograms() {
        super.ocl_loadPrograms();
        if (!openClRenderThread.loadProgram("julia", this.getClass().getResourceAsStream("/mandelbrot/opencl/julia.cl"))) {
            this.isOpenClAvailable = false;
        }
    }

    @Override
    protected double getScaleFactor() {
        return 1;
    }
}
