package com.systek.guide.receiver;

import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.alibaba.fastjson.JSON;
import com.systek.guide.IConstants;
import com.systek.guide.MyApplication;
import com.systek.guide.activity.LockScreenActivity;
import com.systek.guide.entity.ExhibitBean;
import com.systek.guide.service.PlayManager;
import com.systek.guide.utils.LogUtil;

import static android.content.Intent.ACTION_SCREEN_OFF;

/**
 * Created by Qiang on 2015/11/27.
 *
 * 锁屏广播接收器
 */
public class LockScreenReceiver extends BroadcastReceiver implements IConstants{

    private static final String TAG="ZHANG";
    @Override
    public void onReceive(Context context, Intent intent) {
        String action=intent.getAction();
        if (action.equals(Intent.ACTION_SCREEN_ON)||action.equals(ACTION_SCREEN_OFF)){
            if(!PlayManager.getInstance().isPlaying()){return;}
            // 禁用系统锁屏页
            KeyguardManager km = (KeyguardManager) MyApplication.get().getSystemService(Context.KEYGUARD_SERVICE);
            KeyguardManager.KeyguardLock kl = km.newKeyguardLock("IN");
            kl.disableKeyguard();
            LogUtil.i(TAG, "ACTION_SCREEN_ON");
            //*当前展品不为空时，启动锁屏时显示自己的界面
            ExhibitBean exhibitBean=PlayManager.getInstance().getCurrentExhibit();
            if(exhibitBean==null){return;}
            Intent intent1 = new Intent(context,LockScreenActivity.class);
            String exhibitStr= JSON.toJSONString(exhibitBean);
            intent1.putExtra(INTENT_EXHIBIT,exhibitStr);
            // 隐式启动锁屏页
            intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent1);
        } else if (intent.getAction().equals(ACTION_SCREEN_OFF)) {
            LogUtil.i(TAG, "ACTION_SCREEN_OFF");
        } else if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            LogUtil.i(TAG, "ACTION_BOOT_COMPLETED");
        }
    }
}
