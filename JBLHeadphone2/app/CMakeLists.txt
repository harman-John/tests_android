cmake_minimum_required(VERSION 3.6.0)



add_library( # Specifies the name of the library.
             calculateCalib
             SHARED
             src/main/jni/src/complex_op.c
             src/main/jni/src/EQAlgorithm.c
             src/main/jni/src/freqz.c
             src/main/jni/src/iirfilt.c
             src/main/jni/src/iirfilt_design.c
             src/main/jni/calculateCalib.cpp )


include_directories( inc )

find_library( log-lib
              log )

target_link_libraries( calculateCalib
                       ${log-lib} )
