package mandelbrot.events;

import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

/**
 * Adapter for AdvancedComponentListener
 *
 * @author Huw Jones
 * @since 01/03/2016
 */
public class AdvancedComponentAdapter extends ComponentAdapter implements AdvancedComponentListener {
    /**
     * Invoked when the component's size starts changing.
     *
     * @param e
     */
    @Override
    public void componentResizeStart(ComponentEvent e) {
    }

    /**
     * Invoked when the component's size stops changing.
     *
     * @param e
     */
    @Override
    public void componentResizeEnd(ComponentEvent e) {
    }

}
