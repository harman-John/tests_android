

#include "../inc/EQAlgorithm.h"

#include <android/log.h>
#define TAG "EQAlgorithm-jni"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG,TAG ,__VA_ARGS__)


void test_cpx_div(void)
{
	Complex a = cpx_create(1.f, 2.f);
	Complex b = cpx_create(3.f, 4.f);

	Complex c = cpx_div(a, b);

	//cpx_print(stdout, c);
}

IIR_CFG_T iir_designer_deafult_eq_cfg = {
#if 0
    .gain0 = -3,
    .gain1 = -3,
    .num = 6,
    .param = {
        { IIR_BIQUARD_PEAKINGEQ, 3.0,   9500,   4.0 },
        { IIR_BIQUARD_PEAKINGEQ, -3.0,   6300,  8.0 },
        { IIR_BIQUARD_PEAKINGEQ, 3.0,   300.0, 3.0 },
        { IIR_BIQUARD_PEAKINGEQ, -4.0,  120.0, 1.0 },
        { IIR_BIQUARD_PEAKINGEQ, -3.0,  1000.0, 1.0 },
        { IIR_BIQUARD_PEAKINGEQ, 1.0,  3000.0, 10.0 },
        
    }
#endif
    
};
designer_cfg device_cfg;

#define EQ_DESIGNER_BANDS_NUM 6
#define EQ_USER_BANDS_NUM 10
#define EQ_TOTAL_BANDS_NUM	EQ_DESIGNER_BANDS_NUM + EQ_USER_BANDS_NUM

#define audio_sample_rate 48000
#ifndef audio_sample_rate
#define audio_sample_rate 0
#endif
IIR_CFG_T iir_user_eq_cfg = {
#if 0
	.gain0 = 0,
	.gain1 = 0,
	.num = 10,
	.param = {
		{ IIR_TYPE_PEAK, 6.0,   100,   2.0 },
		{ IIR_TYPE_PEAK, 0.0,   200,  2.0 },
		{ IIR_TYPE_PEAK, 1.0,   400.0, 2.0 },
		{ IIR_TYPE_PEAK, 2.0,  800.0, 2.0 },
		{ IIR_TYPE_PEAK, 3.0,  1600.0, 2.0 },
		{ IIR_TYPE_PEAK, 4.0,  3200.0, 2.0 },
		{ IIR_TYPE_PEAK, 5.0,   6400.0, 2.0 },
		{ IIR_TYPE_PEAK, 6.0,  12800.0, 2.0 },
		{ IIR_TYPE_PEAK, -1.0,  1000.0, 2.0 },
		{ IIR_TYPE_PEAK, -2.0,  2000.0, 2.0 },

		}
#endif
};

IIR_CFG_T iir_total_audio_eq_cfg;
IirBiquardState designer_eq_coefs[EQ_DESIGNER_BANDS_NUM];
IirBiquardState user_eq_coefs[EQ_USER_BANDS_NUM];
IirBiquardState total_eq_coefs[EQ_TOTAL_BANDS_NUM];


int sys_fs = 0;
/*
	Function
*/

void app_audio_designer_eq_init(designer_cfg designEQ)
{
	memset((uint8_t *)&iir_designer_deafult_eq_cfg,0, sizeof(iir_designer_deafult_eq_cfg));
    iir_designer_deafult_eq_cfg.gain0 = designEQ.eq.gain0;
	iir_designer_deafult_eq_cfg.gain1 = designEQ.eq.gain1;
	iir_designer_deafult_eq_cfg.num = designEQ.eq.num;

	iir_designer_deafult_eq_cfg.param[0].type = designEQ.eq.param[0].type;
	iir_designer_deafult_eq_cfg.param[0].gain = designEQ.eq.param[0].gain;
	iir_designer_deafult_eq_cfg.param[0].fc = designEQ.eq.param[0].fc;
	iir_designer_deafult_eq_cfg.param[0].Q = designEQ.eq.param[0].Q;

	iir_designer_deafult_eq_cfg.param[1].type = designEQ.eq.param[1].type;
	iir_designer_deafult_eq_cfg.param[1].gain = designEQ.eq.param[1].gain;
	iir_designer_deafult_eq_cfg.param[1].fc = designEQ.eq.param[1].fc;
	iir_designer_deafult_eq_cfg.param[1].Q = designEQ.eq.param[1].Q;

	iir_designer_deafult_eq_cfg.param[2].type = designEQ.eq.param[2].type;
	iir_designer_deafult_eq_cfg.param[2].gain = designEQ.eq.param[2].gain;
	iir_designer_deafult_eq_cfg.param[2].fc = designEQ.eq.param[2].fc;
	iir_designer_deafult_eq_cfg.param[2].Q = designEQ.eq.param[2].Q;

	iir_designer_deafult_eq_cfg.param[3].type = designEQ.eq.param[3].type;
	iir_designer_deafult_eq_cfg.param[3].gain = designEQ.eq.param[3].gain;
	iir_designer_deafult_eq_cfg.param[3].fc = designEQ.eq.param[3].fc;
	iir_designer_deafult_eq_cfg.param[3].Q = designEQ.eq.param[3].Q;

	iir_designer_deafult_eq_cfg.param[4].type = designEQ.eq.param[4].type;
	iir_designer_deafult_eq_cfg.param[4].gain = designEQ.eq.param[4].gain;
	iir_designer_deafult_eq_cfg.param[4].fc = designEQ.eq.param[4].fc;
	iir_designer_deafult_eq_cfg.param[4].Q = designEQ.eq.param[4].Q;

	iir_designer_deafult_eq_cfg.param[5].type = designEQ.eq.param[5].type;
	iir_designer_deafult_eq_cfg.param[5].gain = designEQ.eq.param[5].gain;
	iir_designer_deafult_eq_cfg.param[5].fc = designEQ.eq.param[5].fc;
	iir_designer_deafult_eq_cfg.param[5].Q = designEQ.eq.param[5].Q;
}


void app_audio_user_eq_init(designer_cfg userEQ)
{

	iir_user_eq_cfg.gain0 = userEQ.eq.gain0;
	iir_user_eq_cfg.gain1 = userEQ.eq.gain1;
	iir_user_eq_cfg.num = 10;

	iir_user_eq_cfg.param[0].type = userEQ.eq.param[0].type;
	iir_user_eq_cfg.param[0].gain = userEQ.eq.param[0].gain;
	iir_user_eq_cfg.param[0].fc = userEQ.eq.param[0].fc;
	iir_user_eq_cfg.param[0].Q = userEQ.eq.param[0].Q;

	iir_user_eq_cfg.param[1].type = userEQ.eq.param[1].type;
	iir_user_eq_cfg.param[1].gain = userEQ.eq.param[1].gain;
	iir_user_eq_cfg.param[1].fc = userEQ.eq.param[1].fc;
	iir_user_eq_cfg.param[1].Q = userEQ.eq.param[1].Q;

	iir_user_eq_cfg.param[2].type = userEQ.eq.param[2].type;
	iir_user_eq_cfg.param[2].gain = userEQ.eq.param[2].gain;
	iir_user_eq_cfg.param[2].fc = userEQ.eq.param[2].fc;
	iir_user_eq_cfg.param[2].Q = userEQ.eq.param[2].Q;

	iir_user_eq_cfg.param[3].type = userEQ.eq.param[3].type;
	iir_user_eq_cfg.param[3].gain = userEQ.eq.param[3].gain;
	iir_user_eq_cfg.param[3].fc = userEQ.eq.param[3].fc;
	iir_user_eq_cfg.param[3].Q = userEQ.eq.param[3].Q;

	iir_user_eq_cfg.param[4].type = userEQ.eq.param[4].type;
	iir_user_eq_cfg.param[4].gain = userEQ.eq.param[4].gain;
	iir_user_eq_cfg.param[4].fc = userEQ.eq.param[4].fc;
	iir_user_eq_cfg.param[4].Q = userEQ.eq.param[4].Q;

	iir_user_eq_cfg.param[5].type = userEQ.eq.param[5].type;
	iir_user_eq_cfg.param[5].gain = userEQ.eq.param[5].gain;
	iir_user_eq_cfg.param[5].fc = userEQ.eq.param[5].fc;
	iir_user_eq_cfg.param[5].Q = userEQ.eq.param[5].Q;

	iir_user_eq_cfg.param[6].type = userEQ.eq.param[6].type;
	iir_user_eq_cfg.param[6].gain = userEQ.eq.param[6].gain;
	iir_user_eq_cfg.param[6].fc = userEQ.eq.param[6].fc;
	iir_user_eq_cfg.param[6].Q = userEQ.eq.param[6].Q;

	iir_user_eq_cfg.param[7].type = userEQ.eq.param[7].type;
	iir_user_eq_cfg.param[7].gain = userEQ.eq.param[7].gain;
	iir_user_eq_cfg.param[7].fc = userEQ.eq.param[7].fc;
	iir_user_eq_cfg.param[7].Q = userEQ.eq.param[7].Q;

	iir_user_eq_cfg.param[8].type = userEQ.eq.param[8].type;
	iir_user_eq_cfg.param[8].gain = userEQ.eq.param[8].gain;
	iir_user_eq_cfg.param[8].fc = userEQ.eq.param[8].fc;
	iir_user_eq_cfg.param[8].Q = userEQ.eq.param[8].Q;

	iir_user_eq_cfg.param[9].type = userEQ.eq.param[9].type;
	iir_user_eq_cfg.param[9].gain = userEQ.eq.param[9].gain;
	iir_user_eq_cfg.param[9].fc = userEQ.eq.param[9].fc;
	iir_user_eq_cfg.param[9].Q = userEQ.eq.param[9].Q;
}


void app_audio_total_audio_eq_init()
{
	int i = 0;
	iir_total_audio_eq_cfg.num = iir_user_eq_cfg.num + iir_designer_deafult_eq_cfg.num;
	for (i = 0;i < EQ_DESIGNER_BANDS_NUM;i++) {
		memcpy((uint8_t*)&iir_total_audio_eq_cfg.param[i], (uint8_t*)&iir_designer_deafult_eq_cfg.param[i],sizeof(IIR_PARAM_T));
	}
	for (i = iir_designer_deafult_eq_cfg.num;i < iir_total_audio_eq_cfg.num;i++) {
		memcpy((uint8_t*)&iir_total_audio_eq_cfg.param[i], (uint8_t*)&iir_user_eq_cfg.param[i], sizeof(IIR_PARAM_T));
	}

	sys_fs = device_cfg.sample_rate;
}


void app_audio_eq_get_desinger_eq_cfg(designer_cfg * designer_eq_cfg)
{
	memcpy((uint8_t *)&designer_eq_cfg->eq, (uint8_t *)&iir_designer_deafult_eq_cfg, sizeof(iir_designer_deafult_eq_cfg));
	if (audio_sample_rate == 0)
		designer_eq_cfg->sample_rate = 48000;
	else
		designer_eq_cfg->sample_rate = audio_sample_rate;
}



void app_audio_eq_generate_coef(enum IIR_BIQUARD_TYPE type, int fs , int f0, float gain , float q , IirBiquardState * band)
{
	iirfilt_design(band, fs, f0, gain, q, type);
}

void app_audio_eq_coefs_write_fd(char* file_name, float *h, int len);

void app_audio_eq_get_designer_coefs(IirBiquardState bands[],float * buffer,int len)
{
	int i = 0;
	int num = device_cfg.eq.num;

	for (i = 0;i < num; i++) {
		
		app_audio_eq_generate_coef(device_cfg.eq.param[i].type, device_cfg.sample_rate, device_cfg.eq.param[i].fc,
			device_cfg.eq.param[i].gain, device_cfg.eq.param[i].Q,&bands[i]);
		float a[3] = { 1.f, bands[i].a1, bands[i].a2 };
		float b[3] = { bands[i].b0, bands[i].b1, bands[i].b2 };
		freqz(b, ARRAY_SIZE(b), a, ARRAY_SIZE(a), buffer, len);
//		file_name[10] = i;
//		app_audio_eq_coefs_write_fd(file_name,buffer,len);
	}
}

void app_audio_eq_get_user_coefs(IirBiquardState bands[], float * buffer, int len)
{
	int i = 0;
	int num = iir_user_eq_cfg.num;

	for (i = 0;i < num; i++) {

		app_audio_eq_generate_coef(iir_user_eq_cfg.param[i].type, audio_sample_rate, iir_user_eq_cfg.param[i].fc,
			iir_user_eq_cfg.param[i].gain, iir_user_eq_cfg.param[i].Q, &bands[i]);
		float a[3] = { 1.f, bands[i].a1, bands[i].a2 };
		float b[3] = { bands[i].b0, bands[i].b1, bands[i].b2 };
		freqz(b, ARRAY_SIZE(b), a, ARRAY_SIZE(a), buffer, len);
		//		file_name[10] = i;
		//		app_audio_eq_coefs_write_fd(file_name,buffer,len);
	}
}

void app_audio_eq_get_coefs_run(IirBiquardState bands[], float * buffer, int len)
{
	int i = 0;
	int num = iir_total_audio_eq_cfg.num;

	for (i = 0;i < num; i++) {

		app_audio_eq_generate_coef(iir_total_audio_eq_cfg.param[i].type, sys_fs, iir_total_audio_eq_cfg.param[i].fc,
			iir_total_audio_eq_cfg.param[i].gain, iir_total_audio_eq_cfg.param[i].Q, &bands[i]);
		float a[3] = { 1.f, bands[i].a1, bands[i].a2 };
		float b[3] = { bands[i].b0, bands[i].b1, bands[i].b2 };
		freqz(b, ARRAY_SIZE(b), a, ARRAY_SIZE(a), buffer, len);

	}
}

void app_audio_eq_coefs_write_fd(char* file_name,float *h, int len)
{
	FILE * fd;
	if(file_name == NULL)
		fd = fopen("test_freqz_mul.m", "w");
	else
		fd = fopen(file_name, "w");
	fprintf(fd, "h = [");
	for (int i = 0; i < len - 1; i++) {
		fprintf(fd, "%f, ", h[i]);
	}
	fprintf(fd, "];\n");
	fprintf(fd, "w = linspace(0, pi, length(h));\n");
	fprintf(fd, "plot(w, h);\n");
	fprintf(fd, "[a,b] = max(h);");
	fclose(fd);

}
typedef struct{
	float gain;
	float freq_point;
}freqz_point;
freqz_point designer_gain_max_point;
freqz_point total_gain_max_point;
int gain_cut_mask = 0;
float gain_cut_diff = 0.0f;
float gain_cut_diff_gain = 0.0f;

freqz_point app_audio_eq_get_curv_max_point(float * buffer, int len)
{
	freqz_point point;
	point.gain = buffer[0];
	point.freq_point = 0;

	for (int i = 0;i < len;i++) {
		if (point.gain < buffer[i]){
		point.gain = buffer[i];
		point.freq_point = i;
	}
		
	}
	return point;
}

void app_audio_eq_designer_eq_run(float *buffer, int len, designer_cfg designEQ)
{
	int i = 0;
	freqz_point desinger_eq_point;

	//	int buffer_len = sizeof(float) * len;
	//	float buffer[4 * 1024];
	for (i = 0;i < len;i++)
		buffer[i] = 1.0f;

	
	app_audio_designer_eq_init(designEQ);
	app_audio_eq_get_desinger_eq_cfg(&device_cfg);
	app_audio_eq_get_designer_coefs(designer_eq_coefs, buffer, len);	
	desinger_eq_point = app_audio_eq_get_curv_max_point(buffer, len);
	designer_gain_max_point = desinger_eq_point;
    LOGD("designer eq run max_point = %f /%f \n", desinger_eq_point.gain, desinger_eq_point.freq_point);

	//app_audio_eq_coefs_write_fd("designer_eq_coefs.m", buffer, len);
}

void app_audio_eq_user_eq_run(float *buffer, int len, designer_cfg userEQ)
{
	int i = 0;
	freqz_point user_eq_point;

	//	int buffer_len = sizeof(float) * len;
	//	float buffer[4 * 1024];
	for (i = 0;i < len;i++)
		buffer[i] = 1.0f;


	app_audio_user_eq_init(userEQ);
	app_audio_eq_get_user_coefs(user_eq_coefs, buffer, len);
	
	user_eq_point = app_audio_eq_get_curv_max_point(buffer, len);
    LOGD("user eq run max_point = %f /%f \n", user_eq_point.gain, user_eq_point.freq_point);

	//app_audio_eq_coefs_write_fd("user_eq_coefs.m", buffer, len);
}

void app_audio_total_eq_run(float *buffer, int len, designer_cfg userEQ)
{	
	int i = 0;
	freqz_point total_eq_point;

	for (i = 0;i < len;i++)
		buffer[i] = 1.0f;

	app_audio_user_eq_init(userEQ);
	app_audio_total_audio_eq_init();
	app_audio_eq_get_coefs_run(total_eq_coefs, buffer, len);	
	total_eq_point = app_audio_eq_get_curv_max_point(buffer, len);
	total_gain_max_point = total_eq_point;
    LOGD("eq run max_point = %f /%f \n", total_eq_point.gain, total_eq_point.freq_point);

	//app_audio_eq_coefs_write_fd("total_eq_coefs.m", buffer, len);
}

#if 0
int main(int argc, char **argv)
{
	test_cpx_div();

	// design a peaking filter
	int fs = 48000;
	int f0 = 2000;
	float gain = 6.f;
	float q = 0.707f;
	enum IIR_BIQUARD_TYPE type = IIR_BIQUARD_PEAKINGEQ;
	IirBiquardState band;
	iirfilt_design(&band, fs, f0, gain, q, type);

	float a[3] = { 1.f, band.a1, band.a2 };
	float b[3] = { band.b0, band.b1, band.b2 };

	int len = 1024;
	float *h = (float *)malloc(sizeof(float) * len);
	for (int i = 0; i < len;i++)
		h[i] = 1.0f;
	freqz(b, ARRAY_SIZE(b), a, ARRAY_SIZE(a), h, len);

	FILE *fd = fopen("test_freqz_2.m", "w");
	fprintf(fd, "h = [");
	for (int i = 0; i < len - 1; i++) {
		fprintf(fd, "%f, ", h[i]);
	}
	fprintf(fd, "];\n");
	fprintf(fd, "w = linspace(0, pi, length(h));\n");
	fprintf(fd, "plot(w, h);\n");
	fclose(fd);


	return 0;
}
#else

#define err_threshold 0.5

//int main(int argc, char **argv)
//{
//    int len = 1024;
//    float *buffer = (float *)malloc(sizeof(float) * len);
//    float gain_max_diff = 0;
//
//    app_audio_eq_designer_eq_run(buffer,len);
//    app_audio_eq_user_eq_run(buffer,len);
//    app_audio_total_eq_run(buffer,len);
//    gain_max_diff = total_gain_max_point.gain - designer_gain_max_point.gain;
//    if (total_gain_max_point.gain > designer_gain_max_point.gain) {
//        gain_cut_mask = 1;
//        gain_cut_diff = gain_max_diff;
//        gain_cut_diff_gain = 20 * log10(total_gain_max_point.gain) - 20 * log10(designer_gain_max_point.gain);
//        gain_cut_diff_gain = gain_cut_diff_gain + err_threshold;
//        printf("gain_cut_diff_gain = %f", gain_cut_diff_gain);
//    }
//    else {
//        printf("no need to adjust gain , hold");
//    }
//
//    printf("gain_max_diff = %f \n", gain_max_diff);
//    printf("db:gain_max_diff = %f \n", 20 * log10(total_gain_max_point.gain) - 20 * log10(designer_gain_max_point.gain));
//
//    free(buffer);
//    while (1);
//    return 0;
//}

float calculateCalib(designer_cfg designEQ, designer_cfg userEQ)
{
    int len = 1024;
    float *buffer = (float *)malloc(sizeof(float) * len);
    float gain_max_diff = 0;
    
    app_audio_eq_designer_eq_run(buffer, len, designEQ);
    app_audio_eq_user_eq_run(buffer, len, userEQ);
    app_audio_total_eq_run(buffer, len, userEQ);
    gain_max_diff = total_gain_max_point.gain - designer_gain_max_point.gain;
    LOGD("total gain = %f, designer gain = %f \n", total_gain_max_point.gain,designer_gain_max_point.gain);
    if (total_gain_max_point.gain > designer_gain_max_point.gain) {
        gain_cut_mask = 1;
        gain_cut_diff = gain_max_diff;
        gain_cut_diff_gain = 20 * log10(total_gain_max_point.gain) - 20 * log10(designer_gain_max_point.gain);
        gain_cut_diff_gain = gain_cut_diff_gain + err_threshold;
        LOGD("gain_cut_diff_gain = %f", gain_cut_diff_gain);
    }
    else {
        LOGD("no need to adjust gain , hold");
    }

    LOGD("gain_max_diff = %f \n", gain_max_diff);
    LOGD("db:gain_max_diff = %f \n", 20 * log10(total_gain_max_point.gain) - 20 * log10(designer_gain_max_point.gain));
    
    free(buffer);
    return gain_cut_diff_gain;
}
#endif
