package utils;

import mandelbrot.events.AdvancedChangeListener;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.EventListenerList;

/**
 * Advanced JSpinner
 *
 * @author Huw Jones
 * @since 29/02/2016
 */
public class JSliderAdvanced extends JSlider {

    private Timer sliderTimer;
    private ChangeEvent lastChangeEvent;

    /**
     * Creates a horizontal slider with the range 0 to 100 and
     * an initial value of 50.
     */
    public JSliderAdvanced() {
        this(0, 100, 50);
    }

    /**
     * Creates a horizontal slider using the specified min, max and value.
     * <p>
     * The <code>BoundedRangeModel</code> that holds the slider's data
     * handles any issues that may arise from improperly setting the
     * minimum, initial, and maximum values on the slider.  See the
     * {@code BoundedRangeModel} documentation for details.
     *
     * @param min   the minimum value of the slider
     * @param max   the maximum value of the slider
     * @param value the initial value of the slider
     * @see BoundedRangeModel
     * @see #setMinimum
     * @see #setMaximum
     * @see #setValue
     */
    public JSliderAdvanced(int min, int max, int value) {
        super(min, max, value);

        this.addChangeListener(e -> {
            if(!sliderTimer.isRunning()) {
                fireStateChangedStart(e);
            }
            sliderTimer.restart();
            lastChangeEvent = e;
        });

        sliderTimer = new Timer(250, e -> {
            fireStateChangedFinish(lastChangeEvent);
            lastChangeEvent = null;
        });
    }

    /**
     * Send a {@code ChangeEvent}, whose source is this {@code JSlider}, to
     * all {@code ChangeListener}s that have registered interest in
     * {@code AdvancedChangeEvent}s.
     * This method is called each time a {@code ChangeEvent} is received from
     * the model timer.
     * <p>
     * The event instance is created if necessary, and stored in
     * {@code changeEvent}.
     *
     * @see #addChangeListener
     * @see EventListenerList
     */
    protected void fireStateChangedStart(ChangeEvent changeEvent) {
        Object[] listeners = listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i]==AdvancedChangeListener.class) {
                ((AdvancedChangeListener)listeners[i+1]).changeStarted(changeEvent);
            }
        }
    }

    /**
     * Send a {@code ChangeEvent}, whose source is this {@code JSlider}, to
     * all {@code ChangeListener}s that have registered interest in
     * {@code AdvancedChangeEvent}s.
     * This method is called each time a {@code ChangeEvent} is received from
     * the model timer.
     * <p>
     * The event instance is created if necessary, and stored in
     * {@code changeEvent}.
     *
     * @see #addChangeListener
     * @see EventListenerList
     */
    protected void fireStateChangedFinish(ChangeEvent changeEvent) {
        Object[] listeners = listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i]==AdvancedChangeListener.class) {
                ((AdvancedChangeListener)listeners[i+1]).changeFinish(changeEvent);
            }
        }
    }

    /**
     * Creates a horizontal slider using the specified min and max
     * with an initial value equal to the average of the min plus max.
     * <p>
     * The <code>BoundedRangeModel</code> that holds the slider's data
     * handles any issues that may arise from improperly setting the
     * minimum and maximum values on the slider.  See the
     * {@code BoundedRangeModel} documentation for details.
     *
     * @param min the minimum value of the slider
     * @param max the maximum value of the slider
     * @see BoundedRangeModel
     * @see #setMinimum
     * @see #setMaximum
     */
    public JSliderAdvanced(int min, int max) {
        this(min, max, (min + max) / 2);
    }

    /**
     * Adds an <code>AdvancedChangeListener</code> to this spinner.
     *
     * @param listener the <code>AdvancedChangeListener</code> to add
     * @see #fireStateChanged
     * @see #addChangeListener
     */
    public void addAdvancedChangeListener(AdvancedChangeListener listener) {
        listenerList.add(AdvancedChangeListener.class, listener);
    }

    /**
     * Removes an <code>AdvancedChangeListener</code> from this spinner.
     *
     * @param listener the <code>AdvancedChangeListener</code> to remove
     * @see #fireStateChanged
     * @see #addChangeListener
     */
    public void removeAdvancedChangeListener(AdvancedChangeListener listener) {
        listenerList.remove(AdvancedChangeListener.class, listener);
    }
}
