import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Random;

/**
 * An abstract shape
 *
 * @author Huw Jones
 * @since 14/02/2016
 */
public abstract class Shape extends JPanel {

    public Shape (int width, int height) {
        this.addMouseListener(new mouseClickManager());
        Dimension size = new Dimension(width, height);
        this.setPreferredSize(size);
        this.setSize(size);
        this.setMaximumSize(size);
        this.setMinimumSize(size);
        this.setOpaque(false);
    }

    /**
     * Abstract method implemented in shape that returns a boolean that represents whether the x and y co-ordinates
     * are in the shape.
     *
     * @param x int x co-ordinates to check
     * @param y int y-co-ordinates to check
     * @return boolean, true if the x, y coordinates are in the shape
     */
    public abstract boolean isInShape (int x, int y);

    private class mouseClickManager extends MouseAdapter {

        /**
         * {@inheritDoc}
         *
         * @param e
         */
        @Override
        public void mouseClicked (MouseEvent e) {
            if (isInShape(e.getX(), e.getY())) {
                Shape.this.repaint();
            }
        }
    }

    @Override
    protected void paintComponent (Graphics g) {
        super.paintComponent(g);
        paintShape(g, new RandomColour());
    }


    /**
     * Paints the shape in the given colour.
     *
     * @param g the <code>Graphics</code> context in which to paint
     * @param colour Colour to paint shape in
     */
    public abstract void paintShape (Graphics g, Color colour);

    /**
     * Creates a random colour
     */
    private class RandomColour extends Color {
        public RandomColour () {
            super(new Random().nextInt(255), new Random().nextInt(255), new Random().nextInt(255));
        }
    }
}
