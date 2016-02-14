import javax.swing.*;

/**
 * Draws circles
 *
 * @author Huw Jones
 * @since 14/02/2016
 */
public class CircleDrawer extends JFrame {

    public static void main(String[] args){
        CircleDrawer drawer = new CircleDrawer();
    }

    public CircleDrawer(){
        super("Circle Drawer");

        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {

        }
    }


}
