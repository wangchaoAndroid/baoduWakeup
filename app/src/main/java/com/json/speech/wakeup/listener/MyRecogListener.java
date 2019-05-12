package com.json.speech.wakeup.listener;

import android.util.Log;

import com.baidu.aip.asrwakeup3.core.recog.RecogResult;
import com.baidu.aip.asrwakeup3.core.recog.listener.IRecogListener;
import com.baidu.speech.asr.SpeechConstant;
import com.json.speech.PcmToWavUtil;
import com.json.speech.wakeup.FiLeManager;

import java.io.File;

public class MyRecogListener implements IRecogListener {

    public static final String  TAG = "MyRecogListener";
    @Override
    public void onAsrReady() {
        Log.e(TAG,"onAsrReady");
    }

    @Override
    public void onAsrBegin() {
        Log.e(TAG,"onAsrBegin");
    }

    /**
     * 说话结束
     */
    @Override
    public void onAsrEnd() {
        Log.e(TAG,"onAsrEnd");
        PcmToWavUtil pcmToWavUtil = new PcmToWavUtil();
        String outPutPath  = FiLeManager.WAV_PATH + "/outfile.wav";
        File file =  new File(FiLeManager.WAV_PATH);
        File CACHE_PATH =  new File(FiLeManager.WAV_PATH);
        Log.e(TAG,"CACHE_PATH" + CACHE_PATH.exists());

        if(!file.exists()){
            file.mkdirs();
        }
        pcmToWavUtil.pcmToWav(FiLeManager.CACHE_PATH + "/outfile.pcm",
                outPutPath);
    }

    @Override
    public void onAsrPartialResult(String[] results, RecogResult recogResult) {
        Log.e(TAG,"onAsrPartialResult" + results.toString());
    }

    @Override
    public void onAsrOnlineNluResult(String nluResult) {

    }

    @Override
    public void onAsrFinalResult(String[] results, RecogResult recogResult) {
        Log.e(TAG,"onAsrFinalResult" + results.toString());
    }

    @Override
    public void onAsrFinish(RecogResult recogResult) {
        Log.e(TAG,"onAsrFinish" );
    }

    @Override
    public void onAsrFinishError(int errorCode, int subErrorCode, String descMessage, RecogResult recogResult) {
        Log.e(TAG,"onAsrFinishError" +descMessage );
    }

    @Override
    public void onAsrLongFinish() {

    }

    @Override
    public void onAsrVolume(int volumePercent, int volume) {

    }

    @Override
    public void onAsrAudio(byte[] data, int offset, int length) {

    }

    @Override
    public void onAsrExit() {

    }

    @Override
    public void onOfflineLoaded() {
         Log.e(TAG,"onOfflineLoaded");

    }

    @Override
    public void onOfflineUnLoaded() {
        Log.e(TAG,"onOfflineUnLoaded");
    }
}
