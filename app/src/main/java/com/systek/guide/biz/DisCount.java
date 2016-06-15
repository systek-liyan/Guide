package com.systek.guide.biz;

import com.systek.guide.MyApplication;

import org.altbeacon.beacon.Beacon;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

/**
 * Created by Qiang on 2016/6/3.
 */
public class DisCount {

    private static DisCount instance;
    private LinkedList<Beacon> beaconLink=new LinkedList<>();

    private Beacon lastBeacon;

    public Beacon getLastBeacon() {
        return lastBeacon;
    }

    final Object o=new Object();
    public static DisCount getInstance() {
        if(instance==null){
            synchronized (MyApplication.class){
                if(instance==null){
                    instance=new DisCount();
                }
            }
        }
        return instance;
    }

    public synchronized Beacon dis(Collection<Beacon> beaconList){

        if(beaconList==null||beaconList.size()==0){return null; }
        ArrayList<Beacon> tempList=new ArrayList<>(beaconList);
        Collections.sort(tempList, new Comparator<Beacon>() {
            @Override
            public int compare(Beacon lhs, Beacon rhs) {
                return lhs.getDistance()<=rhs.getDistance()?-1:1;
            }
        });
        beaconLink.add(tempList.get(0));
        if(beaconLink.size()<10){return null;}
        if(beaconLink.size()>10){
            beaconLink.removeFirst();
        }
        Beacon beacon=maxCount(beaconLink);
        if(beacon!=null){
            lastBeacon=beacon;
        }
        return beacon;

    }
    public synchronized ArrayList<Beacon> disArray(Collection<Beacon> beaconList){

        if(beaconList==null||beaconList.size()==0){return null; }
        ArrayList<Beacon> tempList=new ArrayList<>(beaconList);
        Collections.sort(tempList, new Comparator<Beacon>() {
            @Override
            public int compare(Beacon lhs, Beacon rhs) {
                return lhs.getDistance()<=rhs.getDistance()?-1:1;
            }
        });

        return tempList;

    }


    public synchronized Beacon maxCount(LinkedList<Beacon> beaconLinkedList) {

        //这里用你给的这个数组简单测试一下。
        //Object数组中存放对象，
        Set<Beacon> s = new HashSet<>();//HashSet用来去掉重复
        for (Beacon b : beaconLinkedList) {
            s.add(b);
        }  //现在的集合s中无重复的包含beaconLinkedList中的所有元素

        ArrayList<Beacon> beacons2 = new ArrayList<>(s);//把集合s中的元素存入数组beacons2中
        int[] n = new int[beacons2.size()];//这个数组用来存放每一个元素出现的次数
        int max = 0;
        for (int i = 0; i < beacons2.size(); i++) {
            int count = 0;
            for (int j = 0; j < beaconLinkedList.size(); j++) {
                if (beacons2.get(i).equals(beaconLinkedList.get(j))) {
                    count++;
                }
                //用obj2中的元素跟obj1中的每一个比较，如果相同cout自增
            }
            n[i] = count;//每一个元素出现的次数存入数组n
            //数组n的下标i跟数组obj2的下标是一一对应的。
            if (max < count) {//得到元素出现次数最多是多少次
                max = count;
            }
        }
        for (int i = 0; i < n.length; i++) {
            if (max == n[i] && max>=5) {
                //如果出现的次数等于最大次数，就输出对应数组obj2中的元素
                return beacons2.get(i);
                // System.out.println(beacons2.get(i));
            }
        }
        return null;
    }


}
