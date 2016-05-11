package shared.utils;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Utilities for Images
 *
 * @author Huw Jones
 * @since 11/05/2016
 */
public class ImageUtils {

    public static BufferedImage getScaledImage(BufferedImage image, int width, int height){
        double scale = Math.min((double)width/image.getWidth(), (double)height/image.getHeight());
        Double newWidth = scale * image.getWidth();
        Double newHeight = scale * image.getHeight();
        Double xPos = ((double)width - newWidth)/2d;
        Double yPos = ((double)height - newHeight)/2d;

        Image tmp = image.getScaledInstance(newWidth.intValue(), newHeight.intValue(), Image.SCALE_SMOOTH);
        BufferedImage scaledImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = scaledImage.createGraphics();
        g2d.drawImage(tmp, xPos.intValue(), yPos.intValue(), null);
        g2d.dispose();

        return scaledImage;
    }
}
