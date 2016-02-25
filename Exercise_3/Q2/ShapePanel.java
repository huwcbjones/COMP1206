import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Random;

/**
 * Draws Shapes
 *
 * @author Huw Jones
 * @since 24/02/2016
 */
public class ShapePanel extends JPanel {

    private ArrayList<Shape> shapes;

    public ShapePanel() {
        shapes = new ArrayList<>();
        this.addMouseListener(new clickEventHandler());
    }

    /**
     * Adds a shape to the panel
     * @param shape Shape to add
     */
    public void addShape(Shape shape) {
        shapes.add(shape);
        repaint();
    }

    /**
     * Removes a shape from the panel
     * @param shape shape to remove
     * @return Returns true if shape was removed
     */
    public boolean removeShape(Shape shape) {
        boolean removed = shapes.remove(shape);
        repaint();
        return removed;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        for (Shape s : shapes) {
            g2d.setColor(s.getColour());
            g2d.fill(s.getShape());
        }
    }

    /**
     * Creates a random colour
     */
    private class RandomColour extends Color {
        public RandomColour() {
            super(new Random().nextInt(255), new Random().nextInt(255), new Random().nextInt(255));
        }
    }

    private class clickEventHandler extends MouseAdapter {
        /**
         * {@inheritDoc}
         *
         * @param e
         */
        @Override
        public void mouseClicked(MouseEvent e) {
            for (Shape s : shapes) {
                // If shape was clicked
                if (s.getShape().contains(e.getPoint())) {
                    // Recolour shape
                    s.setRandomColour();
                }
            }
            // Repaint all shapes
            repaint();
        }
    }

}
