package mandelbrot.events;

import javax.swing.event.ChangeEvent;

/**
 * Adapter for AdvancedChangeListener
 *
 * @author Huw Jones
 * @since 29/02/2016
 */
public class AdvancedChangeAdapter implements AdvancedChangeListener {

    /**
     * Invoked when the target of the listener has started to change its state.
     *
     * @param e a ChangeEvent object
     */
    @Override
    public void changeStarted(ChangeEvent e) {
    }

    /**
     * Invoked when the target of the listener has finished changing its state.
     *
     * @param e a ChangeEvent object
     */
    @Override
    public void changeFinish(ChangeEvent e) {
    }

    /**
     * Invoked when the target of the listener has changed its state.
     *
     * @param e a ChangeEvent object
     */
    @Override
    public void stateChanged(ChangeEvent e) {
    }
}
