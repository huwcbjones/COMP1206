import javax.swing.*;
import java.awt.*;
import java.awt.geom.Line2D;

/**
 * Draws circles on a panel
 *
 * @author Huw Jones
 * @since 22/02/2016
 */
public class CirclePanel extends JPanel {
    private int maxIterations = 0;

    public void repaint(int iterations) {
        this.maxIterations = iterations;
        super.repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        float height = this.getHeight() / 2;
        Line2D line = new Line2D.Float(0, height, this.getWidth(), height);
        paintLine(0, new CirclePainter(g, line));
    }

    private void paintLine(int iteration, CirclePainter painter) {
        if (iteration >= maxIterations) {
            return;
        }

        Graphics2D g = (Graphics2D) painter.getGraphics();
        Line2D line = painter.getLine2D();

        int diameter = Double.valueOf(LineUtils.getLineLength(line) / 3d).intValue();
        int radius = Double.valueOf(0.5 * diameter).intValue();
        int midPoint = LineUtils.getXPercentile(line, 0.5d).intValue();

        int x = Double.valueOf(midPoint - radius).intValue();
        int y = Double.valueOf(line.getY1() - radius).intValue();

        g.setColor(getColour(iteration));
        g.fillOval(x, y, diameter, diameter);

        if (iteration + 1 >= maxIterations) {
            return;
        } else {
            iteration++;
        }

        Line2D leftLine = new Line2D.Double(line.getX1(), line.getY1(), LineUtils.getXPercentile(line, 1/3d), line.getY2());
        Line2D centreLine = new Line2D.Double(LineUtils.getXPercentile(line, 1/3d), line.getY1(), LineUtils.getXPercentile(line, 2/3d), line.getY2());
        Line2D rightLine = new Line2D.Double(LineUtils.getXPercentile(line, 2/3d), line.getY1(), line.getX2(), line.getY2());

        paintLine(iteration, new CirclePainter(g, leftLine));
        paintLine(iteration, new CirclePainter(g, centreLine));
        paintLine(iteration, new CirclePainter(g, rightLine));
    }

    private Color getColour(int iterationNumber) {
        return new Color(0, 255 - Double.valueOf(255 * (1d * iterationNumber / maxIterations)).intValue(), 0);
    }
}
