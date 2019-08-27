package com.jiazhu.demo.audio;

import android.media.AudioFormat;

public enum PCMFormat {
    PCM_8BIT(1, AudioFormat.ENCODING_PCM_8BIT),
    PCM_16BIT(2, AudioFormat.ENCODING_PCM_16BIT);

    private int mBytesPerFrame;
    private int mAudioFormat;

    private PCMFormat(int bytesPerFrame, int audioFormat) {
        this.mBytesPerFrame = bytesPerFrame;
        this.mAudioFormat = audioFormat;
    }

    public int getBytesPerFrame() {
        return mBytesPerFrame;
    }

    public void setBytesPerFrame(int mBytesPerFrame) {
        this.mBytesPerFrame = mBytesPerFrame;
    }

    public int getAudioFormat() {
        return mAudioFormat;
    }

    public void setAudioFormat(int mAudioFormat) {
        this.mAudioFormat = mAudioFormat;
    }
}
