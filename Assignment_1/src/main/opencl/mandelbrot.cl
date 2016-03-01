__kernel mandelbrot(
    const unsigned int maxIterations,
    const unsigned int escapeRadiusSquared,
    const int2 dimensions,
    const float2 scale,
    const float2 shift,
    const float scaleFactor,
    __global int* output
) {
    // int2/float2 allow us to execute commands simultaneously on vectors

    int2 coords = (int2)(get_global_id(0), get_global_id(1));

    float2 complex = ((coords - dimensions / 2.0) * scale + shift) / scaleFactor;

    float2 z_square = complex * complex;
    float2 z = complex;

    int iterationNum = 0;

    while ( (z_square.x + z_square.y <= escapeRadiusSquared) && (iterationNum < maxIterations)) {
        // (a + bi)^2 = (a^2 - b^2) + 2 ab i

        // z.real * z.imaginary
        z.y = z.x * z.y;
        // 2 * z.real * z.imaginary
        z.y += z.y;
        // 2 * z.real * z.imaginary
        z.y += c_i;

        // z.real^2 - z.imaginary^2
        z.x = z_square.x - z_square.y

        // |z(n)| ^2 + c
        z += complex;

        z_square = z * z;
        iterationNum++;
    }

    if(iterationNum >= maxIterations ) {
        iterationNum = 0;
    }

    output[coords.y * dimensions.y + coords.x] = iterationNum;
}