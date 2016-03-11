package mandelbrot;


import mandelbrot.events.AdvancedComponentAdapter;
import mandelbrot.events.ConfigChangeAdapter;
import mandelbrot.events.RenderListener;
import mandelbrot.management.*;
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
    public final BurningShipManagementThread burningShipRenderer;

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
        this.openClThread = new OpenClThread();

        this.config = new ConfigManager(this);
        this.config.addConfigChangeListener(new configChangeHandler());

        this.config.addFractal("Mandelbrot");
        this.config.addFractal("Burning Ship");
        this.config.setFractal("Mandelbrot");

        // If OpenCL features aren't available, disable them
        if(!this.openClThread.isAvailable()) this.config.disableOpenCL();
        if(!this.openClThread.useDouble()) this.config.disableOpenCL_double();

        this.bookmarks = new BookmarkManager(this);

        // Create GUI
        this.initComponents();
        this.pack();

        this.mandelbrotRenderer = new MandelbrotRenderManagementThread(this, this.openClThread, this.imgPanel_image);
        this.mandelbrotRenderer.addRenderListener(new renderCompleteHandler());
        this.mandelbrotRenderer.start();

        this.burningShipRenderer = new BurningShipManagementThread(this, this.openClThread, this.imgPanel_image);
        this.burningShipRenderer.addRenderListener(new renderCompleteHandler());
        this.burningShipRenderer.start();

        this.juliaRenderer = new JuliaRenderManagementThread(this, this.openClThread, this.imgPanel_julia);
        this.juliaRenderer.start();

        this.setVisible(true);
        this.addAdvancedComponentListener(new resizeHandler());
    }

    public ConfigManager getConfigManager(){
        return this.config;
    }

    //region Initialise Components

    private void initComponents(){
        Container c = this.getContentPane();
        c.setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.BOTH;

        this.panel_display = new JPanel();
        this.panel_display.setLayout(new BorderLayout());
        this.panel_display.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Mandelbrot Set"));
        this.panel_display.setMinimumSize(new Dimension(700, 700));
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.weightx = DISPLAY_CONSTRAINT;
        constraints.weighty = DISPLAY_CONSTRAINT;
        constraints.gridheight = 4;
        constraints.gridwidth = 1;
        c.add(this.panel_display, constraints);

        this.imgPanel_image = new ImagePanel();
        this.imgPanel_image.setBackground(Color.WHITE);
        this.panel_display.add(this.imgPanel_image, BorderLayout.CENTER);

        // Adds listener for clicking
        this.imgPanel_image.addMouseListener(new mouseClickHandler());

        // Adds listener for mouse cursor position
        // Need to add to both events to get mouse exit + mouse move events
        this.imgPanel_image.addMouseListener(new mouseMotionHandler());
        this.imgPanel_image.addMouseMotionListener(new mouseMotionHandler());

        // Adds listener for zoom box/interaction with image
        // Need to add to both events to get mouse exit + mouse move events
        interactionHandler interactionHandler = new interactionHandler();
        this.imgPanel_image.addMouseListener(interactionHandler);
        this.imgPanel_image.addMouseMotionListener(interactionHandler);

        this.initSidePanels(constraints);
        this.initInfoPanel();
    }

    private void initSidePanels(GridBagConstraints c) {
        Container pane = this.getContentPane();
        c.weightx = 1 - DISPLAY_CONSTRAINT;
        c.gridx = 1;
        c.gridheight = 1;
        c.gridwidth = 1;
        c.weighty = 0.01;
        c.fill = GridBagConstraints.HORIZONTAL;

        this.panel_info = new JPanel();
        this.panel_info.setOpaque(false);
        c.gridy = 0;
        pane.add(this.panel_info, c);

        this.panel_controls = this.config.getConfigPanel();
        this.panel_controls.setOpaque(false);
        c.gridy = 1;
        pane.add(this.panel_controls, c);

        c.fill = GridBagConstraints.BOTH;

        this.panel_bookmarks = this.bookmarks.getBookmarkPanel();
        this.panel_bookmarks.setOpaque(false);
        c.gridy = 2;
        pane.add(this.panel_bookmarks, c);

        BorderLayout juliaLayout = new BorderLayout();
        this.panel_julia = new JPanel(juliaLayout);
        this.panel_julia.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Julia Set"));
        c.weighty = 1;
        c.gridy = 3;
        pane.add(this.panel_julia, c);


        this.imgPanel_julia = new ImagePanel();
        this.imgPanel_julia.setBackground(Color.WHITE);
        this.panel_julia.add(this.imgPanel_julia, BorderLayout.CENTER);
    }

    private void initInfoPanel() {
        SpringLayout layout = new SpringLayout();
        this.panel_info.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Information"));
        this.panel_info.setLayout(layout);

        this.label_xRange = new JLabel("X Range:", JLabel.TRAILING);
        this.panel_info.add(this.label_xRange);

        this.text_xRange = new JTextField("- to -");
        this.text_xRange.setEditable(false);
        this.panel_info.add(this.text_xRange);

        this.label_yRange = new JLabel("Y Range:", JLabel.TRAILING);
        this.panel_info.add(this.label_yRange);

        this.text_yRange = new JTextField("- to -");
        this.text_yRange.setEditable(false);
        this.panel_info.add(this.text_yRange);

        this.label_cursorPoint = new JLabel("Cursor Point:", JLabel.TRAILING);
        this.panel_info.add(this.label_cursorPoint);

        this.text_cursorPoint = new JTextField("-");
        this.text_cursorPoint.setEditable(false);
        this.panel_info.add(this.text_cursorPoint);

        this.label_selectedPoint = new JLabel("Current Selection:", JLabel.TRAILING);
        this.panel_info.add(this.label_selectedPoint);

        this.text_selectedPoint = new JTextField("-");
        this.text_selectedPoint.setEditable(false);
        this.panel_info.add(this.text_selectedPoint);

        SpringUtilities.makeCompactGrid(this.panel_info, 4, 2, 6, 6, 6, 6);
    }

    //endregion

    private RenderManagementThread getCurrentFractal() throws IllegalStateException {
        RenderManagementThread t;
        switch(this.config.getFractal()){
            case "Mandelbrot":
                t = this.mandelbrotRenderer;
                break;
            case "Burning Ship":
                t = this.burningShipRenderer;
                break;
            default:
                throw new IllegalStateException();
        }
        return t;
    }


    //region Update Info Sections
    private void updateRangeDisplay() {
        Complex minimum = this.getCurrentFractal().getComplexFromPoint(0, 0);
        Complex maximum = this.getCurrentFractal().getComplexFromPoint(this.imgPanel_image.getWidth(), this.imgPanel_image.getHeight());
        // Dynamically scale precision to make the range clearer
        int decimalPlaces = 1;
        do {
            decimalPlaces++;
        } while(String.format("%." + decimalPlaces + "f", minimum.getReal()).equals(String.format("%." + decimalPlaces + "f", maximum.getReal())));

        this.text_xRange.setText(String.format("%."+ decimalPlaces +"f to %."+ decimalPlaces +"f", minimum.getReal(), maximum.getReal()));
        this.text_yRange.setText(String.format("%."+ decimalPlaces +"f to %."+ decimalPlaces +"f", minimum.getImaginary(), maximum.getImaginary()));
    }

    private void updateSelection() {
        //If selected point is null (not set), then display a -, otherwise display the complex
        String text = "-";
        if(this.config.getSelectedPoint() != null){
            text = this.config.getSelectedPoint().toString();
        }
        this.text_selectedPoint.setText(text);
    }

    private void updateSelection(Rectangle2D selection) {
        // If the selection is null (not set), display -, otherwise display the range
        String text = "-";

        if (selection != null) {
            // Top left of rectangle
            text = this.mandelbrotRenderer.getComplexFromPoint(selection.getMinX(), selection.getMinY()).toString();
            text += " to ";

            // Bottom right of rectangle
            text += this.mandelbrotRenderer.getComplexFromPoint(selection.getMaxX(), selection.getMaxY()).toString();
        }
        this.text_selectedPoint.setText(text);
    }

    private void updatedCursorPoint() {
        this.text_cursorPoint.setText("-");
    }

    private void updatedCursorPoint(Complex c) {
        this.text_cursorPoint.setText(c.toString());
    }

    //endregion

    //region Render Handlers
    public void renderMainPanel(){
        this.getCurrentFractal().render();
    }

    public void renderJulia(){
        if(this.config.getSelectedPoint() == null) return;
        this.juliaRenderer.render();
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
            Main.this.renderMainPanel();
            Main.this.renderJulia();
        }
    }

    /**
     * Invoked when the config has changed
     */
    private class configChangeHandler extends ConfigChangeAdapter {
        @Override
        public void fractalChange(String fractal) {
            if (
                    Main.this.mandelbrotRenderer == null ||
                            Main.this.burningShipRenderer == null
            ) return;
            Main.this.renderMainPanel();
        }

        @Override
        public void escapeRadiusSquaredChange(double escapeRadiusSquared) {
            Main.this.renderMainPanel();
            Main.this.renderJulia();
        }

        @Override
        public void iterationChange(int iterations) {
            Main.this.renderMainPanel();
            Main.this.renderJulia();
        }

        @Override
        public void xShiftChange(double xShift) {
            Main.this.renderMainPanel();
        }

        @Override
        public void yShiftChange(double yShift) {
            Main.this.renderMainPanel();
        }

        @Override
        public void scaleChange(double scale) {
            Main.this.renderMainPanel();
        }

        @Override
        public void colourChange(float shift, float saturation, float brightness) {
            Main.this.renderMainPanel();
            Main.this.renderJulia();
        }

        @Override
        public void selectedPointChange(Complex complex) {
            Main.this.renderJulia();
        }
    }

    /**
     * Invoked when the render has completed
     */
    private class renderCompleteHandler implements RenderListener {
        @Override
        public void renderComplete() {
            Main.this.updateRangeDisplay();
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
                this.startPos = e.getPoint();
                this.dragging = true;
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            Main.this.updateSelection();
            Main.this.imgPanel_image.drawZoomBox(null);

            // Make sure only the left mouse button was released
            if (e.getButton() != MouseEvent.BUTTON1 || this.rectangle2D == null) {
                this.rectangle2D = null;
                this.dragging = false;
                return;
            }

            // Prevent accidental zoom on click
            if (this.rectangle2D.getWidth() <= 10 && this.rectangle2D.getHeight() <= 10) return;

            // Get top left/bottom right panel coordinates
            double x1 = this.rectangle2D.getMinX(), y1 = this.rectangle2D.getMinY();
            double x2 = this.rectangle2D.getMaxX(), y2 = this.rectangle2D.getMaxY();

            // Get top left/bottom right complex coordinates
            Complex new1 = this.getComplex(x1, y1);
            Complex new2 = this.getComplex(x2, y2);

            // Get new image scale
            double scale = Main.this.config.getRangeX() / (new2.getReal() - new1.getReal());

            // Get new midpoints
            double new_midPointX = (new2.getReal() + new1.getReal()) / 2d;
            double new_midPointY = (new2.getImaginary() + new1.getImaginary()) / 2d;

            // Set config options, but don't trigger events (prevents multiple renders)
            Main.this.config.setShiftX(new_midPointX, false);
            Main.this.config.setShiftY(new_midPointY, false);
            Main.this.config.setScaleFactor(scale, false);

            // Clear zoom flags
            this.rectangle2D = null;
            this.dragging = false;

            // Now render
            Main.this.renderMainPanel();
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            if (!this.dragging) return;

            // Updated zoom box and draw it
            this.rectangle2D = this.getRectangle(e.getPoint());
            Main.this.imgPanel_image.drawZoomBox(this.rectangle2D);
            Main.this.updateSelection(this.rectangle2D);
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

            double aspectRatio = (double) Main.this.imgPanel_image.getWidth() / (double) Main.this.imgPanel_image.getHeight();
            double width = Math.abs(e.getX() - this.startPos.getX());
            double height = Math.abs(e.getY() - this.startPos.getY());
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
            if (this.startPos.getX() < e.getX() && this.startPos.getY() < e.getY()) {
                x = this.startPos.getX();
                y = this.startPos.getY();

                // Quadrant 2:
            } else if (this.startPos.getX() < e.getX() && this.startPos.getY() > e.getY()) {
                x = this.startPos.getX();
                y = this.startPos.getY() - height;

                // Quadrant 3:
            } else if (this.startPos.getX() > e.getX() && this.startPos.getY() > e.getY()) {
                x = this.startPos.getX() - width;
                y = this.startPos.getY() - height;

                // Quadrant 4:
            } else if (this.startPos.getX() > e.getX() && this.startPos.getY() < e.getY()) {
                x = this.startPos.getX() - width;
                y = this.startPos.getY();
            } else {

                // Width and/or height are 0, don't draw a box
                return null;
            }

            return new Rectangle2D.Double(x, y, width, height);
        }

        /**
         * Gets the complex position from the relative render thread
         * @param x x coord
         * @param y y coord
         * @return new complex
         */
        private Complex getComplex(double x, double y){
            Complex newComplex = null;
            switch(Main.this.config.getFractal()){
                case "Mandelbrot":
                    newComplex = Main.this.mandelbrotRenderer.getComplexFromPoint(x, y);
                    break;
                case "Burning Ship":
                    newComplex = Main.this.burningShipRenderer.getComplexFromPoint(x, y);
                    break;
            }
            return newComplex;
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
                if (Main.this.mandelbrotRenderer.hasRendered()) {
                    Main.this.config.setSelectedPoint(Main.this.mandelbrotRenderer.getComplexFromPoint(e.getPoint()));
                }
                Main.this.updateSelection();
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
            Main.this.updatedCursorPoint();
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            super.mouseMoved(e);
            if (Main.this.mandelbrotRenderer.hasRendered()) {
                Main.this.updatedCursorPoint(Main.this.mandelbrotRenderer.getComplexFromPoint(e.getPoint()));
            } else {
                Main.this.updatedCursorPoint();
            }

            // Render julia set if we are rendering when the cursor moves
            if(Main.this.config.juliaDisplayOnMove()) {
                if (Main.this.mandelbrotRenderer.hasRendered()) {
                    Main.this.config.setSelectedPoint(Main.this.mandelbrotRenderer.getComplexFromPoint(e.getPoint()));
                }
                Main.this.updateSelection();
            }
        }

    }

    //endregion
}