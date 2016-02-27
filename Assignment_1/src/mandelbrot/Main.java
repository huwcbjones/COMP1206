package mandelbrot;


import utils.SpringUtilities;

import javax.swing.*;
import java.awt.*;

/**
 * Main Application Window
 *
 * @author Huw Jones
 * @since 22/02/2016
 */
public class Main extends JFrame {

    private static final double DISPLAY_CONSTRAINT = 0.75;
    private JPanel panel_display;
    private JPanel panel_info;
    private JPanel panel_julia;
    private JPanel panel_controls;

    // Axis Range
    private JLabel label_rangeX;
    private JSpinner spinner_rangeMinX;
    private JSpinner spinner_rangeMaxX;
    private JLabel label_rangeY;
    private JSpinner spinner_rangeMinY;
    private JSpinner spinner_rangeMaxY;

    // Info
    private JLabel label_complexPoint;

    public Main() {
        super("Mandelbrot Viewer");
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
        c.setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.BOTH;

        panel_display = new JPanel();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.weightx = DISPLAY_CONSTRAINT;
        constraints.weighty = DISPLAY_CONSTRAINT;
        constraints.gridheight = 3;
        panel_display.setBackground(Color.black);
        c.add(panel_display, constraints);

        initSidePanels(constraints);
        initControlPanel();
        initInfoPanel();
    }

    private void initSidePanels(GridBagConstraints c) {
        Container pane = this.getContentPane();
        c.weightx = 1 - DISPLAY_CONSTRAINT;
        c.gridx = 1;
        c.gridheight = 1;

        panel_info = new JPanel();
        c.gridy = 0;
        c.weighty = 0.25;
        pane.add(panel_info, c);

        panel_controls = new JPanel(new SpringLayout());
        c.gridy = 1;
        c.weighty = 0.25;
        pane.add(panel_controls, c);

        panel_julia = new JPanel();
        c.gridy = 2;
        c.weighty = 0.5;
        pane.add(panel_julia, c);
    }

    private void initControlPanel() {
        label_rangeX = new JLabel("X Range:", JLabel.TRAILING);
        panel_controls.add(label_rangeX);

        spinner_rangeMinX = new JSpinner(new SpinnerNumberModel(-2, -10, 10, 0.2));
        panel_controls.add(spinner_rangeMinX);

        spinner_rangeMaxX = new JSpinner(new SpinnerNumberModel(2, -10, 10, 0.2));
        panel_controls.add(spinner_rangeMaxX);

        label_rangeY = new JLabel("Y Range:", JLabel.TRAILING);
        panel_controls.add(label_rangeY);

        spinner_rangeMinY = new JSpinner(new SpinnerNumberModel(-1.6, -10, 10, 0.2));
        panel_controls.add(spinner_rangeMinY);

        spinner_rangeMaxY = new JSpinner(new SpinnerNumberModel(1.6, -10, 10, 0.2));
        panel_controls.add(spinner_rangeMaxY);

        SpringUtilities.makeCompactGrid(panel_controls,
                2, 3,
                6, 6,
                6, 6

        );
    }

    private void initInfoPanel() {

    }
}
