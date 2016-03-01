/**
 * Copied and adapted from JavaCL Demo :
 * https://github.com/nativelibs4java/JavaCL/blob/5d2f5c2224357595a02278e7836d32014c9bebe9/Demos/src/main/opencl/com/nativelibs4java/opencl/demos/mandelbrot/Mandelbrot.cl
 */
__kernel void julia(
    const unsigned int maxIterations,
    const unsigned int escapeRadiusSquared,
    const float2 complex,
    const float2 dimensions,
    const float2 scale,
    const float2 shift,
    const float scaleFactor,
    __global float* outputi
) {
    // int2/float2 allow us to execute commands simultaneously on vectors

    float2 coords = convert_float2((int2)(get_global_id(0), get_global_id(1)));

    float2 z = ((coords - dimensions / 2.0f) * scale + shift) / scaleFactor;

    float2 z_square = z * z;

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
        float log_z = log(sqrt((z.x * z.x) + (z.y * z.y)));
        hue = (iterationNum + 1 - log(log_z) / M_LN2_F) / (float)100.0;
    }
    outputi[convert_int(coords.y * dimensions.x + coords.x)] = hue;
}