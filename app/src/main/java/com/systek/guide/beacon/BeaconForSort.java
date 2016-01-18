package com.systek.guide.beacon;


import org.altbeacon.beacon.Beacon;

import java.util.Comparator;

/**
 * 实现Comparator 并根据distance(距离的平均值) 排序
 * @author sa
 * 2015-11-26
 */
public class BeaconForSort implements Comparator<BeaconForSort>{

	Beacon beacon;
	double distance;

	public Beacon getBeacon() {
		return beacon;
	}

	public double getDistance() {
		return distance;
	}

	@Override
	public int compare(BeaconForSort lhs, BeaconForSort rhs) {
		// TODO Auto-generated method stub
		return new Double(lhs.getDistance()+"").compareTo(new Double (rhs.getDistance()+""));
	}
	

}
