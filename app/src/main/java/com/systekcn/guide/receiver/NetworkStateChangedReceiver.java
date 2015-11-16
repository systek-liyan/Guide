package com.systekcn.guide.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.systekcn.guide.common.utils.NetworkUtil;

/**
 * Created by Qiang on 2015/10/28.
 */
public class NetworkStateChangedReceiver extends BroadcastReceiver{
    @Override
    public void onReceive(Context context, Intent intent) {
        NetworkUtil.checkNet(context);
    }
}
