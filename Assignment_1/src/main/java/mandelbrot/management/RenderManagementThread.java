package mandelbrot.management;

import com.nativelibs4java.opencl.*;
import com.nativelibs4java.util.IOUtils;
import mandelbrot.ConfigManager;
import mandelbrot.events.RenderListener;
import mandelbrot.render.TintTask;
import org.bridj.Pointer;
import utils.*;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.concurrent.*;

import static com.nativelibs4java.opencl.JavaCL.createBestContext;
/**
 * Manages and delegates drawing/calculation threads
 *
 * @author Huw Jones
 * @since 27/02/2016
 */

public abstract class RenderManagementThread extends Thread {
    private ArrayList<RenderListener> listeners;

    protected final Object runThread = new Object();

    protected final int numberStrips;
    protected ImagePanel panel;
    private FractalImage image;
    protected boolean hasRendered = false;

    protected final ConfigManager config;

    protected double imgHeight;
    protected double imgWidth;

    protected double xShift;
    protected double yShift;
    protected double scaleFactor;
    protected int iterations;
    protected float tint;

    protected double xScale;
    protected double yScale;

    protected ExecutorCompletionService<ImageSegment> executorService;
    protected int numberThreads;

    protected LinkedHashMap<ImageProperties, FractalImage> renderCache;

    CLContext context;
    CLQueue queue;
    CLProgram oclProgram_mandelbrot;
    CLProgram oclProgram_hueToRGB;
    private boolean openCL_support = false;

    /**
     * Creates a Render Management Thread
     *
     * @param config     Config Manager
     * @param panel      ImagePanel to output render to
     * @param threadName Name of thread
     */
    public RenderManagementThread(ConfigManager config, ImagePanel panel, String threadName) {
        this.config = config;
        this.panel = panel;
        this.setName("Drawing_Management_Thread_" + threadName);

        renderCache = new LinkedHashMap<>();

        listeners = new ArrayList<>();

        numberThreads = Runtime.getRuntime().availableProcessors();
        numberStrips = numberThreads * 2;

        // Get an executor service for the amount of cores we have
        ExecutorService executorService = Executors.newFixedThreadPool(numberThreads);
        this.executorService = new ExecutorCompletionService<>(executorService);

        initOpenCL();
    }

    public void initOpenCL() {
        try {
            InputStream mandelBrotIS = this.getClass().getResourceAsStream("/mandelbrot/mandelbrot.cl");
            InputStream hueToRGBIS = this.getClass().getResourceAsStream("/mandelbrot/hueToRGB.cl");
            String mandelSource = IOUtils.readText(mandelBrotIS);
            String hueSource = IOUtils.readText(hueToRGBIS);
            context = createBestContext();
            queue = context.createDefaultQueue();
            oclProgram_mandelbrot = context.createProgram(mandelSource);
            oclProgram_hueToRGB = context.createProgram(hueSource);
            openCL_support = true;

            CLPlatform plat = context.getPlatform();
            System.out.println("********************************************************************************");
            System.out.println("* OPEN CL SUPPORT                                                              *");
            System.out.println("********************************************************************************");
            System.out.println("* Vendor: " + plat.getBestDevice().getVendor());
            System.out.println("* Name: " + plat.getName());
            System.out.println("* Device: " + plat.getBestDevice().getName());
            System.out.println("* Version: " + plat.getBestDevice().getVersion());
            System.out.println("* Driver Version: " + plat.getBestDevice().getDriverVersion());
            System.out.println("* OpenCL Version: " + plat.getBestDevice().getOpenCLCVersion());
            System.out.println("* Compute Units: " + plat.getBestDevice().getMaxComputeUnits());
            System.out.println("* Samplers: " + plat.getBestDevice().getMaxSamplers());
            System.out.println("* Work Group: " + plat.getBestDevice().getMaxWorkGroupSize());
            System.out.println("* Address Bits: " + plat.getBestDevice().getAddressBits());
            System.out.println("* GPU Memory: " + (plat.getBestDevice().getGlobalMemSize() / 1024 / 1024) + "MB");
            System.out.println("* Profile: " + plat.getBestDevice().getProfile());
            System.out.print("* Extensions: \n");
            Iterator it = new ArrayList<>(Arrays.asList(plat.getExtensions())).iterator();
            while(it.hasNext()){
                System.out.print("*\t- " +it.next() + "\n");
            }
            System.out.println("********************************************************************************");
        } catch (IOException ex) {
            System.err.println("Failed to load 'mandelbrot.cl', OpenCL support unavailable.");
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
     * Gets max iteration number
     *
     * @return int max iteration count
     */
    protected int getIterations() {
        return config.getIterations();
    }

    /**
     * Gets scale factor
     *
     * @return double scale factor
     */
    protected double getScaleFactor() {
        return config.getScaleFactor();
    }

    /**
     * Gets X axis shift
     *
     * @return double X axis shift
     */
    protected double getShiftX() {
        return config.getShiftX();
    }

    /**
     * Gets Y axis shift
     *
     * @return double Y axis shift
     */
    protected double getShiftY() {
        return config.getShiftY();
    }

    /**
     * Gets image tint offset
     *
     * @return float image tint
     */
    protected float getTint() {
        return config.getTint();
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
        x = ((x - imgWidth / 2d) * xScale + xShift) / scaleFactor;
        y = ((y - imgHeight / 2d) * yScale + yShift) / scaleFactor;

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
    public void addDrawListenener(RenderListener listener){
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
     * Creates the task to dispatch to the workers
     *
     * @param properties Properties of image to render
     * @param bounds     Bounds of render area
     * @return ImageSegment with result of render
     */
    protected abstract Callable<ImageSegment> createTask(ImageProperties properties, Rectangle2D bounds);

    /**
     * Private method to do the render
     */
    private void doRender() {
        // Check if cache is valid for settings
        if (!checkCacheValidity()) {
            // Recreate cache of rendered images
            renderCache = new LinkedHashMap<>();
        }

        image = FractalImage.fromBufferedImage(panel.createImage());
        updateImageProperties();

        long startTime = System.nanoTime();
        if (config.useOpenCL()) {
            try {
                runOpenCL_render();
            } catch (CLException.OutOfResources e){
                e.printStackTrace();
            }
        } else {
            runCPU_render();
        }
        long endTime = System.nanoTime();
        System.out.printf("Image rendered in: %.5f\n", ((endTime - startTime) / 1000000000.0));
    }

    private void runOpenCL_render() throws CLException.OutOfResources {
        Pointer<Integer> results = Pointer.allocateInts((int) imgHeight * (int) imgWidth);

        long startTime = System.nanoTime();
        results = buildAndExecuteKernel(iterations, config.getEscapeRadiusSquared(), new Dimension((int) imgWidth, (int) imgHeight),
                (float)xScale, (float)yScale,
                (float)xShift, (float)yShift,
                (float) scaleFactor, results
                );
        long time = System.nanoTime() - startTime;
        System.out.printf("Generation took: %.5f\n", time / 1000000000d);

        /*int black = Color.BLACK.getRGB();
        float[] output = results.getFloats((int) imgHeight * (int) imgWidth);
        int[] colours = new int[output.length];
        for (int i = output.length; i-- != 0;) {
            if (output[i] == Float.POSITIVE_INFINITY || output[i] == Float.NEGATIVE_INFINITY) {
                colours[i] = black;
            } else {
                colours[i] = Color.HSBtoRGB(output[i], 1, 1);
            }
        }*/
        image.setRGB(0, 0, (int) imgWidth, (int) imgHeight, results.getInts(), 0, (int)imgWidth);
        panel.setImage(image, true);
    }

    private Pointer<Integer> buildAndExecuteKernel(int maxIterations, int escapeRadiusSquared, Dimension dimensions,
                                       float xScale, float yScale,
                                       float xShift, float yShift,
                                       float scaleFactor,
                                       Pointer<Integer> results
    ) {
        Pointer<Float> huePointer = Pointer.allocateFloats((int) imgHeight * (int) imgWidth);

        CLBuffer<Float> hueBuffer = context.createFloatBuffer(CLMem.Usage.InputOutput, huePointer, false);
        CLKernel kernel = oclProgram_mandelbrot.createKernel(
                "mandelbrot",
                maxIterations,
                escapeRadiusSquared,
                new float[] {(float)dimensions.width, (float)dimensions.height},
                new float[] {xScale, yScale},
                new float[] {xShift, yShift},
                scaleFactor,
                hueBuffer
        );

        kernel.enqueueNDRange(queue, new int[]{dimensions.width, dimensions.height}, new int[]{1, 1});

        queue.finish();

        CLBuffer<Integer> resultsBuffer = context.createIntBuffer(CLMem.Usage.Output, results, false);

        float hueAdj = config.getTint();
        float huePrev = image.getTint();

        CLKernel hueKernel = oclProgram_hueToRGB.createKernel(
                "hueToRGB",
                hueBuffer,
                (int)imgWidth,
                1.0f,
                1.0f,
                hueAdj,
                huePrev,
                resultsBuffer
        );

        hueKernel.enqueueNDRange(queue, new int[]{dimensions.width, dimensions.height}, new int[]{1, 1});
        queue.finish();

        return resultsBuffer.read(queue);
    }

    /**
     * Runs the render on the CPU
     */
    private void runCPU_render() {
        boolean fullRender = true;

        ImageProperties properties = getImageProperties();

        // Check if an image that matches our settings is in the cache
        if (imageIsCached()) {
            useCachedImage(properties);
            fullRender = false;
        }

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
                task = createTask(properties, bounds);
            } else if (image.getTint() != properties.getTint()) {
                task = new TintTask(this, bounds, properties);
            } else {
                panel.setImage(image, true);
                return;
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
                    System.err.println("Failed to calculate segment. (not done)");
                    continue;
                }

                // Try to get result and reconstruct the image
                try {
                    imgSeg = result.get();
                    g.drawImage(imgSeg.getImage(), (int) imgSeg.getBounds().getX(), (int) imgSeg.getBounds().getY(), null);
                } catch (ExecutionException ex) {
                    ex.printStackTrace(System.err);
                    System.err.println("Failed to calculate segment.\n" + ex.getMessage());
                }

            } catch (InterruptedException e) {
                break;
            }
        }
        g.dispose();

        checkCleanCache();
        image.setTint(properties.getTint());
        cacheImage(properties, image);


        panel.setImage(image, true);

        fireRenderComplete();
        hasRendered = true;
    }
    //endregion

    //region Cache Management

    /**
     * Checks to see if the cache is valid for these settings
     * If not, it returns false
     *
     * @return Boolean, true, if cache is still valid
     */
    private boolean checkCacheValidity() {
        if (image == null) return true;
        return (image.getHeight() == panel.getHeight()) && (image.getWidth() == panel.getWidth());
    }

    /**
     * Checks cache to see if the requested doRender has already been rendered
     *
     * @return Boolean, true if doRender is available
     */
    private boolean imageIsCached() {
        ImageProperties imageProperties = getImageProperties();
        return renderCache.containsKey(imageProperties);
    }

    private boolean cacheImage(ImageProperties properties, FractalImage image){
        if(renderCache.containsValue(image) || renderCache.containsKey(properties)) return false;
        renderCache.put(properties, image);
        return true;
    }

    /**
     * Displays the image that was found in the cache
     *
     * @param properties Properties of image to display
     */
    private void useCachedImage(ImageProperties properties) {
        System.out.println("Using cached image.");
        this.image = renderCache.get(properties);
    }

    /**
     * Checks if the cache needs cleaning and if so, cleans the cache
     */
    private void checkCleanCache() {
        long total = Runtime.getRuntime().totalMemory();
        long free = Runtime.getRuntime().freeMemory();

        if (renderCache.size() == 0) {
            return;
        }
        if (free * 100d / total >= 80) {
            renderCache.remove(renderCache.entrySet().iterator().next().getKey());
        }
    }

    //endregion

    //region Image Properties

    /**
     * Updates the properties of the image
     */
    private void updateImageProperties() {
        iterations = this.getIterations();
        scaleFactor = this.getScaleFactor();
        xShift = this.getShiftX();
        yShift = this.getShiftY();
        tint = this.getTint();

        imgHeight = image.getHeight();
        imgWidth = image.getWidth();

        double aspectRatio = imgWidth / imgHeight;
        double xRange = 4;
        double yRange = 3.2;

        if (aspectRatio * yRange < 4) {
            yRange = 4 / aspectRatio;
        } else {
            xRange = 3.2 * aspectRatio;
        }

        xScale = xRange / imgWidth;
        yScale = yRange / imgHeight;
    }


    public ImageProperties getImageProperties() {
        return new ImageProperties(iterations, scaleFactor, xShift, yShift, tint);
    }

    //endregion

}
