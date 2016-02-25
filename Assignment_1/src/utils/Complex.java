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

    /**
     * Calculates square of the complex number
     * @return Returns the square of the complex number
     */
    public double square(){
        // (a + bi) ^2 can be expanded to a^2 + 2ab + b^2
        return (real * real) + (2 * real * imaginary) + (imaginary * imaginary);
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
}