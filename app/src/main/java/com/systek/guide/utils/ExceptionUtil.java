package com.systek.guide.utils;

import android.content.Context;

import com.systek.guide.MyApplication;
import com.systek.guide.R;

import org.apache.http.HttpException;
import org.apache.http.conn.ConnectTimeoutException;
import org.json.JSONException;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.SocketTimeoutException;

/**
 * Created by Qiang on 2015/11/26.
 */
public class ExceptionUtil {
    public static void handleException(Exception e) {
        if(MyApplication.isRelease){return;}
        // 把异常信息变成字符串，发给开发人员
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        e.printStackTrace(printWriter);
        String str = stringWriter.toString();
        String type=exceptionCode(e);
        // 开发中
        LogUtil.e("ZHANG",type+"=="+ str);
    }

    // 处理网络异常
    public static String exceptionCode(Exception e) {

        Context context=MyApplication.get().getApplicationContext();
        if(e instanceof HttpException) {
            return context.getString(R.string.networkFailure);      // 网络异常

        }else if (e instanceof SocketTimeoutException) {
            return context.getString(R.string.responseTimeout);     // 响应超时

        }else if (e instanceof ConnectTimeoutException) {
            return context.getString(R.string.requestTimeout);      // 请求超时

        }else if (e instanceof IOException) {
            return context.getString(R.string.networkError);      // 网络异常

        }else if (e instanceof JSONException) {
            return context.getString(R.string.json_error);          //json格式转换异常

        }else {
            return context.getString(R.string.canNotGetConnected);  // 无法连接网络
        }
    }

}
