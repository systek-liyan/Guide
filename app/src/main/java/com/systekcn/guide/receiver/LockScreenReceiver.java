package com.systekcn.guide.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.systekcn.guide.MyApplication;
import com.systekcn.guide.activity.LockScreenActivity;
import com.systekcn.guide.common.utils.LogUtil;

/**
 * Created by Qiang on 2015/11/27.
 */
public class LockScreenReceiver extends BroadcastReceiver{

    private static final String TAG="ZHANG";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
            LogUtil.i(TAG, "ACTION_SCREEN_OFF");
            /**当前展品不为空时，启动锁屏时显示自己的界面*/
            if(MyApplication.get().currentExhibitBean!=null){
                Intent intent1 = new Intent(context,LockScreenActivity.class);
                // 隐式启动锁屏页
                // Intent intent1 = new Intent(context, LockScreenActivity.class);
                intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent1);
            }
        } else if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
            LogUtil.i(TAG, "ACTION_SCREEN_ON");
        } else if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            LogUtil.i(TAG, "ACTION_BOOT_COMPLETED");
        }
    }
}
