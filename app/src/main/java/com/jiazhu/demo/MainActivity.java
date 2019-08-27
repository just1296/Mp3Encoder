package com.jiazhu.demo;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.jiazhu.demo.activity.Mp3Encode2Activity;
import com.jiazhu.demo.activity.Mp3EncodeActivity;
import com.jiazhu.demo.util.IntentUtil;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private String[] permissions = new String[]{
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private List<String> mNeedRequestPermissionList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (!checkAudioPermission()) {
            requestNeedPermissions();
        }

        findViewById(R.id.mp3_encoder).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                IntentUtil.startActivity(MainActivity.this, Mp3EncodeActivity.class);
            }
        });

        findViewById(R.id.mp3_encoder2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                IntentUtil.startActivity(MainActivity.this, Mp3Encode2Activity.class);
            }
        });
    }

    private void requestNeedPermissions() {
        if (!mNeedRequestPermissionList.isEmpty()) {
            String[] permissions = mNeedRequestPermissionList.toArray(new String[mNeedRequestPermissionList.size()]);
            ActivityCompat.requestPermissions(this, permissions, 100);
        }
    }

    private boolean checkAudioPermission() {
        mNeedRequestPermissionList.clear();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (int i = 0; i < permissions.length; i++) {
                if (ContextCompat.checkSelfPermission(this, permissions[i]) != PackageManager.PERMISSION_GRANTED) {
                    mNeedRequestPermissionList.add(permissions[i]);
                }
            }
        }
        return mNeedRequestPermissionList.isEmpty();
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();
}
