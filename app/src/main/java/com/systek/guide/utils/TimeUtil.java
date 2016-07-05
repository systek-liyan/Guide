package com.systek.guide.utils;

import java.text.ParseException;
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


    public static String changeToTime(long time) {
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        return formatter.format(time);
    }


    /**
     *
     * 返回当前时间
     * @return
     *String hh:mm:ss yyyy年MM月dd日
     *String
     * @since  1.0.0
     */
    public static synchronized String getNowTimeString() {
        SimpleDateFormat Fmt=new SimpleDateFormat("HH:mm:ss yyyy年MM月dd日");
        Date now=new Date();
        return Fmt.format(now);
    }

    /**
     *
     * 比较现在时间是不是在给定的时间段里。 比如时间"201407221445"
     * @param timeStart	起始时间
     * @param timeEnd	终止时间
     *boolean
     * @exception ParseException
     * @since  1.0.0
     */
    public boolean betweenTheTimeMinu(String timeStart, String timeEnd) {
        SimpleDateFormat Fmt1 = new SimpleDateFormat("yyyyMMddHHmm");
        try {
            Date dateStart = Fmt1.parse(timeStart);
            Date dateEndDate = Fmt1.parse(timeEnd);
            Date dateNowDate = new Date();
            Long dateStartLong = dateStart.getTime();
            Long dateEndLong = dateEndDate.getTime();
            Long dateNowLong = dateNowDate.getTime();

            if (dateNowLong > dateStartLong && dateNowLong < dateEndLong) {
                return true;
            } else {
                return false;
            }

        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 计算已经过的时间(时间戳单位为秒，当前系统时间需要除以1000)
     *
     * @param oldTime
     *            原始时间
     * @return 经过时间
     */
    public static String getPassTime(long oldTime) {
        return getPassTime(oldTime, System.currentTimeMillis() / 1000);
    }

    /**
     * 计算已经过的时间
     *
     * @param oldTime
     *            原始时间
     * @param nowTime
     *            当前时间
     * @return 经过时间
     */
    public static String getPassTime(long oldTime, long nowTime) {
        String temp = "";
        try {
            long diff = nowTime - oldTime;
            long days = diff / (60 * 60 * 24);
            long hours = (diff - days * (60 * 60 * 24)) / (60 * 60);
            long minutes = (diff - days * (60 * 60 * 24) - hours * (60 * 60)) / 60;
            if (days > 0) {
                temp = days + "天前";
            } else if (hours > 0) {
                temp = hours + "小时前";
            } else {
                temp = minutes + "分钟前";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return temp;
    }


}
