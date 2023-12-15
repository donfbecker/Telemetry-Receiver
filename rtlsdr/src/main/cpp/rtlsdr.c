#include <jni.h>
#include <stdlib.h>
#include <string.h>

#include "rtl-sdr.h"

JNIEXPORT jint JNICALL
Java_com_donfbecker_rtlsdr_RtlSdr_getDeviceCount(JNIEnv *env, jobject instance) {
    return rtlsdr_get_device_count();
}
