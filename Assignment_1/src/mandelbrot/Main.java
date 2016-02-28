package mandelbrot;


import mandelbrot.events.DrawListener;
import utils.Complex;
import utils.ImagePanel;
import utils.SpringUtilities;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.awt.event.*;

/**
 * Main Application Window
 *
 * @author Huw Jones
 * @since 22/02/2016
 */
public class Main extends JFrame {

    private static final double DISPLAY_CONSTRAINT = 0.8;


    private JPanel panel_display;
    private ImagePanel imgPanel_image;
    private JPanel panel_info;
    private JPanel panel_controls;
    private JPanel panel_bookmarks;
    private JPanel panel_julia;

    private final MandelbrotManagementThread mandel_drawer;
    private final JuliaDrawingManagementThread julia_drawer;

    // Julia Set
    private ImagePanel imgPanel_julia;

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
    private JLabel label_xRange;
    private JTextField text_xRange;

    private JLabel label_yRange;
    private JTextField text_yRange;

    private JLabel label_cursorPoint;
    private JTextField text_cursorPoint;

    private JLabel label_selectedPoint;
    private JTextField text_selectedPoint;

    private Complex selectedPosition;

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

        mandel_drawer = new MandelbrotManagementThread(Main.this, imgPanel_image);
        mandel_drawer.addDrawListenener(new redrawHandler());
        mandel_drawer.start();

        julia_drawer = new JuliaDrawingManagementThread(this, imgPanel_julia);
        julia_drawer.start();
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
        imgPanel_image.addMouseListener(new mouseClickPositionHandler());
        imgPanel_image.addMouseMotionListener(new mousePositionHandler());

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

        SpringLayout layout = new SpringLayout();
        panel_julia = new JPanel(layout);
        panel_julia.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Julia Set"));
        c.gridy = 3;
        pane.add(panel_julia, c);


        imgPanel_julia = new ImagePanel();
        imgPanel_julia.setBackground(Color.WHITE);
        panel_julia.add(imgPanel_julia);
        layout.putConstraint(SpringLayout.NORTH, imgPanel_julia, 5, SpringLayout.NORTH, panel_julia);
        layout.putConstraint(SpringLayout.EAST, imgPanel_julia, -5, SpringLayout.EAST, panel_julia);
        layout.putConstraint(SpringLayout.SOUTH, imgPanel_julia, -5, SpringLayout.SOUTH, panel_julia);
        layout.putConstraint(SpringLayout.WEST, imgPanel_julia, 5, SpringLayout.WEST, panel_julia);
    }

    private void initControlPanel() {
        SpringLayout layout = new SpringLayout();
        panel_controls.setLayout(layout);
        panel_controls.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Controls"));

        // Iterations
        label_iterations = new JLabel("Iterations:", JLabel.TRAILING);
        panel_controls.add(label_iterations);

        spinner_iterations = new JSpinner(new SpinnerNumberModel(75, 1, 1000, 1));
        panel_controls.add(spinner_iterations);

        // Scale
        label_scale = new JLabel("Scale:", JLabel.TRAILING);
        panel_controls.add(label_scale);

        spinner_scale = new JSpinner(new SpinnerNumberModel(1, 0, 100, 0.1d));
        panel_controls.add(spinner_scale);

        // x shift
        label_translateX = new JLabel("X Shift:", JLabel.TRAILING);
        panel_controls.add(label_translateX);

        spinner_translateX = new JSpinner(new SpinnerNumberModel(0, -100, 100, 0.1));
        panel_controls.add(spinner_translateX);

         // y shit
        label_translateY = new JLabel("Y Shift:", JLabel.TRAILING);
        panel_controls.add(label_translateY);

        spinner_translateY = new JSpinner(new SpinnerNumberModel(0, -100, 100, 0.1));
        panel_controls.add(spinner_translateY);

        JLabel blank = new JLabel();
        panel_controls.add(blank);

        btn_redraw = new JButton("Redraw");
        btn_redraw.addActionListener(new redrawHandler());
        panel_controls.add(btn_redraw);
        SpringUtilities.makeCompactGrid(panel_controls, 5, 2, 6, 6, 6, 6);

    }

    private void initInfoPanel() {
        SpringLayout layout = new SpringLayout();
        panel_info.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Information"));
        panel_info.setLayout(layout);

        label_xRange = new JLabel("X Range:", JLabel.TRAILING);
        panel_info.add(label_xRange);

        text_xRange = new JTextField("- to -");
        text_xRange.setEditable(false);
        panel_info.add(text_xRange);

        label_yRange = new JLabel("Y Range:", JLabel.TRAILING);
        panel_info.add(label_yRange);

        text_yRange = new JTextField("- to -");
        text_yRange.setEditable(false);
        panel_info.add(text_yRange);

        label_cursorPoint = new JLabel("Cursor Point:", JLabel.TRAILING);
        panel_info.add(label_cursorPoint);

        text_cursorPoint = new JTextField("-");
        text_cursorPoint.setEditable(false);
        panel_info.add(text_cursorPoint);

        label_selectedPoint = new JLabel("Selected Point:", JLabel.TRAILING);
        panel_info.add(label_selectedPoint);

        text_selectedPoint = new JTextField("-");
        text_selectedPoint.setEditable(false);
        panel_info.add(text_selectedPoint);

        SpringUtilities.makeCompactGrid(panel_info, 4, 2, 6, 6, 6, 6);
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

    private void updateRangeDisplay() {
        // Wait for the thread to notify us that it has completed
        Complex minimum = mandel_drawer.getComplexFromPoint(0, 0);
        Complex maximum = mandel_drawer.getComplexFromPoint(imgPanel_image.getWidth(), imgPanel_image.getHeight());
        text_xRange.setText(String.format("%.3f to %.3f", minimum.getReal(), maximum.getReal()));
        text_yRange.setText(String.format("%.3f to %.3f", minimum.getImaginary(), maximum.getImaginary()));
    }

    private void updatedSelectedPoint() {
        text_selectedPoint.setText("-");
    }

    private void updatedSelectedPoint(Complex c) {
        text_selectedPoint.setText(c.toString());
    }

    private void updatedCursorPoint() {
        text_cursorPoint.setText("-");
    }

    private void updatedCursorPoint(Complex c) {
        text_cursorPoint.setText(c.toString());
    }

    /**
     * Triggers redraw of mandelbrot set
     */
    private class redrawHandler implements ActionListener, DrawListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            mandel_drawer.draw();
        }

        @Override
        public void drawComplete() {
            updateRangeDisplay();
        }
    }

    /**
     * Updates diplay of selected position when mouse clicked
     */
    private class mouseClickPositionHandler extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent e) {
            if (mandel_drawer.hasDrawn()) {
                selectedPosition = mandel_drawer.getComplexFromPoint(e.getPoint());
                updatedSelectedPoint(selectedPosition);
                julia_drawer.draw(selectedPosition);
            } else {
                updatedSelectedPoint();
            }
        }
    }

    /**
     * Updates display of mouse cursor when mouse moved
     */
    private class mousePositionHandler extends MouseMotionAdapter {

        @Override
        public void mouseMoved(MouseEvent e) {
            super.mouseMoved(e);
            if (mandel_drawer.hasDrawn()) {
                updatedCursorPoint(mandel_drawer.getComplexFromPoint(e.getPoint()));
            } else {
                updatedCursorPoint();
            }
        }
    }

}