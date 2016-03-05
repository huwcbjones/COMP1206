package utils;

/**
 * Image Colour Properties
 *
 * @author Huw Jones
 * @since 03/03/2016
 */
public class ImageColourProperties {

    private float hue;
    private float saturation;
    private float brightness;

    public ImageColourProperties() {

    }

    public ImageColourProperties(float hue, float saturation, float brightness) {
        this.hue = hue;
        this.saturation = saturation;
        this.brightness = brightness;
    }

    @Override
    public int hashCode() {
        // Create a unique hashcode using prime numbers
        int h_hue = (int) (hue * 499);
        int h_saturation = (int) (saturation * 503);
        int h_brightness = (int) (brightness * 509);

        return h_hue ^ h_saturation ^ h_brightness;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ImageColourProperties)) return false;
        ImageColourProperties p = (ImageColourProperties) obj;

        return hue == p.getHue() &&
                saturation == p.getSaturation() &&
                brightness == p.getBrightness();
    }

    public float getHue() {
        return hue;
    }

    public void setHue(float hue) {
        this.hue = hue;
    }

    public float getSaturation() {
        return saturation;
    }

    public void setSaturation(float saturation) {
        this.saturation = saturation;
    }

    public float getBrightness() {
        return brightness;
    }

    public void setBrightness(float brightness) {
        this.brightness = brightness;
    }

    @Override
    public String toString() {
        return "H: " + hue + ", S: " + saturation + ", B: " + brightness;
    }
}
