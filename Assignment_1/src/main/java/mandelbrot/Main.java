package mandelbrot;


import mandelbrot.events.AdvancedComponentAdapter;
import mandelbrot.events.ConfigChangeAdapter;
import mandelbrot.events.RenderListener;
import mandelbrot.management.JuliaRenderManagementThread;
import mandelbrot.management.MandelbrotRenderManagementThread;
import mandelbrot.management.OpenClThread;
import utils.*;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

/**
 * Main Application Window
 *
 * @author Huw Jones
 * @since 22/02/2016
 */
public class Main extends JFrameAdvanced {

    private static final double DISPLAY_CONSTRAINT = 0.9;

    // Managers
    private ConfigManager config;
    private BookmarkManager bookmarks;

    //region Main Panels
    private JPanel panel_display;
    private JPanel panel_info;
    private JPanel panel_controls;
    private JPanel panel_bookmarks;
    private JPanel panel_julia;
    //endregion

    // Management Threads
    private final OpenClThread openClThread;
    public final MandelbrotRenderManagementThread mandelbrotRenderer;
    public final JuliaRenderManagementThread juliaRenderer;

    // Image Panels
    private ImagePanel imgPanel_image;
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

    public Main() {
        super("Fractal Explorer");
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.setExtendedState(JFrame.MAXIMIZED_BOTH);
        this.setMinimumSize(new Dimension(1024, 768));

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {

        }

        // Create threads and objects
        openClThread = new OpenClThread();

        config = new ConfigManager(this);
        config.addConfigChangeListener(new configChangeHandler());

        // If OpenCL features aren't available, disable them
        if(!openClThread.isAvailable()) config.disableOpenCL();
        if(!openClThread.useDouble()) config.disableOpenCL_double();

        bookmarks = new BookmarkManager(this);

        // Create GUI
        initComponents();
        this.pack();

        mandelbrotRenderer = new MandelbrotRenderManagementThread(this, openClThread, imgPanel_image);
        mandelbrotRenderer.addRenderListener(new renderCompleteHandler());
        mandelbrotRenderer.start();

        juliaRenderer = new JuliaRenderManagementThread(this, openClThread, imgPanel_julia);
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
        panel_display.setMinimumSize(new Dimension(700, 700));
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

        // Adds listener for clicking
        imgPanel_image.addMouseListener(new mouseClickHandler());

        // Adds listener for mouse cursor position
        // Need to add to both events to get mouse exit + mouse move events
        imgPanel_image.addMouseListener(new mouseMotionHandler());
        imgPanel_image.addMouseMotionListener(new mouseMotionHandler());

        // Adds listener for zoom box/interaction with image
        // Need to add to both events to get mouse exit + mouse move events
        interactionHandler interactionHandler = new interactionHandler();
        imgPanel_image.addMouseListener(interactionHandler);
        imgPanel_image.addMouseMotionListener(interactionHandler);

        initSidePanels(constraints);
        initInfoPanel();
    }

    private void initSidePanels(GridBagConstraints c) {
        Container pane = this.getContentPane();
        c.weightx = 1 - DISPLAY_CONSTRAINT;
        c.gridx = 1;
        c.gridheight = 1;
        c.gridwidth = 1;
        c.weighty = 0.01;
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

        label_selectedPoint = new JLabel("Current Selection:", JLabel.TRAILING);
        panel_info.add(label_selectedPoint);

        text_selectedPoint = new JTextField("-");
        text_selectedPoint.setEditable(false);
        panel_info.add(text_selectedPoint);

        SpringUtilities.makeCompactGrid(panel_info, 4, 2, 6, 6, 6, 6);
    }

    //endregion

    //region Update Info Sections
    private void updateRangeDisplay() {
        Complex minimum = mandelbrotRenderer.getComplexFromPoint(0, 0);
        Complex maximum = mandelbrotRenderer.getComplexFromPoint(imgPanel_image.getWidth(), imgPanel_image.getHeight());
        // Dynamically scale precision to make the range clearer
        int decimalPlaces = 1;
        do {
            decimalPlaces++;
        } while(String.format("%." + decimalPlaces + "f", minimum.getReal()).equals(String.format("%." + decimalPlaces + "f", maximum.getReal())));

        text_xRange.setText(String.format("%."+ decimalPlaces +"f to %."+ decimalPlaces +"f", minimum.getReal(), maximum.getReal()));
        text_yRange.setText(String.format("%."+ decimalPlaces +"f to %."+ decimalPlaces +"f", minimum.getImaginary(), maximum.getImaginary()));
    }

    private void updateSelection() {
        //If selected point is null (not set), then display a -, otherwise display the complex
        String text = "-";
        if(config.getSelectedPoint() != null){
            text = config.getSelectedPoint().toString();
        }
        text_selectedPoint.setText(text);
    }

    private void updateSelection(Rectangle2D selection) {
        // If the selection is null (not set), display -, otherwise display the range
        String text = "-";

        if (selection != null) {
            // Top left of rectangle
            text = mandelbrotRenderer.getComplexFromPoint(selection.getMinX(), selection.getMinY()).toString();
            text += " to ";

            // Bottom right of rectangle
            text += mandelbrotRenderer.getComplexFromPoint(selection.getMaxX(), selection.getMaxY()).toString();
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
        mandelbrotRenderer.render();
    }

    public void renderJulia(){
        if(config.getSelectedPoint() == null) return;
        juliaRenderer.render();
    }
    //endregion

    //region Event Handlers

    /**
     * Invoked when JFrame is resized
     */
    private class resizeHandler extends AdvancedComponentAdapter {
        /**
         * Invoked when the component's size stops changing.
         *
         * @param e
         */
        @Override
        public void componentResizeEnd(ComponentEvent e) {
            Log.Information("Resize handled");
            renderMandelbrot();
            renderJulia();
        }
    }

    /**
     * Invoked when the config has changed
     */
    private class configChangeHandler extends ConfigChangeAdapter {
        @Override
        public void escapeRadiusSquaredChange(double escapeRadiusSquared) {
            renderMandelbrot();
            renderJulia();
        }

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

    /**
     * Invoked when the render has completed
     */
    private class renderCompleteHandler implements RenderListener {
        @Override
        public void renderComplete() {
            updateRangeDisplay();
        }
    }

    /**
     * Handles zoom interaction
     */
    private class interactionHandler extends MouseAdapter implements MouseMotionListener {
        private boolean dragging = false;
        private Point2D startPos;
        private Rectangle2D rectangle2D;

        @Override
        public void mousePressed(MouseEvent e) {
            if (SwingUtilities.isLeftMouseButton(e)) {
                startPos = e.getPoint();
                dragging = true;
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            updateSelection();
            imgPanel_image.drawZoomBox(null);

            // Make sure only the left mouse button was released
            if (e.getButton() != MouseEvent.BUTTON1 || rectangle2D == null) {
                rectangle2D = null;
                dragging = false;
                return;
            }

            // Prevent accidental zoom on click
            if (rectangle2D.getWidth() <= 10 && rectangle2D.getHeight() <= 10) return;

            // Get top left/bottom right panel coordinates
            double x1 = rectangle2D.getMinX(), y1 = rectangle2D.getMinY();
            double x2 = rectangle2D.getMaxX(), y2 = rectangle2D.getMaxY();

            // Get top left/bottom right complex coordinates
            Complex new1 = mandelbrotRenderer.getComplexFromPoint(x1, y1);
            Complex new2 = mandelbrotRenderer.getComplexFromPoint(x2, y2);

            // Get new image scale
            double scale = (4) / (new2.getReal() - new1.getReal());

            // Get new midpoints
            double new_midPointX = (new2.getReal() + new1.getReal()) / 2d;
            double new_midPointY = (new2.getImaginary() + new1.getImaginary()) / 2d;

            // Set config options, but don't trigger events (prevents multiple renders)
            config.setShiftX(new_midPointX, false);
            config.setShiftY(new_midPointY, false);
            config.setScaleFactor(scale, false);

            // Clear zoom flags
            rectangle2D = null;
            dragging = false;

            // Now render
            renderMandelbrot();
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            if (!dragging) return;

            // Updated zoom box and draw it
            rectangle2D = getRectangle(e.getPoint());
            imgPanel_image.drawZoomBox(rectangle2D);
            updateSelection(rectangle2D);
        }

        /**
         * Uses the startPos and parameter e to create the Rectangle2D Zoom Box to draw
         *
         * @param e MouseEvent e, with coordinates
         * @return Rectangle2D representing the zoom box to draw
         */
        private Rectangle2D getRectangle(Point2D e) {
            // QUADRANTS (where startPos = (0, 0))
            //  3 | 2
            // ---|---
            //  4 | 1

            double aspectRatio = (double) imgPanel_image.getWidth() / (double) imgPanel_image.getHeight();
            double width = Math.abs(e.getX() - startPos.getX());
            double height = Math.abs(e.getY() - startPos.getY());
            double x, y;

            // Ensure zoom box maintains same aspect ratio as image panel
            if (width / aspectRatio < height) {
                height = width / aspectRatio;
            } else {
                width = height * aspectRatio;
            }

            // Check which quadrant we are in
            // Then set the top left coordinate, whilst fixing one coordinate to the start position

            // Quadrant 1:
            if (startPos.getX() < e.getX() && startPos.getY() < e.getY()) {
                x = startPos.getX();
                y = startPos.getY();

                // Quadrant 2:
            } else if (startPos.getX() < e.getX() && startPos.getY() > e.getY()) {
                x = startPos.getX();
                y = startPos.getY() - height;

                // Quadrant 3:
            } else if (startPos.getX() > e.getX() && startPos.getY() > e.getY()) {
                x = startPos.getX() - width;
                y = startPos.getY() - height;

                // Quadrant 4:
            } else if (startPos.getX() > e.getX() && startPos.getY() < e.getY()) {
                x = startPos.getX() - width;
                y = startPos.getY();
            } else {

                // Width and/or height are 0, don't draw a box
                return null;
            }

            return new Rectangle2D.Double(x, y, width, height);
        }

    }

    //TODO: Add mouse wheel listener! Mouse wheel zoom in/out

    /**
     * Handles julia set click
     */
    private class mouseClickHandler extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent e) {
            if (SwingUtilities.isLeftMouseButton(e) && !SwingUtilities.isRightMouseButton(e)) {
                if (mandelbrotRenderer.hasRendered()) {
                    config.setSelectedPoint(mandelbrotRenderer.getComplexFromPoint(e.getPoint()));
                }
                updateSelection();
            }
        }
    }

    /**
     * Handles cursor position updating
     */
    private class mouseMotionHandler extends MouseAdapter implements MouseMotionListener {
        @Override
        public void mouseExited(MouseEvent e) {
            // If cursor exits panel, set coordinates to "-"
            updatedCursorPoint();
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            super.mouseMoved(e);
            if (mandelbrotRenderer.hasRendered()) {
                updatedCursorPoint(mandelbrotRenderer.getComplexFromPoint(e.getPoint()));
            } else {
                updatedCursorPoint();
            }

            // Render julia set if we are rendering when the cursor moves
            if(config.juliaDisplayOnMove()) {
                if (mandelbrotRenderer.hasRendered()) {
                    config.setSelectedPoint(mandelbrotRenderer.getComplexFromPoint(e.getPoint()));
                }
                updateSelection();
            }
        }

    }

    //endregion
}