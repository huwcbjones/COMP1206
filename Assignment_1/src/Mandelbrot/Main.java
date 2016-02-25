package mandelbrot;

import javax.swing.*;
import java.awt.*;

/**
 * Main Application Window
 *
 * @author Huw Jones
 * @since 22/02/2016
 */
public class Main extends JFrame {

    private JPanel panel_controls;
    private JPanel panel_display;

    private JLabel label_rangeX;
    private JSpinner spinner_rangeX;
    private JLabel label_rangeY;
    private JSpinner spinner_rangeY;

    public Main() {
        super("Mandelbrot Viewer");
        this.getContentPane().setLayout(new BorderLayout());
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.setMinimumSize(new Dimension(800, 600));
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {

        }
        initComponents();
        this.pack();
        this.setVisible(true);
    }

    private void initComponents(){
        Container c = this.getContentPane();

        panel_display = new JPanel();
        c.add(panel_display, BorderLayout.CENTER);

        panel_controls = new JPanel();

        c.add(panel_controls, BorderLayout.LINE_START);
    }
}
