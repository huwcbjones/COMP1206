import javax.swing.*;
import java.awt.*;

/**
 * {DESCRIPTION}
 *
 * @author Huw Jones
 * @since 16/02/2016
 */
public class MultipleCircleDrawer extends JFrame {
    private Circle circle1;
    private Circle circle2;

    public MultipleCircleDrawer() {
        super("Multiple Circle Drawer");

        this.setSize(new Dimension(1200, 600));

        Container cp = this.getContentPane();
        cp.setLayout(new BorderLayout());


        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {

        }


        this.setVisible(true);

        circle1 = new Circle(Double.valueOf(this.getContentPane().getHeight() / 2).intValue());
        cp.add(circle1, BorderLayout.LINE_START);

        circle2 = new Circle(Double.valueOf(this.getContentPane().getHeight() /2 ).intValue());
        cp.add(circle2, BorderLayout.LINE_END);

        repaint();
    }

    public static void main(String[] args) {
        MultipleCircleDrawer drawer = new MultipleCircleDrawer();
    }
}
