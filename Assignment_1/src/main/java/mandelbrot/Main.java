package mandelbrot;


import mandelbrot.events.ConfigChangeAdapter;
import mandelbrot.events.RenderListener;
import mandelbrot.management.JuliaRenderManagementThread;
import mandelbrot.management.MandelbrotRenderManagementThread;
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

    private static final double DISPLAY_CONSTRAINT = 0.9;

    private ConfigManager config;

    //region Main Panels
    private JPanel panel_display;
    private ImagePanel imgPanel_image;
    private JPanel panel_info;
    private JPanel panel_controls;
    private JPanel panel_bookmarks;
    private JPanel panel_julia;

    //endregion

    private final MandelbrotRenderManagementThread mandel_drawer;
    private final JuliaRenderManagementThread julia_drawer;

    // Julia Set
    private ImagePanel imgPanel_julia;

    //region Info Properties
    private JLabel label_xRange;
    private JTextField text_xRange;

    private JLabel label_yRange;
    private JTextField text_yRange;

    private JLabel label_cursorPoint;
    private JTextField text_cursorPoint;

    private JLabel label_selectedPoint;
    private JTextField text_selectedPoint;

    private Complex selectedPosition;

    //endregion


    // Bookmarks


    public Main() {
        super("Fractal Explorer");
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.setExtendedState(JFrame.MAXIMIZED_BOTH);
        this.setMinimumSize(new Dimension(800, 600));
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {

        }

        config = new ConfigManager(this);
        config.addConfigChangeListener(new configChangeHandler());

        initComponents();

        this.pack();

        mandel_drawer = new MandelbrotRenderManagementThread(this, imgPanel_image);
        mandel_drawer.addDrawListenener(new renderCompleteHandler());
        mandel_drawer.start();

        julia_drawer = new JuliaRenderManagementThread(this, imgPanel_julia);
        julia_drawer.start();

        this.setVisible(true);
    }

    //region Initialise Components

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

        panel_controls = config.getConfigPanel();
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

    //endregion

    //region Get Config Options

    public double getShiftX() {
        return config.getShiftX();
    }

    public double getShiftY() {
        return config.getShiftY();
    }

    public int getIterations() {
        return config.getIterations();
    }

    public double getScaleFactor() {
        return config.getScaleFactor();
    }

    public float getTint() {
        return config.getTint();
    }
    //endregion

    //region Update Info Sections
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

    //endregion

    //region Render Handlers
    public void renderMandelbrot(){
        mandel_drawer.render();
    }

    public void renderJulia(){
        if(selectedPosition == null) return;
        julia_drawer.render(selectedPosition);
    }
    //endregion

    //region Event Handlers

    private class configChangeHandler extends ConfigChangeAdapter {
        @Override
        public void iterationChange(int iterations) {
            renderMandelbrot();
            renderJulia();
        }

        @Override
        public void xShiftChange(double xShift) {
            renderMandelbrot();
        }

        @Override
        public void yShiftChange(double yShift) {
            renderMandelbrot();
        }

        @Override
        public void scaleChange(double scale) {
            renderMandelbrot();
        }

        @Override
        public void colourShiftChange(double shift) {
            renderMandelbrot();
            renderJulia();
        }
    }

    private class renderCompleteHandler implements RenderListener {
        @Override
        public void renderComplete() {
            updateRangeDisplay();
        }
    }

    /**
     * Updates display of selected position when mouse clicked
     */
    private class mouseClickPositionHandler extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent e) {
            if (mandel_drawer.hasRendered()) {
                selectedPosition = mandel_drawer.getComplexFromPoint(e.getPoint());
                updatedSelectedPoint(selectedPosition);
                julia_drawer.render(selectedPosition);
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
            if (mandel_drawer.hasRendered()) {
                updatedCursorPoint(mandel_drawer.getComplexFromPoint(e.getPoint()));
            } else {
                updatedCursorPoint();
            }
        }
    }

    //endregion
}