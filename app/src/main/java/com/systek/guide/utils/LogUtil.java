package com.systek.guide.utils;

import android.util.Log;

import com.systek.guide.MyApplication;


/**
 * Created by Qiang on 2015/11/26.
 *
 * 日志统一处理
 */
public class LogUtil {

    public static void i(String tag,Object msg)
    {
        if (MyApplication.isRelease)
        {
            return;
        }
        Log.i(tag, String.valueOf(msg));
    }

    public static void e(String tag,Object msg)
    {
        if (MyApplication.isRelease)
        {
            return;
        }
        Log.e(tag, String.valueOf(msg));
    }
}
