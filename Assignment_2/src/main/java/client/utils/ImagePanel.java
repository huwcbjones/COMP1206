package client.utils;

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

    public ImagePanel() {
    }
    public ImagePanel(BufferedImage image){
        this.image = image;
    }

    public void setImage(BufferedImage image){
        this.setImage(image, false);
    }
    public void setImage (BufferedImage image, boolean repaint) {
        this.image = image;
        if (repaint) this.repaint();
    }
    @Override
    protected void paintComponent(Graphics g){
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D)g;
        g.drawImage(this.image, 0, 0, null);
    }
}
