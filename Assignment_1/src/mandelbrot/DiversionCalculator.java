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
    private int maxIterations;

    public DiversionCalculator(DrawingThread t, Point2D point, Complex complex, int maxIterations) {
        this.drawingThread = t;
        this.point = point;
        this.complex = complex;
        this.maxIterations = maxIterations;
    }

    @Override
    public void run() {
        int currIteration = 0;
        Complex z = new Complex(complex.getReal(), complex.getImaginary());
        Complex c = complex;
        while (z.modulusSquared() <= 4 && currIteration < maxIterations) {
            z = z.square();
            z.add(c);
            currIteration++;
        }
        if (currIteration < maxIterations) {
            drawingThread.paintPixel(point, Color.WHITE);
        } else {
            drawingThread.paintPixel(point, Color.BLACK);
        }
    }
}
