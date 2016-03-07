package mandelbrot;

import utils.FractalImage;
import utils.ImageColourProperties;
import utils.ImageProperties;
import utils.Log;

import java.util.LinkedHashMap;

/**
 * Manages the Image Cache
 *
 * @author Huw Jones
 * @since 03/03/2016
 */
public class CacheManager {

    private LinkedHashMap<ImageProperties, LinkedHashMap<ImageColourProperties, FractalImage>> imageCache;

    public CacheManager(){
        Log.Information("Loading cache manager...");
        this.imageCache = new LinkedHashMap<>();
    }

    public void cacheImage(FractalImage image){
        if(this.imageCache.containsKey(image.getProperties())){
            this.imageCache.get(image.getProperties()).put(image.getColourProperties(), image);
        } else {
            LinkedHashMap<ImageColourProperties, FractalImage> newImageCache = new LinkedHashMap<>();
            newImageCache.put(image.getColourProperties(), image);
            this.imageCache.put(image.getProperties(), newImageCache);
        }
    }

    /**
     * Checks whether an image with the properties ImageProperties is cached
     * and returns a boolean representing whether it is or not
     * @param properties Properties of image to check
     * @return True if image is cached
     */
    public boolean isCached(ImageProperties properties){
        //return false;
        return this.imageCache.containsKey(properties);
    }

    /**
     * Checks whether an image with the properties ImageProperties is cached
     * and returns a boolean representing whether it is or not
     * @param properties Properties of image to check
     * @return True if image is cached
     */
    public boolean isCached(ImageProperties properties, ImageColourProperties colourProperties){
        //return false;
        if(!this.isCached(properties)){
            return false;
        }
        return this.imageCache.get(properties).containsKey(colourProperties);
    }

    public FractalImage getImage(ImageProperties properties){
        return this.imageCache.get(properties).entrySet().iterator().next().getValue();
    }

    public FractalImage getImage(ImageProperties properties, ImageColourProperties colourProperties){
        return this.imageCache.get(properties).get(colourProperties);
    }
}
