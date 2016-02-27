package utils;

import javax.swing.*;
import java.awt.*;
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

    public void setImage (BufferedImage image) {
        this.image = image;
        repaint();
    }
    @Override
    protected void paintComponent(Graphics g){
        super.paintComponent(g);
        g.drawImage(image, 0, 0, null);
    }
}
