package com.jiazhu.demo.audio;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Message;

import com.jiazhu.demo.encoder.Mp3Encoder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Mp3Recorder {
    // 默认采样率
    private static final int DEFAULT_SAMPLING_RATE = 44100;
    // 转换周期，录音每满160帧，进行一次转换
    private static final int FRAME_COUNT = 160;
    // 输出MP3码率
    private static final int BIT_RATE = 32;
    private static final int MAX_VOLUME = 2000;

    private AudioRecord mAudioRecord;
    private int mBufferSize;
    private File mMp3File;
    private int mVolume;
    private short[] mPCMBuffer;
    private FileOutputStream mOutputStream;
    private Mp3EncodeThread mEncodeThread;
    private int mSamplingRate;
    private int mChannelConfig;
    private PCMFormat mAudioFormat;
    private boolean mIsRecording;
    private ExecutorService mExecutor = Executors.newSingleThreadExecutor();
    private OnFinishListener mFinishListener;

    public Mp3Recorder() {
        this(DEFAULT_SAMPLING_RATE, AudioFormat.CHANNEL_IN_MONO, PCMFormat.PCM_16BIT);
    }

    public Mp3Recorder(int samplingRate, int channelConfig, PCMFormat audioFormat) {
        this.mSamplingRate = samplingRate;
        this.mChannelConfig = channelConfig;
        this.mAudioFormat = audioFormat;
    }

    public void startRecording(String path) {
        startRecording(new File(path));
    }

    public void startRecording(File mp3File) {
        if (mIsRecording) {
            return;
        }
        this.mMp3File = mp3File;
        if (mAudioRecord == null) {
            initAudioRecorder();
        }
        mAudioRecord.startRecording();

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                mIsRecording = true;
                while (mIsRecording) {
                    int readSize = mAudioRecord.read(mPCMBuffer, 0, mBufferSize);
                    if (readSize > 0) {
                        mEncodeThread.addChangeBuffer(mPCMBuffer, readSize);
                        calculateRealVolume(mPCMBuffer, readSize);
                    }
                }

                try {
                    // 录音完毕，释放AudioRecord的资源
                    mAudioRecord.stop();
                    mAudioRecord.release();
                    mAudioRecord = null;

                    Message msg = Message.obtain(mEncodeThread.getHandler(), Mp3EncodeThread.PROCESS_STOP);
                    msg.sendToTarget();

                    mEncodeThread.join();
                    if (mFinishListener != null) {
                        mFinishListener.onFinish(mMp3File.getPath());
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    if (mOutputStream != null) {
                        try {
                            mOutputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        };
        mExecutor.execute(runnable);
    }

    public void stopRecording() {
        mIsRecording = false;
        Mp3Encoder.close();
        if (mAudioRecord != null) {
            mAudioRecord.stop();
            mAudioRecord.release();
            mAudioRecord = null;
        }
    }

    public boolean isRecording() {
        return mIsRecording;
    }

    private void calculateRealVolume(short[] buffer, int readSize) {
        double sum = 0;
        for (int i = 0; i < readSize; i++) {
            sum += buffer[i] * buffer[i];
        }
        if (readSize > 0) {
            double amplitude = sum / readSize;
            mVolume = (int) Math.sqrt(amplitude);
        }
    }

    public int getVolume() {
        if (mVolume >= MAX_VOLUME) {
            return mVolume;
        }
        return mVolume;
    }

    public int getMaxVolume() {
        return MAX_VOLUME;
    }

    private void initAudioRecorder() {
        int bytePerFrame = mAudioFormat.getBytesPerFrame();
        int minBufferSize = AudioRecord.getMinBufferSize(mSamplingRate, mChannelConfig, mAudioFormat.getAudioFormat()) / bytePerFrame;
        if (minBufferSize % FRAME_COUNT != 0) {
            minBufferSize = minBufferSize + (FRAME_COUNT - minBufferSize % FRAME_COUNT);
        }
        mBufferSize = minBufferSize * bytePerFrame;
        mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, mSamplingRate, mChannelConfig, mAudioFormat.getAudioFormat(), mBufferSize);
        mPCMBuffer = new short[mBufferSize];

        Mp3Encoder.init(mSamplingRate, 1, mSamplingRate, BIT_RATE);
        try {
            mOutputStream = new FileOutputStream(mMp3File);

            mEncodeThread = new Mp3EncodeThread(mOutputStream, mBufferSize);
            mEncodeThread.start();

            mAudioRecord.setRecordPositionUpdateListener(mEncodeThread, mEncodeThread.getHandler());
            mAudioRecord.setPositionNotificationPeriod(FRAME_COUNT);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void setFinishListener(OnFinishListener listener) {
        this.mFinishListener = listener;
    }

    public interface OnFinishListener {
        void onFinish(String mp3SavePath);
    }
}
