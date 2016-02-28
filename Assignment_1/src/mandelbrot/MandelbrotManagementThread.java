package mandelbrot;

import utils.ImagePanel;
import utils.ImageProperties;
import utils.ImageSegment;

import java.awt.geom.Rectangle2D;
import java.util.concurrent.Callable;

/**
 * {DESCRIPTION}
 *
 * @author Huw Jones
 * @since 28/02/2016
 */
public class MandelbrotManagementThread extends DrawingManagementThread {
    public MandelbrotManagementThread(Main mainWindow, ImagePanel panel) {
        super(mainWindow, panel, "Mandelbrot");
    }

    @Override
    protected Callable<ImageSegment> createTask(ImageProperties properties, Rectangle2D bounds) {
        return new DiversionCalculator(this, bounds, properties.getIterations());
    }
}
