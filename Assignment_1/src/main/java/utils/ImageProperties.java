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
        return this.iterations;
    }

    public void setIterations(int iterations) {
        this.iterations = iterations;
    }

    public double getScale() {
        return this.scale;
    }

    public double getScale(int decimalPlaces) {
        return round(this.scale, decimalPlaces);
    }

    public void setScale(double scale) {
        this.scale = scale;
    }

    public double getxShift() {
        return this.xShift;
    }

    public void setxShift(double xShift) {
        this.xShift = xShift;
    }

    public double getyShift() {
        return this.yShift;
    }

    public void setyShift(double yShift) {
        this.yShift = yShift;
    }

    public Complex getComplex() {
        return this.complex;
    }

    public void setComplex(Complex complex) {
        this.complex = complex;
    }

    @Override
    public int hashCode() {
        // Create a unique hashcode using prime numbers
        int h_width = this.width * 487;
        int h_height = this.height * 491;
        int h_iteration = this.iterations * 499;
        int h_scale = (int)(this.scale * 503);
        int h_xShift = (int)(this.xShift * 509);
        int h_yShift = (int)(this.xShift * 521);

        int code = h_width ^ h_height ^ h_iteration ^ h_scale ^ h_xShift ^ h_yShift;
        if(this.complex != null) code ^= this.complex.hashCode();

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
        if (p.getComplex() == null && this.complex != null) return false;

        boolean complex = this.complex == null || this.complex.equals(p.getComplex());
        return
                this.width == p.getWidth() &&
                        this.height == p.getHeight() &&
                        this.iterations == p.getIterations() &&
                        this.scale == p.getScale() &&
                        this.xShift == p.getxShift() &&
                        this.yShift == p.getyShift() &&
                complex;
    }

    public int getWidth() {
        return this.width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return this.height;
    }

    public void setHeight(int height) {
        this.height = height;
    }


    @Override
    public String toString() {
        String str = "W: " + this.width + ", H: " + this.height + ", I:" + this.iterations + ", S: " + this.scale + ", X:" + this.xShift + ", Y:" + this.yShift;
        if(this.complex != null) {
            str += ", C:(" + this.complex.toString() + ")";
        }
        return str;
    }
}
