package shared.components;

import javax.swing.*;
import javax.swing.event.EventListenerList;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.font.TextAttribute;
import java.util.HashMap;
import java.util.Map;

/**
 * Adds linking functionality to a JLabel
 *
 * @author Huw Jones
 * @since 25/04/2016
 */
public class JLinkLabel extends JLabel {

    /**
     * Creates a <code>JLabel</code> instance with the specified
     * text, image, and horizontal alignment.
     * The label is centered vertically in its display area.
     * The text is on the trailing edge of the image.
     *
     * @param text                The text to be displayed by the label.
     * @param icon                The image to be displayed by the label.
     * @param horizontalAlignment One of the following constants
     *                            defined in <code>SwingConstants</code>:
     *                            <code>LEFT</code>,
     *                            <code>CENTER</code>,
     *                            <code>RIGHT</code>,
     *                            <code>LEADING</code> or
     */
    public JLinkLabel(String text, Icon icon, int horizontalAlignment) {
        super(text, icon, horizontalAlignment);
        this.addMouseListener(new MouseHandler());
    }

    /**
     * Creates a <code>JLabel</code> instance with the specified
     * text and horizontal alignment.
     * The label is centered vertically in its display area.
     *
     * @param text                The text to be displayed by the label.
     * @param horizontalAlignment One of the following constants
     *                            defined in <code>SwingConstants</code>:
     *                            <code>LEFT</code>,
     *                            <code>CENTER</code>,
     *                            <code>RIGHT</code>,
     *                            <code>LEADING</code> or
     */
    public JLinkLabel(String text, int horizontalAlignment) {
        super(text, horizontalAlignment);
        this.addMouseListener(new MouseHandler());
    }

    /**
     * Creates a <code>JLabel</code> instance with the specified text.
     * The label is aligned against the leading edge of its display area,
     * and centered vertically.
     *
     * @param text The text to be displayed by the label.
     */
    public JLinkLabel(String text) {
        super(text);
        this.addMouseListener(new MouseHandler());
    }

    /**
     * Creates a <code>JLabel</code> instance with the specified
     * image and horizontal alignment.
     * The label is centered vertically in its display area.
     *
     * @param image               The image to be displayed by the label.
     * @param horizontalAlignment One of the following constants
     *                            defined in <code>SwingConstants</code>:
     *                            <code>LEFT</code>,
     *                            <code>CENTER</code>,
     *                            <code>RIGHT</code>,
     *                            <code>LEADING</code> or
     */
    public JLinkLabel(Icon image, int horizontalAlignment) {
        super(image, horizontalAlignment);
        this.addMouseListener(new MouseHandler());
    }

    /**
     * Creates a <code>JLabel</code> instance with the specified image.
     * The label is centered vertically and horizontally
     * in its display area.
     *
     * @param image The image to be displayed by the label.
     */
    public JLinkLabel(Icon image) {
        super(image);
        this.addMouseListener(new MouseHandler());
    }

    /**
     * Creates a <code>JLabel</code> instance with
     * no image and with an empty string for the title.
     * The label is centered vertically
     * in its display area.
     * The label's contents, once set, will be displayed on the leading edge
     * of the label's display area.
     */
    public JLinkLabel() {
        this.addMouseListener(new MouseHandler());
    }

    public void addActionListener(ActionListener listener){
        this.listenerList.add(ActionListener.class, listener);
    }

    public void removeActionListener(ActionListener listener){
        this.listenerList.remove(ActionListener.class, listener);
    }
    /**
     * Notifies all listeners that have registered interest for
     * notification on this event type.  The event instance
     * is lazily created using the <code>event</code>
     * parameter.
     *
     * @param event  the <code>ActionEvent</code> object
     * @see EventListenerList
     */
    protected void fireActionPerformed(ActionEvent event) {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        ActionEvent e = null;
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length-2; i>=0; i-=2) {
            if (listeners[i]==ActionListener.class) {
                // Lazily create the event:
                if (e == null) {
                    String actionCommand = event.getActionCommand();
                    e = new ActionEvent(JLinkLabel.this,
                        ActionEvent.ACTION_PERFORMED,
                        actionCommand,
                        event.getWhen(),
                        event.getModifiers());
                }
                ((ActionListener)listeners[i+1]).actionPerformed(e);
            }
        }
    }
    private class MouseHandler extends MouseAdapter {
        private Font original;
        /**
         * {@inheritDoc}
         *
         * @param e
         */
        @Override
        public void mouseClicked(MouseEvent e) {
            if(!JLinkLabel.this.isEnabled()) return;
            fireActionPerformed(new ActionEvent(JLinkLabel.this, ActionEvent.ACTION_PERFORMED, "Click"));
        }

        /**
         * {@inheritDoc}
         *
         * @param e
         */
        @Override
        public void mouseEntered(MouseEvent e) {
            if(!JLinkLabel.this.isEnabled()) return;
            JLinkLabel.this.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            Font f = JLinkLabel.this.getFont();
            original = f;
            Map<TextAttribute, Object> attribs = new HashMap<>(f.getAttributes());
            attribs.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
            JLinkLabel.this.setFont(f.deriveFont(attribs));
        }

        /**
         * {@inheritDoc}
         *
         * @param e
         */
        @Override
        public void mouseExited(MouseEvent e) {
            if(!JLinkLabel.this.isEnabled()) return;
            JLinkLabel.this.setCursor(Cursor.getDefaultCursor());
            JLinkLabel.this.setFont(this.original);
        }
    }

}
