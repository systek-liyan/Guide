package com.systekcn.guide.common.utils;

import com.baidu.navisdk.util.common.LogUtil;
import com.systekcn.guide.MyApplication;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Created by Qiang on 2015/11/26.
 */
public class ExceptionUtil {
    public static void handleException(Exception e) {
        // 把异常信息变成字符串，发给开发人员
        String str = "";
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        e.printStackTrace(printWriter);
        str = stringWriter.toString();

        if (MyApplication.isRelease) {
            // 联网发送
        } else {
            // 开发中
            LogUtil.e("ZHANG", str);
        }

    }
}
