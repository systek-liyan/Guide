package com.systek.guide.activity;

import android.os.Bundle;

import com.systek.guide.R;

public class SettingActivity extends BaseActivity {


    @Override
    protected void initialize(Bundle savedInstanceState) {
        setContentView(R.layout.activity_setting);
        initDrawer();
    }

}
