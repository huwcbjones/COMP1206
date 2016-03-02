package mandelbrot;


import mandelbrot.events.AdvancedComponentAdapter;
import mandelbrot.events.ConfigChangeAdapter;
import mandelbrot.events.RenderListener;
import mandelbrot.management.JuliaRenderManagementThread;
import mandelbrot.management.MandelbrotRenderManagementThread;
import mandelbrot.management.OpenClRenderThread;
import utils.Complex;
import utils.ImagePanel;
import utils.JFrameAdvanced;
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
public class Main extends JFrameAdvanced {

    private static final double DISPLAY_CONSTRAINT = 0.9;

    private ConfigManager config;
    private BookmarkManager bookmarks;

    //region Main Panels
    private JPanel panel_display;
    private ImagePanel imgPanel_image;
    private JPanel panel_info;
    private JPanel panel_controls;
    private JPanel panel_bookmarks;
    private JPanel panel_julia;

    //endregion

    private final OpenClRenderThread openClRenderThread;
    private final MandelbrotRenderManagementThread mandel_drawer;
    private final JuliaRenderManagementThread juliaRenderer;

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

    //endregion


    // Bookmarks


    public Main() {
        super("Fractal Explorer");
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.setExtendedState(JFrame.MAXIMIZED_BOTH);
        this.setMinimumSize(new Dimension(1024, 768));
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {

        }

        openClRenderThread = new OpenClRenderThread();

        config = new ConfigManager(this);
        config.addConfigChangeListener(new configChangeHandler());

        bookmarks = new BookmarkManager(this);

        initComponents();

        this.pack();

        mandel_drawer = new MandelbrotRenderManagementThread(this, openClRenderThread, imgPanel_image);
        mandel_drawer.addDrawListenener(new renderCompleteHandler());
        mandel_drawer.start();

        juliaRenderer = new JuliaRenderManagementThread(this, openClRenderThread, imgPanel_julia);
        juliaRenderer.start();

        this.setVisible(true);
        this.addAdvancedComponentListener(new resizeHandler());
    }

    public ConfigManager getConfigManager(){
        return config;
    }

    //region Initialise Components

    private void initComponents(){
        Container c = this.getContentPane();
        c.setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.BOTH;

        panel_display = new JPanel();
        panel_display.setLayout(new BorderLayout());
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
        panel_display.add(imgPanel_image, BorderLayout.CENTER);
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

        panel_bookmarks = bookmarks.getBookmarkPanel();
        panel_bookmarks.setOpaque(false);
        c.gridy = 2;
        pane.add(panel_bookmarks, c);

        BorderLayout juliaLayout = new BorderLayout();
        juliaLayout.setHgap(3);
        juliaLayout.setVgap(3);
        panel_julia = new JPanel(juliaLayout);
        panel_julia.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Julia Set"));
        c.weighty = 1;
        c.gridy = 3;
        pane.add(panel_julia, c);


        imgPanel_julia = new ImagePanel();
        imgPanel_julia.setBackground(Color.WHITE);
        panel_julia.add(imgPanel_julia, BorderLayout.CENTER);
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

    //region Update Info Sections
    private void updateRangeDisplay() {
        // Wait for the thread to notify us that it has completed
        Complex minimum = mandel_drawer.getComplexFromPoint(0, 0);
        Complex maximum = mandel_drawer.getComplexFromPoint(imgPanel_image.getWidth(), imgPanel_image.getHeight());
        text_xRange.setText(String.format("%.3f to %.3f", minimum.getReal(), maximum.getReal()));
        text_yRange.setText(String.format("%.3f to %.3f", minimum.getImaginary(), maximum.getImaginary()));
    }

    private void updatedSelectedPoint() {
        String text = "-";
        if(config.getSelectedPoint() != null){
            text = config.getSelectedPoint().toString();
        }
        text_selectedPoint.setText(text);
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
        if(config.getSelectedPoint() == null) return;
        juliaRenderer.render();
    }
    //endregion

    //region Event Handlers

    private class resizeHandler extends AdvancedComponentAdapter {
        /**
         * Invoked when the component's size stops changing.
         *
         * @param e
         */
        @Override
        public void componentResizeEnd(ComponentEvent e) {
            renderMandelbrot();
            renderJulia();
        }
    }

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
        public void colourChange(float shift, float saturation, float brightness) {
            renderMandelbrot();
            renderJulia();
        }

        @Override
        public void selectedPointChange(Complex complex) {
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
                config.setSelectedPoint(mandel_drawer.getComplexFromPoint(e.getPoint()));
            }
            updatedSelectedPoint();
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