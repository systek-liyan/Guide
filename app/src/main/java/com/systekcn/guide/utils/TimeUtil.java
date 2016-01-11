package com.systekcn.guide.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Qiang on 2015/11/30.
 */
public class TimeUtil {

    /**
     * @return "HH:mm:ss"
     */
    public static String getCurTime() {
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        Date currentTime = new Date();
        return formatter.format(currentTime);
    }

    /**
     * @return "yyyy-MM-dd HH:mm:ss"
     */
    public static String getCurDayTime() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date currentTime = new Date();
        return formatter.format(currentTime);
    }

    /**
     * @return "HH:mm"
     */
    public static String getTime() {
        String timeString = getCurTime();
        String[] timeArr = timeString.split(":");
        return timeArr[0] + ":" + timeArr[1];
    }
}
