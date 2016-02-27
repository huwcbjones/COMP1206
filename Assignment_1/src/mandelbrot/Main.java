package mandelbrot;


import utils.ImagePanel;
import utils.SpringUtilities;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Main Application Window
 *
 * @author Huw Jones
 * @since 22/02/2016
 */
public class Main extends JFrame {

    private static final double DISPLAY_CONSTRAINT = 0.95;


    private JPanel panel_display;
    private ImagePanel imgPanel_image;
    private JPanel panel_info;
    private JPanel panel_controls;
    private JPanel panel_bookmarks;
    private JPanel panel_julia;

    private DrawingThread drawer;

    // Control Panel
    private JLabel label_iterations;
    private JSpinner spinner_iterations;

    private JLabel label_translateX;
    private JSpinner spinner_translateX;

    private JLabel label_translateY;
    private JSpinner spinner_translateY;

    private JLabel label_scale;
    private JSpinner spinner_scale;

    private JButton btn_redraw;

    // Info
    private JLabel label_complexPoint;
    private JTextField text_complexPoint;

    private JLabel label_xRange;
    private JTextField text_xRange;

    private JLabel label_yRange;
    private JTextField text_yRange;

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
    }

    private void initComponents(){
        Container c = this.getContentPane();
        c.setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.BOTH;

        SpringLayout layout = new SpringLayout();
        panel_display = new JPanel();
        panel_display.setLayout(layout);
        panel_display.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Mandelbrot Set"));
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.weightx = DISPLAY_CONSTRAINT;
        constraints.weighty = DISPLAY_CONSTRAINT;
        constraints.gridheight = 4;
        constraints.gridwidth = 1;
        c.add(panel_display, constraints);

        imgPanel_image = new ImagePanel();
        imgPanel_image.setBackground(Color.WHITE);
        panel_display.add(imgPanel_image);
        layout.putConstraint(SpringLayout.NORTH, imgPanel_image, 5, SpringLayout.NORTH, panel_display);
        layout.putConstraint(SpringLayout.EAST, imgPanel_image, -5, SpringLayout.EAST, panel_display);
        layout.putConstraint(SpringLayout.SOUTH, imgPanel_image, -5, SpringLayout.SOUTH, panel_display);
        layout.putConstraint(SpringLayout.WEST, imgPanel_image, 5, SpringLayout.WEST, panel_display);

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

        btn_redraw = new JButton("Redraw");
        btn_redraw.addActionListener(new redrawHandler());
        pane.add(btn_redraw);

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
        label_iterations = new JLabel("Iterations:", JLabel.TRAILING);
        panel_controls.add(label_iterations);

        spinner_iterations = new JSpinner(new SpinnerNumberModel(10, 1, 1000, 1));
        panel_controls.add(spinner_iterations);

        // Scale
        label_scale = new JLabel("Scale:", JLabel.TRAILING);
        panel_controls.add(label_scale);

        spinner_scale = new JSpinner(new SpinnerNumberModel(1, 0, 100, 0.1d));
        panel_controls.add(spinner_scale);

        // x shift
        label_translateX = new JLabel("X Shift:", JLabel.TRAILING);
        panel_controls.add(label_translateX);

        spinner_translateX = new JSpinner(new SpinnerNumberModel(0, -4, 4, 0.1));
        panel_controls.add(spinner_translateX);

         // y shit
        label_translateY = new JLabel("Y Shift:", JLabel.TRAILING);
        panel_controls.add(label_translateY);

        spinner_translateY = new JSpinner(new SpinnerNumberModel(0, -3.2, 3.2, 0.1));
        panel_controls.add(spinner_translateY);

        SpringUtilities.makeCompactGrid(panel_controls, 4, 2, 6, 6, 6, 6);
    }

    private void initInfoPanel() {
        SpringLayout layout = new SpringLayout();
        panel_info.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Information"));
        panel_info.setLayout(layout);

        label_xRange = new JLabel("X Range:", JLabel.TRAILING);
        panel_info.add(label_xRange);

        text_xRange = new JTextField("");
        text_xRange.setEditable(false);
        panel_info.add(text_xRange);

        label_yRange = new JLabel("Y Range:", JLabel.TRAILING);
        panel_info.add(label_yRange);

        text_yRange = new JTextField("");
        text_yRange.setEditable(false);
        panel_info.add(text_yRange);

        label_complexPoint = new JLabel("Selected Point:", JLabel.TRAILING);
        label_complexPoint.setForeground(Color.lightGray);
        panel_info.add(label_complexPoint);

        text_complexPoint = new JTextField("");
        text_complexPoint.setEditable(false);
        panel_info.add(text_complexPoint);

        SpringUtilities.makeCompactGrid(panel_info, 3, 2, 6, 6, 6, 6);
    }

    public double getTranslateX() {
        return (double) spinner_translateX.getValue();
    }

    public double getTranslateY() {
        return (double) spinner_translateY.getValue();
    }

    public int getIterations() {
        return (int) spinner_iterations.getValue();
    }

    public double getScaleFactor() {
        return (double) spinner_scale.getValue();
    }

    private class redrawHandler implements ActionListener {

        /**
         * Invoked when an action occurs.
         *
         * @param e
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            if (drawer == null || !drawer.isAlive()) {
                drawer = new DrawingThread(Main.this, imgPanel_image);
                drawer.start();
            }
        }
    }


}