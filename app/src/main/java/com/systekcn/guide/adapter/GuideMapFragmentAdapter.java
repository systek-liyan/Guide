package com.systekcn.guide.adapter;


import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.List;


/**
 * Created by Qiang on 2015/11/3.
 */
public class GuideMapFragmentAdapter extends FragmentPagerAdapter {

    List<Fragment> fragmentList;

    public GuideMapFragmentAdapter(FragmentManager fm,List<Fragment> fragmentList) {
        super(fm);
        this.fragmentList=fragmentList;
    }


    @Override
    public Fragment getItem(int position) {
        return fragmentList.get(position);
    }

    @Override
    public int getCount() {
        return fragmentList.size();
    }
}
