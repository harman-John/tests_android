#define LOG_TAG "iirfilt"
//#define NDEBUG

#include "../inc/iirfilt.h"
#include "../inc/ae_math.h"
#include "../inc/ae_common.h"

static void pass_design(IirBiquardState *st)
{
    st->b0 = 1.f;
    st->b1 = 1.f;
    st->b2 = 1.f;
    st->a1 = 1.f;
    st->a2 = 1.f;
}

static void lpf_design(IirBiquardState *st, int fs, int f0, float q)
{
    float a0, a1, a2, b0, b1, b2;
    float w0 = 2.f * AE_PI * f0 / fs;
    float alpha = sinf(w0) / (2 * q);

    b0 = (1 - cosf(w0)) / 2;
    b1 = 1 - cosf(w0);
    b2 = (1 - cosf(w0)) / 2;
    a0 = 1 + alpha;
    a1 = -2 * cosf(w0);
    a2 = 1 - alpha;

    st->b0 = b0 / a0;
    st->b1 = b1 / a0;
    st->b2 = b2 / a0;
    st->a1 = a1 / a0;
    st->a2 = a2 / a0;
}

static void hpf_design(IirBiquardState *st, int fs, int f0, float q)
{
    float a0, a1, a2, b0, b1, b2;
    float w0 = 2.f * AE_PI * f0 / fs;
    float alpha = sinf(w0) / (2 * q);

    b0 = (1 + cosf(w0)) / 2;
    b1 = -(1 + cosf(w0));
    b2 = (1 + cosf(w0)) / 2;
    a0 = 1 + alpha;
    a1 = -2 * cosf(w0);
    a2 = 1 - alpha;

    st->b0 = b0 / a0;
    st->b1 = b1 / a0;
    st->b2 = b2 / a0;
    st->a1 = a1 / a0;
    st->a2 = a2 / a0;
}

// constant skirt gain, peak gain = Q
static void bpf0_design(IirBiquardState *st, int fs, int f0, float q)
{
    float a0, a1, a2, b0, b1, b2;
    float w0 = 2 * AE_PI * f0 / fs;
    float alpha = sinf(w0) / (2 * q);

    b0 = sinf(w0) / 2; // q * alpha;
    b1 = 0;
    b2 = -sinf(w0) / 2; // -q * alpha;
    a0 = 1 + alpha;
    a1 = -2 * cosf(w0);
    a2 = 1 - alpha;

    st->b0 = b0 / a0;
    st->b1 = b1 / a0;
    st->b2 = b2 / a0;
    st->a1 = a1 / a0;
    st->a2 = a2 / a0;
}

// const 0 dB peak gain
static void bpf1_design(IirBiquardState *st, int fs, int f0, float q)
{
    float a0, a1, a2, b0, b1, b2;
    float w0 = 2 * AE_PI * f0 / fs;
    float alpha = sinf(w0) / (2 * q);

    b0 = alpha;
    b1 = 0;
    b2 = alpha;
    a0 = 1 + alpha;
    a1 = -2 * cosf(w0);
    a2 = 1 - alpha;

    st->b0 = b0 / a0;
    st->b1 = b1 / a0;
    st->b2 = b2 / a0;
    st->a1 = a1 / a0;
    st->a2 = a2 / a0;
}

static void notch_design(IirBiquardState *st, int fs, int f0, float q)
{
    float a0, a1, a2, b0, b1, b2;
    float w0 = 2 * AE_PI * f0 / fs;
    float alpha = sinf(w0) / (2 * q);

    b0 = 1;
    b1 = -2 * cosf(w0);
    b2 = 1;
    a0 = 1 + alpha;
    a1 = -2 * cosf(w0);
    a2 = 1 - alpha;

    st->b0 = b0 / a0;
    st->b1 = b1 / a0;
    st->b2 = b2 / a0;
    st->a1 = a1 / a0;
    st->a2 = a2 / a0;
}

static void apf_design(IirBiquardState *st, int fs, int f0, float q)
{
    float a0, a1, a2, b0, b1, b2;
    float w0 = 2 * AE_PI * f0 / fs;
    float alpha = sinf(w0) / (2 * q);

    b0 = 1 - alpha;
    b1 = -2 * cosf(w0);
    b2 = 1 + alpha;
    a0 = 1 + alpha;
    a1 = -2 * cosf(w0);
    a2 = 1 - alpha;

    st->b0 = b0 / a0;
    st->b1 = b1 / a0;
    st->b2 = b2 / a0;
    st->a1 = a1 / a0;
    st->a2 = a2 / a0;
}

static void peaking_design(IirBiquardState *st, float fs, float f0, float gain, float q)
{
    float a0, a1, a2, b0, b1, b2;
    float w0 = 2 * AE_PI * f0 / fs;
    float alpha = sinf(w0) / (2 * q);
    float A = powf(10.f, gain / 40.f);

    b0 = 1 + alpha * A;
    b1 = -2 * cosf(w0);
    b2 = 1 - alpha * A;
    a0 = 1 + alpha / A;
    a1 = -2 * cosf(w0);
    a2 = 1 - alpha / A;

    st->b0 = b0 / a0;
    st->b1 = b1 / a0;
    st->b2 = b2 / a0;
    st->a1 = a1 / a0;
    st->a2 = a2 / a0;
}

static void lowshelf_design(IirBiquardState *st, int fs, int f0, float gain, float q)
{
    float a0, a1, a2, b0, b1, b2;
    float w0 = 2 * AE_PI * f0 / fs;
    float alpha = sinf(w0) / (2 * q);
    float A = powf(10.f, gain / 40);

    b0 = A * ((A + 1) - (A - 1) * cosf(w0) + 2 * sqrtf(A) * alpha);
    b1 = 2 * A * ((A - 1) - (A + 1) * cosf(w0));
    b2 = A * ((A + 1) - (A - 1) * cosf(w0) - 2 * sqrtf(A) * alpha);
    a0 = (A + 1) + (A - 1) * cosf(w0) + 2 * sqrtf(A) * alpha;
    a1 = -2 * ((A - 1) + (A + 1) * cosf(w0));
    a2 = (A + 1) + (A - 1) * cosf(w0) - 2 * sqrtf(A) * alpha;

    st->b0 = b0 / a0;
    st->b1 = b1 / a0;
    st->b2 = b2 / a0;
    st->a1 = a1 / a0;
    st->a2 = a2 / a0;
}

static void highshelf_design(IirBiquardState *st, int fs, int f0, float gain, float q)
{
    float a0, a1, a2, b0, b1, b2;
    float w0 = 2 * AE_PI * f0 / fs;
    float alpha = sinf(w0) / (2 * q);
    float A = powf(10.f, gain / 40);

    b0 = A * ((A + 1) + (A - 1) * cosf(w0) + 2 * sqrtf(A) * alpha);
    b1 = -2 * A * ((A - 1) + (A + 1) * cosf(w0));
    b2 = A * ((A + 1) + (A - 1) * cosf(w0) - 2 * sqrtf(A) * alpha);
    a0 = (A + 1) - (A - 1) * cosf(w0) + 2 * sqrtf(A) * alpha;
    a1 = 2 * ((A - 1) - (A + 1) * cosf(w0));
    a2 = (A + 1) - (A - 1) * cosf(w0) - 2 * sqrtf(A) * alpha;

    st->b0 = b0 / a0;
    st->b1 = b1 / a0;
    st->b2 = b2 / a0;
    st->a1 = a1 / a0;
    st->a2 = a2 / a0;
}

void iirfilt_design(IirBiquardState *st, float fs, float f0, float gain, float q, enum IIR_BIQUARD_TYPE type)
{
    switch (type) {
    case IIR_BIQUARD_LPF:
        lpf_design(st, fs, f0, q);
        break;
    case IIR_BIQUARD_HPF:
        hpf_design(st, fs, f0, q);
        break;
    case IIR_BIQUARD_BPF0:
        bpf0_design(st, fs, f0, q);
        break;
    case IIR_BIQUARD_BPF1:
        bpf1_design(st, fs, f0, q);
        break;
    case IIR_BIQUARD_NOTCH:
        notch_design(st, fs, f0, q);
        break;
    case IIR_BIQUARD_APF:
        apf_design(st, fs, f0, q);
        break;
    case IIR_BIQUARD_PEAKINGEQ:
        peaking_design(st, fs, f0, gain, q);
        break;
    case IIR_BIQUARD_LOWSHELF:
        lowshelf_design(st, fs, f0, gain, q);
        break;
    case IIR_BIQUARD_HIGHSHELF:
        highshelf_design(st, fs, f0, gain, q);
        break;
    default:
        pass_design(st);
    }

//    AE_TRACE("%s, a = %s%d.%06d, %s%d.%06d, b = %s%d.%06d, %s%d.%06d, %s%d.%06d", __FUNCTION__,
//        AE_SIGN(st->a1), AE_INT(st->a1), AE_FRAC(st->a1),
//        AE_SIGN(st->a2), AE_INT(st->a2), AE_FRAC(st->a2),
//        AE_SIGN(st->b0), AE_INT(st->b0), AE_FRAC(st->b0),
//        AE_SIGN(st->b1), AE_INT(st->b1), AE_FRAC(st->b1),
//        AE_SIGN(st->b2), AE_INT(st->b2), AE_FRAC(st->b2));
    //AE_TRACE("%s, a = %f, %f, b = %f, %f, %f", __FUNCTION__,
    //    st->a1, st->a2, st->b0, st->b1, st->b2);

    iirfilt_reset(st, 1);
}
