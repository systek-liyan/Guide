package com.systekcn.guide.common.utils;

import android.util.Log;

import com.systekcn.guide.MyApplication;

/**
 *  日志统一处理
 * @author ZQ
 *
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
