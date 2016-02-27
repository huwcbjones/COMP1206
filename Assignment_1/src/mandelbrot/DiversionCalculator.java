package mandelbrot;

import utils.Complex;

import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

/**
 * {DESCRIPTION}
 *
 * @author Huw Jones
 * @since 27/02/2016
 */
public class DiversionCalculator implements Runnable {

    private DrawingThread drawingThread;
    private Line2D bounds;
    private int maxIterations;

    public DiversionCalculator(DrawingThread t, Line2D bounds, int maxIterations) {
        this.drawingThread = t;
        this.bounds = bounds;
        this.maxIterations = maxIterations;
    }

    @Override
    public void run() {


        for(int y = (int)bounds.getY1(); y < bounds.getY2(); y++) {
            for(int x = (int)bounds.getX1(); x < bounds.getX2(); x++) {
                Point2D p = new Point2D.Float(x, y);
                Complex c = drawingThread.getComplexFromPoint(p);
                int currIteration = 0;
                Complex z = new Complex(c.getReal(), c.getImaginary());

                while (z.modulusSquared() <= 4 && currIteration < maxIterations) {
                    z = z.square();
                    z.add(c);
                    currIteration++;
                }
                if (currIteration < maxIterations) {
                    drawingThread.paintPixel(p, Color.WHITE);
                } else {
                    drawingThread.paintPixel(p, Color.BLACK);
                }
            }
        }
    }
}
