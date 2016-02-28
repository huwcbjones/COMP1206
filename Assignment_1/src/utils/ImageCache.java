package utils;


/**
 * Creates a unique key for an image's settings
 *
 * @author Huw Jones
 * @since 28/02/2016
 */
public class ImageCache {

    private double iterations;
    private double scale;
    private double xShift;
    private double yShift;


    public ImageCache(double iterations, double scale, double xShift, double yShift) {
        this.iterations = iterations;
        this.scale = scale;
        this.xShift = xShift;
        this.yShift = yShift;
    }

    @Override
    public int hashCode() {
        String code = "I:" + iterations +",S:" + scale + ",X:"+ xShift + ",Y:"+yShift;
        return code.hashCode();
    }


    @Override
    public boolean equals(Object obj) {
        return obj.hashCode() == this.hashCode();
    }
}
