package com.jiazhu.demo.audio;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class Mp3Player {

    private static volatile Mp3Player mInstance;
    private static MediaPlayer mMediaPlayer;

    private static void init(Context context) {
        mMediaPlayer = getMediaPlayer(context);
    }

    private Mp3Player(Context context) {
        init(context);
    }

    public static Mp3Player getInstance(Context context) {
        if (mInstance == null) {
            synchronized (Mp3Player.class) {
                if (mInstance == null) {
                    mInstance = new Mp3Player(context);
                }
            }
        }
        return mInstance;
    }

    public void playSound(String path, MediaPlayer.OnCompletionListener listener) {
        try {
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            File file = new File(path);
            FileInputStream fis = new FileInputStream(file);
            mMediaPlayer.setDataSource(fis.getFD());
            mMediaPlayer.prepare();
            mMediaPlayer.setOnCompletionListener(listener);
            mMediaPlayer.start();
        } catch (Exception e) {
            e.printStackTrace();
            mMediaPlayer.reset();
        }
    }

    public void release() {
        if (mMediaPlayer != null) {
            mMediaPlayer.reset();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    private static MediaPlayer getMediaPlayer(Context context) {
        MediaPlayer mediaPlayer = new MediaPlayer();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            return mediaPlayer;
        }
        try {
            Class<?> cMediaTimeProvider = Class.forName("android.media.MediaTimeProvider");
            Class<?> cSubtitleController = Class.forName("android.media.SubtitleController");
            Class<?> iSubtitleControllerAnchor = Class.forName("android.media.SubtitleController$Anchor");
            Class<?> iSubtitleControllerListener = Class.forName("android.media.SubtitleController$Listener");

            Constructor constructor = cSubtitleController.getConstructor(new Class[]{Context.class, cMediaTimeProvider, iSubtitleControllerListener});

            Object subtitleInstance = constructor.newInstance(context, null, null);

            Field f = cSubtitleController.getDeclaredField("mHandler");

            f.setAccessible(true);
            try {
                f.set(subtitleInstance, new Handler());
            } catch (IllegalAccessException e) {
                return mediaPlayer;
            } finally {
                f.setAccessible(false);
            }
            Method setsubtitleanchor = mediaPlayer.getClass().getMethod("setSubtitleAnchor", cSubtitleController, iSubtitleControllerAnchor);
            setsubtitleanchor.invoke(mediaPlayer, subtitleInstance, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mediaPlayer;
    }
}
