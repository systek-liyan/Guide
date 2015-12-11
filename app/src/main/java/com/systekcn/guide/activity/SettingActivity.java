package com.systekcn.guide.activity;

import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.systekcn.guide.R;
import com.systekcn.guide.activity.base.BaseActivity;
import com.systekcn.guide.common.IConstants;
import com.systekcn.guide.common.utils.Tools;
import com.systekcn.guide.custom.DrawerView;
import com.systekcn.guide.custom.slidingmenu.SlidingMenu;

public class SettingActivity extends BaseActivity implements IConstants{

    private ImageView iv_setting_drawer;
    private SlidingMenu side_drawer;

    @Override
    public void initialize(){
        setContentView(R.layout.activity_setting);
        init();
    }

    private void init() {
        initViews();
        addListener();
        initSlidingMenu();
    }

    public void doClick(View view){
        if(view.getId()==R.id.button_test1){
            String test=String.valueOf(Tools.getValue(this, "test", "false"));
            Toast.makeText(this,test,Toast.LENGTH_SHORT).show();
        }else if(view.getId()==R.id.button_test2){
            Tools.saveValue(this, "test", "true");
            Toast.makeText(this,"数据已保存",Toast.LENGTH_SHORT).show();
        }
    }

    private void initSlidingMenu() {
        DrawerView dv =new DrawerView(this);
        side_drawer = dv.initSlidingMenu();
    }

    private void addListener() {
        iv_setting_drawer.setOnClickListener(new View.OnClickListener() {
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
        iv_setting_drawer=(ImageView)findViewById(R.id.iv_setting_drawer);
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
