package org.altbeacon.beacon.service.scanner;

import android.bluetooth.BluetoothDevice;

/**
 * Created by dyoung on 10/6/14.
 */
public interface CycledLeScanCallback {
     void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord);
     void onCycleEnd();
}
