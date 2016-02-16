package com.systek.guide.utils;

import android.content.Context;
import android.telephony.TelephonyManager;

/**
 * Created by Qiang on 2016/2/16.
 */
public class PhoneUtil {

    public static void  getMessageAboutPhone(Context context){
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

        String deviceid = tm.getDeviceId();
        LogUtil.e("ZHANG","deviceid="+deviceid);
        String tel = tm.getLine1Number();
        LogUtil.e("ZHANG","tel="+tel);
        String imei = tm.getSimSerialNumber();
        LogUtil.e("ZHANG","imei="+imei);
        String imsi = tm.getSubscriberId();
        LogUtil.e("ZHANG","imsi="+imsi);
    }

}
