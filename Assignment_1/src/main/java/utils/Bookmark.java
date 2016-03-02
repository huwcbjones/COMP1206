package utils;

/**
 * A bookmark
 *
 * @author Huw Jones
 * @since 01/03/2016
 */
public class Bookmark implements Comparable<Bookmark> {

    private String name;
    private double real;
    private double imaginary;

    public Bookmark(String name, double real, double imaginary) {
        this.name = name;
        this.real = real;
        this.imaginary = imaginary;
    }

    public double getImaginary() {
        return imaginary;
    }

    public String getName() {
        return name;
    }

    public double getReal() {
        return real;
    }

    @Override
    public int compareTo(Bookmark o) {
        return name.compareTo(o.getName());
    }

    public Complex getComplex() {
        return new Complex(real, imaginary);
    }

    /**
     * Returns a string representation of the object. In general, the
     * {@code toString} method returns a string that
     * "textually represents" this object. The result should
     * be a concise but informative representation that is easy for a
     * person to read.
     * It is recommended that all subclasses override this method.
     * <p>
     * The {@code toString} method for class {@code Object}
     * returns a string consisting of the name of the class of which the
     * object is an instance, the at-sign character `{@code @}', and
     * the unsigned hexadecimal representation of the hash code of the
     * object. In other words, this method returns a string equal to the
     * value of:
     * <blockquote>
     * <pre>
     * getClass().getName() + '@' + Integer.toHexString(hashCode())
     * </pre></blockquote>
     *
     * @return a string representation of the object.
     */
    @Override
    public String toString() {
        return String.format("%s (%.5f, %.5f)", name, real, imaginary);
    }
}
