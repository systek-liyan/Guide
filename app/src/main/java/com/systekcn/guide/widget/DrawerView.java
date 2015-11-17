package com.systekcn.guide.widget;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.magic.mapdemo.R;
import com.systekcn.guide.MyApplication;
import com.systekcn.guide.activity.AboutUsActivity;
import com.systekcn.guide.activity.CityActivity;
import com.systekcn.guide.activity.CollectionActivity;
import com.systekcn.guide.activity.DownloadActivity;
import com.systekcn.guide.activity.SettingActivity;
import com.systekcn.guide.common.IConstants;
import com.systekcn.guide.common.utils.LogUtil;
import com.systekcn.guide.widget.slidingmenu.SlidingMenu;

import java.util.ArrayList;

public class DrawerView implements View.OnClickListener,IConstants{

    private final Activity activity;
    private SlidingMenu localSlidingMenu;
    private SwitchButton switch_mode_btn;
    private TextView auto_mode_text;
    private LinearLayout drawer_city_choose;
    private LinearLayout drawer_download_center;
    private LinearLayout drawer_collect;
    private LinearLayout drawer_about_us;
    private LinearLayout drawer_setting;


    public DrawerView(Activity activity) {
        this.activity = activity;
    }
    public SlidingMenu initSlidingMenu() {
        localSlidingMenu = new SlidingMenu(activity);
        localSlidingMenu.setMode(SlidingMenu.LEFT);//设置左右滑菜单
        localSlidingMenu.setTouchModeAbove(SlidingMenu.SLIDING_WINDOW);//设置要使菜单滑动，触碰屏幕的范围
        //localSlidingMenu.setTouchModeBehind(SlidingMenu.SLIDING_CONTENT);//设置了这个会获取不到菜单里面的焦点，所以先注释掉
        //localSlidingMenu.setShadowWidthRes(R.dimen.shadow_width);//设置阴影图片的宽度
        //localSlidingMenu.setShadowDrawable(R.drawable.shadow);//设置阴影图片
        localSlidingMenu.setBehindOffsetRes(R.dimen.slidingmenu_offset);//SlidingMenu划出时主页面显示的剩余宽度
        localSlidingMenu.setFadeDegree(0.35F);//SlidingMenu滑动时的渐变程度
        localSlidingMenu.attachToActivity(activity, SlidingMenu.RIGHT);//使SlidingMenu附加在Activity右边
        //localSlidingMenu.setBehindWidthRes(R.dimen.left_drawer_avatar_size);//设置SlidingMenu菜单的宽度
        localSlidingMenu.setMenu(R.layout.left_drawer_fragment);//设置menu的布局文件
        //localSlidingMenu.toggle();//动态判断自动关闭或开启SlidingMenu
        localSlidingMenu.setOnOpenedListener(new SlidingMenu.OnOpenedListener() {
            public void onOpened() {
            }
        });
        localSlidingMenu.setOnClosedListener(new SlidingMenu.OnClosedListener() {

            @Override
            public void onClosed() {

            }
        });
        initView();
        return localSlidingMenu;
    }

    private void initView() {
        switch_mode_btn = (SwitchButton) localSlidingMenu.findViewById(R.id.switch_mode_btn);
        auto_mode_text = (TextView) localSlidingMenu.findViewById(R.id.switch_mode_text);
        if (MyApplication.guideModel == MyApplication.GUIDE_MODEL_AUTO) {
            switch_mode_btn.setChecked(true);
            auto_mode_text.setText(activity.getResources().getString(R.string.drawer_guide_mode_auto_text));
        } else {
            switch_mode_btn.setChecked(false);
            auto_mode_text.setText(activity.getResources().getString(R.string.drawer_guide_mode_hand_text));
        }
        switch_mode_btn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences settings = activity.getSharedPreferences(APP_SETTING, 0);
                SharedPreferences.Editor editor = settings.edit();
                if (isChecked) {
                    editor.putString(GUIDE_MODEL_KEY, GUIDE_MODEL_AUTO);
                    editor.commit();
                    MyApplication.guideModel = MyApplication.GUIDE_MODEL_AUTO;
                    auto_mode_text.setText(activity.getResources().getString(R.string.drawer_guide_mode_auto_text));
                    LogUtil.i("ZHANG", "当前模式为GUIDE_MODEL_AUTO已保存");
                    Intent intent = new Intent();
                    intent.setAction(ACTION_MODEL_CHANGED);
                    activity.sendBroadcast(intent);
                } else {
                    editor.putString(GUIDE_MODEL_KEY, GUIDE_MODEL_HAND);
                    editor.commit();
                    MyApplication.guideModel = MyApplication.GUIDE_MODEL_HAND;
                    auto_mode_text.setText(activity.getResources().getString(R.string.drawer_guide_mode_hand_text));
                    LogUtil.i("ZHANG", "当前模式为GUIDE_MODEL_HAND已保存");
                    Intent intent = new Intent();
                    intent.setAction(ACTION_MODEL_CHANGED);
                    activity.sendBroadcast(intent);
                }
            }
        });

        ArrayList<LinearLayout> list = new ArrayList<LinearLayout>();

        drawer_download_center = (LinearLayout) activity.findViewById(R.id.drawer_download_center);
        drawer_collect = (LinearLayout) activity.findViewById(R.id.drawer_collect);
        drawer_city_choose = (LinearLayout) activity.findViewById(R.id.drawer_city_choose);
        drawer_about_us = (LinearLayout) activity.findViewById(R.id.drawer_about_us);
        drawer_setting = (LinearLayout) localSlidingMenu.findViewById(R.id.drawer_setting);

        list.add(drawer_download_center);
        list.add(drawer_collect);
        list.add(drawer_city_choose);
        list.add(drawer_about_us);
        list.add(drawer_setting);

        for (LinearLayout rl : list) {
            rl.setOnClickListener(this);
        }

    }

    Class<?> targetClass = null;

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.drawer_download_center:
                targetClass= DownloadActivity.class;
                break;
            case R.id.drawer_collect:
                targetClass=CollectionActivity.class;
                break;
            case R.id.drawer_city_choose:
                targetClass = CityActivity.class;
                break;
            case R.id.drawer_about_us:
                targetClass= AboutUsActivity.class;
                break;
            case R.id.drawer_setting:
                targetClass= SettingActivity.class;
        }
        if (targetClass != null) {
            Intent intent = new Intent(activity, targetClass);
            activity.startActivity(intent);
        }
    }

}
