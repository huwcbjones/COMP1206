import java.awt.*;
import java.util.Random;

/**
 * {DESCRIPTION}
 *
 * @author Huw Jones
 * @since 25/02/2016
 */
public abstract class Shape {

    private Color colour;

    public java.awt.Shape getShape() {
        return shape;
    }

    protected java.awt.Shape shape;

    public void setRandomColour(){
        this.colour = new RandomColour();
    }

    public Color getColour() {
        return colour;
    }

    public void setColour(Color colour) {
        this.colour = colour;
    }

    public static class RandomColour extends Color {
        public RandomColour () {
            super(new Random().nextInt(255), new Random().nextInt(255), new Random().nextInt(255));
        }
    }
}
