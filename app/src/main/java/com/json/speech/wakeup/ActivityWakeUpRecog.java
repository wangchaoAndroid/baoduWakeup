package com.json.speech.wakeup;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.baidu.aip.asrwakeup3.core.mini.AutoCheck;
import com.baidu.aip.asrwakeup3.core.recog.IStatus;
import com.baidu.aip.asrwakeup3.core.recog.MyRecognizer;
import com.baidu.aip.asrwakeup3.core.recog.listener.ChainRecogListener;
import com.baidu.aip.asrwakeup3.core.recog.listener.IRecogListener;
import com.baidu.aip.asrwakeup3.core.recog.listener.MessageStatusRecogListener;
import com.baidu.aip.asrwakeup3.core.util.FileUtil;
import com.baidu.aip.asrwakeup3.core.util.MyLogger;
import com.baidu.aip.asrwakeup3.core.wakeup.IWakeupListener;
import com.baidu.aip.asrwakeup3.core.wakeup.RecogWakeupListener;
import com.baidu.aip.asrwakeup3.uiasr.params.CommonRecogParams;
import com.baidu.aip.asrwakeup3.uiasr.params.OfflineRecogParams;
import com.baidu.speech.asr.SpeechConstant;
import com.json.speech.R;
import com.json.speech.utils.NetWorkUtils;
import com.json.speech.utils.SPUtil;
import com.json.speech.wakeup.listener.MyRecogListener;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 唤醒后识别 本例可与ActivityWakeUp 对比作为集成识别代码的参考
 */
public class ActivityWakeUpRecog extends ActivityWakeUp implements IStatus {


    private static final String TAG = "ActivityWakeUpRecog";

    private String samplePath = FiLeManager.CACHE_PATH;

    private static boolean enableOffline = true;

    private CommonRecogParams apiParams;


    /**
     * 识别控制器，使用MyRecognizer控制识别的流程
     */
    protected MyRecognizer myRecognizer;

    /**
     * 0: 方案1， backTrackInMs > 0,唤醒词说完后，直接接句子，中间没有停顿。
     *              开启回溯，连同唤醒词一起整句识别。推荐4个字 1500ms
     *          backTrackInMs 最大 15000，即15s
     *
     * >0 : 方案2：backTrackInMs = 0，唤醒词说完后，中间有停顿。
     *       不开启回溯。唤醒词识别回调后，正常开启识别。
     * <p>
     *
     */
    private int backTrackInMs = 0;

    private ChainRecogListener chainRecogListener;



    public ActivityWakeUpRecog() {
        super(R.raw.recog_wakeup);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        chainRecogListener = new ChainRecogListener();
        IRecogListener recogListener = new MessageStatusRecogListener(handler);
        IRecogListener myRecogListener = new MyRecogListener();
        chainRecogListener.addListener(recogListener);
        chainRecogListener.addListener(myRecogListener);
        // 改为 SimpleWakeupListener 后，不依赖handler，但将不会在UI界面上显示
        myRecognizer = new MyRecognizer(this, chainRecogListener);

        IWakeupListener listener = new RecogWakeupListener(handler);
        myWakeup.setEventListener(listener); // 替换原来的 listener

        loadOffEngine();

    }

    public void loadOffEngine(){

        boolean isOfflineInited  = (boolean) SPUtil.get(this, SPUtil.OFFLINE_INITED, false);
        if ( !isOfflineInited && enableOffline ) {
            // 基于DEMO集成1.4 加载离线资源步骤(离线时使用)。offlineParams是固定值，复制到您的代码里即可
            if(NetWorkUtils.isConnected(this)){
                Map<String, Object> offlineParams = OfflineRecogParams.fetchOfflineParams();
                myRecognizer.loadOfflineEngine(offlineParams);
                SPUtil.put(this,SPUtil.OFFLINE_INITED,true);
            }else {
                Toast.makeText(this,"语音识别未初始化，请检查网络",Toast.LENGTH_SHORT).show();
            }

        }
    }

    @Override
    protected void handleMsg(Message msg) {
        super.handleMsg(msg);
        if (msg.what == STATUS_WAKEUP_SUCCESS) { // 唤醒词识别成功的回调，见RecogWakeupListener
            // 此处 开始正常识别流程
            FiLeManager.initCchePath(this);
            Map<String, Object> params = new LinkedHashMap<String, Object>();
            params.put(SpeechConstant.ACCEPT_AUDIO_VOLUME, false);
            params.put(SpeechConstant.VAD, SpeechConstant.VAD_DNN);
            params.put(SpeechConstant.DECODER, 2);
            params.put(SpeechConstant.ACCEPT_AUDIO_DATA, true); // 目前必须开启此回掉才嫩保存音频
            params.put(SpeechConstant.OUT_FILE, samplePath + "/outfile.pcm");
            MyLogger.info(TAG, "语音录音文件将保存在：" + samplePath + "/outfile.pcm");
            // 如识别短句，不需要需要逗号，使用1536搜索模型。其它PID参数请看文档
            params.put(SpeechConstant.PID, 1536);
            if (backTrackInMs > 0) {
                // 方案1  唤醒词说完后，直接接句子，中间没有停顿。开启回溯，连同唤醒词一起整句识别。
                // System.currentTimeMillis() - backTrackInMs ,  表示识别从backTrackInMs毫秒前开始
                params.put(SpeechConstant.AUDIO_MILLS, System.currentTimeMillis() - backTrackInMs);
            }
            myRecognizer.cancel();

            (new AutoCheck(getApplicationContext(), new Handler() {
                public void handleMessage(Message msg) {
                    if (msg.what == 100) {
                        AutoCheck autoCheck = (AutoCheck) msg.obj;
                        synchronized (autoCheck) {
                            String message = autoCheck.obtainErrorMessage(); // autoCheck.obtainAllMessage();
                            txtLog.append(message + "\n");
                            ; // 可以用下面一行替代，在logcat中查看代码
                             Log.e("AutoCheckMessage", message);
                        }
                    }
                }
            }, enableOffline)).checkAsr(params);
            Log.e(TAG,"params" + params.toString());
            myRecognizer.start(params);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        myRecognizer.stop();
    }


    @Override
    protected void onDestroy() {
        myRecognizer.release();
        super.onDestroy();
    }

    // 点击“开始识别”按钮
    // 基于DEMO唤醒词集成第2.1, 2.2 发送开始事件开始唤醒
    private void start() {

        Map<String, Object> params = new HashMap<String, Object>();
        params.put(SpeechConstant.WP_WORDS_FILE, "assets:///WakeUp.bin");
        // 复制此段可以自动检测常规错误
        (new AutoCheck(getApplicationContext(), new Handler() {
            public void handleMessage(Message msg) {
                if (msg.what == 100) {
                    AutoCheck autoCheck = (AutoCheck) msg.obj;
                    synchronized (autoCheck) {
                        String message = autoCheck.obtainErrorMessage(); // autoCheck.obtainAllMessage();
//                        txtLog.append(message + "\n");
                        ; // 可以用下面一行替代，在logcat中查看代码
                        Log.w("AutoCheckMessage", message);
                    }
                }
            }
        }, true)).checkAsr(params);

        myWakeup.start(params);
    }

    // 基于DEMO唤醒词集成第4.1 发送停止事件
    protected void stopWakeUp() {
        myWakeup.stop();
    }


    @Override
    protected void initView() {
        super.initView();
        btn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                switch (status) {
                    case STATUS_NONE:
                        //检查离线引擎是否已经初始化
                        loadOffEngine();
                        start();

                        status = STATUS_WAITING_READY;
                        updateBtnTextByStatus();
                        txtLog.setText("");
                        txtResult.setText("");
                        break;
                    case STATUS_WAITING_READY:
                        stopWakeUp();
                        status = STATUS_NONE;
                        updateBtnTextByStatus();
                        break;
                    default:
                        break;
                }
            }
        });
    }

    private void updateBtnTextByStatus() {
        switch (status) {
            case STATUS_NONE:
                btn.setText("启动唤醒");
                break;
            case STATUS_WAITING_READY:
                btn.setText("停止唤醒");
                break;
            default:
                break;
        }
    }

}
