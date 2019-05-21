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

#include <malloc.h>
#include "aac_decoder.h"
#include "fdk-aac/libAACdec/include/aacdecoder_lib.h"
#include "fdk-aac/libFDK/include/clz.h"
#include "fdk-aac/libSYS/include/FDK_audio.h"
#include "logger.h"

struct aac_decoder_s {
    logger_t *logger;
    HANDLE_AACDECODER phandle;
};

static int fdk_flags = 0;

HANDLE_AACDECODER
create_fdk_aac_decoder(logger_t *logger)
{
    int ret = 0;
    UINT nrOfLayers = 1;
    HANDLE_AACDECODER phandle = aacDecoder_Open(TT_MP4_RAW, nrOfLayers);
    if (phandle == NULL) {
        logger_log(logger, LOGGER_DEBUG, "aacDecoder open faild!\n");
        return NULL;
    }
    /* ASC config binary data */
    UCHAR eld_conf[] = { 0xF8, 0xE8, 0x50, 0x00 };
    UCHAR *conf[] = { eld_conf };
    static UINT conf_len = sizeof(eld_conf);
    ret = aacDecoder_ConfigRaw(phandle, conf, &conf_len);
    if (ret != AAC_DEC_OK) {
        logger_log(logger, LOGGER_DEBUG, "Unable to set configRaw\n");
        return NULL;
    }
    CStreamInfo *aac_stream_info = aacDecoder_GetStreamInfo(phandle);
    if (aac_stream_info == NULL) {
        logger_log(logger, LOGGER_DEBUG, "aacDecoder_GetStreamInfo failed!\n");
        return NULL;
    }
    logger_log(logger, LOGGER_DEBUG, "> stream info: channel = %d\tsample_rate = %d\tframe_size = %d\taot = %d\tbitrate = %d\n",   \
            aac_stream_info->channelConfig, aac_stream_info->aacSampleRate,
               aac_stream_info->aacSamplesPerFrame, aac_stream_info->aot, aac_stream_info->bitRate);
    return phandle;
}

aac_decoder_t *
aac_create(logger_t *logger)
{
    aac_decoder_t *aac_decoder = malloc(sizeof(aac_decoder_t));
    aac_decoder->logger = logger;
    aac_decoder->phandle = create_fdk_aac_decoder(logger);
    return aac_decoder;
}

int
aac_decode_frame(aac_decoder_t *aac_decoder, unsigned char *input, int payloadsize, void *output, int pcm_pkt_size)
{
    int ret = 0;
    int pkt_size = payloadsize;
    UINT valid_size = payloadsize;
    UCHAR *input_buf[1] = {input};
    ret = aacDecoder_Fill(aac_decoder->phandle, input_buf, &pkt_size, &valid_size);
    if (ret != AAC_DEC_OK) {
        logger_log(aac_decoder->logger, LOGGER_ERR, "aacDecoder_Fill error : %x", ret);
        return ret;
    }
    ret = aacDecoder_DecodeFrame(aac_decoder->phandle, output, pcm_pkt_size, fdk_flags);
    if (ret != AAC_DEC_OK) {
        logger_log(aac_decoder->logger, LOGGER_ERR, "aacDecoder_DecodeFrame error : 0x%x", ret);
    }
    return ret;
}

void
aac_free(aac_decoder_t *aac_decoder)
{
    if (aac_decoder) {
        aacDecoder_Close(aac_decoder->phandle);
        free(aac_decoder);
    }

}