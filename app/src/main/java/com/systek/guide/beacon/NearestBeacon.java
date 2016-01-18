package com.systek.guide.beacon;



import org.altbeacon.beacon.Beacon;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * <pre>
 * 一、展品定位
 *   展品beacon，距离最近(m)(并且小于最小距离NEAREST_DISTANCE)，逗留时间大于最小停留时间(ms)(MIN_STAY_MILLISECONDS) 
 *       NearestBeacon mNearestBeacon = new NearestBeacon();
 *       mNearestBeacon.setExhibit_distance(3);          // 3m, default NEAREST_DISTANCE
 *       mNearestBeacon.setMin_stay_milliseconds(3000);  // 3000ms, default MIN_STAY_MILLISECONDS
 *       // 在RangeNotifier接口的回调函数didRangeBeaconsInRegion()中执行：
 *       Beacon beacon = mNearestBeacon.getNeaestBeacon(NearestBeacon.GET_EXHIBIT_BEACON,beacons);
 *   客户端注意：在播放展品介绍期间，getNeaestBeacon(type=NearestBeacon.GET_EXHIBIT_BEACON)返回的符合以上条件的beacon.
 *   (1)与当前播放展品对应的beacon相同，则忽略之，继续播放该展品。
 *   (2)与当前播放展品对应的beacon不同，则停止当前播放，转而播放收到beacon对应的展品。
 *   (3)返回null,处理方式同(1)
 *
 * 二、游客定位
 *   返回距离最近(m)的beacon
 *   NearestBeacon mNearestBeacon = new NearestBeacon();
 *   // 在RangeNotifier接口的回调函数didRangeBeaconsInRegion()中执行：
 *   Beacon beacon = mNearestBeacon.getNeaestBeacon(NearestBeacon.GET_LOCATION_BEACON,beacons);
 * @author Dun
 *
 */
public class NearestBeacon {

	/** 默认最近距离(m),用于展品定位 **/
	public static double NEAREST_DISTANCE = 1.5; // Can we change its to
	// DISTANCE_THRESHOLD, and
	// should we set a less
	// number.

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

	/**
	 * 上一扫描周期存储的符合条件的展品定位beacon，距离小于mExhibit_distance，
	 * 逗留时间大于mMin_stay_milliseconds
	 */
	private Beacon mExhibitBeacon = null;

	/** mExhibitBeacon对应的时间戳 */
	private long mTimestamp = 0L;

	/** 根据距离排序beacons，用于计算距离最近的beacon */
	private ArrayList<BeaconForSort> mBeaconList = new ArrayList<BeaconForSort>();

	/** 扫描到的beacon 以及距离beacon的距离 by sa */
	private Map<String, List<Beacon>> map = new HashMap<String, List<Beacon>>();

	/** 阈值,用于2个超近的信标的选择 by sa */
	private double threshold = 0.2;

	/** 阈值,用于2个超近的信标的选择 by sa */
	private double avgDistance = 0D;

	/** 集合默认长度(后台扫描周期1/前台扫描周期0.5*时长3) by sa */
	private int INDEX = 6;

	/**
	 * 展品定位使用默认最小距离和最小停留时间<br>
	 * 默认最小距离,NearestBeacon.NEAREST_DISTANCE
	 * 默认最小停留时间(ms)，NearestBeacon.MIN_STAY_MILLISECONDS
	 *
	 * @see NearestBeacon#NEAREST_DISTANCE
	 * @see NearestBeacon#MIN_STAY_MILLISECONDS
	 */
	public NearestBeacon() {
		mExhibit_distance = NEAREST_DISTANCE;
		mMin_stay_milliseconds = MIN_STAY_MILLISECONDS;
	}

	/**
	 * 参数设定展品定位时的最小距离和最小停留时间
	 *
	 * @param nearest_distance最小距离
	 *            (m), min_stay_time最小停留时间(ms)
	 */
	public NearestBeacon(double nearest_distance, long min_stay_time) {
		mExhibit_distance = nearest_distance;
		mMin_stay_milliseconds = min_stay_time;
	}

	/**
	 * 获得当前设定用于展品定位的最小距离(m)
	 *
	 * @return the mExhibit_distance
	 */
	public double getExhizibit_distance() {
		return mExhibit_distance;
	}

	/**
	 * 设定用于展品定位的最小距离(m)
	 *
	 * @param Exhibit_distance
	 *            the mExhibit_distance to set
	 */
	public void setExhibit_distance(double Exhibit_distance) {
		this.mExhibit_distance = Exhibit_distance;
	}

	/**
	 * 获得当前设定用于展品定位的最小停留时间(ms)
	 *
	 * @return the mMin_stay_milliseconds
	 */
	public long getMin_stay_milliseconds() {
		return mMin_stay_milliseconds;
	}

	/**
	 * 设定用于展品定位的最小停留时间(ms)
	 *
	 * @param Min_stay_milliseconds
	 *            the mMin_stay_milliseconds to set
	 */
	public void setMin_stay_milliseconds(long Min_stay_milliseconds) {
		this.mMin_stay_milliseconds = Min_stay_milliseconds;
	}

	/**
	 * 每个扫描周期结束，获取展品定位beacon或游客定位beacon<br>
	 * 注:展品定位beacon: 距离小于最小距离，并且逗留时间大于最小停留时间的beacon<br>
	 * 游客定位beacon: 距离最近的beacon，展品的beacon也可以用于定位游客位置 <br>
	 * 应该没有同时要求返回两种beacon的情况存在。因此，用type区分返回beacon的类型是可行的。<br>
	 * 在RangeNotifier接口的回调函数didRangeBeaconsInRegion()中调用此函数。<br>
	 * 每个扫描周期结束，根据20秒内各beacon的RSSI平均值计算它的距离，该回调获取这些beacon的距离值。<br>
	 *
	 * @param type
	 *            NearestBeacon.GET_LOCATION_BEACON:返回游客定位beacon;
	 *            NearestBeacon.GET_EXHIBIT_BEACON:返回展品定位beacon
	 * @param beacons
	 *            本次扫描周期发现的beacon,BeaconService对这些beacon进行了距离修正。
	 * @return 根据type，返回展品定位beacon或游客定位beacon或null
	 */
	public List<BeaconForSort> getNearestBeacon(int type, final Collection<Beacon> beacons) {
		List<BeaconForSort>beaconList = nearestBeacon(beacons);
		if (type == GET_LOCATION_BEACON)
			return beaconList;
		if (type == GET_EXHIBIT_BEACON)
			return exhibitBeacon(); // 注意必须在nearestBeacon(beacons)后调用。
		return null;
	}

	/**
	 * 每个扫描周期结束，获取最近的Beacon<br>
	 * 每个扫描周期结束对beacon进行归类，写入List<Beacon>,并将List存入Map<beaconId,List<Beacon>>,
	 * key为beaconId<br>
	 * 多次扫描后，根据beaconId(也就是同一个信标)计算距离(Beacon.distance)的平均值<br>
	 * 获得距离(Beacon.distance)的平均值后,然后将一组平均值进行比较，获取最小的平均值<br>
	 * 最小的平均值对应的信标(Beacon)即为最近的信标<br>
	 *
	 * @param beacons
	 * <br>
	 * @return 距离最近的beacon; 如果本次扫描，没有beacon发现，返回null<br>
	 * @author sa
	 * */
	private List<BeaconForSort> nearestBeacon(final Collection<Beacon> beacons) {
		// 信标的ID
		String beaconId;
		// 手机和beacon的距离
		Double beaconDistance = 0.0;

		// 本次扫描周期没有发现beacon
		if (beacons.size() == 0) {
			mNeaestBeacon = null;
			mNeaestBeacon_distance = -1.0D;
			return null;
		}
		mBeaconList.clear();
		Iterator<Beacon> iterator = beacons.iterator();

		// 一个扫描周期结束后获取到beacons,并以beaconId为key把beacon归类并放入Map<beaconId,List<Beacon>>中
		// 第一次循环，创建Map,key为：beaconId，value为：List<Beacon>
		// 再次循环,判断beaconId是否存在Map中，如果存在则List.add,不存在则Map.put
		while (iterator.hasNext()) {
			Beacon beacon = iterator.next();
			List<Beacon> list;
			beaconId = beacon.getId3().toString();
			beaconDistance = beacon.getDistance();
			// 如果检测到的beaconId在map中不存在，则创建
			// 如果map中已经存在beaconId,那就累计"距离值"到list中
			if (map.get(beaconId) == null) {
				list = new ArrayList<>();
				list.add(beacon);
				map.put(beaconId, list);
			} else {
				list = map.get(beaconId);
				list.add(beacon);
				if (list.size() == INDEX) {
					list.remove(0);
				}
				map.put(beaconId, list);
			}
		}

		// 循环Map中的List判断哪个是最近的beacon
		// Map中的一条数据对应的是同一个信标的多次检测数据
		// 计算出同一个信标的距离平均值，再于别的信标的平均值比较从而得到最近的信标
		for (Map.Entry<String, List<Beacon>> entry : map.entrySet()) {
			List<Beacon> beaconList = entry.getValue();
			// 平均值
			double ave = 0;//
			if (beaconList != null) {
				for (int i = 0; i < beaconList.size(); i++) {
					// 循环取出同一个信标的距离值并累计，用于求平均值
					ave += Double.parseDouble(beaconList.get(i).getDistance()
							+ "");
					// 在最后一次循环时,获取距离的平均值
					if (i == beaconList.size() - 1) {
						ave = ave / beaconList.size();
						//展品在设定距离范围内(1.5米)时，将其放入List
						//由于定位不能将范围限定为1.5米，这里取消范围限定，只按顺序返回检索到的信标，由使用者处理"范围"  2015-12-17 by sa
						//if(ave<=mExhibit_distance) {
						// 将平均值和Beacon对象存入List，通过该List即可获取最近信标
						BeaconForSort bfs = new BeaconForSort();
						bfs.beacon = beaconList.get(i);
						bfs.distance = ave;
						mBeaconList.add(bfs);
						//Log.i("zz", bfs.getDistance() + "--------------" + bfs.getBeacon().getId3());
						//}
					}
				}
			}
		}

		// 获取最小平均值及最近Beacon
		double min = 0;// 最小距离
		if (mBeaconList != null) {
			//排序检测到的Beacon
			BeaconForSort bf = new BeaconForSort();
			Collections.sort(mBeaconList,bf);
//			for(int i=0;i<mBeaconList.size();i++){
//				//Log.i("twoooo", "信标ID："+ bf.beacon.getId3()+" 距离 :"+bf.distance);
//				if(bf.distance>mExhibit_distance){
//					mBeaconList.remove(i);
//				}
//				Log.i("zz",mBeaconList.get(i).getDistance()+"--------------"+mBeaconList.get(i).getBeacon().getId3());
//			}
			return mBeaconList;
		} else
			return null;
	}



	/**
	 * 符合条件的信标列表
	 * 条件：NEAREST_DISTANCE距离范围内
	 * @return 符合条件的beaconList; 不符合条件，返回null
	 * @author sa
	 * 2015-11-26
	 */
	int count=0;
	private List<BeaconForSort> exhibitBeacon() {
		count++;
		return mBeaconList;
	}


}
