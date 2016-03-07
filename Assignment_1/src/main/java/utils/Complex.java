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
        double abi = this.real * this.imaginary;
        return new Complex(this.squareReal() - this.squareImaginary(), abi + abi);
    }

    /**
     * Calculates the square of the real and returns it
     * @return double, square of the real
     */
    public double squareReal() {
        // Check if square has been calculated
        if (this.real_square != -1) return this.real_square;
        this.real_square = this.real * this.real;
        return this.real_square;
    }

    /**
     * Calculates the square of the imaginary and returns it
     * @return double, square of the imaginary
     */
    public double squareImaginary() {
        // Check if square has been calculated
        if (this.imaginary_square != -1) return this.imaginary_square;
        this.imaginary_square = this.imaginary * this.imaginary;
        return this.imaginary_square;
    }

    /**
     * Calculates the square of the modulus
     * @return Returns the square of the modulus of the complex number
     */
    public double modulusSquared(){
        // |a + bi| = root( a^2 + b^2)
        // therefore
        // |a + bi|^2 = a^2 + b^2
        return this.squareReal() + this.squareImaginary();
    }

    /**
     * Calculates the modulus of the complex and returns it
     * @return double, modulus of the complex
     */
    public double modulus(){
        double modulusSquared = this.modulusSquared();
        return modulusSquared * modulusSquared;
    }

    /**
     * Adds a complex to this complex
     * @param complex Complex to add
     */
    public void add(Complex complex){
        this.real += complex.getReal();
        this.imaginary += complex.getImaginary();
    }

    /**
     * Subtracts a complex from this complex
     * @param complex Complex to subtract
     */
    public void subtract(Complex complex){
        this.real -= complex.getReal();
        this.imaginary -= complex.getImaginary();
    }

    /**
     * Gets the real component of the complex
     * @return double, real
     */
    public double getReal(){
        return this.real;
    }

    /**
     * Gets the imaginary component of the complex
     * @return double, imaginary
     */
    public double getImaginary(){
        return this.imaginary;
    }

    @Override
    public String toString(){
        String imaginaryString = String.format("%.3f", this.imaginary);
        if (this.getImaginary() >= 0) {
            imaginaryString = "+ " + imaginaryString;
        } else {
            imaginaryString = "- " + String.format("%.3f", -1 * this.imaginary);
        }
        return String.format("%.3f %s", this.real, imaginaryString) + "i";
    }

    @Override
    public int hashCode() {
        return (int) (this.real * this.imaginary + this.imaginary);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Complex)) return false;
        Complex c = (Complex) obj;
        return this.imaginary == c.getImaginary() && this.real == c.getReal();
    }

    @Override
    public Complex clone() {
        return new Complex(this.real, this.imaginary);
    }
}