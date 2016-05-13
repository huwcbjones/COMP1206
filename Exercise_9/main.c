#include <stdio.h>
#include "analyse.c"

int main () {
    double number = 15;
    int sign;
    int *whole;
    double *fraction;

    sign = analyse(whole, fraction, number);
    printf("Analysis of: %f, (sign: %i, whole: %i, fraction: %f)", number, sign, whole, fraction);
}