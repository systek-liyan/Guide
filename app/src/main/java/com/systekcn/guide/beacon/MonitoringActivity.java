package org.altbeacon.mytest;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.logging.LogManager;
import org.altbeacon.beacon.logging.Loggers;
//import org.altbeacon.beacon.service.ArmaRssiFilter;
import org.altbeacon.beacon.service.RunningAverageRssiFilter;
import org.altbeacon.mytest.BeaconSearcher.OnNearestBeaconListener;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.TextView;
import edu.xidian.logtofile.LogcatHelper;

/**
 * 
 * @author dyoung
 * @author Matt Tyler
 */
public class MonitoringActivity extends Activity {
	protected static final String TAG = "MonitoringActivity";
	private BeaconSearcher mBeaconSearcher;

	private OnNearestBeaconListener mOnNearestBeaconListener = new OnNearestBeaconListener() {
		private ArrayList<Beacon> fixedLengthBeaconList = new ArrayList<Beacon>();

		@Override
		public void getNearestBeacon(int type, Beacon beacon) {
			String str = (type == NearestBeacon.GET_LOCATION_BEACON) ? "游客定位" : "展品定位";
			if (beacon != null&&beacon.getDistance()<=2) {
//				fixedLengthBeaconList.add(beacon);
//				int count = 0;
//				if (fixedLengthBeaconList.size() == 6) {
//					fixedLengthBeaconList.remove(0);
//
//					for (int i = 0; i < fixedLengthBeaconList.size(); i++) {
//						for (int j = 0; j < fixedLengthBeaconList.size(); j++) {
//							if (fixedLengthBeaconList.get(i).getId3().equals(fixedLengthBeaconList.get(j).getId3())) {
//								count++;
//							}
//						}
//						if (count >= 3) {
//							// System.out.println(fixedLengthBeaconList.get(i)+"相同的有"+count);
//							logToDisplay("最近：" + fixedLengthBeaconList.get(i).getId3()+"："+fixedLengthBeaconList.get(i).getDistance());
//							count = count-1;
//							break;
//						}
//						
//					}
					// logToDisplay(str+","+beacon.getId3()+":"+fixedLengthBeaconList.get(0).getId3()+":"+fixedLengthBeaconList.get(1).getId3()+":"+fixedLengthBeaconList.get(2).getId3()+":"+fixedLengthBeaconList.get(3).getId3()+":"+fixedLengthBeaconList.get(4).getId3());
//					logToDisplay(fixedLengthBeaconList.get(0).getId3() + ":"
//							+ fixedLengthBeaconList.get(1).getId3() + ":" + fixedLengthBeaconList.get(2).getId3() + ":"
//							+ fixedLengthBeaconList.get(3).getId3() + ":" + fixedLengthBeaconList.get(4).getId3());
//				}
				
				logToDisplay("最近：" + beacon.getId3()+"："+beacon.getDistance()+":=="+beacon.getRssi()+"="+beacon.getTxPower());
			} else
				logToDisplay("无" + str + "Beacon");

		}

	};

	private static LogcatHelper loghelper; // 日志文件
	private Button start_logfile; // 开始记录日志文件
	private Button end_logfile; // 停止日志文件
	private String Logformat = ""; // 日志拟制符格式

	private EditText ScanPeriod_edit; // 前台扫描周期
	private EditText StayTime_edit; // 最小停留时间
	private EditText Distance_edit; // 最小距离
	private EditText Speed_edit; // 收敛系数

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// 建议使用org.altbeacon.beacon.logging.LogManager.javaLogManager输出日志，altbeacon就是使用这种机制，便于发布版本时，减少输出日志信息。
		// 输出所有ERROR(Log.e()), WARN(Log.w()), INFO(Log.i()), DEBUG(Log.d()),
		// VERBOSE(Log.v())
		// 对应日志级别由高到低
		LogManager.setLogger(Loggers.verboseLogger());

		// 全部不输出，在release版本中设置
		// LogManager.setLogger(Loggers.empty());

		// 输出ERROR(Log.e()), WARN(Log.w()),缺省状态，仅输出错误和警告信息，即输出警告级别以上的日志
		// LogManager.setLogger(Loggers.warningLogger());

		// 试验日志输出
		// LogManager.e(TAG,"Error");
		// LogManager.w(TAG,"Warn");
		// LogManager.i(TAG,"info");
		// LogManager.d(TAG,"debug");
		// LogManager.v(TAG,"verbose");

		LogManager.d(TAG, "onCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// 日志文件
		start_logfile = (Button) findViewById(R.id.start_log);
		end_logfile = (Button) findViewById(R.id.end_log);

		// 设置SD卡中的日志文件,sd卡根目录
		// loghelper = LogcatHelper.getInstance(this,"mylog1","mydistance.log");
		loghelper = LogcatHelper.getInstance(this, "", "mydistance.log");

		// 打印D级以上(包括D,I,W,E,F)的TAG，其它tag不打印
		// Logformat = TAG + ":D *:S";

		// 打印D级以上的TAG，和LogcatHelper全部，其它tag不打印
		// Logformat = TAG + ":D LogcatHelper:V *:S";

		// 打印D以上的TAG和BeaconSearcher，其他tag不打印(*:S)
		// Logformat = TAG + ":D BeaconSearcher:D *:S";

		// 打印D以上的BeaconSearcher，其他tag不打印(*:S)
		Logformat = "BeaconSearcher:D *:S";

		// Logformat = "RangedBeacon:V *:S";

		// 打印所有日志， priority=V | D | I | W | E ,级别由低到高
		// Logformat = "";

		// 日志文件
		loghelper.start(Logformat);

		// "开始"按钮失效
		start_logfile.setEnabled(false);

		// 获取BeaconSearcher唯一实例
		mBeaconSearcher = BeaconSearcher.getInstance(this);

		// 显示默认前台扫描周期,default 1.1s
		ScanPeriod_edit = (EditText) findViewById(R.id.ScanPeriod_edit);
		ScanPeriod_edit.setText("1.31");

		// 显示默认最小停留时间，用于展品定位
		StayTime_edit = (EditText) findViewById(R.id.stayTimeEdit);
		StayTime_edit.setText("" + mBeaconSearcher.getMin_stay_milliseconds() / 1000);

		// 显示默认最小距离，用于展品定位
		Distance_edit = (EditText) findViewById(R.id.distanceEdit);
		Distance_edit.setText("" + mBeaconSearcher.getExhizibit_distance());

		//////////////////////////////////////////////////////////////////////////
		// 自回归滑动滤波
		// /**
		// * 设置Rssi滤波模型
		// * Default class for rssi filter/calculation
		////////////////////////////////////////////////////////////////////////// implementation：RunningAverageRssiFilter.class
		// * others：ArmaRssiFilter.class
		// */
		// BeaconSearcher.setRssiFilterImplClass(ArmaRssiFilter.class);
		//
		// // 收敛系数,仅用于ArmaRssiFilter
		// Speed_edit = (EditText)findViewById(R.id.speedEdit);
		// Speed_edit.setText("0.1");
		// Button speedBtn = (Button)findViewById(R.id.speedBtn);
		// speedBtn.setText("收敛系数");

		/////////////////////////////////////////////////////////////////////////
		// 均值滤波
		/**
		 * 设置Rssi滤波模型 Default class for rssi filter/calculation
		 * implementation：RunningAverageRssiFilter.class
		 * others：ArmaRssiFilter.class
		 */
		BeaconSearcher.setRssiFilterImplClass(RunningAverageRssiFilter.class);

		// RSSI采样时间
		Speed_edit = (EditText) findViewById(R.id.speedEdit);
		Speed_edit.setText("1.5");

		Button speedBtn = (Button) findViewById(R.id.speedBtn);
		// speedBtn.setText("采样周期(s)");
		////////////////////////////////////////////////////////////////////////////

		// 设置找到最近beacon的回调
		mBeaconSearcher.setNearestBeaconListener(mOnNearestBeaconListener);

		// 选择获取游客定位、展品定位beacon
		final CheckBox cb = (CheckBox) findViewById(R.id.checkBox1);
		cb.setChecked(true);
		cb.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, final boolean isChecked) {
				if (isChecked) {
					// cb.setText("游客定位beacon");
					// 设置获取最近beacon类型
					mBeaconSearcher.setNearestBeaconType(NearestBeacon.GET_LOCATION_BEACON);
				} else {
					// cb.setText("展品定位beacon");
					// 设置获取最近beacon类型
					mBeaconSearcher.setNearestBeaconType(NearestBeacon.GET_EXHIBIT_BEACON);
				}
			}
		});

		logToDisplay("Mstart,Mstop分别代表查找最近的beacon的开始和结束");
	}

	@Override
	protected void onDestroy() {
		LogManager.d(TAG, "onDestroy()");
		super.onDestroy();

		mBeaconSearcher.closeSearcher();
		loghelper.stop();
	}

	/** 开始记录日志文件 */
	public void onStartLog(View view) {
		loghelper.start(Logformat);
		start_logfile.setEnabled(false);
		end_logfile.setEnabled(true);
	}

	/** 结束记录日志文件 */
	public void onEndLog(View view) {
		loghelper.stop();
		start_logfile.setEnabled(true);
		end_logfile.setEnabled(false);
	}

	/** 删除日志文件文件 */
	public void onDelLog(View view) {
		loghelper.delLogDir();
	}

	/** 开始查找最近beacon */
	public void onMonitoringStart(View view) {
		logToDisplay("onMonitoringStart(),startMonitoringBeaconsInRegion");
		LogManager.d(TAG, "onMonitoringStart(),startMonitoringBeaconsInRegion");

		mBeaconSearcher.openSearcher();
	}

	/** 停止查找beacon */
	public void onMonitoringStop(View view) {
		logToDisplay("onMonitoringStop(),stopMonitoringBeaconsInRegion");
		LogManager.d(TAG, "onMonitoringStop(),stopMonitoringBeaconsInRegion");
		mBeaconSearcher.closeSearcher();
	}

	/** 设置前台扫描周期 */
	public void onForegroundScanPeriod(View view) {
		String period_str = ScanPeriod_edit.getText().toString();
		long period = (long) (Double.parseDouble(period_str) * 1000.0D);
		mBeaconSearcher.setForegroundScanPeriod(period);
	}

	// 显示最小停留时间，用于展品定位
	public void onStayTimeBtn(View view) {
		String str = StayTime_edit.getText().toString();
		long t = (long) (Double.parseDouble(str) * 1000.0D);
		mBeaconSearcher.setMin_stay_milliseconds(t);
	}

	// 显示最小距离，用于展品定位
	public void onDistanceBtn(View view) {
		String str = Distance_edit.getText().toString();
		mBeaconSearcher.setExhibit_distance(Double.parseDouble(str));
	}

	////////////////////////////////////////////////////////////////////
	// 收敛系数，仅用于ArmaRssiFilter
	// 采样周期，用于均值滤波
	public void onSpeedBtn(View view) {
		String str = Speed_edit.getText().toString();

		// 收敛系数，仅用于ArmaRssiFilter
		// BeaconSearcher.setDEFAULT_ARMA_SPEED(Double.parseDouble(str));

		// 采样周期，用于均值滤波
		long t = (long) (Double.parseDouble(str) * 1000.0D);
		BeaconSearcher.setSampleExpirationMilliseconds(t);
	}

	public void logToDisplay(final String line) {
		runOnUiThread(new Runnable() {
			Date date = new Date(System.currentTimeMillis());
			SimpleDateFormat sfd = new SimpleDateFormat("ss.SSS", Locale.CHINA);
			String dateStr = sfd.format(date);

			public void run() {
				// EditText editText =
				// (EditText)MonitoringActivity.this.findViewById(R.id.monitoringText);
				TextView editText = (TextView) MonitoringActivity.this.findViewById(R.id.monitoringText);
				editText.append(dateStr + "=" + line + "\n");
			}
		});
	}

}
