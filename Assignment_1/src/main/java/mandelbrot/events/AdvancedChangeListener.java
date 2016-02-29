package mandelbrot.events;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Defines an object which listens for AdvancedChangeEvents.
 *
 * @author Huw Jones
 * @since 29/02/2016
 */
public interface AdvancedChangeListener extends ChangeListener {

    /**
     * Invoked when the target of the listener has started to change its state.
     *
     * @param e a ChangeEvent object
     */
    void changeStarted(ChangeEvent e);

    /**
     * Invoked when the target of the listener has finished changing its state.
     *
     * @param e a ChangeEvent object
     */
    void changeFinish(ChangeEvent e);
}
