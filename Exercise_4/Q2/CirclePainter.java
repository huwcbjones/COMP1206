import java.awt.*;
import java.awt.geom.Line2D;

/**
 * Utility class to pass multiple arguments as a method return value
 *
 * @author Huw Jones
 * @since 22/02/2016
 */
public class CirclePainter {

    private Graphics graphics;
    private Line2D line2D;

    public CirclePainter(Graphics graphics, Line2D l) {
        this.graphics = graphics;
        this.line2D = l;
    }

    public Graphics getGraphics() {
        return graphics;
    }

    public Line2D getLine2D() {
        return line2D;
    }
}
