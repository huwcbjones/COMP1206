import javax.swing.*;
import java.awt.*;

/**
 * Draws 2 squares and 2 circles
 *
 * @author Huw Jones
 * @since 25/02/2016
 */
public class TwoSquareTwoCircle extends JFrame {

    private Circle circle1;
    private Circle circle2;

    private Rectangle square1;
    private Rectangle square2;

    private ShapePanel panel;

    public TwoSquareTwoCircle() {
        super("2 Square 2 Circle");

        this.setSize(new Dimension(800, 600));

        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {

        }

        panel = new ShapePanel();
        this.getContentPane().add(panel);

        this.setVisible(true);


        int size = panel.getHeight() / 2;
        circle1 = new Circle(0, 0, size, size);
        circle2 = new Circle(panel.getWidth() / 2, panel.getHeight() / 2, size, size);
        circle1.setRandomColour();
        circle2.setRandomColour();
        panel.addShape(circle1);
        panel.addShape(circle2);

        square1 = new Rectangle(panel.getWidth() / 2, 0, size, size);
        square2 = new Rectangle(0, panel.getHeight() / 2, size, size);
        square1.setRandomColour();
        square2.setRandomColour();
        panel.addShape(square1);
        panel.addShape(square2);

        repaint();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                TwoSquareTwoCircle drawer = new TwoSquareTwoCircle();
            }
        });
    }
}