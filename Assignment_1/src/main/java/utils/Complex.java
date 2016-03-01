package utils;

/**
 * Represents a complex number
 *
 * @author Huw Jones
 * @since 22/02/2016
 */
public class Complex implements Cloneable {

    private double real;
    private double imaginary;

    private double real_square = -1;
    private double imaginary_square = -1;

    public Complex(double real, double imaginary) {
        this.real = real;
        this.imaginary = imaginary;
    }


    /**
     * Calculates square of the complex number
     * @return Returns the square of the complex number
     */
    public Complex square(){
        // (a + bi) ^2 can be expanded to a^2 + 2abi + i^2 * b^2
        // which equals a^2 - b^2 + abi
        double abi = real * imaginary;
        return new Complex(squareReal() - squareImaginary(), abi + abi);
    }

    public double squareReal() {
        if (real_square != -1) return real_square;
        real_square = real * real;
        return real_square;
    }

    public double squareImaginary() {
        if (imaginary_square != -1) return imaginary_square;
        imaginary_square = imaginary * imaginary;
        return imaginary_square;
    }

    /**
     * Cacuclates the square of the modulus
     * @return Returns the square of the modulus of the complex number
     */
    public double modulusSquared(){
        // |a + bi| = root( a^2 + b^2)
        // therefore
        // |a + bi|^2 = a^2 + b^2
        return (real * real) + (imaginary * imaginary);
    }

    public double modulus(){
        double modulusSquared =  modulusSquared();
        return modulusSquared * modulusSquared;
    }

    public void add(Complex d){
        real += d.getReal();
        imaginary += d.getImaginary();
    }

    public void subtract(Complex d){
        real -= d.getReal();
        imaginary -= d.getImaginary();
    }

    public double getReal(){
        return real;
    }
    public double getImaginary(){
        return imaginary;
    }

    @Override
    public String toString(){
        String imaginaryString = String.format("%.3f", imaginary);
        if (getImaginary() > 0) {
            imaginaryString = "+ " + imaginaryString;
        } else {
            imaginaryString = "- " + String.format("%.3f", -1 * imaginary);
        }
        return String.format("%.3f %s", real, imaginaryString) + "i";
    }

    @Override
    public int hashCode() {
        return (int) (real * imaginary + imaginary);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Complex)) return false;
        Complex c = (Complex) obj;
        return imaginary == c.getImaginary() && real == c.getReal();
    }

    public Complex clone() {
        return new Complex(real, imaginary);
    }

}
