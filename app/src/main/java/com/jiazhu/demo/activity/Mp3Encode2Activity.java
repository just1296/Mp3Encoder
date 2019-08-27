package com.jiazhu.demo.activity;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.jiazhu.demo.R;
import com.jiazhu.demo.audio.Mp3Player;
import com.jiazhu.demo.audio.Mp3RecorderTwo;
import com.jiazhu.demo.util.FileUtil;

public class Mp3Encode2Activity extends AppCompatActivity implements Mp3RecorderTwo.OnTransformFinishListener {

    private Button mRecordBtn;
    private Mp3RecorderTwo mMp3Recorder;
    private String mMp3Path;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mp3_encoder2);
        mMp3Recorder = new Mp3RecorderTwo();
        mMp3Recorder.setFinishListener(this);

        mRecordBtn = findViewById(R.id.record_btn);
        mRecordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mMp3Recorder.isRecording()) {
                    mRecordBtn.setText("开始录音");
                    mMp3Recorder.stopRecording();
                } else {
                    mRecordBtn.setText("停止录音");
                    mMp3Recorder.startRecording(FileUtil.getBasePath(Mp3Encode2Activity.this) + "/lame.pcm");

                }
            }
        });

        findViewById(R.id.trans_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMp3Recorder.transformPCM2Mp3();
            }
        });

        findViewById(R.id.play_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!TextUtils.isEmpty(mMp3Path)) {
                    Mp3Player.getInstance(Mp3Encode2Activity.this).playSound(mMp3Path, new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mediaPlayer) {
                            Toast.makeText(Mp3Encode2Activity.this, "播放完成", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(Mp3Encode2Activity.this, "文件为空", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onTransFinish(String mp3SavePath) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(Mp3Encode2Activity.this, "转换完成", Toast.LENGTH_SHORT).show();
            }
        });
        mMp3Path = mp3SavePath;
    }
}
