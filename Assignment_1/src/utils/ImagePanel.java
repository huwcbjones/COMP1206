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

    private BufferedImage tint;
    private BufferedImage image;
    private boolean paintTintedImage = false;

    public ImagePanel() {
    }
    public ImagePanel(BufferedImage image){
        this.image = image;
    }

    public void setImage (BufferedImage image) {
        this.image = image;
        this.tint = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = this.tint.createGraphics();
        g.drawImage(image, 0, 0, null);
        g.dispose();

        paintTintedImage = false;
        repaint();
    }
    @Override
    protected void paintComponent(Graphics g){
        super.paintComponent(g);
        if(paintTintedImage) {
            g.drawImage(tint, 0, 0, null);
        } else {
            g.drawImage(image, 0, 0, null);
        }
    }

    public BufferedImage createImage() {
        return new BufferedImage(this.getWidth(), this.getHeight(), BufferedImage.TYPE_INT_RGB);
    }

    // TODO: Multi-thread this
    public void tintImage(double factor) {
        tintImage(Double.valueOf(factor).floatValue());
    }
    public void tintImage(float factor) {
        if(image == null) return;

        Color c;
        float[] hsb = new float[3];
        for(int y = 0; y < image.getHeight(); y++){
            for(int x = 0; x < image.getWidth(); x++) {
                c = new Color(image.getRGB(x, y));
                Color.RGBtoHSB(c.getRed(), c.getBlue(), c.getGreen(), hsb);
                tint.setRGB(x, y, Color.HSBtoRGB(hsb[0] + factor, hsb[1], hsb[2]));
            }
        }
        paintTintedImage = true;
        repaint();
    }
}
