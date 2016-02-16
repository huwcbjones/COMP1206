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

    public Shape () {
        this.addMouseListener(new mouseClickManager());
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
            int relX, relY;
            relX = e.getX() - Shape.this.getX();
            relY = e.getY() - Shape.this.getY();
            if (isInShape(relX, relY)) {
                Shape.this.repaint();
            }
        }
    }

    /**
     * Invoked by Swing to draw components.
     * Applications should not invoke <code>paint</code> directly,
     * but should instead use the <code>repaint</code> method to
     * schedule the component for redrawing.
     * <p>
     * This method actually delegates the work of painting to three
     * protected methods: <code>paintComponent</code>,
     * <code>paintBorder</code>,
     * and <code>paintChildren</code>.  They're called in the order
     * listed to ensure that children appear on top of component itself.
     * Generally speaking, the component and its children should not
     * paint in the insets area allocated to the border. Subclasses can
     * just override this method, as always.  A subclass that just
     * wants to specialize the UI (look and feel) delegate's
     * <code>paint</code> method should just override
     * <code>paintComponent</code>.
     *
     * @param g the <code>Graphics</code> context in which to paint
     * @see #paintComponent
     * @see #paintBorder
     * @see #paintChildren
     * @see #getComponentGraphics
     * @see #repaint
     */
    @Override
    public void paint (Graphics g) {
        super.paint(g);
        paintShape(g, new RandomColour());
    }


    /**
     * Paints the shape in the given colour.
     *
     * @param @param g the <code>Graphics</code> context in which to paint
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
