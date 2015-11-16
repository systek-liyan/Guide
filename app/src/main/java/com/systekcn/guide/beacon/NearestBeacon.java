package com.systekcn.guide.beacon;

import org.altbeacon.beacon.Beacon;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

/**
 * Created by Qiang on 2015/11/1.
 */
public class NearestBeacon {

    /** 默认最近距离(m),用于展品定位 **/
    public static double NEAREST_DISTANCE = 3.0;

    /** 默认最小停留时间(ms),用于展品定位 */
    public static long MIN_STAY_MILLISECONDS = 3000L; // 3s

    /** 获取游客定位beacon */
    public static int GET_LOCATION_BEACON = 0;

    /** 获取展品定位beacon */
    public static int GET_EXHIBIT_BEACON = 1;

    /** 用于展品定位的最小距离(m) **/
    private double mExhibit_distance;

    /** 最小停留时间(ms) */
    private long mMin_stay_milliseconds;

    /** 本次扫描周期计算的最小距离的beacon，即距离最近的beacon */
    private Beacon mNeaestBeacon = null;

    /** mNeaestBeacon对应的距离 */
    private double mNeaestBeacon_distance = -1.0D;

    /** 上一扫描周期存储的符合条件的展品定位beacon，距离小于mExhibit_distance，逗留时间大于mMin_stay_milliseconds */
    private Beacon mExhibitBeacon = null;

    /** mExhibitBeacon对应的时间戳 */
    private long mTimestamp = 0L;

    /** 根据距离排序beacons，用于计算距离最近的beacon */
    private ArrayList<Beacon> mBeaconList = new ArrayList<Beacon>();


    /*无参构造方法，最短定位距离与最小停留时间为默认值*/
    public NearestBeacon() {
        mExhibit_distance = NEAREST_DISTANCE;
        mMin_stay_milliseconds = MIN_STAY_MILLISECONDS;
    }

    /*构造方法，设置最小停留时间与最短定位距离*/
    public NearestBeacon(double mExhibit_distance, long mMin_stay_milliseconds) {
        this.mExhibit_distance = mExhibit_distance;
        this.mMin_stay_milliseconds = mMin_stay_milliseconds;
    }

    public long getmMin_stay_milliseconds() {
        return mMin_stay_milliseconds;
    }

    public void setmMin_stay_milliseconds(long mMin_stay_milliseconds) {
        this.mMin_stay_milliseconds = mMin_stay_milliseconds;
    }

    public double getmNeaestBeacon_distance() {
        return mNeaestBeacon_distance;
    }

    public void setmNeaestBeacon_distance(double mNeaestBeacon_distance) {
        this.mNeaestBeacon_distance = mNeaestBeacon_distance;
    }

    public Beacon getNearestBeacon(int type,Collection<Beacon> beacons){
        Beacon beacon = nearestBeacon(beacons);
        if (type == GET_LOCATION_BEACON) {
            return beacon;
        }else if (type == GET_EXHIBIT_BEACON){
            return exhibitBeacon();// 注意必须在nearestBeacon(beacons)后调用。
        }
        return null;
    }

    private Beacon nearestBeacon(Collection<Beacon> beacons) {
        if (beacons.size() == 0) {  // 本次扫描周期没有发现beacon
            mNeaestBeacon = null;
            mNeaestBeacon_distance = -1.0D;
            return null;
        }
        mBeaconList.clear();
        Iterator<Beacon> iterator = beacons.iterator();
        while (iterator.hasNext()) {
            Beacon beacon = iterator.next();
            mBeaconList.add(beacon);
        }
        Collections.sort(mBeaconList,new Comparator<Beacon>() {

            @Override
            public int compare(Beacon lhs, Beacon rhs) {
                double temp=lhs.getDistance()-rhs.getDistance();
                if  (Math.abs(temp) < 0.001) { // 0.001毫米级精度
                    return 0;
                }else if(temp<0){
                    return -1;
                }else{
                    return 1;
                }
            }
        });
        mNeaestBeacon = mBeaconList.get(0);
        return mNeaestBeacon;
    }

    private Beacon exhibitBeacon() {
        // 如果本次扫描没有发现beacon
        // 如果本次扫描周期最小距离beacon(mNeaestBeacon)
        //大于展品定位的最小距离，本次扫描周期没有符合条件的展品定位beacon
        if (mNeaestBeacon == null||mNeaestBeacon_distance > mExhibit_distance){
            return null;
        }
        // 如果是第一次发现的beacon(mNeaestBeacon),并且它的距离小于展品定位最小距离(mExhibit_distance),则初始化该beacon是展品定位的beacon(mExhibitBeacon)
        if (mExhibitBeacon == null) {
            mExhibitBeacon = mNeaestBeacon;
            mTimestamp = System.currentTimeMillis();
            return null; // 第一次存储查品定位beacon，逗留时间肯定不符合条件，因此返回null
        }

        // 如果本次扫描周期最小距离beacon(mNeaestBeacon)
        //与上一扫描周期存储的展品beacon(mExhibitBeacon)不同，则替换mExhibitBeacon
        if (!mExhibitBeacon.equals(mNeaestBeacon)) {
            mExhibitBeacon = mNeaestBeacon;
            mTimestamp = System.currentTimeMillis();
            return null; // 刚刚替换距离最近的beacon，逗留时间肯定不符合条件，因此返回null
        }else{
            // 如果本次扫描周期最小距离beacon(mNeaestBeacon)与上一扫描周期存储的展品beacon(mExhibitBeacon)相同，则判断是否满足最小停留时间
            if (System.currentTimeMillis() - mTimestamp >= mMin_stay_milliseconds) {
                return mExhibitBeacon;
            }
        }
        return null;
    }


}
