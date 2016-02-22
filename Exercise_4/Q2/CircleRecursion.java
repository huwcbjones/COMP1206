import javax.swing.*;

/**
 * Circle Recursion Application
 *
 * @author Huw Jones
 * @since 22/02/2016
 */
public class CircleRecursion extends JFrame {

    public CircleRecursion(int iterations) {
        this.setExtendedState(JFrame.MAXIMIZED_BOTH);
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        CirclePanel panel = new CirclePanel(iterations);

        this.getContentPane().add(panel);

        this.setVisible(true);
        this.repaint();
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Please specify a number of iterations.");
            return;
        }
        try {
            int iterations = Integer.parseInt(args[0]);
            CircleRecursion circle = new CircleRecursion(iterations);
        } catch (NumberFormatException ex) {
            System.err.println("Please specify a valid number of iterations.");
        }
    }
}
