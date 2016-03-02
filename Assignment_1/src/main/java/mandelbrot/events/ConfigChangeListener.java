package mandelbrot.events;

import utils.Complex;
import utils.ImageProperties;

/**
 * Event Handler for Config Change Events
 *
 * @author Huw Jones
 * @since 28/02/2016
 */
public interface ConfigChangeListener {

    void iterationChange(int iterations);

    void xShiftChange(double xShift);

    void yShiftChange(double yShift);

    void scaleChange(double scale);

    void colourChange(float hue, float saturation, float brightness);

    void configChange(ImageProperties properties);

    void selectedPointChange(Complex complex);
}
