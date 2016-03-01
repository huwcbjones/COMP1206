package utils;

/**
 * A bookmark
 *
 * @author Huw Jones
 * @since 01/03/2016
 */
public class Bookmark {

    private String name;
    private float real;
    private float imaginary;

    public Bookmark(String name, float real, float imaginary) {
        this.name = name;
        this.real = real;
        this.imaginary = real;
    }

    public float getImaginary() {
        return imaginary;
    }

    public String getName() {
        return name;
    }

    public float getReal() {
        return real;
    }
}
