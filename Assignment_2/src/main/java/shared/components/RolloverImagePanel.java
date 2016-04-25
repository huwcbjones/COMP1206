package shared.components;

import javax.swing.*;
import javax.swing.event.EventListenerList;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

/**
 * Loads image into JPanel
 *
 * @author Huw Jones
 * @since 20/11/2015
 */
public class RolloverImagePanel extends JPanel {

    private BufferedImage defaultImage;
    private BufferedImage hoverImage;

    private boolean isHover = false;

    public RolloverImagePanel() {
        super();
        this.setBackground(Color.WHITE);
        this.addMouseListener(new MouseHandler());
    }
    public RolloverImagePanel(BufferedImage defaultImage, BufferedImage hoverImage){
        this();
        this.setPreferredSize(new Dimension(defaultImage.getWidth(), defaultImage.getHeight()));
        this.defaultImage = defaultImage;
        this.hoverImage = hoverImage;
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
                    e = new ActionEvent(RolloverImagePanel.this,
                        ActionEvent.ACTION_PERFORMED,
                        actionCommand,
                        event.getWhen(),
                        event.getModifiers());
                }
                ((ActionListener)listeners[i+1]).actionPerformed(e);
            }
        }
    }

    public void setDefaultImage(BufferedImage image){
        this.defaultImage = image;
    }
    public void setHoverImage(BufferedImage image){
        this.hoverImage = image;
    }

    @Override
    protected void paintComponent(Graphics g){
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D)g;
        if(isHover){
            g2d.drawImage(this.hoverImage, 0, 0, null);
        } else {
            g2d.drawImage(this.defaultImage, 0, 0, null);
        }
    }

    private class MouseHandler extends MouseAdapter {
        /**
         * {@inheritDoc}
         *
         * @param e
         */
        @Override
        public void mouseEntered(MouseEvent e) {
            RolloverImagePanel.this.isHover = true;
            RolloverImagePanel.this.repaint();
            RolloverImagePanel.this.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        /**
         * {@inheritDoc}
         *
         * @param e
         */
        @Override
        public void mouseExited(MouseEvent e) {
            RolloverImagePanel.this.isHover = false;
            RolloverImagePanel.this.repaint();
            RolloverImagePanel.this.setCursor(Cursor.getDefaultCursor());
        }

        /**
         * {@inheritDoc}
         *
         * @param e
         */
        @Override
        public void mouseClicked(MouseEvent e) {
            RolloverImagePanel.this.fireActionPerformed(new ActionEvent(RolloverImagePanel.this, ActionEvent.ACTION_PERFORMED, "Click"));
        }
    }
}
