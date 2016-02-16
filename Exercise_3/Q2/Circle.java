import java.awt.*;

/**
 * A Circle
 *
 * @author Huw Jones
 * @since 14/02/2016
 */
public class Circle extends Shape {

    int radius;

    public Circle (int radius) {
        super();
        this.radius = radius;
    }

    /**
     * Abstract method implemented in shape that returns a boolean that represents whether the x and y co-ordinates
     * are in the shape.
     *
     * @param x int x co-ordinates to check
     * @param y int y-co-ordinates to check
     * @return boolean, true if the x, y coordinates are in the shape
     */
    @Override
    public boolean isInShape (int x, int y) {
        x -= radius;
        y -= radius;
        return Math.pow(x, 2) + Math.pow(y, 2) <= Math.pow(radius, 2);
    }

    /**
     * Paints the shape in the given colour.
     *
     * @param g
     * @param colour Colour to paint shape in
     */
    @Override
    public void paintShape (Graphics g, Color colour) {
        int x = super.getX();
        int y = super.getY();
        g.setColor(colour);
        g.fillOval(x, y, 2 * radius, 2 * radius);
    }
}
