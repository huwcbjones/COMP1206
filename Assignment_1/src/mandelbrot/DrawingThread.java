package mandelbrot;

import utils.Complex;
import utils.ImagePanel;

import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

    double xShift;
    double yShift;
    double scaleFactor;

    double imgHeight;
    double imgWidth;

    double xScale;
    double yScale;

    int numberThreads;

    public DrawingThread(Main mainWindow, ImagePanel panel) {
        this.mainWindow = mainWindow;
        this.panel = panel;
        this.setName("Mandelbrot_Drawing_Thread");

        numberThreads = Runtime.getRuntime().availableProcessors();
    }

    @Override
    public void run() {
        image = panel.createImage();

        // Get an executor service for the amount of cores we have
        ExecutorService executorService = Executors.newFixedThreadPool(numberThreads);

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

        int iterations = mainWindow.getIterations();

        Line2D bounds;
        int stripWidth = (int) Math.floor(imgWidth / (numberThreads * 2d));
        for (int i = 0; i < numberThreads * 2; i++) {
            int xStart = (i == 0) ? 0 : i * stripWidth;
            int xEnd = (i == (numberThreads * 2) - 1) ? (int)imgWidth : (i + 1) * stripWidth;

            bounds = new Line2D.Double(xStart, 0, xEnd, imgHeight);
            executorService.execute(new DiversionCalculator(this, bounds, iterations));
        }

        executorService.shutdown();
        while(!executorService.isTerminated()){

        }

        panel.setImage(image);
        panel.repaint();

        hasDrawn = true;

        // Let objects waiting on us know we're done
        synchronized (this){
            notify();
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
    public void paintPixel(Point2D p, Color c) {
        int x = Double.valueOf(p.getX()).intValue();
        int y = Double.valueOf(p.getY()).intValue();

        // Need to syncronise as we are using multithreading to paint
        synchronized (image) {
            image.setRGB(x, y, c.getRGB());
        }
    }

    public boolean hasDrawn(){
        return hasDrawn;
    }
}
