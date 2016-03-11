package mandelbrot.management;

import com.nativelibs4java.opencl.*;
import mandelbrot.CacheManager;
import mandelbrot.ConfigManager;
import mandelbrot.Main;
import mandelbrot.events.RenderListener;
import mandelbrot.render.RecolourTask;
import org.bridj.Pointer;
import utils.*;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.concurrent.*;
/**
 * Manages and delegates drawing/calculation threads
 *
 * @author Huw Jones
 * @since 27/02/2016
 */

public abstract class RenderManagementThread extends Thread {
    private ArrayList<RenderListener> listeners;

    protected final Object runThread = new Object();

    protected ImagePanel panel;
    private FractalImage image;
    protected boolean hasRendered = false;

    protected final ConfigManager config;
    protected final OpenClThread openClThread;
    protected final CacheManager cacheManager;

    //region Image Properties
    protected double imgHeight;
    protected double imgWidth;

    protected double escapeRadiusSquared;
    protected double xShift;
    protected double yShift;
    protected double scaleFactor;
    protected int iterations;
    protected float hue;
    protected float saturation = 1.0f;
    protected float brightness = 1.0f;

    protected double xScale;
    protected double yScale;
    //endregion

    //region CPU Multithreading
    protected ExecutorCompletionService<ImageSegment> executorService;
    protected int numberThreads;
    protected int numberStrips;
    //endregion

    /**
     * Creates a Render Management Thread
     *  @param mainWindow     Config Manager
     * @param panel      ImagePanel to output render to
     * @param threadName Name of thread
     */
    public RenderManagementThread(Main mainWindow, OpenClThread openCL, ImagePanel panel, String threadName) {
        Log.Information("Loading Render Management Thread: '" + threadName + "'");

        this.setName("Render_Management_Thread_" + threadName);
        this.config = mainWindow.getConfigManager();
        this.panel = panel;
        this.openClThread = openCL;

        // Load OpenCL programs
        this.ocl_loadPrograms();

        this.cacheManager = new CacheManager();

        // Initialise Event Handling
        this.listeners = new ArrayList<>();

        // Initialise CPU Multithreaded Rendering
        this.numberThreads = Runtime.getRuntime().availableProcessors();
        this.numberStrips = this.numberThreads * 2;

        Log.Information("Multicore Processing: Using " + this.numberThreads + ", and " + this.numberStrips + " workers.");

        // Get an executor service for the amount of cores we have
        ExecutorService executorService = Executors.newFixedThreadPool(this.numberThreads);
        this.executorService = new ExecutorCompletionService<>(executorService);
        Log.Information("Starting execution pool...");
    }

    /**
     * Loads programs into the OpenCL context
     */
    protected void ocl_loadPrograms() {
        if (!this.openClThread.loadProgram("recolour", this.getClass().getResourceAsStream("/mandelbrot/opencl/recolour.cl"))) {
            this.config.disableOpenCL();
        }
    }

    //region Get Methods

    /**
     * Returns the original un-tinted image
     *
     * @return BufferedImage, render result
     */
    public FractalImage getImage() {
        return this.image;
    }

    /**
     * Gets escape radius squared
     *
     * @return double escape radius squared
     */
    public final double getEscapeRadiusSquared() {
        return this.escapeRadiusSquared;
    }

    /**
     * Gets max iteration number
     *
     * @return int max iteration count
     */
    public final int getIterations() {
        return this.iterations;
    }

    /**
     * Gets scale factor
     *
     * @return double scale factor
     */
    public double getScale() {
        return this.scaleFactor;
    }

    /**
     * Gets X axis shift
     *
     * @return double X axis shift
     */
    public double getShiftX() {
        return this.xShift;
    }

    /**
     * Gets Y axis shift
     *
     * @return double Y axis shift
     */
    public double getShiftY() {
        return this.yShift;
    }

    /**
     * Gets image hue offset
     *
     * @return float image hue
     */
    public final float getHue() {
        return this.hue;
    }

    /**
     * Gets the current hue offset of the image
     * @return float image hue
     */
    public final float getImageHue() {
        return this.image.getColourProperties().getHue();
    }
    /**
     * Gets image saturation
     *
     * @return float image saturation
     */
    public final float getSaturation() {
        return this.saturation;
    }

    /**
     * Gets image brightness
     *
     * @return float image brightness
     */
    public float getBrightness() {
        return this.brightness;
    }

    /**
     * Gets the complex represented by point, p.
     *
     * @param p Point2D to convert to a Complex number
     * @return Complex number
     */
    public final Complex getComplexFromPoint(Point2D p) {
        return this.getComplexFromPoint(p.getX(), p.getY());
    }

    /**
     * Gets the complex represented by point, (x, y).
     *
     * @param x X co-ordinate
     * @param y Y co-ordinate
     * @return Complex number
     */
    public final Complex getComplexFromPoint(double x, double y) {
        x = (x - this.imgWidth / 2d) * this.xScale / this.scaleFactor + this.xShift;
        y = (y - this.imgHeight / 2d) * this.yScale / this.scaleFactor + this.yShift;

        return new Complex(x, y);
    }

    /**
     * Returns whether an image has rendered
     *
     * @return true if an image has been rendered
     */
    public final boolean hasRendered() {
        return this.hasRendered;
    }

    //endregion

    //region Event Listening & Handling
    public void addRenderListener(RenderListener listener) {
        this.listeners.add(listener);
    }

    private void fireRenderComplete() {
        this.listeners.forEach(RenderListener::renderComplete);
    }
    //endregion

    //region Public Thread Unblocking
    /*
     * Prevents calling threads from entering a blocked state whilst the render processes
     */

    @Override
    public final void run() {
        Log.Information(this.getName() + " started!");
        while (!this.isInterrupted()) {

            // Dispatch render method as appropriate to notify
            synchronized (this.runThread) {
                try {
                    this.runThread.wait();
                    this.doRender();
                } catch (InterruptedException e) {
                    break;
                }
            }
        }
    }

    /**
     * Public method to notify the thread to call doRender without blocking
     */
    public void render() {
        synchronized (this.runThread) {
            this.runThread.notify();
        }
    }
    //endregion

    //region Render Processing

    /**
     * Gets properties for this render
     * @return ImageProperties
     */
    protected ImageProperties getRenderProperties(){
        return new ImageProperties((int) this.imgWidth, (int) this.imgHeight, this.getIterations(), this.getScale(), this.getShiftX(), this.getShiftY());
    }

    /**
     * Private method to do the render
     */
    private void doRender() {
        // Create image
        this.image = FractalImage.fromBufferedImage(this.panel.createImage());

        // Update image properties for this render
        this.updateImageProperties();

        // Check if image is cached
        boolean fullRender = true;

        // Create image properties
        ImageProperties properties = this.getRenderProperties();
        ImageColourProperties colourProperties = new ImageColourProperties(this.getHue(), this.getSaturation(), this.getBrightness());

        // If cache is enabled, do cache stuff
        if (!this.config.isCacheDisabled()) {

            // If image is cached with both properties and colours, retrieve it and display it.
            if (this.cacheManager.isCached(properties, colourProperties)) {
                Log.Information("Displaying cached image. " + properties.toString() + "/" + colourProperties.toString());
                this.image = this.cacheManager.getImage(properties, colourProperties);
                this.panel.setImage(this.image, true);
                this.fireRenderComplete();
                this.hasRendered = true;
                return;
            }

            // If image with same properties (dimensions, iterations, etc) is cached, use this
            // as recolouring is quicker than rendering
            if (this.cacheManager.isCached(properties)) {
                Log.Information("Recolouring cached image. " + properties.toString());
                this.image = this.cacheManager.getImage(properties);
                Log.Information(" - from: " + this.image.getColourProperties().toString());
                Log.Information(" - to  : " + colourProperties.toString());
                fullRender = false;
            }
        }

        if (this.config.useOpenCL()) {
            try {
                this.runOpenCL_render(fullRender);
            } catch (CLException e){

                // Fallback to CPU if OpenCL failed (for whatever reason)
                this.config.disableOpenCL();
                e.printStackTrace();
            }
        } else {
            this.runCPU_render(fullRender);
        }

        // Update image properties
        this.image.setProperties(properties);
        this.image.setColourProperties(colourProperties);

        // Cache the image if cache is enabled
        if(!this.config.isCacheDisabled()) {
            this.cacheManager.cacheImage(this.image);
        }

        this.panel.setImage(this.image, true);

        // Let everyone listening to us know that we're done
        this.fireRenderComplete();
        this.hasRendered = true;
    }

    /**
     * Creates the task to dispatch to the workers
     *
     * @param bounds     Bounds of render area
     * @return ImageSegment with result of render
     */
    protected abstract Callable<ImageSegment> createTask(Rectangle2D bounds);

    /**
     * Creates the Open CL Kernel for execution
     *
     * @param dimension    Dimensions of image to render
     * @param results Buffer to put results into
     * @return CLKernel to execute
     */
    protected abstract CLKernel createOpenCLKernel(Dimension dimension, CLBuffer<Integer> results);

    /**
     * Renders the image using OpenCL
     * @throws CLException Throw if there was an error whilst executing
     */
    private void runOpenCL_render(boolean fullRender) throws CLException {
        boolean shouldDisableOpenCL = true;

        if(!fullRender){
            // If we are recolouring, do the recolour
            this.ocl_recolourImage();
            return;
        }
        try {
            CLQueue queue = this.openClThread.getQueue();
            Dimension dimensions = new Dimension((int) this.imgWidth, (int) this.imgHeight);

            // Create results buffer and pointer
            Pointer<Integer> results = Pointer.allocateInts(dimensions.height * dimensions.width);
            CLBuffer<Integer> resultsBuffer = this.openClThread.getContext().createIntBuffer(CLMem.Usage.Output, results, false);

            // Get Render kernel, queue it, and wait for it to finish
            CLKernel kernel = this.createOpenCLKernel(dimensions, resultsBuffer);

            if (kernel == null) {
                this.config.disableOpenCL();
                this.runCPU_render(fullRender);
                return;
            }

            // Queue workers
            kernel.enqueueNDRange(queue, new int[]{dimensions.width, dimensions.height}, new int[]{1, 1});

            // Wait for workers to finish
            queue.finish();

            // Get results
            results = resultsBuffer.read(queue);

            // Paint the colours onto the image
            this.image.setRGB(0, 0, (int) this.imgWidth, (int) this.imgHeight, results.getInts(), 0, (int) this.imgWidth);

            shouldDisableOpenCL = false;

        } catch (CLException.OutOfResources ex) {
            Log.Error("OpenCL render failed with CL_OUT_OF_RESOURCES.");
            ex.printStackTrace();
        } catch (CLException.InvalidCommandQueue ex) {
            Log.Error("OpenCL render failed with CL_INVALID_COMMAND_QUEUE.");
            ex.printStackTrace();
        }

        if (shouldDisableOpenCL) {
            this.config.disableOpenCL();
        }
    }

    /**
     * Runs the render on the CPU
     */
    private void runCPU_render(boolean fullRender) {
        int stripWidth = (int) Math.floor(this.imgWidth / this.numberStrips);
        Rectangle2D bounds;
        int start;

        Callable<ImageSegment> task;

        // Queue up strips to be calculated
        for (int i = 0; i < this.numberStrips; i++) {

            // If first strip, start at 0
            if (i == 0) {
                start = 0;
            } else {
                // Otherwise start at -1
                start = i * stripWidth;
            }
            if (i == this.numberStrips - 1) {
                bounds = new Rectangle2D.Double(start, 0, this.imgWidth - start, this.imgHeight);
            } else {
                bounds = new Rectangle2D.Double(start, 0, stripWidth, this.imgHeight);
            }

            // Dispatch task to execution service
            if (fullRender) {
                task = this.createTask(bounds);
            } else {
                task = new RecolourTask(this, bounds);
            }
            this.executorService.submit(task);
        }

        Future<ImageSegment> result;
        ImageSegment imgSeg;
        Graphics2D g = (Graphics2D) this.image.getGraphics();

        // Get results from strips
        for (int i = 0; i < this.numberStrips; i++) {
            try {
                // Get result from Executor Completion Service (this method is blocking)
                result = this.executorService.take();

                // If result failed, skip it
                if (!result.isDone()) {
                    Log.Warning("Failed to calculate segment.");
                    continue;
                }

                // Try to get result and reconstruct the image
                try {
                    imgSeg = result.get();
                    g.drawImage(imgSeg.getImage(), (int) imgSeg.getBounds().getX(), (int) imgSeg.getBounds().getY(), null);
                } catch (ExecutionException ex) {
                    Log.Warning("Failed to calculate segment.\n" + ex.getMessage());
                }

            } catch (InterruptedException e) {
                break;
            }
        }
        g.dispose();
    }

    private void ocl_recolourImage() {
        try {
            CLQueue queue = this.openClThread.getQueue();
            Dimension dimensions = new Dimension((int) this.imgWidth, (int) this.imgHeight);

            int[] pixelRGBs = new int[dimensions.height * dimensions.width];
            this.image.getRGB(0, 0, dimensions.width, dimensions.height, pixelRGBs, 0, dimensions.width);

            // Create results buffer and pointer
            Pointer<Integer> pixels = Pointer.pointerToInts(pixelRGBs);
            CLBuffer<Integer> pixelsBuffer = this.openClThread.getContext().createIntBuffer(CLMem.Usage.InputOutput, pixels, false);

            ImageColourProperties oldImgColour = this.image.getColourProperties();

            // Create hue to RGB kernel
            CLKernel kernel = this.openClThread.getProgram("recolour").createKernel(
                    "recolour",
                    pixelsBuffer,
                    dimensions.width,
                    oldImgColour.getHue(),
                    oldImgColour.getSaturation(),
                    oldImgColour.getBrightness(),
                    this.config.getHue(),
                    this.config.getSaturation(),
                    this.config.getBrightness()
            );

            if (kernel == null) {
                this.config.disableOpenCL();
                this.runCPU_render(false);
                return;
            }


            // Queue it, and wait for it to finish
            kernel.enqueueNDRange(queue, new int[]{dimensions.width, dimensions.height}, new int[]{1, 1});
            queue.finish();


            kernel.enqueueNDRange(queue, new int[]{dimensions.width, dimensions.height}, new int[]{1, 1});
            queue.finish();

            pixels = pixelsBuffer.read(queue);

            // Paint the colours onto the image
            this.image.setRGB(0, 0, (int) this.imgWidth, (int) this.imgHeight, pixels.getInts(), 0, (int) this.imgWidth);

        } catch (CLException.OutOfResources ex) {
            Log.Error("OpenCL render failed with CL_OUT_OF_RESOURCES.");
            ex.printStackTrace();
        } catch (CLException.InvalidCommandQueue ex) {
            Log.Error("OpenCL render failed with CL_INVALID_COMMAND_QUEUE.");
            ex.printStackTrace();
        }
    }
    //endregion

    //region Image Properties

    /**
     * Updates the properties of the image
     */
    private void updateImageProperties() {
        this.escapeRadiusSquared = this.config.getEscapeRadiusSquared();
        this.iterations = this.config.getIterations();
        this.scaleFactor = this.config.getScaleFactor();
        this.xShift = this.config.getShiftX();
        this.yShift = this.config.getShiftY();
        this.hue = this.config.getHue();
        this.saturation = this.config.getSaturation();
        this.brightness = this.config.getBrightness();

        this.imgHeight = this.image.getHeight();
        this.imgWidth = this.image.getWidth();

        double aspectRatio = this.imgWidth / this.imgHeight;
        double xRange = this.config.getRangeX();
        double yRange = this.config.getRangeY();

        if (aspectRatio * yRange < 4) {
            yRange = xRange / aspectRatio;
        } else {
            xRange = yRange * aspectRatio;
        }

        this.xScale = xRange / this.imgWidth;
        this.yScale = yRange / this.imgHeight;
    }
    //endregion
}