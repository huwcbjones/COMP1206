package shared.components;

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
        this.setBackground(Color.WHITE);
    }
    public ImagePanel(BufferedImage image){
        super();
        this.image = image;
    }

    public void setImage(BufferedImage image){
        this.setImage(image, false);
    }
    public void setImage (BufferedImage image, boolean repaint) {
        this.image = image;
        this.setPreferredSize(new Dimension(image.getWidth(), image.getHeight()));
        if (repaint) this.repaint();
    }
    @Override
    protected void paintComponent(Graphics g){
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D)g;
        g2d.drawImage(this.image, 0, 0, null);
    }
}
