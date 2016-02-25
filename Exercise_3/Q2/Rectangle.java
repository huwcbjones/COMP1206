import java.awt.geom.Rectangle2D;

/**
 * A Rectangle
 *
 * @author Huw Jones
 * @since 25/02/2016
 */
public class Rectangle  extends Shape {

    public Rectangle(float x, float y, float w, float h){
        shape = new Rectangle2D.Float(x, y, w, h);
    }

}
