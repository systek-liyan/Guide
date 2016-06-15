package org.altbeacon.beacon.client.altbeacon.beacon.startup;

import android.content.Context;

import org.altbeacon.beacon.client.altbeacon.beacon.MonitorNotifier;

public interface BootstrapNotifier extends MonitorNotifier {
    public Context getApplicationContext();
}
