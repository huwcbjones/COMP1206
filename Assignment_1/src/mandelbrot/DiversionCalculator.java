package mandelbrot;

import utils.Complex;

import java.awt.*;
import java.awt.geom.Point2D;

/**
 * {DESCRIPTION}
 *
 * @author Huw Jones
 * @since 27/02/2016
 */
public class DiversionCalculator implements Runnable {

    private DrawingThread drawingThread;
    private Point2D point;
    private Complex complex;
    private int iterations;

    public DiversionCalculator(DrawingThread t, Point2D point, Complex complex, int iterations) {
        this.drawingThread = t;
        this.point = point;
        this.complex = complex;
        this.iterations = iterations;
    }

    @Override
    public void run() {
        int currIteration = 0;
        while (complex.modulusSquared() <= 4 && currIteration < iterations) {

            currIteration++;
        }
        if (currIteration < iterations) {
            drawingThread.paintPixel(point, Color.BLACK);
        } else {
            drawingThread.paintPixel(point, Color.WHITE);
        }
    }
}
