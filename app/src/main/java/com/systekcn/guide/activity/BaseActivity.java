package com.systekcn.guide.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.systekcn.guide.MyApplication;
import com.systekcn.guide.common.IConstants;
import com.systekcn.guide.common.utils.ExceptionUtil;

/**
 * Created by Qiang on 2015/10/22.
 */
public class BaseActivity extends FragmentActivity implements IConstants{
    public  MyApplication application;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        application=(MyApplication)getApplication();
        try {
            MyApplication.listActivity.add(this);
        } catch (Exception e) {
            ExceptionUtil.handleException(e);
        }
    }
}
