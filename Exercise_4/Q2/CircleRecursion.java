import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Circle Recursion Application
 *
 * @author Huw Jones
 * @since 22/02/2016
 */
public class CircleRecursion extends JFrame {

    private CirclePanel panel_circle;
    private JPanel panel_controls;
    private JSpinner spinner_iterations;
    private JLabel label_iterations;
    private JButton btn_update;

    public CircleRecursion() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {

        }

        this.setExtendedState(JFrame.MAXIMIZED_BOTH);
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        panel_circle = new CirclePanel();

        Container c = this.getContentPane();
        c.setLayout(new BorderLayout());
        c.add(panel_circle, BorderLayout.CENTER);

        panel_controls = new JPanel(new FlowLayout());

        label_iterations = new JLabel("Iterations: ");
        panel_controls.add(label_iterations);

        SpinnerNumberModel slm_iterations = new SpinnerNumberModel(3, 1, 20, 1);
        spinner_iterations = new JSpinner(slm_iterations);
        panel_controls.add(spinner_iterations);

        btn_update = new JButton("Update");
        btn_update.addActionListener(new updateHandler());
        panel_controls.add(btn_update);

        c.add(panel_controls, BorderLayout.PAGE_END);

        this.setVisible(true);
        this.repaint();
    }

    public static void main(String[] args) {
        CircleRecursion circle = new CircleRecursion();
    }

    protected class updateHandler implements ActionListener {

        /**
         * Invoked when an action occurs.
         *
         * @param e
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            panel_circle.repaint((int) spinner_iterations.getValue());
        }
    }
}
