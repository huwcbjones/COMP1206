import java.awt.*;
import java.util.Random;

/**
 * Represents a shape with a colour
 *
 * @author Huw Jones
 * @since 25/02/2016
 */
public abstract class Shape {

    private Color colour;
    protected java.awt.Shape shape;

    /**
     * Gets the current shape
     *
     * @return The shape
     */
    public java.awt.Shape getShape() {
        return shape;
    }

    /**
     * Sets the colour of the shape to a random colour
     */
    public void setRandomColour(){
        this.colour = new RandomColour();
    }

    /**
     * Gets the shape's colour
     * @return Color of shape
     */
    public Color getColour() {
        return colour;
    }

    /**
     * Sets the shape's colour
     * @param colour Color to set shape
     */
    public void setColour(Color colour) {
        this.colour = colour;
    }

    /**
     * Returns a random colour
     */
    public static class RandomColour extends Color {
        public RandomColour () {
            super(new Random().nextInt(255), new Random().nextInt(255), new Random().nextInt(255));
        }
    }
}
