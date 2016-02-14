import javax.swing.*;
import javax.swing.plaf.DimensionUIResource;
import java.awt.*;

/**
 * Draws circles
 *
 * @author Huw Jones
 * @since 14/02/2016
 */
public class CircleDrawer extends JFrame {

    private Circle circle;
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

        circle = new Circle(Double.valueOf(this.getContentPane().getHeight() / 2).intValue());
        this.add(circle);
    }


}
