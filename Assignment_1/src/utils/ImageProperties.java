package utils;


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

    private Complex complex;

    public ImageProperties(int iterations, double scale, double xShift, double yShift) {
        this.iterations = iterations;
        this.scale = scale;
        this.xShift = xShift;
        this.yShift = yShift;
    }

    public ImageProperties(int iterations, double scale, double xShift, double yShift, Complex complex) {
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

        String code = "I:" + iterations +",S:" + scale + ",X:"+ xShift + ",Y:"+yShift;
        if(complex != null) code += ",C:" + complex.toString();
        return code.hashCode();
    }


    @Override
    public boolean equals(Object obj) {
        return obj.hashCode() == this.hashCode();
    }
}
