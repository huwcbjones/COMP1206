import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;

/**
 * Draws circles
 *
 * @author Huw Jones
 * @since 14/02/2016
 */
public class CircleDrawer extends JFrame {

    private Circle circle;
    private ShapePanel panel;
    public static void main(String[] args){
        CircleDrawer drawer = new CircleDrawer();
    }

    public CircleDrawer(){
        super("Circle Drawer");

        this.setSize(new Dimension(800, 600));

        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {

        }

        this.setVisible(true);

        panel = new ShapePanel();
        this.getContentPane().add(panel);

        circle = new Circle(0, 0, this.getContentPane().getHeight(), this.getContentPane().getHeight());
        circle.setRandomColour();
        panel.addShape(circle);
        repaint();
    }


}
