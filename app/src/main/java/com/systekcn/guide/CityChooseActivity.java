package com.systekcn.guide;

import com.systekcn.guide.activity.base.BaseActivity;
import com.systekcn.guide.common.utils.ViewUtils;

public class CityChooseActivity extends BaseActivity {

    @Override
    protected void initialize() {
        ViewUtils.setStateBarToAlpha(this);
        setContentView(R.layout.activity_city_choose);

    }

}
