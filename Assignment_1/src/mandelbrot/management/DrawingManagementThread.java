package mandelbrot.management;

import mandelbrot.Main;
import mandelbrot.events.DrawListener;
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

public abstract class DrawingManagementThread extends Thread {
    private ArrayList<DrawListener> listeners;
    protected final Object shouldRedraw = new Object();
    protected Main mainWindow;
    protected ImagePanel panel;
    protected BufferedImage image;
    protected boolean hasDrawn = false;
    protected double xShift;
    protected double yShift;
    protected double scaleFactor;

    protected double imgHeight;
    protected double imgWidth;

    protected double xScale;
    protected double yScale;
    protected int iterations;
    protected ExecutorCompletionService<ImageSegment> executorService;
    protected int numberThreads;
    protected LinkedHashMap<ImageProperties, BufferedImage> images;

    public DrawingManagementThread(Main mainWindow, ImagePanel panel, String threadName) {
        this.mainWindow = mainWindow;
        this.panel = panel;
        this.setName("Drawing_Management_Thread_" + threadName);

        images = new LinkedHashMap<>();

        listeners = new ArrayList<>();

        numberThreads = Runtime.getRuntime().availableProcessors();

        // Get an executor service for the amount of cores we have
        ExecutorService executorService = Executors.newFixedThreadPool(numberThreads);
        this.executorService = new ExecutorCompletionService<>(executorService);
    }

    public void addDrawListenener(DrawListener listener){
        listeners.add(listener);
    }

    private void drawComplete(){
        listeners.forEach(DrawListener::drawComplete);
    }

    @Override
    public final void run() {
        while (!this.isInterrupted()) {
            synchronized (this.shouldRedraw) {
                try {
                    shouldRedraw.wait();
                    redraw();
                } catch (InterruptedException e) {
                    break;
                }
            }
        }
    }

    /**
     * Private method to do the redraw
     */
    private void redraw() {
        if (!checkCacheValidity()) {
            images = new LinkedHashMap<>();
        }
        calculateVariables();

        if (checkCache()) {
            System.out.println("Image found in cache.");
            drawComplete();
            return;
        }

        ImageProperties properties = getImageProperties();

        int numberStrips = numberThreads * 2;
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
        checkCacheSize();
        images.put(properties, image);
        panel.setImage(image);
        panel.repaint();

        hasDrawn = true;

        drawComplete();
    }

    private void calculateVariables() {
        image = panel.createImage();

        iterations = this.getIterations();
        scaleFactor = this.getScaleFactor();
        xShift = this.getxShift();
        yShift = this.getyShift();

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

    private boolean checkCacheValidity() {
        if (image == null) return true;
        return (image.getHeight() == panel.getHeight()) && (image.getWidth() == panel.getWidth());
    }

    private boolean checkCache() {
        ImageProperties cache = getImageProperties();
        if (!images.containsKey(cache)) return false;
        panel.setImage(images.get(cache));
        panel.repaint();

        return true;
    }

    protected ImageProperties getImageProperties() {
        return new ImageProperties(iterations, scaleFactor, xShift, yShift);
    }

    protected abstract Callable<ImageSegment> createTask(ImageProperties properties, Rectangle2D bounds);

    /**
     * Public method to notify the thread to call redraw
     */
    public void draw() {
        synchronized (this.shouldRedraw) {
            shouldRedraw.notify();
        }
    }

    private void checkCacheSize() {
        long total = Runtime.getRuntime().totalMemory();
        long free = Runtime.getRuntime().freeMemory();

        if (images.size() == 0) {
            return;
        }
        if(free * 100d/ total >= 80){
            images.remove(images.entrySet().iterator().next().getKey());
        }
    }

    protected int getIterations() {
        return mainWindow.getIterations();
    }

    protected double getScaleFactor() {
        return mainWindow.getScaleFactor();
    }

    protected double getxShift() {
        return mainWindow.getTranslateX();
    }

    protected double getyShift() {
        return mainWindow.getTranslateY();
    }

    public final Complex getComplexFromPoint(Point2D p) {
        return getComplexFromPoint(p.getX(), p.getY());
    }

    public final Complex getComplexFromPoint(double x, double y) {
        x = ((x- imgWidth / 2d) * xScale + xShift) / scaleFactor;
        y = ((y - imgHeight / 2d) * yScale + yShift) / scaleFactor;

        return new Complex(x, y);
    }

    public final boolean hasDrawn() {
        return hasDrawn;
    }

}
