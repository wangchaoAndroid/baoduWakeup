package com.json.speech.wakeup;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.baidu.aip.asrwakeup3.core.util.FileUtil;

public class FiLeManager {

    private static final String  CACHE_DIR = "wmo-record";

    public static final String CACHE_PATH = Environment.getExternalStorageDirectory().toString() + "/" + CACHE_DIR;
    public static final String WAV_PATH = Environment.getExternalStorageDirectory().toString() + "/" + "test";

    public static void initCchePath(Context context) {
        boolean b = FileUtil.makeDir(CACHE_PATH);
        Log.e("ActivityWakeUpRecog","" +b);
        if (!b) {
            String samplePath = context.getExternalFilesDir(CACHE_PATH).getAbsolutePath();
            if (!FileUtil.makeDir(samplePath)) {
                throw new RuntimeException("创建临时目录失败 :" + samplePath);
            }
        }
    }
}
