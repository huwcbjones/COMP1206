import javax.swing.*;
import java.awt.*;
import java.awt.geom.Point2D;

/**
 * A dice face
 *
 * @author Huw Jones
 * @since 22/02/2016
 */
public class Dice extends JPanel {

    private static final double DOT_MARGIN = 0.05d;
    private static final double DOT_DIAMETER = (1 / 3d) - 2 * DOT_MARGIN;
    private int value = 0;

    public Dice() {
        this.setMinimumSize(new Dimension(60, 60));
    }

    public void updateVal(int i) throws IllegalArgumentException {
        if (i < 0 || i > 6) throw new IllegalArgumentException("Dice value must be in range 1-6");
        this.value = i;
        repaint();
    }

    public int getValue() {
        return value;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        paintFace(g);
    }

    private void paintFace(Graphics g) {
        clearPanel(g);
        switch (value) {
            case 1:
                draw_1(g);
                break;
            case 2:
                draw_2(g);
                break;
            case 3:
                draw_3(g);
                break;
            case 4:
                draw_4(g);
                break;
            case 5:
                draw_5(g);
                break;
            case 6:
                draw_6(g);
                break;
        }
    }

    private void clearPanel(Graphics g) {
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, this.getWidth(), this.getHeight());
    }

    private void paintCircle(Graphics g, Point2D p) {
        int r = Double.valueOf(DOT_DIAMETER * this.getWidth()).intValue();
        g.setColor(Color.BLACK);
        g.fillOval(Double.valueOf(p.getX()).intValue(), Double.valueOf(p.getY()).intValue(), r, r);
    }

    private void drawMiddleLeftDot(Graphics g) {
        Point2D pos = new Point2D.Double(0, this.getHeight() / 3d);
        paintCircle(g, applyMargin(pos));
    }

    private void drawMiddleRightDot(Graphics g) {
        Point2D pos = new Point2D.Double(this.getWidth() * 2 / 3d, this.getHeight() / 3);
        paintCircle(g, applyMargin(pos));
    }

    private void drawBottomLeftDot(Graphics g) {
        Point2D pos = new Point2D.Double(0, this.getHeight() * 2 / 3d);
        paintCircle(g, applyMargin(pos));
    }

    private void drawBottomRightDot(Graphics g) {
        Point2D pos = new Point2D.Double(this.getWidth() * 2 / 3d, this.getHeight() * 2 / 3d);
        paintCircle(g, applyMargin(pos));
    }

    private void drawMiddleDot(Graphics g) {
        Point2D pos = new Point2D.Double(this.getWidth() / 3d, this.getHeight() / 3d);
        paintCircle(g, applyMargin(pos));
    }

    private void drawTopRightDot(Graphics g) {
        Point2D pos = new Point2D.Double(this.getWidth() * 2 / 3d, 0);
        paintCircle(g, applyMargin(pos));
    }

    private void drawTopLeftDot(Graphics g) {
        Point2D pos = new Point2D.Double(0, 0);
        paintCircle(g, applyMargin(pos));
    }

    /**
     * Applies the margin to the start point of shape
     *
     * @param position Position to apply margin
     * @return New point
     */
    private Point2D applyMargin(Point2D position) {
        double newX = this.getWidth() * DOT_MARGIN + position.getX();
        double newY = this.getHeight() * DOT_MARGIN + position.getY();
        return new Point2D.Double(newX, newY);
    }


    private void draw_1(Graphics g) {
        drawMiddleDot(g);
    }

    private void draw_2(Graphics g) {
        drawTopRightDot(g);
        drawBottomLeftDot(g);
    }

    private void draw_3(Graphics g) {
        drawTopRightDot(g);
        drawMiddleDot(g);
        drawBottomLeftDot(g);
    }

    private void draw_4(Graphics g) {
        drawTopLeftDot(g);
        drawTopRightDot(g);
        drawBottomLeftDot(g);
        drawBottomRightDot(g);
    }

    private void draw_5(Graphics g) {
        draw_4(g);
        drawMiddleDot(g);
    }

    private void draw_6(Graphics g) {
        draw_4(g);
        drawMiddleLeftDot(g);
        drawMiddleRightDot(g);
    }
}
