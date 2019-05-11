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
package com.fang.myapplication.player;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.util.Log;
import android.view.Surface;

import com.fang.myapplication.model.NALPacket;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class VideoPlayer extends Thread {

    private static final String TAG = "VideoPlayer";

    private String mMimeType = "video/avc";
    private MediaCodec.BufferInfo mBufferInfo = new MediaCodec.BufferInfo();
    private MediaCodec mDecoder = null;
    private Surface mSurface = null;
    private boolean mIsEnd = false;
    private List<NALPacket> mListBuffer = Collections.synchronizedList(new ArrayList<NALPacket>());

    public VideoPlayer(Surface surface) {
        mSurface = surface;
    }

    public void addPacker(NALPacket nalPacket) {
        if (isSpsPps(nalPacket)) {
            // sps pps
            try {
                if (mDecoder != null) {
                    mDecoder.stop();
                }
                MediaFormat format = MediaFormat.createVideoFormat(mMimeType, nalPacket.width, nalPacket.height);
                format.setByteBuffer("csd-0", ByteBuffer.wrap(getSps(nalPacket.nalData)));
                format.setByteBuffer("csd-1", ByteBuffer.wrap(getPps(nalPacket.nalData)));
                mDecoder = MediaCodec.createDecoderByType(mMimeType);
                mDecoder.configure(format, mSurface, null, 0);
                mDecoder.setVideoScalingMode(MediaCodec.VIDEO_SCALING_MODE_SCALE_TO_FIT);
                mDecoder.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            // pic
            mListBuffer.add(nalPacket);
        }

    }

    private boolean isSpsPps(NALPacket nalPacket) {
        return (nalPacket.nalData[4] & 0x1F) == 7;
    }

    private int getIndex(byte[] nalData) {
        for (int i = 4; i < nalData.length - 3; i++) {
            if (nalData[i] == 0 && nalData[i + 1] == 0 && nalData[i + 2] == 0 && nalData[i + 3] == 1) {
                return i;
            }
        }
        return -1;
    }

    private byte[] getSps(byte[] nalData) {
        int index = getIndex(nalData);
        byte[] sps = new byte[index];
        System.arraycopy(nalData, 0, sps, 0, sps.length);
        return sps;
    }

    private byte[] getPps(byte[] nalData) {
        int index = getIndex(nalData);
        byte[] pps = new byte[nalData.length - index];
        System.arraycopy(nalData, index, pps, 0, pps.length);
        return pps;
    }
    
    @Override
    public void run() {
        super.run();
        while (!mIsEnd) {
            if (mListBuffer.size() == 0) {
                try {
                    sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                continue;
            }
            doDecode(mListBuffer.remove(0));
        }
    }

    private void doDecode(NALPacket nalPacket) {
        final int TIMEOUT_USEC = 10000;
        ByteBuffer[] decoderInputBuffers = mDecoder.getInputBuffers();
        int inputBufIndex = -10000;
        try {
            inputBufIndex = mDecoder.dequeueInputBuffer(TIMEOUT_USEC);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (inputBufIndex >= 0) {
            ByteBuffer inputBuf = decoderInputBuffers[inputBufIndex];
            inputBuf.put(nalPacket.nalData);
            mDecoder.queueInputBuffer(inputBufIndex, 0, nalPacket.nalData.length, nalPacket.pts, 0);
        } else {
            Log.d(TAG, "dequeueInputBuffer failed");
        }

        int outputBufferIndex = -10000;
        try {
            outputBufferIndex = mDecoder.dequeueOutputBuffer(mBufferInfo, TIMEOUT_USEC);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (outputBufferIndex >= 0) {
            mDecoder.releaseOutputBuffer(outputBufferIndex, true);
            try{
                Thread.sleep(10);
            }  catch (InterruptedException ie){
                ie.printStackTrace();
            }
        } else if (outputBufferIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
            try{
                Thread.sleep(10);
            }  catch (InterruptedException ie){
                ie.printStackTrace();
            }
        } else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
            // not important for us, since we're using Surface

        } else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {

        } else {

        }

    }
}
