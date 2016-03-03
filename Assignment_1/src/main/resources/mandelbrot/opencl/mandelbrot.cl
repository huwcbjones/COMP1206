/**
* Copied and adapted from JavaCL Demo :
* https://github.com/nativelibs4java/JavaCL/blob/5d2f5c2224357595a02278e7836d32014c9bebe9/Demos/src/main/opencl/com/nativelibs4java/opencl/demos/mandelbrot/Mandelbrot.cl
*/
__kernel void mandelbrot(
const unsigned int maxIterations,
const unsigned int escapeRadiusSquared,
const float2 dimensions,
const float2 scale,
const float2 shift,
const float scaleFactor,
const float huePrev,
const float hueAdj,
float saturation,
float brightness,
__global int* outputi
) {
    // int2/float2 allow us to execute commands simultaneously on vectors
    float2 coords = convert_float2((int2)(get_global_id(0), get_global_id(1)));
    int pixelID = convert_int(coords.y * dimensions.x + coords.x);

    float2 complex = ((coords - dimensions / 2.0f) * scale + shift) / scaleFactor;

    float2 z_square = complex * complex;
    float2 z = complex;

    int iterationNum = 0;

    while ( (z_square.x + z_square.y <= escapeRadiusSquared) && (iterationNum < maxIterations)) {
        // (a + bi)^2 = (a^2 - b^2) + 2 ab i

        // z.real * z.imaginary
        z.y = z.x * z.y;
        // 2 * z.real * z.imaginary
        z.y += z.y;

        // z.real^2 - z.imaginary^2
        z.x = z_square.x - z_square.y;

        // |z(n)| ^2 + c
        z += complex;

        z_square = z * z;
        iterationNum++;
    }

    float hue = INFINITY;

    if(iterationNum < maxIterations ) {
        // sqrt of inner term removed using log simplification rules. log(x^(1/2)) = (1/2)*log(x) = log(x) / 2
        float log_z = log((z.x * z.x) + (z.y * z.y)) / (float)2.0;
        float nu = log( log_z / M_LN2_F ) / M_LN2_F;
        hue = (iterationNum + 1 - nu) /   (float)110.0;
    }

    if(hue == INFINITY){
        saturation = 0;
        brightness = 0;
        } else {
        hue = hue + hueAdj - huePrev;
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