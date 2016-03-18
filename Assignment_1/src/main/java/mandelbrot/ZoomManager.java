package mandelbrot;

import utils.Complex;
import utils.ImagePanel;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

/**
 * Zoom Manager
 *
 * @author Huw Jones
 * @since 14/03/2016
 */
public class ZoomManager extends MouseAdapter implements MouseMotionListener {

    private Main mainWindow;
    private ConfigManager config;
    private ImagePanel panel;
    private boolean dragging = false;
    private Point2D startPos;
    private Rectangle2D rectangle2D;

    public ZoomManager(Main mainWindow, ImagePanel panel) {
        this.mainWindow = mainWindow;
        this.config = mainWindow.getConfigManager();
        this.panel = panel;
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e)) {
            this.startPos = e.getPoint();
            this.dragging = true;
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        this.mainWindow.updateSelection();
        this.panel.drawZoomBox(null);

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
        double newScale = this.config.getRangeX() / (new2.getReal() - new1.getReal());

        // Get new midpoints
        double newShiftX = (new2.getReal() + new1.getReal()) / 2d;
        double newShiftY = (new2.getImaginary() + new1.getImaginary()) / 2d;

        AnimationRunner runner = new AnimationRunner(newScale, newShiftX, newShiftY);

        runner.start();
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (!this.dragging) return;

        // Updated zoom box and draw it
        this.rectangle2D = this.getRectangle(e.getPoint());
        this.panel.drawZoomBox(this.rectangle2D);
        this.mainWindow.updateSelection(this.rectangle2D);
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

        double aspectRatio = (double) this.panel.getWidth() / (double) this.panel.getHeight();
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
     *
     * @param x x coord
     * @param y y coord
     * @return new complex
     */
    private Complex getComplex(double x, double y) {
        return this.mainWindow.getCurrentFractal().getComplexFromPoint(x, y);
    }

    private class AnimationRunner extends Thread{

        private double newScale;
        private double newShiftX;
        private double newShiftY;

        public AnimationRunner(double newScale, double newShiftX, double newShiftY){
            super("Zoom_Animation_Runner");
            this.newScale = newScale;
            this.newShiftX = newShiftX;
            this.newShiftY = newShiftY;
        }

        @Override
        public void run() {
            if(ZoomManager.this.config.isAnimateZoom()) {
                int numberOfSteps = 60;
                double startScale = ZoomManager.this.config.getScaleFactor();
                double deltaScale = ( newScale - startScale ) / numberOfSteps;

                double startShiftX = ZoomManager.this.config.getShiftX();
                double deltaShiftX = ( newShiftX - startShiftX ) / numberOfSteps;

                double startShiftY = ZoomManager.this.config.getShiftY();
                double deltaShiftY = ( newShiftY - startShiftY ) / numberOfSteps;

                for (int s = 0; s < 60; s++) {
                    // Set config options after we're done
                    // Set config options, but don't trigger events (prevents multiple renders)
                    ZoomManager.this.config.setScaleFactor(startScale + s * deltaScale, false);
                    ZoomManager.this.config.setShiftX(startShiftX + s * deltaShiftX, false);
                    ZoomManager.this.config.setShiftY(startShiftY + s * deltaShiftY, false);

                    ZoomManager.this.mainWindow.renderMainPanel();
               /* try {
                    Thread.sleep(16);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }*/
                }
                return;
            }

            // Set config options after we're done
            // Set config options, but don't trigger events (prevents multiple renders)
            ZoomManager.this.config.setShiftX(this.newShiftX, false);
            ZoomManager.this.config.setShiftY(this.newShiftY, false);
            ZoomManager.this.config.setScaleFactor(this.newScale, false);

            // Clear zoom flags
            ZoomManager.this.rectangle2D = null;
            ZoomManager.this.dragging = false;

            // Now render
            ZoomManager.this.mainWindow.renderMainPanel();
        }
    }

}
