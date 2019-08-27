package com.jiazhu.demo.audio;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

import com.jiazhu.demo.encoder.Mp3EncoderTwo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Mp3RecorderTwo {
    // 默认采样率
    private static final int DEFAULT_SAMPLING_RATE = 44100;
    // 输出MP3码率
    private static final int BIT_RATE = 32;

    private int mSamplingRate;
    private int mChannelConfig;
    private PCMFormat mAudioFormat;

    private int mBufferSize;
    private AudioRecord mAudioRecord;
    private boolean mIsRecording;
    private File mPcmFile;
    private FileOutputStream mOutputStream;
    private ExecutorService mExecutor = Executors.newSingleThreadExecutor();
    private OnTransformFinishListener mTransFinishListener;

    public Mp3RecorderTwo() {
        this(DEFAULT_SAMPLING_RATE, AudioFormat.CHANNEL_IN_MONO, PCMFormat.PCM_16BIT);
    }

    public Mp3RecorderTwo(int samplingRate, int channelConfig, PCMFormat audioFormat) {
        this.mSamplingRate = samplingRate;
        this.mChannelConfig = channelConfig;
        this.mAudioFormat = audioFormat;
    }

    public void startRecording(String path) {
        startRecording(new File(path));
    }

    public void startRecording(File pcmFile) {
        if (mIsRecording) {
            return;
        }
        int bytePerFrame = mAudioFormat.getBytesPerFrame();
        final int minBufferSize = AudioRecord.getMinBufferSize(mSamplingRate, mChannelConfig, mAudioFormat.getAudioFormat());
        final byte[] data = new byte[minBufferSize];
        mBufferSize = minBufferSize * bytePerFrame;
        mPcmFile = pcmFile;
        if (mAudioRecord == null) {
            initAudioRecorder();
        }

        mAudioRecord.startRecording();

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                mIsRecording = true;
                while (mIsRecording) {
                    int read = mAudioRecord.read(data, 0, minBufferSize);
                    if (AudioRecord.ERROR_INVALID_OPERATION != read) {
                        try {
                            mOutputStream.write(data);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                try {
                    mOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        mExecutor.execute(runnable);
    }

    public void stopRecording() {
        mIsRecording = false;
        if (mAudioRecord != null) {
            mAudioRecord.stop();
            mAudioRecord.release();
            mAudioRecord = null;
        }
    }

    public boolean isRecording() {
        return mIsRecording;
    }

    public void transformPCM2Mp3() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (mPcmFile != null) {
                    String mp3FilePath = mPcmFile.getAbsolutePath().replace("pcm", "mp3");
                    File mp3File = new File(mp3FilePath);
                    if (mp3File.exists()) {
                        mp3File.delete();
                    }
                    Mp3EncoderTwo.init(mPcmFile.getAbsolutePath(), mChannelConfig, BIT_RATE, mSamplingRate, mp3FilePath);
                    Mp3EncoderTwo.encode();
                    Mp3EncoderTwo.destroy();
                    if (mTransFinishListener != null) {
                        mTransFinishListener.onTransFinish(mp3FilePath);
                    }
                }
            }
        };
        mExecutor.execute(runnable);
    }

    private void initAudioRecorder() {
        mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, mSamplingRate, mChannelConfig, mAudioFormat.getAudioFormat(), mBufferSize);
        try {
            mOutputStream = new FileOutputStream(mPcmFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void setFinishListener(OnTransformFinishListener listener) {
        this.mTransFinishListener = listener;
    }

    public interface OnTransformFinishListener {
        void onTransFinish(String mp3SavePath);
    }
}
