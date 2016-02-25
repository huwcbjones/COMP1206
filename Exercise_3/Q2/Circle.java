import java.awt.geom.Ellipse2D;

/**
 * {DESCRIPTION}
 *
 * @author Huw Jones
 * @since 24/02/2016
 */
public class Circle extends Shape {

    public Circle(float x, float y, float w, float h){
        shape = new Ellipse2D.Float(x, y, w, h);
    }

}
