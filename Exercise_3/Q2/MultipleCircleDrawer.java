import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Random;

/**
 * {DESCRIPTION}
 *
 * @author Huw Jones
 * @since 16/02/2016
 */
public class MultipleCircleDrawer extends JFrame {
    private ShapePanel panel_shape;
    private ArrayList<Shape> circles;

    public MultipleCircleDrawer () {
        super("Multiple Circle Drawer");

        this.setSize(new Dimension(1600, 900));
        this.circles = new ArrayList<>();

        this.panel_shape = new ShapePanel();

        this.getContentPane().add(panel_shape);

        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {

        }


        this.setVisible(true);

        addCircles();

        repaint();
    }

    private void addCircles(){
        // Remove all circles
        if (this.circles.size() != 0) this.circles.forEach(this.panel_shape::removeShape);

        this.circles = new ArrayList<>();

        Circle randomCircle;

        // Create a random amount of circles
        for (int i = 0; i < (10 + new Random().nextInt(15)); i++) {
            randomCircle = this.createCircle();
            this.panel_shape.addShape(randomCircle);
        }
    }

    private Circle createCircle() {
        // Diameter
        int d = new Random().nextInt(this.panel_shape.getHeight() / 3) + Double.valueOf(this.panel_shape.getHeight() / 4d).intValue();

        Dimension size = new Dimension(d,d);

        // Create a new circle
        Circle circle = new Circle(getRandomX(size.width), getRandomY(size.height), size.width, size.height);
        circle.setRandomColour();
        this.circles.add(circle);
        return circle;
    }

    /**
     * Gets a valid x coordinate on the JFrame that ensure the object won't disappear offscreen
     *
     * @param width Width of the object
     * @return Random X Coordinate
     */
    private int getRandomX(int width) {
        // Prevent negative bounds
        if (this.getWidth() - width > 0) {
            return new Random().nextInt(this.getWidth() - width);
        }
        return 0;
    }

    /**
     * Gets a valid y coordinate on the JFrame that ensure the object won't disappear offscreen
     *
     * @param height Height of the object
     * @return Random Y Coordinate
     */
    private int getRandomY(int height) {
        // Prevent negative bounds
        if (this.getHeight() - height > 0) {
            return new Random().nextInt(this.getHeight() - height);
        }
        return 0;
    }
    public static void main (String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                MultipleCircleDrawer drawer = new MultipleCircleDrawer();
            }
        });
    }
}
