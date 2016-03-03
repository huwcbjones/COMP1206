__kernel void recolour(
    __global int* pixels,
    const int maxWidth,
    const float huePrev,
    const float saturationPrev,
    const float brightnessPrev,
    const float hueNew,
    const float saturationNew,
    const float brightnessNew
) {
    // int2/float2 allow us to execute commands simultaneously on vectors
    float2 coords = convert_float2((int2)(get_global_id(0), get_global_id(1)));
    int pixelID = convert_int(coords.y * maxWidth + coords.x);

    // hsbValues.x = hue, .y = saturation, .z = brightness
    float3 hsbPrev = (float3)(huePrev, saturationPrev, brightnessPrev);
    float3 hsbNew = (float3)(hueNew, saturationNew, brightnessNew);
    float3 hsbValues = (float3)(0.0, 0.0, 0.0);

    int rgb = pixels[pixelID];

    int r = (rgb >> 16) & 0x000000FF;
    int g = (rgb >>8 ) & 0x000000FF;
    int b = (rgb) & 0x000000FF;

    // Java Color.RGBtoHSB method
    float hue, saturation, brightness;
    int cmax = (r > g) ? r : g;
    if (b > cmax) cmax = b;
    int cmin = (r < g) ? r : g;
    if (b < cmin) cmin = b;

    hsbValues.z = ((float) cmax) / 255.0f;
    if (cmax != 0)
        saturation = ((float) (cmax - cmin)) / ((float) cmax);
    else
        saturation = 0;
    if (saturation == 0)
        hue = 0;
    else {
        float redc = ((float) (cmax - r)) / ((float) (cmax - cmin));
        float greenc = ((float) (cmax - g)) / ((float) (cmax - cmin));
        float bluec = ((float) (cmax - b)) / ((float) (cmax - cmin));
        if (r == cmax)
            hue = bluec - greenc;
        else if (g == cmax)
            hue = 2.0f + redc - bluec;
        else
            hue = 4.0f + greenc - redc;
        hue = hue / 6.0f;
        if (hue < 0)
            hue = hue + 1.0f;
    }
    hsbValues.x = hue;
    hsbValues.y = saturation;
    hsbValues.z = brightness;

    // Vector calculations! :D
    hsbValues -= hsbPrev;
    hsbValues += hsbNew;

    hue = hsbValues.x;
    saturation = hsbValues.y;
    brightness = hsbValues.z;

    // Java Color.getHSBColor
    r = 0, g = 0, b = 0;
    if (saturation == 0) {
        r = g = b = (int) (brightness * 255.0f + 0.5f);
    } else {
        float h = (hue - floor(hue)) * 6.0f;
        float f = h - floor(h);
        float p = brightness * (1.0f - saturation);
        float q = brightness * (1.0f - saturation * f);
        float t = brightness * (1.0f - (saturation * (1.0f - f)));
        switch (convert_int(h)) {
        case 0:
            r = (brightness * 255.0f + 0.5f);
            g = (t * 255.0f + 0.5f);
            b = (p * 255.0f + 0.5f);
            break;
        case 1:
            r =  (q * 255.0f + 0.5f);
            g =  (brightness * 255.0f + 0.5f);
            b =  (p * 255.0f + 0.5f);
            break;
        case 2:
            r =  (p * 255.0f + 0.5f);
            g =  (brightness * 255.0f + 0.5f);
            b =  (t * 255.0f + 0.5f);
            break;
        case 3:
            r =  (p * 255.0f + 0.5f);
            g =  (q * 255.0f + 0.5f);
            b =  (brightness * 255.0f + 0.5f);
            break;
        case 4:
            r =  (t * 255.0f + 0.5f);
            g =  (p * 255.0f + 0.5f);
            b =  (brightness * 255.0f + 0.5f);
            break;
        case 5:
            r =  (brightness * 255.0f + 0.5f);
            g =  (p * 255.0f + 0.5f);
            b =  (q * 255.0f + 0.5f);
            break;
        }
    }

    int r_int = convert_int(r);
    int g_int = convert_int(g);
    int b_int = convert_int(b);

    pixels[pixelID] = 0xff000000 | (r_int << 16) | (g_int << 8) | (b_int << 0);
}