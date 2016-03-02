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
    private int iterations;
    private double scale;
    private double xShift;
    private double yShift;
    private float hue = -1;
    private float saturation = 1;
    private float brightness = 1;

    private Complex complex;

    public ImageProperties(int iterations, double scale, double xShift, double yShift) {
        this.iterations = iterations;
        this.scale = scale;
        this.xShift = xShift;
        this.yShift = yShift;
    }

    public ImageProperties(int iterations, double scale, double xShift, double yShift, float hue, float saturation, float brightness) {
        this.iterations = iterations;
        this.scale = scale;
        this.xShift = xShift;
        this.yShift = yShift;
        this.hue = hue;
        this.saturation = saturation;
        this.brightness = brightness;
    }

    public ImageProperties(int iterations, double scale, double xShift, double yShift, Complex complex) {
        this.iterations = iterations;
        this.scale = ImageProperties.round(scale, 4);
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

    public void setScale(double scale) {
        this.scale = ImageProperties.round(scale, 4);
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

        int h_iteration = iterations * 499;
        int h_scale = (int)(scale * 503);
        int h_xShift = (int)(xShift * 509);
        int h_yShift = (int)(xShift * 521);
        int h_tint = (int) (hue * 523);

        int code = h_iteration ^ h_scale ^ h_xShift ^ h_yShift ^ h_tint;
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

        return equalsNotTint(p) &&
                hue == p.getHue() &&
                saturation == p.getSaturation() &&
                brightness == p.getBrightness();
    }

    public boolean equalsNotTint(ImageProperties p) {
        // If these values are unset in either property, return false
        if (p.getComplex() == null && complex != null) return false;
        if (p.getHue() == -1 && hue != -1) return false;

        boolean complex = (this.complex == null) || this.complex.equals(p.getComplex());
        return iterations == p.getIterations() &&
                scale == p.getScale() &&
                xShift == p.getxShift() &&
                yShift == p.getyShift() &&
                complex;
    }

    public float getHue() {
        return hue;
    }

    public void setHue(float hue) {
        this.hue = hue;
    }

    public float getSaturation() {
        return saturation;
    }

    public void setSaturation(float saturation) {
        this.saturation = saturation;
    }

    public float getBrightness() {
        return brightness;
    }

    public void setBrightness(float brightness) {
        this.brightness = brightness;
    }
}
