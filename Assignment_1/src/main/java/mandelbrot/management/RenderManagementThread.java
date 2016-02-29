package mandelbrot.management;

import mandelbrot.Main;
import mandelbrot.events.RenderListener;
import mandelbrot.render.TintTask;
import utils.Complex;
import utils.ImagePanel;
import utils.ImageProperties;
import utils.ImageSegment;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
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
    protected boolean shouldRender = false;
    protected boolean shouldTint = false;

    protected final int numberStrips;
    protected Main mainWindow;
    protected ImagePanel panel;
    private BufferedImage image;
    private BufferedImage original;
    protected boolean hasRendered = false;

    protected double xShift;
    protected double yShift;
    protected double scaleFactor;
    protected int iterations;
    protected float tint;

    protected double imgHeight;
    protected double imgWidth;

    protected double xScale;
    protected double yScale;

    protected ExecutorCompletionService<ImageSegment> executorService;
    protected int numberThreads;
    protected LinkedHashMap<ImageProperties, BufferedImage> renderCache;

    private boolean isDrawing;
    private boolean shouldStop;

    /**
     * Creates a Render Management Thread
     *
     * @param mainWindow Main Window thread is attached to
     * @param panel      ImagePanel to output render to
     * @param threadName Name of thread
     */
    public RenderManagementThread(Main mainWindow, ImagePanel panel, String threadName) {
        this.mainWindow = mainWindow;
        this.panel = panel;
        this.setName("Drawing_Management_Thread_" + threadName);

        renderCache = new LinkedHashMap<>();

        listeners = new ArrayList<>();

        numberThreads = Runtime.getRuntime().availableProcessors();
        numberStrips = numberThreads * 2;

        // Get an executor service for the amount of cores we have
        ExecutorService executorService = Executors.newFixedThreadPool(numberThreads);
        this.executorService = new ExecutorCompletionService<>(executorService);
    }

    //region Get Methods

    /**
     * Returns the original un-tinted image
     *
     * @return BufferedImage, render result
     */
    public BufferedImage getOriginalImage() {
        return original;
    }

    /**
     * Returns the tinted image
     *
     * @return BufferedImage, tinted render result
     */
    public BufferedImage getTintedImage() {
        return image;
    }

    /**
     * Gets max iteration number
     *
     * @return int max iteration count
     */
    protected int getIterations() {
        return mainWindow.getIterations();
    }

    /**
     * Gets scale factor
     *
     * @return double scale factor
     */
    protected double getScaleFactor() {
        return mainWindow.getScaleFactor();
    }

    /**
     * Gets X axis shift
     *
     * @return double X axis shift
     */
    protected double getxShift() {
        return mainWindow.getShiftX();
    }

    /**
     * Gets Y axis shift
     *
     * @return double Y axis shift
     */
    protected double getyShift() {
        return mainWindow.getShiftY();
    }

    /**
     * Gets image tint offset
     *
     * @return float image tint
     */
    protected float getTint() {
        return mainWindow.getTint();
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

    private void drawComplete() {
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
                    if (shouldRender) {
                        doRender();
                    } else if (shouldTint) {
                        doTint();
                    }
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
            this.shouldRender = true;
            this.shouldTint = false;
            runThread.notify();
        }
    }

    /**
     * Public method to notify the thread to call doTint without blocking
     */
    public void tint() {
        synchronized (this.runThread) {
            this.shouldRender = false;
            this.shouldTint = true;
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

        updateImageProperties();
        ImageProperties properties = getImageProperties();

        // Check if an image that matches our settings is in the cache
        if (imageIsCached()) {
            displayCachedImage(properties);
            return;
        }

        int stripWidth = (int) Math.floor(imgWidth / (numberStrips));
        Rectangle2D bounds;
        int start;

        long startTime = System.nanoTime();

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
                bounds = new Rectangle2D.Double(start, 0, stripWidth , imgHeight);
            }
            executorService.submit(createTask(properties, bounds));
        }

        Future<ImageSegment> result;
        ImageSegment imgSeg;
        Graphics2D g = (Graphics2D) image.getGraphics();

        // Get results from strips
        for (int i = 0; i < numberStrips; i++) {
            try {
                result = executorService.take();
                if (!result.isDone()) {
                    System.err.println("Failed to calculate segment. (not done)");
                    continue;
                }
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
        long endTime = System.nanoTime();

        System.out.printf("Image rendered in: %.5f\n", ((endTime - startTime) / 1000000000.0));
        checkCleanCache();
        renderCache.put(properties, image);

        original = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
        g = original.createGraphics();
        g.drawImage(image, 0, 0, null);
        g.dispose();

        doTint();
    }

    /**
     * Private method to tint the render result
     */
    private void doTint() {
        if (!checkCacheValidity()) {
            renderCache = new LinkedHashMap<>();
        }

        updateImageProperties();
        ImageProperties properties = getImageProperties();

        if (imageIsCached()) {
            displayCachedImage(properties);
            return;
        }


        int stripWidth = (int) Math.floor(imgWidth / (numberStrips));

        Rectangle2D bounds;

        int start;

        long startTime = System.nanoTime();

        // Queue up strips to be calculated
        for (int i = 0; i < numberStrips; i++) {
            if (i == 0) {
                start = 0;
            } else {
                start = i * stripWidth - 1;
            }
            if (i == numberStrips - 1) {
                bounds = new Rectangle2D.Double(start, 0, imgWidth - start, imgHeight);
            } else {
                bounds = new Rectangle2D.Double(start, 0, stripWidth, imgHeight);
            }
            executorService.submit(new TintTask(this, bounds, properties));
        }

        Future<ImageSegment> result;
        ImageSegment imgSeg;
        Graphics2D g = (Graphics2D) image.getGraphics();

        // Get results from strips
        for (int i = 0; i < numberStrips; i++) {
            try {
                result = executorService.take();
                if (!result.isDone()) {
                    System.err.println("Failed to calculate segment. (not done)");
                    continue;
                }
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
        long endTime = System.nanoTime();

        System.out.printf("Image rendered in: %.5f\n", ((endTime - startTime) / 1000000000.0));
        checkCleanCache();
        renderCache.put(properties, image);
        panel.setImage(image);
        panel.repaint();

        hasRendered = true;
        drawComplete();
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

    /**
     * Displays the image that was found in the cache
     *
     * @param properties Properties of image to display
     */
    private void displayCachedImage(ImageProperties properties) {
        System.out.println("Displaying cached image.");

        panel.setImage(renderCache.get(properties), true);
        hasRendered = true;
        drawComplete();
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
        image = panel.createImage();
        original = panel.createImage();

        iterations = this.getIterations();
        scaleFactor = this.getScaleFactor();
        xShift = this.getxShift();
        yShift = this.getyShift();
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
