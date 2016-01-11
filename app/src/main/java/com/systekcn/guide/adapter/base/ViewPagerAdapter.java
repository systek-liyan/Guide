package com.systekcn.guide.adapter.base;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.ViewGroup;

import com.systekcn.guide.fragment.BaseFragment;

import java.util.ArrayList;

/**
 * Created by Qiang on 2015/11/26.
 */
public class ViewPagerAdapter extends FragmentPagerAdapter {
    private ArrayList<BaseFragment> fragmentBases = new ArrayList<BaseFragment>();

    public void setFragmentBases(ArrayList<BaseFragment> fragmentBases) {
        this.fragmentBases = fragmentBases;
    }

    public ViewPagerAdapter(FragmentManager fragmentManager) {
        super(fragmentManager);
    }

    public ViewPagerAdapter(FragmentManager fragmentManager, ArrayList<BaseFragment> fragmentBases) {
        super(fragmentManager);
        this.fragmentBases = fragmentBases;
    }

    @Override
    public Fragment getItem(int position) {
        return fragmentBases.get(position);
    }

    @Override
    public int getCount() {
        return fragmentBases.size();
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        // super.destroyItem(container, position, object);
    }
}
