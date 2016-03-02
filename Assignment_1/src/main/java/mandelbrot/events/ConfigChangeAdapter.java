package mandelbrot.events;

import utils.Complex;
import utils.ImageProperties;

/**
 * Adapter for ConfigChange
 *
 * @author Huw Jones
 * @since 28/02/2016
 */
public class ConfigChangeAdapter implements ConfigChangeListener {

    @Override
    public void iterationChange(int iterations) {
    }

    @Override
    public void xShiftChange(double xShift) {
    }

    @Override
    public void yShiftChange(double yShift) {
    }

    @Override
    public void scaleChange(double scale) {
    }

    @Override
    public void colourShiftChange(double shift) {
    }

    @Override
    public void configChange(ImageProperties properties) {
    }

    @Override
    public void selectedPointChange(Complex complex) {
    }
}
