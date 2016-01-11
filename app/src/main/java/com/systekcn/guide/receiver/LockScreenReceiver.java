package com.systekcn.guide.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by Qiang on 2015/11/27.
 */
public class LockScreenReceiver extends BroadcastReceiver{

    private static final String TAG="ZHANG";

    @Override
    public void onReceive(Context context, Intent intent) {
        /*if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
            // 禁用系统锁屏页
            KeyguardManager km = (KeyguardManager)MyApplication.get().getSystemService(Context.KEYGUARD_SERVICE);
            KeyguardManager.KeyguardLock kl = km.newKeyguardLock("IN");
            kl.disableKeyguard();
            LogUtil.i(TAG, "ACTION_SCREEN_ON");
            *//**当前展品不为空时，启动锁屏时显示自己的界面*//*
            if(MyApplication.get().currentExhibitBean!=null){
                Intent intent1 = new Intent(context,LockScreenActivity.class);
                // 隐式启动锁屏页
                intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent1);
            }
        } else if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
            LogUtil.i(TAG, "ACTION_SCREEN_OFF");
        } else if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            LogUtil.i(TAG, "ACTION_BOOT_COMPLETED");
        }*/
    }
}
