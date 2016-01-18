package com.systek.guide.adapter.base;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.systek.guide.fragment.BaseFragment;

import java.util.ArrayList;


/**
 * Created by Qiang on 2015/11/26.
 *
 * viewpager适配器
 */
public class ViewPagerStateAdapter extends FragmentStatePagerAdapter {
    private ArrayList<BaseFragment> fragmentBases = new ArrayList<BaseFragment>();

    public ArrayList<BaseFragment> getFragmentBases() {
        return fragmentBases;
    }

    public void setFragmentBases(ArrayList<BaseFragment> fragmentBases) {
        this.fragmentBases = fragmentBases;
    }

    public ViewPagerStateAdapter(FragmentManager fm) {
        super(fm);
    }

    public ViewPagerStateAdapter(FragmentManager fm, ArrayList<BaseFragment> fragmentBases) {
        super(fm);
        this.fragmentBases = fragmentBases;
    }

    @Override
    public Fragment getItem(int arg0) {
        return fragmentBases.get(arg0);
    }

    @Override
    public int getCount() {
        return fragmentBases.size();
    }
}
