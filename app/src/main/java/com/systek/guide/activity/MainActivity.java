package com.systek.guide.activity;


import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;

import com.systek.guide.R;
import com.systek.guide.custom.swipeback.SwipeBackActivity;

/**
 * 测试类
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class MainActivity extends SwipeBackActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //setTitleBar();
        //toolbar.setNavigationIcon(R.drawable.setting);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar ( toolbar );
        getSupportActionBar (). setDisplayShowTitleEnabled ( false );
        getSupportActionBar ().setDisplayHomeAsUpEnabled ( true );
        getSupportActionBar(). setHomeButtonEnabled ( true );

       /* handler=new Handler();

        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBluetooth, 1);
        }
        scan();*/
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.normal_menu,menu);
        menu.getItem(0).setIcon(R.drawable.iv_back_normal);
        return true;
    }




    /*

    private BluetoothAdapter mBluetoothAdapter;

    Handler handler;

    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {


        @Override
        public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord) {
            int startByte = 2;
            boolean patternFound = false;
            // 寻找ibeacon
            while (startByte <= 5) {
                if (((int) scanRecord[startByte + 2] & 0xff) == 0x02 && // Identifies
                        // an
                        // iBeacon
                        ((int) scanRecord[startByte + 3] & 0xff) == 0x15) { // Identifies
                    // correct
                    // data
                    // length
                    patternFound = true;
                    break;
                }
                startByte++;
            }
            // 如果找到了的话
            if (patternFound) {
                // 转换为16进制
                byte[] uuidBytes = new byte[16];
                System.arraycopy(scanRecord, startByte + 4, uuidBytes, 0, 16);
                String hexString = bytesToHex(uuidBytes);

                // ibeacon的UUID值
                String uuid = hexString.substring(0, 8)
                        + "-" + hexString.substring(8, 12)
                        + "-" + hexString.substring(12, 16)
                        + "-" + hexString.substring(16, 20)
                        + "-" + hexString.substring(20, 32);

                // ibeacon的Major值
                int major = (scanRecord[startByte + 20] & 0xff) * 0x100 + (scanRecord[startByte + 21] & 0xff);

                // ibeacon的Minor值
                int minor = (scanRecord[startByte + 22] & 0xff) * 0x100 + (scanRecord[startByte + 23] & 0xff);

                String ibeaconName = device.getName();
                String mac = device.getAddress();
                int txPower = (scanRecord[startByte + 24]);
                Log.d("zhang", bytesToHex(scanRecord));
                Log.d("zhang",
                          "Name：" + ibeaconName
                        + "\nMac：" + mac
                        + " \nUUID：" + uuid
                        + "\nMajor：" + major
                        + "\nMinor：" + minor
                        + "\nTxPower：" + txPower
                        + "\nrssi：" + rssi);

                Log.d("zhang","distance："+calculateAccuracy(txPower,rssi));
            }
        }
    };
    private BluetoothLeScanner bluescaner;

ScanCallback scanCallback=new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
        }
    };

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void scan() {
        bluescaner=mBluetoothAdapter.getBluetoothLeScanner();
        bluescaner.startScan(scanCallback);
        //mBluetoothAdapter.startLeScan(mLeScanCallback);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //mBluetoothAdapter.stopLeScan(mLeScanCallback);
                bluescaner.stopScan(scanCallback);

            }
        }, 2000);


    }
    static final char[] hexArray = "0123456789ABCDEF".toCharArray();

    private static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    protected static double calculateAccuracy(int txPower, double rssi) {
        if (rssi == 0) {
            return -1.0; // if we cannot determine accuracy, return -1.
        }

        double ratio = rssi * 1.0 / txPower;
        if (ratio < 1.0) {
            return Math.pow(ratio, 10);
        } else {
            double accuracy = (0.89976) * Math.pow(ratio, 7.7095) + 0.111;
            return accuracy;
        }
    }
    */

    @Override
    protected void onDestroy() {
        //handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}
