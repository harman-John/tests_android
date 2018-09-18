#include "jbl_stc_com_activity_DashboardActivity.h"
#include <android/log.h>
#include <jni.h>


#ifdef __cplusplus
extern "C" {
#endif

#include "inc/EQAlgorithm.h"

#define TAG "calculateCalib-jni"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG,TAG ,__VA_ARGS__)



int getInt(JNIEnv *env, jclass cls, jobject obj, const char *name, const char *sig) {
    jfieldID defFieldID = env->GetFieldID(cls, name, sig);
    jint defVal = env->GetIntField(obj, defFieldID);
    return defVal;
}

float getFloat(JNIEnv *env, jclass cls, jobject obj, const char *name, const char *sig) {
    jfieldID defFieldID = env->GetFieldID(cls, name, sig);
    jfloat defVal = env->GetFloatField(obj, defFieldID);
    return defVal;
}

IIR_BIQUARD_TYPE getType(int type) {
    switch (type) {
        case IIR_BIQUARD_PASS:
            return IIR_BIQUARD_PASS;
        case IIR_BIQUARD_LPF:
            return IIR_BIQUARD_LPF;
        case IIR_BIQUARD_HPF:
            return IIR_BIQUARD_HPF;
        case IIR_BIQUARD_BPF0:
            return IIR_BIQUARD_BPF0;
        case IIR_BIQUARD_BPF1:
            return IIR_BIQUARD_BPF1;
        case IIR_BIQUARD_NOTCH:
            return IIR_BIQUARD_NOTCH;
        case IIR_BIQUARD_APF:
            return IIR_BIQUARD_APF;
        case IIR_BIQUARD_PEAKINGEQ:
            return IIR_BIQUARD_PEAKINGEQ;
        case IIR_BIQUARD_LOWSHELF:
            return IIR_BIQUARD_LOWSHELF;
        case IIR_BIQUARD_HIGHSHELF:
            return IIR_BIQUARD_HIGHSHELF;
        case IIR_BIQUARD_QTY:
            return IIR_BIQUARD_QTY;
    }
    return IIR_BIQUARD_PASS;
}

designer_cfg getEq(JNIEnv *env, jobject defObj, jobjectArray defParamObjArray, jint def_size) {
    designer_cfg designEQ = designer_cfg();

    jclass clsDesignEq = env->GetObjectClass(defObj);
    if (clsDesignEq == NULL) {
        LOGD("calculate calibration, in jni, design eq is null");
        return designEQ;
    }

    float mGain0 = getFloat(env, clsDesignEq, defObj, "mGain0", "F");
    float mGain1 = getFloat(env, clsDesignEq, defObj, "mGain1", "F");
    int num = getInt(env, clsDesignEq, defObj, "mNum", "I");
    int sampleRate = getInt(env, clsDesignEq, defObj, "mSampleRate", "I");
    LOGD("calculate calibration, in jni, mGain0 = %f, mGain1 = %f, num = %d, sampleRate = %d\n",
         mGain0, mGain1, num, sampleRate);

    IIR_CFG_T iir_cfg_t;
    iir_cfg_t.gain0 = mGain0;
    iir_cfg_t.gain1 = mGain1;
    iir_cfg_t.num = num;

    IIR_PARAM_T iir_param_t[def_size];
    for (int i = 0; i < def_size; i++) {
        jobject objPArray = env->GetObjectArrayElement(defParamObjArray, i);
        if (objPArray == NULL) {
            LOGD("calculate calibration, in jni, object param array is null");
            return designEQ;
        }
        jclass clsPArray = env->GetObjectClass(objPArray);
        iir_param_t[i].type = getType(getInt(env, clsPArray, objPArray, "mType", "I"));
        iir_param_t[i].gain = getFloat(env, clsPArray, objPArray, "mGain", "F");
        iir_param_t[i].fc = getFloat(env, clsPArray, objPArray, "mFc", "F");
        iir_param_t[i].Q = getFloat(env, clsPArray, objPArray, "mQ", "F");
        LOGD("calculate calibration, i = %d, type = %d, Gain = %f , fc = %f, Q = %f \n", i,iir_param_t[i].type,iir_param_t[i].gain, iir_param_t[i].fc, iir_param_t[i].Q);
        iir_cfg_t.param[i] = iir_param_t[i];
    }
    designEQ.eq = iir_cfg_t;
    designEQ.sample_rate = sampleRate;
    return designEQ;
}

JNIEXPORT jfloat JNICALL Java_jbl_stc_com_activity_DashboardActivity_calculateCalib
        (JNIEnv *env, jobject obj, jobject defObj, jobjectArray defParamObjArray, jint def_size,
         jobject userObj, jobjectArray userParamObjArray, jint user_size) {

    LOGD("calculate calibration, in jni");
    designer_cfg designEQ = getEq(env,defObj,defParamObjArray,def_size);
    designer_cfg userEQ = getEq(env,userObj,userParamObjArray,user_size);
    float a = calculateCalib(designEQ,userEQ);
    LOGD("calculate calibration, in jni, a = %f", a);
    return a;
}
#ifdef __cplusplus
}
#endif