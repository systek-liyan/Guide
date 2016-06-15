package com.systek.guide.activity;

import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.systek.guide.IConstants;
import com.systek.guide.MyApplication;
import com.systek.guide.R;
import com.systek.guide.callback.PlayChangeCallback;
import com.systek.guide.custom.LoadingDialog;
import com.systek.guide.entity.ExhibitBean;

/**
 * Created by Qiang on 2015/12/30.
 *
 * activity基类
 */
public abstract class BaseActivity extends AppCompatActivity implements IConstants,PlayChangeCallback {

    public String TAG = getClass().getSimpleName();//类的唯一标记
    protected Drawer drawer;//抽屉
    protected Toolbar toolbar;
    protected TextView toolbarTitle;
    protected Dialog dialog;
    protected View mErrorView;
    protected Button refreshBtn;
    public  int state= PlayChangeCallback.STATE_INVALID;

    protected Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setIntent(getIntent());
    }

    @Override
    public void onStateChanged(int state) {

    }

    @Override
    public void onExhibitChanged(ExhibitBean exhibit) {

    }

    @Override
    public void onPositionChanged(int duration, int position) {

    }

    public void registerBluetoothReceiver() {
        registerReceiver(bluetoothState,new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
    }
    public void unRegisterBluetoothReceiver() {
        unregisterReceiver(bluetoothState);
    }


    @Override
    protected void onDestroy() {
        if(handler!=null){
            handler.removeCallbacksAndMessages(null);
        }
        super.onDestroy();
        //beaconManager.unbind(this);
    }

    public void showDialog(String msg){
        if(dialog==null){
            dialog= LoadingDialog.createLoadingDialog(BaseActivity.this,msg);
        }
        dialog.show();
    }

    public void closeDialog(){
        if(dialog!=null&&dialog.isShowing()){
            dialog.dismiss();
        }
    }
    public void showErrorView(){
        showErrors(true);
    }


    public void showErrors(final boolean forceError) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mErrorView = findViewById(R.id.mErrorView);
                if (mErrorView != null) {
                    mErrorView.setVisibility(forceError ? View.VISIBLE : View.GONE);
                }
            }
        });
    }


    protected void setHomeIcon(){
        ActionBar actionBar=getSupportActionBar();
        if(actionBar!=null){ actionBar.setDisplayHomeAsUpEnabled(true); }
    }

    protected void setHomeIcon(@DrawableRes int resource){

        if(toolbar!=null){
            toolbar.setNavigationIcon(resource);
        }
        ActionBar actionBar=getSupportActionBar();
        if(actionBar!=null){ actionBar.setHomeButtonEnabled(true); }
    }

    protected void setHomeClickListener(View.OnClickListener listener){
        if(toolbar!=null){
            toolbar.setNavigationOnClickListener(listener);
        }
    }

    protected void setTitleBar() {
        View v = findViewById(R.id.toolbar);
        if (v != null) {
            toolbar = (Toolbar) v;
            setSupportActionBar(toolbar);
            toolbarTitle = (TextView) v.findViewById(R.id.toolbar_title);
            if (toolbarTitle == null) {return;}
            ActionBar actionBar= getSupportActionBar();
            if(actionBar==null){ return;}
            actionBar.setDisplayShowTitleEnabled(false);
        }
    }

    protected void setTitleBarTitle(String text){
        if(toolbarTitle==null|| TextUtils.isEmpty(text)){return;}
        toolbarTitle.setText(text);
    }

    protected void setTitleBarTitle(@StringRes int  text){
        if(toolbarTitle==null){return;}
        toolbarTitle.setText(getResources().getString(text));
    }


    protected <T extends View> T $(int id) {
        return (T) findViewById(id);
    }

    protected View.OnClickListener backOnClickListener=new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            finish();
        }
    };

    /**
     * 加载抽屉
     */
    protected void initDrawer() {
        drawer = new DrawerBuilder()
                .withActivity(this)
                        //.withFullscreen(true)
                .withHeader(R.layout.header)
                .inflateMenu(R.menu.drawer_menu)
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        Class<?>  targetClass=null;
                        switch (position){
                            case 1:
                                targetClass=DownloadManagerActivity.class;
                                break;
                            case 2:
                                targetClass=CollectionActivity.class;
                                break;
                            case 3:
                                targetClass=CityChooseActivity.class;
                                break;
                            case 4:
                                targetClass=MuseumListActivity.class;
                                break;
                            case 5:
                                targetClass=SettingActivity.class;
                                break;
                        }
                        Intent intent=new Intent(BaseActivity.this,targetClass);
                        startActivity(intent);
                        return false;
                    }
                }).build();
    }

    /**
     * 获得当前activity的tag
     *
     * @return activity的tag
     */
    public String getTag() {
        return TAG;
    }

    /**
     * 得到当前activity对象
     *
     * @return activity对象
     */
    protected BaseActivity getActivity() {
        return this;
    }

    /**
     * 显示一个toast
     *
     * @param msg
     *            toast内容
     */
    public void showToast(final String msg) {
        Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
    }

    /*
    * 响应后退按键
    */
    public void keyBack() {
        //如果未关闭抽屉，先关闭抽屉，再销毁activity
        if(drawer!=null&&drawer.isDrawerOpen()){
            drawer.closeDrawer();
        }
        finish();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        boolean onKey = true;
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                keyBack();
                break;
            default:
                onKey = super.onKeyDown(keyCode, event);
                break;
        }
        return onKey;
    }


    public void onDataError(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showToast("数据获取失败，请检查网络状态...");
            }
        });
    }
    public void onNoData(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showToast("暂无相关数据...");
            }
        });
    }

    BroadcastReceiver bluetoothState = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {

            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if(bluetoothAdapter == null) {
                //the device doesn't support bluetooth
            } else {
                //the device support bluetooth
                if(!bluetoothAdapter.isEnabled()) {
                bluetoothAdapter.enable();
                    MyApplication a = (MyApplication) getApplication();
                    //a.initBlueTooth();
            }
        }
            /*String stateExtra = BluetoothAdapter.EXTRA_STATE;
            int state = intent.getIntExtra(stateExtra, -1);
            switch(state) {
                case BluetoothAdapter.STATE_TURNING_ON:
                    break;
                case BluetoothAdapter.STATE_ON:
                    break;
                case BluetoothAdapter.STATE_TURNING_OFF:
                    break;
                case BluetoothAdapter.STATE_OFF:
                    break;
            }*/
        }
    };


}
