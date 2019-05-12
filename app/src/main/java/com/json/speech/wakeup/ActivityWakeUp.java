package com.json.speech.wakeup;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;


import com.baidu.aip.asrwakeup3.core.mini.AutoCheck;
import com.baidu.aip.asrwakeup3.core.recog.IStatus;
import com.baidu.aip.asrwakeup3.core.wakeup.IWakeupListener;
import com.baidu.aip.asrwakeup3.core.wakeup.MyWakeup;
import com.baidu.aip.asrwakeup3.core.wakeup.RecogWakeupListener;
import com.baidu.aip.asrwakeup3.uiasr.activity.ActivityCommon;
import com.baidu.aip.asrwakeup3.uiasr.params.CommonRecogParams;
import com.baidu.aip.asrwakeup3.uiasr.params.OfflineRecogParams;
import com.baidu.aip.asrwakeup3.uiasr.params.OnlineRecogParams;
import com.baidu.speech.asr.SpeechConstant;
import com.json.speech.R;

import java.util.HashMap;
import java.util.Map;

/**
 * 集成文档： http://ai.baidu.com/docs#/ASR-Android-SDK/top 集成指南一节
 * 唤醒词功能
 */
public class ActivityWakeUp extends ActivityCommon implements IStatus {

    protected MyWakeup myWakeup;
    public int status = STATUS_NONE;
    private static final String TAG = "ActivityWakeUp";
    private final CommonRecogParams apiParams;

    public ActivityWakeUp() {
      this(R.raw.normal_wakeup);
    }

    public ActivityWakeUp(int textId) {
        super(textId, R.layout.common_without_setting);
        apiParams = new OfflineRecogParams();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 改为 SimpleWakeupListener 后，不依赖handler，但将不会在UI界面上显示
        // 基于DEMO唤醒词集成第1.1, 1.2, 1.3步骤
        IWakeupListener listener = new RecogWakeupListener(handler);
        myWakeup = new MyWakeup(this, listener);

    }

    protected Map<String, Object> fetchParams() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        //  上面的获取是为了生成下面的Map， 自己集成时可以忽略
        Map<String, Object> params = apiParams.fetch(sp);

        //  集成时不需要上面的代码，只需要params参数。
        return params;
    }









    @Override
    protected void onDestroy() {
        // 基于DEMO唤醒词集成第5 退出事件管理器
        myWakeup.release();
        super.onDestroy();
    }
}
