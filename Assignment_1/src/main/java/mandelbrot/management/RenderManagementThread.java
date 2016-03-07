package mandelbrot.management;

import com.nativelibs4java.opencl.*;
import mandelbrot.CacheManager;
import mandelbrot.ConfigManager;
import mandelbrot.Main;
import mandelbrot.events.AdvancedComponentAdapter;
import mandelbrot.events.RenderListener;
import mandelbrot.render.RecolourTask;
import org.bridj.Pointer;
import utils.*;

import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.LinkedHashMap;
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
        ocl_loadPrograms();

        cacheManager = new CacheManager();

        // Initialise Event Handling
        listeners = new ArrayList<>();

        // Initialise CPU Multithreaded Rendering
        numberThreads = Runtime.getRuntime().availableProcessors();
        numberStrips = numberThreads * 2;

        Log.Information("Multicore Processing: Using " + numberThreads + ", and " + numberStrips + " workers.");

        // Get an executor service for the amount of cores we have
        ExecutorService executorService = Executors.newFixedThreadPool(numberThreads);
        this.executorService = new ExecutorCompletionService<>(executorService);
        Log.Information("Starting execution pool...");
    }

    /**
     * Loads programs into the OpenCL context
     */
    protected void ocl_loadPrograms() {
        if (!openClThread.loadProgram("recolour", this.getClass().getResourceAsStream("/mandelbrot/opencl/recolour.cl"))) {
            config.disableOpenCL();
        }
    }

    //region Get Methods

    /**
     * Returns the original un-tinted image
     *
     * @return BufferedImage, render result
     */
    public FractalImage getImage() {
        return image;
    }

    /**
     * Gets escape radius squared
     *
     * @return double escape radius squared
     */
    public final double getEscapeRadiusSquared() {
        return escapeRadiusSquared;
    }

    /**
     * Gets max iteration number
     *
     * @return int max iteration count
     */
    public final int getIterations() {
        return iterations;
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
        return image.getColourProperties().getHue();
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
        return getComplexFromPoint(p.getX(), p.getY());
    }

    /**
     * Gets the complex represented by point, (x, y).
     *
     * @param x X co-ordinate
     * @param y Y co-ordinate
     * @return Complex number
     */
    public final Complex getComplexFromPoint(double x, double y) {
        x = ((x - imgWidth / 2d) * xScale) / scaleFactor + xShift;
        y = ((y - imgHeight / 2d) * yScale) / scaleFactor + yShift;

        return new Complex(x, y);
    }

    /**
     * Returns whether an image has rendered
     *
     * @return true if an image has been rendered
     */
    public final boolean hasRendered() {
        return hasRendered;
    }

    //endregion

    //region Event Listening & Handling
    public void addRenderListener(RenderListener listener) {
        listeners.add(listener);
    }

    private void fireRenderComplete() {
        listeners.forEach(RenderListener::renderComplete);
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
                    runThread.wait();
                    doRender();
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
            runThread.notify();
        }
    }
    //endregion

    //region Render Processing

    /**
     * Gets properties for this render
     * @return ImageProperties
     */
    protected ImageProperties getRenderProperties(){
        return new ImageProperties((int) imgWidth, (int) imgHeight, getIterations(), getScale(), getShiftX(), getShiftY());
    }

    /**
     * Private method to do the render
     */
    private void doRender() {
        // Create image
        image = FractalImage.fromBufferedImage(panel.createImage());

        // Update image properties for this render
        updateImageProperties();

        // Check if image is cached
        boolean fullRender = true;

        // Create image properties
        ImageProperties properties = this.getRenderProperties();
        ImageColourProperties colourProperties = new ImageColourProperties(getHue(), getSaturation(), getBrightness());

        // If cache is enabled, do cache stuff
        if (!config.isCacheDisabled()) {

            // If image is cached with both properties and colours, retrieve it and display it.
            if (cacheManager.isCached(properties, colourProperties)) {
                Log.Information("Displaying cached image. " + properties.toString() + "/" + colourProperties.toString());
                image = cacheManager.getImage(properties, colourProperties);
                panel.setImage(image, true);
                fireRenderComplete();
                hasRendered = true;
                return;
            }

            // If image with same properties (dimensions, iterations, etc) is cached, use this
            // as recolouring is quicker than rendering
            if (cacheManager.isCached(properties)) {
                Log.Information("Recolouring cached image. " + properties.toString());
                image = cacheManager.getImage(properties);
                Log.Information(" - from: " + image.getColourProperties().toString());
                Log.Information(" - to  : " + colourProperties.toString());
                fullRender = false;
            }
        }

        if (config.useOpenCL()) {
            try {
                runOpenCL_render(fullRender);
            } catch (CLException e){

                // Fallback to CPU if OpenCL failed (for whatever reason)
                config.disableOpenCL();
                e.printStackTrace();
            }
        } else {
            runCPU_render(fullRender);
        }

        // Update image properties
        image.setProperties(properties);
        image.setColourProperties(colourProperties);

        // Cache the image if cache is enabled
        if(!config.isCacheDisabled()) {
            cacheManager.cacheImage(image);
        }

        panel.setImage(image, true);

        // Let everyone listening to us know that we're done
        fireRenderComplete();
        hasRendered = true;
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
            ocl_recolourImage();
            return;
        }
        try {
            CLQueue queue = openClThread.getQueue();
            Dimension dimensions = new Dimension((int) imgWidth, (int) imgHeight);

            // Create results buffer and pointer
            Pointer<Integer> results = Pointer.allocateInts(dimensions.height * dimensions.width);
            CLBuffer<Integer> resultsBuffer = openClThread.getContext().createIntBuffer(CLMem.Usage.Output, results, false);

            // Get Render kernel, queue it, and wait for it to finish
            CLKernel kernel = createOpenCLKernel(dimensions, resultsBuffer);

            if (kernel == null) {
                config.disableOpenCL();
                runCPU_render(fullRender);
                return;
            }

            // Queue workers
            kernel.enqueueNDRange(queue, new int[]{dimensions.width, dimensions.height}, new int[]{1, 1});

            // Wait for workers to finish
            queue.finish();

            // Get results
            results = resultsBuffer.read(queue);

            // Paint the colours onto the image
            image.setRGB(0, 0, (int) imgWidth, (int) imgHeight, results.getInts(), 0, (int) imgWidth);

            shouldDisableOpenCL = false;

        } catch (CLException.OutOfResources ex) {
            Log.Error("OpenCL render failed with CL_OUT_OF_RESOURCES.");
            ex.printStackTrace();
        } catch (CLException.InvalidCommandQueue ex) {
            Log.Error("OpenCL render failed with CL_INVALID_COMMAND_QUEUE.");
            ex.printStackTrace();
        }

        if (shouldDisableOpenCL) {
            config.disableOpenCL();
        }
    }

    /**
     * Runs the render on the CPU
     */
    private void runCPU_render(boolean fullRender) {
        int stripWidth = (int) Math.floor(imgWidth / (numberStrips));
        Rectangle2D bounds;
        int start;

        Callable<ImageSegment> task;

        // Queue up strips to be calculated
        for (int i = 0; i < numberStrips; i++) {

            // If first strip, start at 0
            if (i == 0) {
                start = 0;
            } else {
                // Otherwise start at -1
                start = i * stripWidth;
            }
            if (i == numberStrips - 1) {
                bounds = new Rectangle2D.Double(start, 0, imgWidth - start, imgHeight);
            } else {
                bounds = new Rectangle2D.Double(start, 0, stripWidth, imgHeight);
            }

            // Dispatch task to execution service
            if (fullRender) {
                task = createTask(bounds);
            } else {
                task = new RecolourTask(this, bounds);
            }
            executorService.submit(task);
        }

        Future<ImageSegment> result;
        ImageSegment imgSeg;
        Graphics2D g = (Graphics2D) image.getGraphics();

        // Get results from strips
        for (int i = 0; i < numberStrips; i++) {
            try {
                // Get result from Executor Completion Service (this method is blocking)
                result = executorService.take();

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
            CLQueue queue = openClThread.getQueue();
            Dimension dimensions = new Dimension((int) imgWidth, (int) imgHeight);

            int[] pixelRGBs = new int[dimensions.height * dimensions.width];
            image.getRGB(0, 0, dimensions.width, dimensions.height, pixelRGBs, 0, dimensions.width);

            // Create results buffer and pointer
            Pointer<Integer> pixels = Pointer.pointerToInts(pixelRGBs);
            CLBuffer<Integer> pixelsBuffer = openClThread.getContext().createIntBuffer(CLMem.Usage.InputOutput, pixels, false);

            ImageColourProperties oldImgColour = image.getColourProperties();

            // Create hue to RGB kernel
            CLKernel kernel = openClThread.getProgram("recolour").createKernel(
                    "recolour",
                    pixelsBuffer,
                    dimensions.width,
                    oldImgColour.getHue(),
                    oldImgColour.getSaturation(),
                    oldImgColour.getBrightness(),
                    config.getHue(),
                    config.getSaturation(),
                    config.getBrightness()
            );

            if (kernel == null) {
                config.disableOpenCL();
                runCPU_render(false);
                return;
            }


            // Queue it, and wait for it to finish
            kernel.enqueueNDRange(queue, new int[]{dimensions.width, dimensions.height}, new int[]{1, 1});
            queue.finish();


            kernel.enqueueNDRange(queue, new int[]{dimensions.width, dimensions.height}, new int[]{1, 1});
            queue.finish();

            pixels = pixelsBuffer.read(queue);

            // Paint the colours onto the image
            image.setRGB(0, 0, (int) imgWidth, (int) imgHeight, pixels.getInts(), 0, (int) imgWidth);

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
        escapeRadiusSquared = config.getEscapeRadiusSquared();
        iterations = config.getIterations();
        scaleFactor = config.getScaleFactor();
        xShift = config.getShiftX();
        yShift = config.getShiftY();
        hue = config.getHue();
        saturation = config.getSaturation();
        brightness = config.getBrightness();

        imgHeight = image.getHeight();
        imgWidth = image.getWidth();

        double aspectRatio = imgWidth / imgHeight;
        double xRange = 4.0;
        double yRange = 4.0;

        if (aspectRatio * yRange < 4) {
            yRange = xRange / aspectRatio;
        } else {
            xRange = yRange * aspectRatio;
        }

        xScale = xRange / imgWidth;
        yScale = yRange / imgHeight;
    }
    //endregion
}