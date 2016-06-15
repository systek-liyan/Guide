package org.altbeacon.beacon.client.altbeacon.beacon.simulator;

import org.altbeacon.beacon.client.altbeacon.beacon.Beacon;

import java.util.List;

/**
 * Created by dyoung on 4/18/14.
 */
public interface BeaconSimulator {
    public List<Beacon> getBeacons();
}
