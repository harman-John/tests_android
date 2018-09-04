#include "../inc/iirfilt.h"
#include "../inc/ae_math.h"

// Direct-form 2 transposed biquad
// https://en.wikibooks.org/wiki/Digital_Signal_Processing/IIR_Filter_Design
static float biquard_step(IirBiquardState *st, float sample)
{
    st->s2 = st->b0 * sample + st->s1;
    st->s1 = st->b1 * sample + st->s0 - st->a1 * st->s2;
    st->s0 = st->b2 * sample - st->a2 * st->s2;

    return st->s2;
}

static void biquard_reset(IirBiquardState *st)
{
    st->s0 = 0.f;
    st->s1 = 0.f;
    st->s2 = 0.f;
}

static float step(IirBiquardState *st, int stages, float sample)
{
    float tmp = sample;
    for (int i = 0; i < stages; i++) {
        tmp = biquard_step(&st[i], tmp);
    }

    return tmp;
}

void iirfilt_reset(IirBiquardState *st, int stages)
{
    for (int i = 0; i < stages; i++) {
        biquard_reset(&st[i]);
    }
}

void iirfilt_process(IirBiquardState *st, int stages, int16_t *buf, int frame_size)
{
    for (int i = 0; i < frame_size; i++) {
        float tmp = AE_ROUND(step(st, stages, (float)buf[i]));
        buf[i] = (int16_t)AE_SSAT16(tmp);
    }
}

void iirfilt_process2(IirBiquardState *st, int stages, float master_gain, int16_t *buf, int frame_size)
{
    for (int i = 0; i < frame_size; i++) {
        float tmp = buf[i] * master_gain;
        tmp = AE_ROUND(step(st, stages, tmp));
        buf[i] = (int16_t)AE_SSAT16(tmp);
    }
}

void iirfilt_process_float(IirBiquardState *st, int stages, float *buf, int frame_size)
{
    for (int i = 0; i < frame_size; i++) {
        buf[i] = step(st, stages, buf[i]);
    }
}

void iirfilt_process2_float(IirBiquardState *st, int stages, float master_gain, float *buf, int frame_size)
{
    for (int i = 0; i < frame_size; i++) {
        float tmp = buf[i] * master_gain;
        buf[i] = step(st, stages, tmp);
    }
}