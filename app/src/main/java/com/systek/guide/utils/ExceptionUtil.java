package com.systek.guide.utils;

import com.systek.guide.MyApplication;

import java.io.PrintWriter;
import java.io.StringWriter;

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
        // 开发中
        LogUtil.e("ZHANG", str);
    }
}
