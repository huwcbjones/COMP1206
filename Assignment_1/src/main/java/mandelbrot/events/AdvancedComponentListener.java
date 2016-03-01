package mandelbrot.events;

import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

/**
 * Advanced Window Listener
 *
 * @author Huw Jones
 * @since 01/03/2016
 */
public interface AdvancedComponentListener extends ComponentListener {

    /**
     * Invoked when the component's size starts changing.
     */
    void componentResizeStart(ComponentEvent e);

    /**
     * Invoked when the component's size stops changing.
     */
    void componentResizeEnd(ComponentEvent e);
}