package mandelbrot;


import utils.Complex;
import utils.ImagePanel;
import utils.SpringUtilities;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Main Application Window
 *
 * @author Huw Jones
 * @since 22/02/2016
 */
public class Main extends JFrame {

    private static final double DISPLAY_CONSTRAINT = 0.95;


    private ImagePanel panel_display;
    private JPanel panel_info;
    private JPanel panel_controls;
    private JPanel panel_bookmarks;
    private JPanel panel_julia;

    private DrawingThread drawer;

    // Control Panel
    private JLabel label_iterations;
    private JSpinner spinner_iterations;

    private JLabel label_rangeX;
    private JPanel panel_rangeX;
    private JSpinner spinner_rangeMinX;
    private JSpinner spinner_rangeMaxX;

    private JLabel label_rangeY;
    private JPanel panel_rangeY;
    private JSpinner spinner_rangeMinY;
    private JSpinner spinner_rangeMaxY;

    // Info
    private JLabel label_complexPoint;
    private JTextField text_complexPoint;

    // Bookmarks


    public Main() {
        super("Mandelbrot Viewer");
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.setExtendedState(JFrame.MAXIMIZED_BOTH);
        this.setMinimumSize(new Dimension(800, 600));
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {

        }

        initComponents();
        this.pack();
        this.setVisible(true);

        drawer = new DrawingThread(this, panel_display);
        drawer.start();
    }

    private void initComponents(){
        Container c = this.getContentPane();
        c.setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.BOTH;

        panel_display = new ImagePanel();
        panel_display.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Mandelbrot Set"));
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.weightx = DISPLAY_CONSTRAINT;
        constraints.weighty = DISPLAY_CONSTRAINT;
        constraints.gridheight = 4;
        constraints.gridwidth = 1;
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
        c.gridwidth = 1;
        c.weighty = 0;
        c.fill = GridBagConstraints.HORIZONTAL;

        panel_info = new JPanel();
        panel_info.setOpaque(false);
        c.gridy = 0;
        pane.add(panel_info, c);

        panel_controls = new JPanel();
        panel_controls.setOpaque(false);
        c.gridy = 1;
        pane.add(panel_controls, c);

        c.fill = GridBagConstraints.BOTH;
        c.weighty = 0.5;

        panel_bookmarks = new JPanel();
        panel_bookmarks.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Bookmarks"));
        c.gridy = 2;


        pane.add(panel_bookmarks, c);

        panel_julia = new JPanel();
        panel_julia.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Julia Set"));
        c.gridy = 3;
        pane.add(panel_julia, c);
    }

    private void initControlPanel() {
        SpringLayout layout = new SpringLayout();
        panel_controls.setLayout(layout);
        panel_controls.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Controls"));

        // Iterations
        label_iterations = new JLabel("Iterations: ", JLabel.TRAILING);
        panel_controls.add(label_iterations);

        spinner_iterations = new JSpinner(new SpinnerNumberModel(10, 1, 1000, 1));
        panel_controls.add(spinner_iterations);

        // x Range
        label_rangeX = new JLabel("X Range:", JLabel.TRAILING);
        panel_controls.add(label_rangeX);

        panel_rangeX = new JPanel(new GridLayout(0, 2));
        spinner_rangeMinX = new JSpinner(new SpinnerNumberModel(-2, -10, 10, 0.2));
        panel_rangeX.add(spinner_rangeMinX);

        spinner_rangeMaxX = new JSpinner(new SpinnerNumberModel(2, -10, 10, 0.2));
        panel_rangeX.add(spinner_rangeMaxX);
        panel_controls.add(panel_rangeX);

        // y Range
        label_rangeY = new JLabel("Y Range:", JLabel.TRAILING);
        panel_controls.add(label_rangeY);

        panel_rangeY = new JPanel(new GridLayout(0, 2));
        spinner_rangeMinY = new JSpinner(new SpinnerNumberModel(-1.6, -10, 10, 0.2));
        panel_rangeY.add(spinner_rangeMinY);

        spinner_rangeMaxY = new JSpinner(new SpinnerNumberModel(1.6, -10, 10, 0.2));
        panel_rangeY.add(spinner_rangeMaxY);
        panel_controls.add(panel_rangeY);

        SpringUtilities.makeCompactGrid(panel_controls, 3, 2, 6, 6, 6, 6);
    }

    private void initInfoPanel() {
        SpringLayout layout = new SpringLayout();
        panel_info.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Information"));
        panel_info.setLayout(layout);

        label_complexPoint = new JLabel("Selected Point:");
        label_complexPoint.setForeground(Color.lightGray);
        panel_info.add(label_complexPoint);

        text_complexPoint = new JTextField("");
        text_complexPoint.setEditable(false);
        panel_info.add(text_complexPoint);

        SpringUtilities.makeCompactGrid(panel_info, 1, 2, 6, 6, 6, 6);
    }

    public double getRangeX() {
        return (double) spinner_rangeMaxX.getValue() - (double) spinner_rangeMinX.getValue();
    }

    public double getRangeY() {
        return (double) spinner_rangeMaxY.getValue() - (double) spinner_rangeMinY.getValue();
    }

    public int getIterations() {
        return (int) spinner_iterations.getValue();
    }

}