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
    private ArrayList<Circle> circles;
    private boolean redrawInProgress = false;

    public MultipleCircleDrawer () {
        super("Multiple Circle Drawer");

        this.setSize(new Dimension(1600, 900));
        this.circles = new ArrayList<>();

        Container cp = this.getContentPane();
        cp.setLayout(null);


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
        redrawInProgress = true;

        if (this.circles.size() != 0) this.circles.forEach(this.getContentPane()::remove);

        this.circles = new ArrayList<>();

        Circle randomCircle;

        for (int i = 0; i < (10 + new Random().nextInt(15)); i++) {
            randomCircle = this.createCircle();
            this.getContentPane().add(randomCircle);
        }
        redrawInProgress = false;
    }

    private Circle createCircle() {
        Circle circle = new Circle(new Random().nextInt(this.getContentPane().getHeight() / 2));
        Dimension size = new Dimension(circle.getWidth(), circle.getHeight());
        circle.setBounds(getRandomX(size.width), getRandomY(size.height), size.width, size.height);

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
        MultipleCircleDrawer drawer = new MultipleCircleDrawer();
    }
}
