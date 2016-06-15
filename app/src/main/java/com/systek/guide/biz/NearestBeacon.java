package com.systek.guide.biz;

import com.systek.guide.MyApplication;

import org.altbeacon.beacon.Beacon;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by Qiang on 2016/5/30.
 *
 */
public class NearestBeacon {

    // 蓝牙扫描范围
    private static final double SCAN_RANGE = 3;
    // 用户1.5米范围内的展品进入“可切换”列表，用于展品定位和切换展品使用
    private static final double USER_RANGE = 1.5;
    // 集合默认长度,最佳距离值计算
    private static final int INDEX = 4;
    // 阈值,判断检测到信标的距离值是否可用
    private static final double DISTANCE_THRESHOLD = 0.5;
    // 最近信标列表的长度
    private  static final int NEAREST_MINOR_COUNT = 8;

    private static final double TOW_BEACON_CHOOSE_THRESHOLD = 0.2D;

    // 记录上一次的信标列表
    private List<SystekBeacon> lastBeacons = new ArrayList<>();
    // 记录信标再多个周期内没有出现的次数
    private Map<String, Integer> scanCountMap = new HashMap<>();
    // 曾经扫描到的信标，现在已经很久都不出现了....
    private List<String> nullKey = new ArrayList<>();
    // key为次号，value为长度为4的距离的集合，用于获取可用的距离值
    private Map<String, List<ScanCount>> minorMap = new HashMap<>();
    // 处理后的信标列表(每次扫描结果中最近的信标/小于1.5米)，这组数据是可信的
    private List<String> minors = new ArrayList<>();
    // 处理后的信标列表，这组数据是可信的
    private List<SystekBeacon> myBeacons = new ArrayList<>();

    private  SystekBeacon nearestBeacon = null;

    //单例对象
    private static NearestBeacon instance;

    /**
     * 单例构造方法
     * @return 唯一对象instance
     */
    public static NearestBeacon getInstance(){
        if(instance==null){
            synchronized (MyApplication.class){
                if(instance==null){
                    instance=new NearestBeacon();
                }
            }
        }
        return instance;
    }

    /**
     * 重新计算距离
     * @param beacons 要计算的beacon集合
     * @return 计算后的SystekBeacon 集合（已排序）
     */
    public  synchronized List<SystekBeacon> calculateDistance(final Collection<Beacon> beacons) {
        // 本次扫描周期没有发现beacon
        if (beacons==null||beacons.size() == 0) {return null;}
        // 为每个扫描到的信标累计扫描次数(下一个扫描周期没有检测到累计)
        if (null != scanCountMap && scanCountMap.size() > 0) {
            for (Map.Entry<String, Integer> entry : scanCountMap.entrySet()) {
                if (entry.getValue() >= 30) {
                    nullKey.add(entry.getKey());
                } else {
                    scanCountMap.put(entry.getKey(), entry.getValue() + 1);
                }
            }
        }
        // 清空上次扫描的Beacons(不能使用list.clear())
        myBeacons = new ArrayList<>();
        // 一个扫描周期结束后获取到beacons,并以beaconId为key把beacon归类并放入Map<beaconId,List<Object>>中
        // 第一次循环，创建Map,key为：beaconId，value为：List<Object>
        // 再次循环,判断beaconId是否存在Map中，如果存在则List.add,不存在则Map.put
        Iterator<Beacon> iterator = beacons.iterator();
        while (iterator.hasNext()) {
            Beacon beacon = iterator.next();
            String minor = String.valueOf(beacon.getId3());

            List<ScanCount> scList;
            ScanCount nsc;
            // 如果检测到的beaconId在map中不存在，则创建
            // 如果map中已经存在beaconId,那就累计"距离值"到list中
            if (minorMap.get(minor) == null) {
                scList = new ArrayList<>();
                nsc = new ScanCount(1, beacon.getDistance());
                scList.add(nsc);
                minorMap.put(minor, scList);
            } else {
                scList = minorMap.get(minor);
                if (scanCountMap.get(minor) == null) {
                    nsc = new ScanCount(1, beacon.getDistance());
                } else {
                    nsc = new ScanCount(scanCountMap.get(minor), beacon.getDistance());
                }
                scList.add(nsc);
                minorMap.put(minor, scList);
                // 限定集合长度
                if (scList.size() > INDEX) {
                    scList.remove(0);
                }

            }
            scanCountMap.put(minor, 0);// 初始化扫描次数
        }

        // (同一个信标)比较最新的4条距离值，获取最合理的距离(图：data.jpg)
        for (Map.Entry<String, List<ScanCount>> entry : minorMap.entrySet()) {
            List<ScanCount> scans = entry.getValue();
            double distance = 0;
            if (null != scans && scans.size() == INDEX) {
                // 判断第4个采样周期（最近时间）和相邻的第3个采样周期的距离差值是否合理
                if (Math.abs(scans.get(3).getDistance() - scans.get(2).getDistance())
                        < DISTANCE_THRESHOLD * scans.get(3).getCount()) {
                    distance = scans.get(3).getDistance();
                    // 第4个采样周期（最近时间）和相邻的第3个采样周期的距离差值不合理，则处理第3和第2个采样周期
                } else if (Math.abs(scans.get(2).getDistance() - scans.get(1).getDistance())
                        < DISTANCE_THRESHOLD * scans.get(2).getCount()) {
                    distance = scans.get(2).getDistance();
                    // 第3个采样周期和相邻的第2个采样周期的距离差值不合理，可能第3个采样周期值异常
                    // 判断第4个采样周期和相隔的第2个采样周期的距离差值是否合理
                } else if (Math.abs(scans.get(3).getDistance() - scans.get(1).getDistance())
                        < DISTANCE_THRESHOLD * (scans.get(3).getCount() + scans.get(2).getCount())) {
                    distance = scans.get(3).getDistance();
                    // 判断第3个采样周期和相隔的第1个采样周期的距离差值是否合理
                } else if (Math.abs(scans.get(2).getDistance() - scans.get(0).getDistance())
                        < DISTANCE_THRESHOLD * (scans.get(2).getCount() + scans.get(1).getCount())) {
                    distance = scans.get(2).getDistance();
                    // 判断第2个采样周期和相隔的第1个采样周期的距离差值是否合理
                } else if (Math.abs(scans.get(1).getDistance() - scans.get(0).getDistance())
                        < DISTANCE_THRESHOLD * scans.get(1).getCount()) {
                    distance = scans.get(1).getDistance();
                }
            }

            if (distance > 0 && distance <= SCAN_RANGE) {
                myBeacons.add(new SystekBeacon(null, entry.getKey(), distance));
            }
        }

        if(myBeacons==null||myBeacons.size()==0){return myBeacons;}
        // 排序检测到的Beacon
        Collections.sort(myBeacons, new SystekBeacon());
        // 清空多次扫描不到的beacon
        for (String key : nullKey) {
            if (key != null && !key.equals("")) {
                scanCountMap.remove(key);
            }
        }
        return myBeacons;
    }

    private int status = 2; // 0:切同时刷，1：只刷不切,2:不刷不切
    private String msg = ""; // 消息
    private int flag = 0;
    private int flag_stop = 0;


    private String getMsg() {
        return msg;
    }

    // 根据用户的状态(停留、移动)决定展品列表是否刷新、切换
    public  synchronized List<SystekBeacon> getExhibitLocateBeacons() {

        //集合为空不计算
        if(myBeacons==null||myBeacons.size()==0){return myBeacons;}
        flag++;
        flag_stop++;
        if (flag_stop >= 6) {
            if (isStop(myBeacons)) {
                msg = "stop";
            } else {
                msg = "move";
            }
            flag_stop = 0;
        }
        if (lastBeacons == null) {
            lastBeacons = myBeacons;
        }
        if (flag >= 10) {
            if (isSame(lastBeacons, myBeacons)) {
                if (lastBeacons.get(0).getMinor().equals(myBeacons.get(0).getMinor())) {
                    status = 2;// 不切不刷，列表保持不变
                } else {
                    status = 1;// 只刷新不切换
                }
            }
            lastBeacons = myBeacons;// 每10次循环记录一次最新的beacon列表
            flag = 0;
        } else {
            // 循环取出每个扫描周期内，距离最小切小于1米的Beacon(的次号)，进行累计，计算出现的次数
            double nearestDistance = myBeacons.get(0).getDistance();
            //最近距离大于用户设定范围，不计算
            if(nearestDistance>USER_RANGE){return myBeacons;}

            minors.add(myBeacons.get(0).getMinor());
            //小于最近信标列表的长度,不计算
            if(minors.size() <= NEAREST_MINOR_COUNT){return myBeacons;}
            // 固定最近信标列表的长度(长度为8，第9个数据出现时，删除第一个数据)
            minors.remove(0);
            Map<String, ScanCount> map = new HashMap<>();
            for (String i : minors) {
                int count = map.get(i) == null ? 1 : map.get(i).getCount() + 1;
                ScanCount sc = new ScanCount(count, nearestDistance);
                map.put(i, sc);
            }
            // 计算哪个信标出现的次数多(8次扫描出现6次、本次最近的信标和上次最近的信标不同(并且绝对值小于阈值0.2))
            for (Map.Entry<String, ScanCount> entry : map.entrySet()) {
                if (entry.getValue().getCount() >= 6) {
                    flag = 0;
                    // 判断本次扫描结果是否和上次扫描结果相同
                    if (isSame(lastBeacons, myBeacons)) {// 两个周期的列表完全相同
                        status = 2;// 不切不刷
                    } else if (null != lastBeacons
                            && lastBeacons.size() > 0
                            && lastBeacons.get(0).getMinor()
                            .equals(myBeacons.get(0).getMinor())) {// 列表不同，第一个值相同
                        status = 2;// 只刷新，不切换
                    } else if (msg.equals("stop")) {
                        status = 0;// 切换并刷新
                        lastBeacons = myBeacons;
                    }
                }
            }
        }
        return myBeacons;
    }


    public  boolean isStop(List<SystekBeacon> beacons) {
        boolean stop = false;
        if(beacons != null && beacons.size() > 0) {
            if(nearestBeacon != null) {
                Iterator var4 = beacons.iterator();

                while(var4.hasNext()) {
                    SystekBeacon b = (SystekBeacon)var4.next();
                    if(nearestBeacon.getMinor().equals(b.getMinor())) {
                        double abs = Math.abs(nearestBeacon.getDistance() - b.getDistance());
                        if(abs <= 0.5D && nearestBeacon.getMinor().equals((beacons.get(0)).getMinor())) {
                            stop = true;
                        }
                    }
                }
            }

            if((beacons.get(0)).getDistance() < 1.5D) {
                nearestBeacon = beacons.get(0);
            }
        }
        return stop;
    }


    public static boolean isSame(List<SystekBeacon> thisList, List<SystekBeacon> lastList) {

        if(thisList==null||lastList==null){return false;}

        if(thisList.size() != lastList.size()) {
            return false;
        } else if(!(thisList.get(0)).getMinor().equals((lastList.get(0)).getMinor())) {
            return false;
        } else {
            int flag = 0;
            Iterator var5 = lastList.iterator();

            while(var5.hasNext()) {
                SystekBeacon lastBeacon = (SystekBeacon)var5.next();
                Iterator var7 = thisList.iterator();

                while(var7.hasNext()) {
                    SystekBeacon thisBeacon = (SystekBeacon)var7.next();
                    if(thisBeacon.getMinor().equals(lastBeacon.getMinor())) {
                        ++flag;
                    }
                }
            }

            if(flag == thisList.size() && Math.abs((thisList.get(0)).getDistance() - (lastList.get(0)).getDistance()) < TOW_BEACON_CHOOSE_THRESHOLD) {
                return true;
            } else {
                return false;
            }
        }
    }




}
