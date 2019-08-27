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
import com.jiazhu.demo.audio.Mp3Recorder;
import com.jiazhu.demo.util.FileUtil;

public class Mp3EncodeActivity extends AppCompatActivity {

    private Button mRecordBtn;
    private String mMp3Path;
    private Mp3Recorder mMp3Recorder;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mp3_encoder);
        mMp3Recorder = new Mp3Recorder();

        mRecordBtn = findViewById(R.id.record_btn);
        mRecordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mMp3Recorder.isRecording()) {
                    mRecordBtn.setText("开始录音");
                    mMp3Recorder.stopRecording();
                } else {
                    mRecordBtn.setText("停止录音");
                    mMp3Recorder.startRecording(FileUtil.getBasePath(Mp3EncodeActivity.this) + "/lame.mp3");

                }
            }
        });

        findViewById(R.id.play_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!TextUtils.isEmpty(mMp3Path)) {
                    Mp3Player.getInstance(Mp3EncodeActivity.this).playSound(mMp3Path, new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mediaPlayer) {
                            Toast.makeText(Mp3EncodeActivity.this, "播放完成", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(Mp3EncodeActivity.this, "文件为空", Toast.LENGTH_SHORT).show();
                }
            }
        });
        mMp3Recorder.setFinishListener(new Mp3Recorder.OnFinishListener() {
            @Override
            public void onFinish(String mp3SavePath) {
                mMp3Path = mp3SavePath;
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Mp3Player.getInstance(this).release();
    }
}
