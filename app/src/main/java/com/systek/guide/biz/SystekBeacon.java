package com.systek.guide.biz;

import java.util.Comparator;

/**
 * Created by Qiang on 2016/5/30.
 */
public class SystekBeacon implements Comparator<SystekBeacon> {

    String major;
    String minor;
    double distance;

    public SystekBeacon() {
    }

    public SystekBeacon(String major, String minor, double distance) {
        this.major = "10001";
        this.minor = minor;
        this.distance = distance;
    }

    public String getMajor() {
        return this.major;
    }

    public void setMajor(String major) {
        this.major = major;
    }

    public String getMinor() {
        return this.minor;
    }

    public void setMinor(String minor) {
        this.minor = minor;
    }

    public double getDistance() {
        return this.distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public int compare(SystekBeacon arg0, SystekBeacon arg1) {
        return (Double.valueOf(String.valueOf(arg0.getDistance()))).compareTo(Double.valueOf(String.valueOf(arg1.getDistance())));
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) return true;
        if (!(o instanceof SystekBeacon)) return false;
        SystekBeacon beacon = (SystekBeacon) o;
        return minor.equals(beacon.getMinor())
                && major.equals(beacon.getMajor())
                && Math.abs(distance - beacon.getDistance()) <= 0.2D;
    }
}
