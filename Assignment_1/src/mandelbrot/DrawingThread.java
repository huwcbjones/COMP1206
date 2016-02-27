package mandelbrot;

import utils.Complex;
import utils.ImagePanel;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * {DESCRIPTION}
 *
 * @author Huw Jones
 * @since 27/02/2016
 */
class DrawingThread extends Thread {
    private Main mainWindow;
    private ImagePanel panel;
    private BufferedImage image;

    public DrawingThread(Main mainWindow, ImagePanel panel) {
        this.mainWindow = mainWindow;
        this.panel = panel;
        this.setName("Mandelbrot_Drawing_Thread");
    }

    @Override
    public void run() {
        image = panel.createImage();

        // Get an executor service for the amount of cores we have
        ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        double x_factor = mainWindow.getRangeX() / image.getWidth();
        double y_factor = mainWindow.getRangeY() / image.getWidth();
        int iterations = mainWindow.getIterations();

        double adjX, adjY;
        Point2D point;
        Complex complex;

        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                point = new Point2D.Double(x, y);
                adjX = (x - (image.getWidth() / 2)) * x_factor;
                adjY = (y - (image.getHeight() / 2)) * y_factor;

                complex = new Complex(adjX, adjY);
                executorService.execute(new DiversionCalculator(this, point, complex, iterations));
            }
        }

        executorService.shutdown();
        while(!executorService.isTerminated()){

        }
        panel.repaint();
    }

    public void paintPixel(Point2D p, Color c) {
        int x = Double.valueOf(p.getX()).intValue();
        int y = Double.valueOf(p.getY()).intValue();

        // Need to syncronise as we are using multithreading to paint
        synchronized (image) {
            image.setRGB(x, y, c.getRGB());
        }
    }
}
