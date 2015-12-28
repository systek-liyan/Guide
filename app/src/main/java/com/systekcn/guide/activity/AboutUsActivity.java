package com.systekcn.guide.activity;

import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;

import com.systekcn.guide.R;
import com.systekcn.guide.activity.base.BaseActivity;
import com.systekcn.guide.common.utils.ViewUtils;
import com.systekcn.guide.custom.DrawerView;
import com.systekcn.guide.custom.slidingmenu.SlidingMenu;

public class AboutUsActivity extends BaseActivity {

    private ImageView iv_about_us_drawer;
    private SlidingMenu side_drawer;

    @Override
    public void initialize() {
        ViewUtils.setStateBarColor(this, R.color.orange);
        setContentView(R.layout.activity_about_us);
        initViews();
        addListener();
        initSlidingMenu();
    }

    private void initSlidingMenu() {
        DrawerView dv =new DrawerView(this);
        side_drawer = dv.initSlidingMenu();
    }

    private void addListener() {
        iv_about_us_drawer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (side_drawer.isMenuShowing()) {
                    side_drawer.showContent();
                } else {
                    side_drawer.showMenu();
                }
            }
        });
    }

    private void initViews() {
        iv_about_us_drawer=(ImageView)findViewById(R.id.title_bar_more);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if(side_drawer.isMenuShowing() ||side_drawer.isSecondaryMenuShowing()){
                side_drawer.showContent();
            }
        }
        return super.onKeyDown(keyCode, event);
    }

}
