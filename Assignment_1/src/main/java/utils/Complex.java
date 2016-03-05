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

    /**
     * Calculates the square of the real and returns it
     * @return double, square of the real
     */
    public double squareReal() {
        // Check if square has been calculated
        if (real_square != -1) return real_square;
        real_square = real * real;
        return real_square;
    }

    /**
     * Calculates the square of the imaginary and returns it
     * @return double, square of the imaginary
     */
    public double squareImaginary() {
        // Check if square has been calculated
        if (imaginary_square != -1) return imaginary_square;
        imaginary_square = imaginary * imaginary;
        return imaginary_square;
    }

    /**
     * Calculates the square of the modulus
     * @return Returns the square of the modulus of the complex number
     */
    public double modulusSquared(){
        // |a + bi| = root( a^2 + b^2)
        // therefore
        // |a + bi|^2 = a^2 + b^2
        return (squareReal()) + (squareImaginary());
    }

    /**
     * Calculates the modulus of the complex and returns it
     * @return double, modulus of the complex
     */
    public double modulus(){
        double modulusSquared =  modulusSquared();
        return modulusSquared * modulusSquared;
    }

    /**
     * Adds a complex to this complex
     * @param complex Complex to add
     */
    public void add(Complex complex){
        real += complex.getReal();
        imaginary += complex.getImaginary();
    }

    /**
     * Subtracts a complex from this complex
     * @param complex Complex to subtract
     */
    public void subtract(Complex complex){
        real -= complex.getReal();
        imaginary -= complex.getImaginary();
    }

    /**
     * Gets the real component of the complex
     * @return double, real
     */
    public double getReal(){
        return real;
    }

    /**
     * Gets the imaginary component of the complex
     * @return double, imaginary
     */
    public double getImaginary(){
        return imaginary;
    }

    @Override
    public String toString(){
        String imaginaryString = String.format("%.3f", imaginary);
        if (getImaginary() >= 0) {
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