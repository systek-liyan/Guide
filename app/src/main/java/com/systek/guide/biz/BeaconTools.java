package com.systek.guide.biz;

import java.text.NumberFormat;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Qiang on 2016/5/30.
 */
public class BeaconTools {

    private static final double TOW_BEACON_CHOOSE_THRESHOLD = 0.2D;
    private  SystekBeacon nearestBeacon = null;

    public BeaconTools() {
    }

    public static double isExist(String id3, double distance, List<SystekBeacon> beacons) {
        double abs = -1.0D;
        Iterator var9 = beacons.iterator();

        while(var9.hasNext()) {
            SystekBeacon sb = (SystekBeacon)var9.next();
            if(sb.getMinor().equals(id3)) {
                abs = Math.abs(distance - sb.getDistance());
                break;
            }
        }

        return abs;
    }

    public static double getRatio(String id3, double distance, List<SystekBeacon> beacons) {
        double abs = -1.0D;
        Iterator var8 = beacons.iterator();

        while(var8.hasNext()) {
            SystekBeacon sb = (SystekBeacon)var8.next();
            if(sb.getMinor().equals(id3)) {
                abs = Math.abs(distance - sb.getDistance());
                break;
            }
        }

        return abs;
    }

    public static double percentage(double num, double total) {
        NumberFormat numberFormat = NumberFormat.getInstance();
        numberFormat.setMaximumFractionDigits(2);
        String result = numberFormat.format(num / total * 100.0D);
        return Double.parseDouble(result);
    }

    public static <T> List<T> indexExChange(List<T> list, int index1, int index2) {
        T t = list.set(index1, list.get(index2));
        list.set(index2, t);
        return list;
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

}
