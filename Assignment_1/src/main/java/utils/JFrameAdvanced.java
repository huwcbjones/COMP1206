package utils;

import mandelbrot.events.AdvancedComponentListener;

import javax.swing.*;
import javax.swing.event.EventListenerList;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

/**
 * Extends JFrame to add AdvancedComponentListener
 *
 * @author Huw Jones
 * @since 01/03/2016
 */
public class JFrameAdvanced extends JFrame {

    /** A list of event listeners for this component. */
    protected EventListenerList listenerList = new EventListenerList();

    private static int TIMEOUT = 50;
    private Timer sliderTimer;
    private ComponentEvent lastChangeEvent;

    /**
     * Constructs a new frame that is initially invisible.
     * <p>
     * This constructor sets the component's locale property to the value
     * returned by <code>JComponent.getDefaultLocale</code>.
     *
     * @throws HeadlessException if GraphicsEnvironment.isHeadless()
     *                           returns true.
     * @see GraphicsEnvironment#isHeadless
     * @see Component#setSize
     * @see Component#setVisible
     * @see JComponent#getDefaultLocale
     */
    public JFrameAdvanced() throws HeadlessException {
        this("", null);
    }

    /**
     * Creates a <code>Frame</code> in the specified
     * <code>GraphicsConfiguration</code> of
     * a screen device and a blank title.
     * <p>
     * This constructor sets the component's locale property to the value
     * returned by <code>JComponent.getDefaultLocale</code>.
     *
     * @param gc the <code>GraphicsConfiguration</code> that is used
     *           to construct the new <code>Frame</code>;
     *           if <code>gc</code> is <code>null</code>, the system
     *           default <code>GraphicsConfiguration</code> is assumed
     * @throws IllegalArgumentException if <code>gc</code> is not from
     *                                  a screen device.  This exception is always thrown when
     *                                  GraphicsEnvironment.isHeadless() returns true.
     * @see GraphicsEnvironment#isHeadless
     * @see JComponent#getDefaultLocale
     * @since 1.3
     */
    public JFrameAdvanced(GraphicsConfiguration gc) {
        this("", gc);
    }

    /**
     * Creates a new, initially invisible <code>Frame</code> with the
     * specified title.
     * <p>
     * This constructor sets the component's locale property to the value
     * returned by <code>JComponent.getDefaultLocale</code>.
     *
     * @param title the title for the frame
     * @throws HeadlessException if GraphicsEnvironment.isHeadless()
     *                           returns true.
     * @see GraphicsEnvironment#isHeadless
     * @see Component#setSize
     * @see Component#setVisible
     * @see JComponent#getDefaultLocale
     */
    public JFrameAdvanced(String title) throws HeadlessException {
        this(title, null);
    }

    /**
     * Creates a <code>JFrame</code> with the specified title and the
     * specified <code>GraphicsConfiguration</code> of a screen device.
     * <p>
     * This constructor sets the component's locale property to the value
     * returned by <code>JComponent.getDefaultLocale</code>.
     *
     * @param title the title to be displayed in the
     *              frame's border. A <code>null</code> value is treated as
     *              an empty string, "".
     * @param gc    the <code>GraphicsConfiguration</code> that is used
     *              to construct the new <code>JFrame</code> with;
     *              if <code>gc</code> is <code>null</code>, the system
     *              default <code>GraphicsConfiguration</code> is assumed
     * @throws IllegalArgumentException if <code>gc</code> is not from
     *                                  a screen device.  This exception is always thrown when
     *                                  GraphicsEnvironment.isHeadless() returns true.
     * @see GraphicsEnvironment#isHeadless
     * @see JComponent#getDefaultLocale
     * @since 1.3
     */
    public JFrameAdvanced(String title, GraphicsConfiguration gc) {
        super(title, gc);
        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                if(!sliderTimer.isRunning()) {
                    fireComponentResizeStart(e);
                }
                sliderTimer.restart();
                lastChangeEvent = e;
            }
        });

        sliderTimer = new Timer(TIMEOUT, e -> {
            fireComponentResizeEnd(lastChangeEvent);
            lastChangeEvent = null;
        });
        sliderTimer.setRepeats(false);
    }

    /**
     * Send a {@code ComponentEvent}, whose source is this {@code JFrameAdvanced}, to
     * all {@code AdvancedComponentListener}s that have registered interest in
     * {@code AdvancedChangeEvent}s.
     * This method is called each time a {@code ComponentEvent} is received from
     * the model timer.
     * <p>
     * The event instance is created if necessary, and stored in
     * {@code componentEvent}.
     *
     * @see #addComponentListener
     * @see EventListenerList
     */
    protected void fireComponentResizeStart(ComponentEvent componentEvent) {
        Object[] listeners = listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i]==AdvancedComponentListener.class) {
                ((AdvancedComponentListener)listeners[i+1]).componentResizeStart(componentEvent);
            }
        }
    }

    /**
     * Send a {@code ComponentEvent}, whose source is this {@code JSlider}, to
     * all {@code AdvancedComponentListener}s that have registered interest in
     * {@code AdvancedChangeEvent}s.
     * This method is called each time a {@code ComponentEvent} is received from
     * the model timer.
     * <p>
     * The event instance is created if necessary, and stored in
     * {@code componentEvent}.
     *
     * @see #addComponentListener
     */
    protected void fireComponentResizeEnd(ComponentEvent componentEvent) {
        Object[] listeners = listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i]==AdvancedComponentListener.class) {
                ((AdvancedComponentListener)listeners[i+1]).componentResizeEnd(componentEvent);
            }
        }
    }

    /**
     * Adds an <code>AdvancedChangeListener</code> to this JFrameAdvanced.
     *
     * @param listener the <code>AdvancedChangeListener</code> to add
     * @see #addComponentListener
     */
    public void addAdvancedComponentListener(AdvancedComponentListener listener) {
        listenerList.add(AdvancedComponentListener.class, listener);
    }

    /**
     * Removes an <code>AdvancedChangeListener</code> from this JFrameAdvanced.
     *
     * @param listener the <code>AdvancedChangeListener</code> to remove
     * @see #removeComponentListener
     */
    public void removeAdvancedComponentListener(AdvancedComponentListener listener) {
        listenerList.remove(AdvancedComponentListener.class, listener);
    }
}
