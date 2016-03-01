__kernel void hueToRGB(
    __global float* input,
    const int maxWidth,
    float saturation,
    float brightness,
    const float hueAdj,
    const float huePrev,
    __global int* outputi
) {
    // int2/float2 allow us to execute commands simultaneously on vectors

    float2 coords = convert_float2((int2)(get_global_id(0), get_global_id(1)));
    int pixelID = convert_int(coords.y * maxWidth + coords.x);
    float hue = input[pixelID];
    if(hue == INFINITY){
        saturation = 0;
        brightness = 0;
    }
    float r = 0, g = 0, b = 0;
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

    outputi[pixelID] = 0xff000000 | (r_int << 16) | (g_int << 8) | (b_int << 0);
}