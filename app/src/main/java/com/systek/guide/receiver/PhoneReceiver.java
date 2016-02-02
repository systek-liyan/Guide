package com.systek.guide.receiver;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.systek.guide.manager.MediaServiceManager;
import com.systek.guide.utils.LogUtil;

/**
 * Created by Qiang on 2016/1/13.
 *
 * 电话状态监听器
 */
public class PhoneReceiver extends BroadcastReceiver{

    private static boolean incomingFlag = false;
    private MediaServiceManager mediaServiceManager;

    @Override
    public void onReceive(Context context, Intent intent) {
        LogUtil.i("ZHANG","接收了电话广播");
        mediaServiceManager=MediaServiceManager.getInstance(context);
        //拨打电话
        if (intent.getAction().equals(Intent.ACTION_NEW_OUTGOING_CALL)) {
            incomingFlag = false;
            final String phoneNum = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
            Log.d("PhoneReceiver", "phoneNum: " + phoneNum);
        } else {
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Service.TELEPHONY_SERVICE);
            tm.listen(listener, PhoneStateListener.LISTEN_CALL_STATE);
        }
    }
    final PhoneStateListener listener=new PhoneStateListener(){
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            super.onCallStateChanged(state, incomingNumber);
            switch(state){
                //电话等待接听
                case TelephonyManager.CALL_STATE_RINGING:
                    incomingFlag = true;
                    Log.i("PhoneReceiver", "CALL IN RINGING :" + incomingNumber);
                    if(mediaServiceManager!=null&&mediaServiceManager.isPlaying()){
                        mediaServiceManager.pause();
                    }
                    break;
                //电话接听
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    if (incomingFlag) {
                        Log.i("PhoneReceiver", "CALL IN ACCEPT :" + incomingNumber);
                    }
                    if(mediaServiceManager!=null&&mediaServiceManager.isPlaying()){
                        mediaServiceManager.pause();
                    }
                    break;
                //电话挂机
                case TelephonyManager.CALL_STATE_IDLE:
                    if (incomingFlag) {
                        Log.i("PhoneReceiver", "CALL IDLE");
                    }
                    /*if(mediaServiceManager!=null&&mediaServiceManager.is()){
                        mediaServiceManager.pause();
                    }*/
                    break;

            }

        }
    };
}
