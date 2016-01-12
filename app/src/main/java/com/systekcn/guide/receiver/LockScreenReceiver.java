package com.systekcn.guide.receiver;

import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.alibaba.fastjson.JSON;
import com.systekcn.guide.IConstants;
import com.systekcn.guide.MyApplication;
import com.systekcn.guide.activity.LockScreenActivity;
import com.systekcn.guide.entity.ExhibitBean;
import com.systekcn.guide.manager.MediaServiceManager;
import com.systekcn.guide.utils.LogUtil;

/**
 * Created by Qiang on 2015/11/27.
 */
public class LockScreenReceiver extends BroadcastReceiver implements IConstants{

    private static final String TAG="ZHANG";


    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
            MediaServiceManager mediaServiceManager =MediaServiceManager.getInstance(context);
            if(mediaServiceManager==null||!mediaServiceManager.isPlaying()){return;}
            // 禁用系统锁屏页
            KeyguardManager km = (KeyguardManager) MyApplication.get().getSystemService(Context.KEYGUARD_SERVICE);
            KeyguardManager.KeyguardLock kl = km.newKeyguardLock("IN");
            kl.disableKeyguard();
            LogUtil.i(TAG, "ACTION_SCREEN_ON");
            //*当前展品不为空时，启动锁屏时显示自己的界面
            ExhibitBean exhibitBean=mediaServiceManager.getCurrentExhibit();
            if(exhibitBean==null){return;}
            Intent intent1 = new Intent(context,LockScreenActivity.class);
            String exhibitStr= JSON.toJSONString(exhibitBean);
            intent1.putExtra(INTENT_EXHIBIT,exhibitStr);
            // 隐式启动锁屏页
            intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent1);
        } else if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
            LogUtil.i(TAG, "ACTION_SCREEN_OFF");
        } else if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            LogUtil.i(TAG, "ACTION_BOOT_COMPLETED");
        }
    }
}
