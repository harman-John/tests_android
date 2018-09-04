#include "../inc/iirfilt.h"
#include "../inc/complex_op.h"
#include "../inc/ae_math.h"
#include "../inc/ae_common.h"

/*
 * input: a -> real input, length M
 *        b -> real input, length N
 *
 * output: h -> complex output, length K
 */
void freq(float *b, int M, float *a, int N, float *h, int K)
{
	Complex b_tmp;
	Complex a_tmp;
	Complex hk;
	Complex wm;
	Complex wn;

	int m;
	int n;
	float w;

    for (int k = 0; k < K / 2; k++) {
        b_tmp = cpx_create(0.f, 0.f);
        for (m = 0; m < M; m++) {
            w = m * AE_PI * k / K;
            wm = cpx_create(cosf(w), -sinf(w));
            b_tmp = cpx_add(b_tmp, cpx_scale(wm, b[m]));
        }
        a_tmp = cpx_create(0.f, 0.f);
        for (n = 0; n < N; n++) {
            w = n * AE_PI * k / K;
            wn = cpx_create(cosf(w), -sinf(w));
            a_tmp = cpx_add(a_tmp, cpx_scale(wn, a[n]));
        }
        hk = cpx_div(b_tmp, a_tmp);
        h[k * 2] = hk.r;
        h[k * 2 + 1] = hk.i;
    }
}

/*
* input: a -> real input, length M
*        b -> real input, length N
*
* output: h -> real output, length K
*/
void freqz(float *b, int M, float *a, int N, float *h, int K)
{
    for (int k = 0; k < K; k++) {
        Complex b_tmp = cpx_create(0.f, 0.f);
        for (int m = 0; m < M; m++) {
            float w = m * AE_PI * k / K;
            Complex wm = cpx_create(cosf(w), -sinf(w));
            b_tmp = cpx_add(b_tmp, cpx_scale(wm, b[m]));
        }
        Complex a_tmp = cpx_create(0.f, 0.f);
        for (int n = 0; n < N; n++) {
            float w = n * AE_PI * k / K;
            Complex wn = cpx_create(cosf(w), -sinf(w));
            a_tmp = cpx_add(a_tmp, cpx_scale(wn, a[n]));
        }
        Complex hk = cpx_div(b_tmp, a_tmp);
        h[k] =h[k] * cpx_abs(hk);
    }
}

/*
* input: a -> real input, length M
*        b -> real input, length N
*
* output: h -> real output, length K
*/
void phasez(float *b, int M, float *a, int N, float *h, int K)
{
    for (int k = 0; k < K; k++) {
        Complex b_tmp = cpx_create(0.f, 0.f);
        for (int m = 0; m < M; m++) {
            float w = m * AE_PI * k / K;
            Complex wm = cpx_create(cosf(w), -sinf(w));
            b_tmp = cpx_add(b_tmp, cpx_scale(wm, b[m]));
        }
        Complex a_tmp = cpx_create(0.f, 0.f);
        for (int n = 0; n < N; n++) {
            float w = n * AE_PI * k / K;
            Complex wn = cpx_create(cosf(w), -sinf(w));
            a_tmp = cpx_add(a_tmp, cpx_scale(wn, a[n]));
        }
        Complex hk = cpx_div(b_tmp, a_tmp);
        h[k] = cpx_angle(hk);
    }
}