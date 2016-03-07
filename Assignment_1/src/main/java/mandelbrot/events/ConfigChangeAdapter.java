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
    public void fractalChange(String fractal){
    }

    @Override
    public void escapeRadiusSquaredChange(double escapeRadiusSquared){
    }

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
    public void colourChange(float shift, float saturation, float brightness) {
    }

    @Override
    public void selectedPointChange(Complex complex) {
    }

}
