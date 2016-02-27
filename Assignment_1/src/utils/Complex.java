package utils;

/**
 * Represents a complex number
 *
 * @author Huw Jones
 * @since 22/02/2016
 */
public class Complex {

    private double real;
    private double imaginary;

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

        return new Complex((real * real) - (imaginary * imaginary), (2 * real * imaginary));
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

    public void add(Complex d){
        real += d.getReal();
        imaginary += d.getImaginary();
    }

    public double getReal(){
        return real;
    }
    public double getImaginary(){
        return imaginary;
    }

    @Override
    public String toString(){
        String imaginaryString = String.format("%.2f", imaginary);
        if (getImaginary() > 0) {
            imaginaryString = "+ " + imaginaryString;
        } else {
            imaginaryString = "- " + String.format("%.2f", -1 * imaginary);
        }
        return String.format("%.2f %s", real, imaginaryString) + "i";
    }
}
