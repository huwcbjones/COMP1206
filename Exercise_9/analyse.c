#include <math.h>
/**
 * Analyses a double
 *
 * @param int* wholePtr Pointer for whole result
 * @param double* fracPtr Pointer to fraction result
 * @param double d Double to analyse
 *
 * @return Sign, -1 = negative, 1 = positive
 */
int analyse(int *wholePtr, double *fracPtr, double d);

int analyse(int *wholePtr, double *fracPtr, double d) {

    *wholePtr = floor(d);

    // Return -1 if d < 0, otherwise return 1
    return (d < 0) ? -1 : 1;
}
