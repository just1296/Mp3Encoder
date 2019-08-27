package com.jiazhu.demo.util;

import android.content.Context;
import android.os.Environment;

import java.io.File;

public class FileUtil {
    public static String getBasePath(Context context) {
        String path = null;
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            path = context.getFilesDir() + "/lameMp3";
        } else {
            path = Environment.getExternalStorageDirectory() + "/lameMp3";
        }
        File dir = new File(path);
        if (!dir.exists()) {
            dir.mkdir();
        }
        return path;
    }
}
