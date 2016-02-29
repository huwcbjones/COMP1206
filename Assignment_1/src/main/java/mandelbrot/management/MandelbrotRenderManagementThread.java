package mandelbrot.management;

import mandelbrot.ConfigManager;
import mandelbrot.Main;
import mandelbrot.render.MandelbrotTask;
import utils.ImagePanel;
import utils.ImageProperties;
import utils.ImageSegment;

import java.awt.geom.Rectangle2D;
import java.util.concurrent.Callable;

/**
 * Management Thread for Mandelbrot Set
 *
 * @author Huw Jones
 * @since 28/02/2016
 */
public class MandelbrotRenderManagementThread extends RenderManagementThread {
    public MandelbrotRenderManagementThread(ConfigManager config, ImagePanel panel) {
        super(config, panel, "Mandelbrot");
    }

    @Override
    protected Callable<ImageSegment> createTask(ImageProperties properties, Rectangle2D bounds) {
        return new MandelbrotTask(this, bounds, properties);
    }
}
