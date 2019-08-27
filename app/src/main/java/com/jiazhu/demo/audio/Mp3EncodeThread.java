package com.jiazhu.demo.audio;

import android.media.AudioRecord;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;

import com.jiazhu.demo.encoder.Mp3Encoder;

import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class Mp3EncodeThread extends Thread implements AudioRecord.OnRecordPositionUpdateListener {
    public static final int PROCESS_STOP = 1;

    private StopHandler mHandler;
    private byte[] mMp3Buffer;
    private List<ChangeBuffer> mChangeBuffers = Collections.synchronizedList(new LinkedList<ChangeBuffer>());
    private FileOutputStream mOutputStream;
    private CountDownLatch mHandlerInitLatch = new CountDownLatch(1);

    public Mp3EncodeThread(FileOutputStream outputStream, int bufferSize) {
        this.mOutputStream = outputStream;
        this.mMp3Buffer = new byte[(int) (7200 + bufferSize * 2 * 1.25)];
    }

    public Handler getHandler() {
        try {
            mHandlerInitLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return mHandler;
    }

    @Override
    public void run() {
        Looper.prepare();
        mHandler = new StopHandler(this);
        mHandlerInitLatch.countDown();
        Looper.loop();
    }

    @Override
    public void onMarkerReached(AudioRecord audioRecord) {

    }

    @Override
    public void onPeriodicNotification(AudioRecord audioRecord) {
        // 由AudioRecorder进行回调，满足帧数，通知数据转换
        processData();
    }

    private static class StopHandler extends Handler {
        WeakReference<Mp3EncodeThread> mEncodeThread;

        public StopHandler(Mp3EncodeThread thread) {
            this.mEncodeThread = new WeakReference<>(thread);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            if (msg.what == PROCESS_STOP) {
                Mp3EncodeThread threadRef = mEncodeThread.get();
                if (threadRef != null) {
                    while (threadRef.processData() > 0) ;

                    removeCallbacksAndMessages(null);
                    threadRef.flushAndRelease();
                    getLooper().quit();
                }
            }
            super.handleMessage(msg);
        }
    }

    // 从缓存区ChangeBuffers中获取待转换的PCM数据，转换为MP3数据，并写入文件
    private int processData() {
        if (mChangeBuffers != null && mChangeBuffers.size() > 0) {
            ChangeBuffer changeBuffer = mChangeBuffers.remove(0);
            short[] buffer = changeBuffer.getRawData();
            int readSize = changeBuffer.getReadSize();
            if (readSize > 0) {
                int encodeSize = Mp3Encoder.encode(buffer, buffer, readSize, mMp3Buffer);
                try {
                    mOutputStream.write(mMp3Buffer, 0, encodeSize);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return readSize;
            }
        }
        return 0;
    }

    private void flushAndRelease() {
        int flushResult = Mp3Encoder.flush(mMp3Buffer);
        if (flushResult > 0) {
            try {
                mOutputStream.write(mMp3Buffer, 0, flushResult);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void addChangeBuffer(short[] rawData, int readSize) {
        mChangeBuffers.add(new ChangeBuffer(rawData, readSize));
    }

    private class ChangeBuffer {
        private short[] rawData;
        private int readSize;

        public ChangeBuffer(short[] rawData, int readSize) {
            this.rawData = rawData;
            this.readSize = readSize;
        }

        public short[] getRawData() {
            return rawData;
        }

        public int getReadSize() {
            return readSize;
        }
    }
}
