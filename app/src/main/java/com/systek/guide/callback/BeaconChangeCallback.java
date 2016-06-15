package com.systek.guide.callback;

import com.systek.guide.entity.BeaconBean;
import com.systek.guide.entity.ExhibitBean;

import java.util.List;

/**
 * Created by Qiang on 2016/5/24.
 */
public interface BeaconChangeCallback {

    void getExhibits(List<ExhibitBean> exhibits);
    void getNearestExhibit(ExhibitBean exhibit);
    void getNearestBeacon(BeaconBean bean);
}