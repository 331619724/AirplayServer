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

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

import com.fang.myapplication.model.PCMPacket;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AudioPlayer extends Thread {

    private AudioTrack mTrack;
    private int mChannel = AudioFormat.CHANNEL_OUT_STEREO;
    private int mSampleRate = 44100;
    private boolean isStopThread = false;
    private int mAudioFormat = AudioFormat.ENCODING_PCM_16BIT;
    private List<PCMPacket> mListBuffer = Collections.synchronizedList(new ArrayList<PCMPacket>());

    public AudioPlayer() {
        this.mTrack = new AudioTrack(AudioManager.STREAM_MUSIC, mSampleRate, mChannel, mAudioFormat,
                AudioTrack.getMinBufferSize(mSampleRate, mChannel, mAudioFormat), AudioTrack.MODE_STREAM);
        this.mTrack.play();
    }

    public void addPacker(PCMPacket pcmPacket) {
        mListBuffer.add(pcmPacket);
    }

    @Override
    public void run() {
        super.run();
        while (!isStopThread) {
            if (mListBuffer.size() == 0) {
                try {
                    Thread.sleep(2);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                doPlay(mListBuffer.remove(0));
            }
        }
    }

    private void doPlay(PCMPacket pcmPacket) {
        if (mTrack != null) {
            mTrack.write(pcmPacket.data, 0, 960);
        }
    }


    public void stopPlay() {
        isStopThread = true;
        interrupt();
        if (mTrack != null) {
            mTrack.flush();
            mTrack.stop();
            mTrack.release();
            mTrack = null;
        }
    }

}
