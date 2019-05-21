/*
 * MIT License
 *
 * Copyright (c) 2019 dsafa22, All Rights Reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

#include <jni.h>
#include <stddef.h>
#include "lib/raop.h"
#include "lib/dnssd.h"
#include "log.h"
#include "lib/stream.h"
#include "lib/logger.h"
#include <malloc.h>
#include <cstring>
#include <math.h>

static JavaVM* g_JavaVM;

void OnRecvAudioData(void *observer, pcm_data_struct *data) {
    jobject obj = (jobject) observer;
    JNIEnv* jniEnv = NULL;
    g_JavaVM->AttachCurrentThread(&jniEnv, NULL);
    jclass cls = jniEnv->GetObjectClass(obj);
    jmethodID onRecvVideoDataM = jniEnv->GetMethodID(cls, "onRecvAudioData", "([SJ)V");
    jniEnv->DeleteLocalRef(cls);
    jshortArray sarr = jniEnv->NewShortArray(data->data_len);
    if (sarr == NULL) return;
    jniEnv->SetShortArrayRegion(sarr, (jint) 0, data->data_len, (jshort *) data->data);
    jniEnv->CallVoidMethod(obj, onRecvVideoDataM, sarr, data->pts);
    jniEnv->DeleteLocalRef(sarr);
    g_JavaVM->DetachCurrentThread();
}

void OnRecvVideoData(void *observer, h264_decode_struct *data) {
    jobject obj = (jobject) observer;
    JNIEnv* jniEnv = NULL;
    g_JavaVM->AttachCurrentThread(&jniEnv, NULL);
    jclass cls = jniEnv->GetObjectClass(obj);
    jmethodID onRecvVideoDataM = jniEnv->GetMethodID(cls, "onRecvVideoData", "([BIJJII)V");
    jniEnv->DeleteLocalRef(cls);
    jbyteArray barr = jniEnv->NewByteArray(data->data_len);
    if (barr == NULL) return;
    jniEnv->SetByteArrayRegion(barr, (jint) 0, data->data_len, (jbyte *) data->data);
    jniEnv->CallVoidMethod(obj, onRecvVideoDataM, barr, data->frame_type,
                                         data->pts, data->pts, data->width, data->height);
    free(data->data);
    jniEnv->DeleteLocalRef(barr);
    g_JavaVM->DetachCurrentThread();
}

// 目前只含有音量
typedef struct {

    float volume;

} audio_session_t;

extern "C" void *
audio_init(void *cls)
{
    audio_session_t *session;
    session = (audio_session_t *)calloc(1, sizeof(audio_session_t));
    session->volume = 1.0f;
    return session;
}

extern "C" void
audio_process(void *cls, void *opaque, pcm_data_struct *data)
{
    audio_session_t *session = (audio_session_t *)opaque;
    for (int i = 0; i < data->data_len; i++) {
        data->data[i] = data->data[i] * session->volume;
    }
    OnRecvAudioData(cls, data);
}

extern "C" void
audio_destroy(void *cls, void *opaque)
{
    audio_session_t *session = (audio_session_t *)opaque;
    free(session);
}
// 处理音量变化
extern "C" void
audio_set_volume(void *cls, void *opaque, float volume)
{
    audio_session_t *session = (audio_session_t *)opaque;
    session->volume = pow(10.0, 0.05*volume);
}

extern "C" void
video_process(void *cls, h264_decode_struct *data)
{
    OnRecvVideoData(cls, data);
}

extern "C" void
log_callback(void *cls, int level, const char *msg) {
    switch (level) {
        case LOGGER_DEBUG: {
            LOGD("%s", msg);
            break;
        }
        case LOGGER_WARNING: {
            LOGW("%s", msg);
            break;
        }
        case LOGGER_INFO: {
            LOGI("%s", msg);
            break;
        }
        case LOGGER_ERR: {
            LOGE("%s", msg);
            break;
        }
        default:break;
    }
}

extern "C" JNIEXPORT jint JNICALL
JNI_OnLoad(JavaVM* vm, void* reserved) {
    g_JavaVM = vm;
    return JNI_VERSION_1_6;
}

static dnssd_t *dnssd;

extern "C" JNIEXPORT jlong JNICALL
Java_com_fang_myapplication_RaopServer_start(JNIEnv* env, jobject object, jstring deviceName, jbyteArray hwAddr, jint airplayPort) {
    raop_t *raop;
    raop_callbacks_t raop_cbs;
    memset(&raop_cbs, 0, sizeof(raop_cbs));
    raop_cbs.cls = (void *) env->NewGlobalRef(object);
    raop_cbs.audio_init = audio_init;
    raop_cbs.audio_process = audio_process;
    raop_cbs.audio_destroy = audio_destroy;
    raop_cbs.audio_set_volume = audio_set_volume;
    raop_cbs.video_process = video_process;
    raop = raop_init(10, &raop_cbs);
    if (raop == NULL) {
        LOGE("raop = NULL");
        return 0;
    } else {
        LOGD("raop init success");
    }
    raop_set_log_callback(raop, log_callback, NULL);
    raop_set_log_level(raop, RAOP_LOG_DEBUG);
    unsigned short port = 0;
    raop_start(raop, &port);
    raop_set_port(raop, port);
    const char *device_name = env->GetStringUTFChars(deviceName, 0);
    jbyte *hw_addr = env->GetByteArrayElements(hwAddr, 0);
    jsize hw_addr_len = env->GetArrayLength(hwAddr);
    int error;
    dnssd = dnssd_init(device_name, (char *)hw_addr, hw_addr_len, &error);
    env->ReleaseByteArrayElements(hwAddr, hw_addr, 0);
    env->ReleaseStringUTFChars(deviceName, device_name);
    if (dnssd == NULL) {
        LOGD("dnssd init error = %d", error);
        raop_destroy(raop);
        return 0;
    }
    dnssd_register_raop(dnssd, port);
    dnssd_register_airplay(dnssd, airplayPort);
    LOGD("raop port = %d, airplay port = %d", port, airplayPort);
    return (jlong) (void *) raop;
}

extern "C" JNIEXPORT jint JNICALL
Java_com_fang_myapplication_RaopServer_getPort(JNIEnv* env, jobject object, jlong opaque) {
    raop_t *raop = (raop_t *) (void *) opaque;
    return raop_get_port(raop);
}

extern "C" JNIEXPORT void JNICALL
Java_com_fang_myapplication_RaopServer_stop(JNIEnv* env, jobject object, jlong opaque) {
    raop_t *raop = (raop_t *) (void *) opaque;
    jobject obj = (jobject) raop_get_callback_cls(raop);
    raop_destroy(raop);
    env->DeleteGlobalRef(obj);
    dnssd_unregister_raop(dnssd);
    dnssd_unregister_airplay(dnssd);
    dnssd_destroy(dnssd);
}
