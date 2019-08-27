package com.jiazhu.demo.util;

import android.content.Context;
import android.content.Intent;

public class IntentUtil {
    public static void startActivity(Context context, Class targetActivity) {
        Intent intent = new Intent(context, targetActivity);
        context.startActivity(intent);
    }
}
