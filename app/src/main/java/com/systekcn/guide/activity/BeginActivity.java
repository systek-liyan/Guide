package com.systekcn.guide.activity;

import android.content.Intent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;

import com.systekcn.guide.R;
import com.systekcn.guide.activity.base.BaseActivity;
import com.systekcn.guide.biz.BeansManageBiz;
import com.systekcn.guide.biz.BizFactory;
import com.systekcn.guide.common.IConstants;
import com.systekcn.guide.common.utils.ViewUtils;
import com.systekcn.guide.entity.BeaconBean;
import com.systekcn.guide.entity.MuseumBean;
import com.systekcn.guide.manager.BluetoothManager;

public class BeginActivity extends BaseActivity implements IConstants {


    private View view;

    private Class<?> targetClass;

    @Override
    protected void initialize() {
        ViewUtils.setStateBarToAlpha(this);
        BluetoothManager bluetoothManager=BluetoothManager.newInstance(this);
        bluetoothManager.setGetBeaconCallBack(getBeaconCallBack);
        view = View.inflate(this, R.layout.activity_begin, null);
        setContentView(view);
        initData();
    }


    private void initData() {
        /*默认跳转界面为城市选择*/
        targetClass=CityActivity.class;
        AlphaAnimation startAnimation = new AlphaAnimation(1.0f, 1.0f);
        startAnimation.setDuration(3000);
        view.startAnimation(startAnimation);
        startAnimation.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                redirectTo();
            }
        });
    }
    protected void redirectTo() {
        startActivity(new Intent(getApplicationContext(),targetClass));
        finish();
    }


    private String museumId;
    private BluetoothManager.GetBeaconCallBack getBeaconCallBack=new BluetoothManager.GetBeaconCallBack() {

        int count ;

        @Override
        public String getMuseumByBeaconCallBack(BeaconBean beaconBean) {
            if(beaconBean==null){return null; }
            count++;
            if(count==1){
                museumId=beaconBean.getMuseumId();
                BeansManageBiz biz= (BeansManageBiz) BizFactory.getBeansManageBiz(BeginActivity.this);
                application.currentMuseum= (MuseumBean) biz.getBeanById(IConstants.URL_TYPE_GET_MUSEUM_BY_ID,museumId);
                targetClass=CityChooseActivity.class;
            }
            return museumId;
        }
    };

}
