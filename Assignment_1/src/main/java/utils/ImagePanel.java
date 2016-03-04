package utils;

import javax.swing.*;
import javax.swing.border.StrokeBorder;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

/**
 * Loads image into JPanel
 *
 * @author Huw Jones
 * @since 20/11/2015
 */
public class ImagePanel extends JPanel {

    private BufferedImage image;
    private Rectangle2D zoomBox;

    public ImagePanel() {
    }
    public ImagePanel(BufferedImage image){
        this.image = image;
    }

    public void setImage(BufferedImage image){
        setImage(image, false);
    }
    public void setImage (BufferedImage image, boolean repaint) {
        this.image = image;
        if (repaint) repaint();
    }
    @Override
    protected void paintComponent(Graphics g){
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D)g;
        g.drawImage(image, 0, 0, null);

        if(zoomBox == null) return;

        g2d.setColor(new Color(0, 0, 255, 96));
        g2d.fill(zoomBox);

        g2d.setColor(new Color(0, 0, 255, 48));
        g2d.draw(zoomBox);
    }

    public BufferedImage createImage() {
        return new BufferedImage(this.getWidth(), this.getHeight(), BufferedImage.TYPE_INT_RGB);
    }

    public void drawZoomBox(Rectangle2D box) {
        zoomBox = box;
        repaint();
    }
}
