import java.awt.geom.Line2D;

/**
 * Adds utility methods for Line2D
 *
 * @author Huw Jones
 * @since 22/02/2016
 */
public class LineUtils {

    public static Double getYPercentile(Line2D line, Double percentile) throws IllegalArgumentException {
        return getPercentile(line, percentile, false);
    }

    private static double getPercentile(Line2D line, double percentile, boolean isX) throws IllegalArgumentException {
        if (percentile >= 1) throw new IllegalArgumentException("Percentile cannot be greater than 1.");
        if (percentile <= 0) throw new IllegalArgumentException("Percentile cannot be less than 0.");

        if (isX) {
            return (1 - percentile) * line.getX1() + percentile * line.getX2();
        } else {
            return (1 - percentile) * line.getY1() + percentile * line.getY2();
        }
    }

    public static Double getXPercentile(Line2D line, Double percentile) throws IllegalArgumentException {
        return getPercentile(line, percentile, true);
    }

    public static Double getLineLength(Line2D line) {
        return Math.sqrt(Math.pow(line.getX2() - line.getX1(), 2) + Math.pow(line.getY2() - line.getY1(), 2));
    }

}
