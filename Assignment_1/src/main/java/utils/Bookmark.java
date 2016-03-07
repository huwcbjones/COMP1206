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

    public Bookmark(String name, Complex complex){
        this.name = name;
        this.real = complex.getReal();
        this.imaginary = complex.getImaginary();
    }

    public Bookmark(String name, double real, double imaginary) {
        this.name = name;
        this.real = real;
        this.imaginary = imaginary;
    }

    public String getName() {
        return this.name;
    }

    public double getReal() {
        return this.real;
    }

    public double getImaginary() {
        return this.imaginary;
    }

    public Complex getComplex() {
        return new Complex(this.real, this.imaginary);
    }

    @Override
    public int compareTo(Bookmark o) {
        return this.name.compareTo(o.getName());
    }

    @Override
    public String toString() {
        return String.format("%s (%.5f, %.5f)", this.name, this.real, this.imaginary);
    }
}
