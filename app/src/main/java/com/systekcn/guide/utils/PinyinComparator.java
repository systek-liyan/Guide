package com.systekcn.guide.utils;

import com.systekcn.guide.entity.CityBean;

import java.util.Comparator;

/**
 * Created by Qiang on 2015/12/28.
 */
public class PinyinComparator implements Comparator<CityBean> {

    public int compare(CityBean o1, CityBean o2) {
        if (o1.getAlpha().equals("@")
                || o2.getAlpha().equals("#")) {
            return -1;
        } else if (o1.getAlpha().equals("#")
                || o2.getAlpha().equals("@")) {
            return 1;
        } else {
            return o1.getAlpha().compareTo(o2.getAlpha());
        }
    }

}
