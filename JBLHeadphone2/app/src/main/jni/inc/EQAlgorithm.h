//
//  EQAlgorithm.h
//  BESEngine
//
//  Created by software on 2018/8/21.
//  Copyright © 2018 software. All rights reserved.
//

#ifndef EQAlgorithm_h
#define EQAlgorithm_h

#include <stdio.h>
#include <stdlib.h>
#include "signal.h"
#include "iirfilt.h"
#include "complex_op.h"
#include "ae_common.h"
#include "string.h"
#include "math.h"

#define IIR_PARAM_NUM 20

typedef struct {
    enum IIR_BIQUARD_TYPE  type;
    float       gain;
    float       fc;
    float       Q;
} IIR_PARAM_T;

typedef struct {
    float   gain0;
    float   gain1;
    int     num;
    IIR_PARAM_T param[IIR_PARAM_NUM];
} IIR_CFG_T;


typedef struct{
    IIR_CFG_T eq;
    int sample_rate;
}designer_cfg;

float calculateCalib(designer_cfg designEQ, designer_cfg userEQ);

#endif /* EQAlgorithm_h */
