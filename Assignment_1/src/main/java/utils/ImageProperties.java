package utils;


import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Creates a unique key for an image's settings
 *
 * @author Huw Jones
 * @since 28/02/2016
 */
public class ImageProperties {
    private int width;
    private int height;

    private int iterations;
    private double scale;
    private double xShift;
    private double yShift;

    private Complex complex;

    public ImageProperties(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public ImageProperties(int width, int height, int iterations, double scale, double xShift, double yShift) {
        this(width, height);
        this.iterations = iterations;
        this.scale = scale;
        this.xShift = xShift;
        this.yShift = yShift;
    }

    public ImageProperties(int width, int height, int iterations, double scale, double xShift, double yShift, Complex complex) {
        this(width, height);
        this.iterations = iterations;
        this.scale = scale;
        this.xShift = xShift;
        this.yShift = yShift;
        this.complex = complex;
    }

    public int getIterations() {
        return iterations;
    }

    public void setIterations(int iterations) {
        this.iterations = iterations;
    }

    public double getScale() {
        return scale;
    }

    public double getScale(int decimalPlaces) {
        return round(scale, decimalPlaces);
    }

    public void setScale(double scale) {
        this.scale = scale;
    }

    public double getxShift() {
        return xShift;
    }

    public void setxShift(double xShift) {
        this.xShift = xShift;
    }

    public double getyShift() {
        return yShift;
    }

    public void setyShift(double yShift) {
        this.yShift = yShift;
    }

    public Complex getComplex() {
        return complex;
    }

    public void setComplex(Complex complex) {
        this.complex = complex;
    }

    @Override
    public int hashCode() {
        // Create a unique hashcode using prime numbers
        int h_width = width * 487;
        int h_height = height * 491;
        int h_iteration = iterations * 499;
        int h_scale = (int)(scale * 503);
        int h_xShift = (int)(xShift * 509);
        int h_yShift = (int)(xShift * 521);

        int code = h_width ^ h_height ^ h_iteration ^ h_scale ^ h_xShift ^ h_yShift;
        if(complex != null) code ^= complex.hashCode();

        return code;
    }

    private static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }


    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ImageProperties)) return false;
        ImageProperties p = (ImageProperties) obj;

        // If these values are unset in either property, return false
        if (p.getComplex() == null && complex != null) return false;

        boolean complex = (this.complex == null) || this.complex.equals(p.getComplex());
        return
                width == p.getWidth() &&
                height == p.getHeight() &&
                iterations == p.getIterations() &&
                scale == p.getScale() &&
                xShift == p.getxShift() &&
                yShift == p.getyShift() &&
                complex;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }


    @Override
    public String toString() {
        String str = "W: " + width + ", H: " + height + ", I:" + iterations + ", S: " + scale + ", X:" + xShift + ", Y:" + yShift;
        if(complex != null) {
            str += ", C:(" + complex.toString() + ")";
        }
        return str;
    }
}
