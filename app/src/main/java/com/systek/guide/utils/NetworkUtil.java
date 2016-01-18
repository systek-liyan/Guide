package com.systek.guide.utils;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.systek.guide.IConstants;
import com.systek.guide.MyApplication;

/**
 * Created by Qiang on 2015/11/26.
 */
public class NetworkUtil implements IConstants{
    public static int checkNet(Context context) {
        // 判断用户是打开还是关闭
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = manager.getActiveNetworkInfo();
        int interNet=0;
        if (activeNetworkInfo == null) {
            interNet = INTERNET_TYPE_NONE;
            LogUtil.i("NetworkStateChanged", "关闭");
        } else {
			/* 用户开了WIFI和移动网络，操作系统使用的是WIFI */
            NetworkInfo wifiNetworkInfo = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            if (wifiNetworkInfo != null && wifiNetworkInfo.isConnected()) {
                LogUtil.i("NetworkStateChanged", "打开的是wifi");
                interNet = INTERNET_TYPE_WIFI;
            }
            NetworkInfo mobileNetworkInfo = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            if (mobileNetworkInfo != null && mobileNetworkInfo.isConnected()) {
                LogUtil.i("NetworkStateChanged", "打开的是mobile");
                interNet = INTERNET_TYPE_MOBILE;
            }
        }
        if(MyApplication.currentNetworkType==INTERNET_TYPE_NONE&&interNet!=INTERNET_TYPE_NONE){
            Intent intent=new Intent();
            intent.setAction(ACTION_NET_IS_COMMING);
            context.sendBroadcast(intent);
        }else if(MyApplication.currentNetworkType!=INTERNET_TYPE_NONE&&interNet==INTERNET_TYPE_NONE){
            Intent intent=new Intent();
            intent.setAction(ACTION_NET_IS_OUT);
            context.sendBroadcast(intent);
        }
        MyApplication.currentNetworkType=interNet;
        return interNet;
    }
}
