#ifndef COMPLEX_OP_H
#define COMPLEX_OP_H

#ifdef CMATRIX_DEBUG
#include <stdio.h>
#endif

typedef struct
{
    float r;
    float i;
} Complex;

Complex cpx_create(float r, float i);

Complex cpx_add(Complex a, Complex b);

Complex cpx_sub(Complex a, Complex b);

Complex cpx_mult(Complex a, Complex b);

Complex cpx_div(Complex a, Complex b);

Complex cpx_scale(Complex a, float b);

Complex cpx_conj(Complex a);

float cpx_power(Complex a);

float cpx_abs(Complex a);

float cpx_angle(Complex a);

#ifdef CMATRIX_DEBUG
void cpx_print(FILE *fd, Complex a);
#endif

#endif