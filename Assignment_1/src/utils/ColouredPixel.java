package utils;

import java.awt.*;
import java.awt.geom.Point2D;

/**
 * Represents a coloured Pixel
 *
 * @author Huw Jones
 * @since 28/02/2016
 */
public class ColouredPixel {

    private Color colour;
    private Point2D point;


    public ColouredPixel(Point2D point, Color colour) {
        this.colour = colour;
        this.point = point;
    }

    public Color getColour() {
        return colour;
    }

    public void setColour(Color colour) {
        this.colour = colour;
    }

    public Point2D getPoint() {
        return point;
    }

    public void setPoint(Point2D point) {
        this.point = point;
    }
}
