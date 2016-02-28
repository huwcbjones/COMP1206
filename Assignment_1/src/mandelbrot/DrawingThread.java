package mandelbrot;

import utils.Complex;
import utils.ImageCache;
import utils.ImagePanel;
import utils.ImageSegment;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.LinkedHashMap;
import java.util.concurrent.*;

/**
 * Manages and delegates drawing/calculation threads
 *
 * @author Huw Jones
 * @since 27/02/2016
 */
class DrawingThread extends Thread {
    private Main mainWindow;
    private ImagePanel panel;
    private BufferedImage image;

    private boolean hasDrawn = false;

    private final Object shouldRedraw = new Object();
    double xShift;
    double yShift;
    double scaleFactor;

    double imgHeight;
    double imgWidth;

    double xScale;
    double yScale;
    int iterations;
    ExecutorCompletionService<ImageSegment> executorService;
    int numberThreads;
    private LinkedHashMap<ImageCache, BufferedImage> images;

    public DrawingThread(Main mainWindow, ImagePanel panel) {
        this.mainWindow = mainWindow;
        this.panel = panel;
        this.setName("Mandelbrot_Drawing_Thread");

        images = new LinkedHashMap<>();

        numberThreads = Runtime.getRuntime().availableProcessors();
        // Get an executor service for the amount of cores we have
        ExecutorService executorService = Executors.newFixedThreadPool(numberThreads);
        this.executorService = new ExecutorCompletionService<>(executorService);
    }

    @Override
    public void run() {
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
            return;
        }


        int numberStrips = numberThreads;
        int stripWidth = (int) Math.floor(imgWidth / (numberStrips));

        Rectangle2D bounds;

        int start;
        // Queue up strips to be calculated
        for (int i = 0; i < numberStrips; i++) {
            if (i == 0) {
                start = 0;
            } else {
                start = i * stripWidth - 1;
            }

            bounds = new Rectangle2D.Double(start + 1, 0, stripWidth + 2, imgHeight);
            executorService.submit(new DiversionCalculator(this, bounds, iterations));
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

        checkCacheSize();
        images.put(getCacheKey(), image);
        panel.setImage(image);
        panel.repaint();

        hasDrawn = true;

        // Let objects waiting on us know we're done
        synchronized (this) {
            notify();
        }
    }

    private boolean checkCacheValidity() {
        if (image == null) return true;
        return (image.getHeight() == panel.getHeight()) && (image.getWidth() == panel.getWidth());
    }

    private void calculateVariables() {
        image = panel.createImage();

        iterations = mainWindow.getIterations();
        scaleFactor = mainWindow.getScaleFactor();
        xShift = mainWindow.getTranslateX();
        yShift = mainWindow.getTranslateY();

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

    private boolean checkCache() {
        ImageCache cache = getCacheKey();
        if (!images.containsKey(cache)) return false;
        panel.setImage(images.get(cache));
        panel.repaint();

        return true;
    }

    private ImageCache getCacheKey() {
        return new ImageCache(iterations, scaleFactor, xShift, yShift);
    }

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

        if(free * 100d/ total >= 80){
            images.remove(images.entrySet().iterator().next().getKey());
        }
    }

    public Complex getComplexFromPoint(Point2D p) {
        return getComplexFromPoint(p.getX(), p.getY());
    }

    public Complex getComplexFromPoint(double x, double y){
        x = ((x- imgWidth / 2d) * xScale + xShift) / scaleFactor;
        y = ((y - imgHeight / 2d) * yScale + yShift) / scaleFactor;

        return new Complex(x, y);
    }

    public boolean hasDrawn(){
        return hasDrawn;
    }
}
