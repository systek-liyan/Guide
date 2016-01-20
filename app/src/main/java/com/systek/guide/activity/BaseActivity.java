package com.systek.guide.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Toast;

import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.systek.guide.IConstants;
import com.systek.guide.MyApplication;
import com.systek.guide.R;
import com.systek.guide.utils.ExceptionUtil;

/**
 * Created by Qiang on 2015/12/30.
 */
public abstract class BaseActivity extends AppCompatActivity implements IConstants{
    /**
     * 类唯一标记
     */
    private String TAG = getClass().getSimpleName();
    public int netState;
    MyApplication application;
    protected Drawer drawer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            application=MyApplication.get();
            netState=MyApplication.currentNetworkType;
        } catch (Exception e) {
            ExceptionUtil.handleException(e);
        }
        initialize(savedInstanceState);
    }

    protected void initDrawer() {
        drawer = new DrawerBuilder()
                .withActivity(this)
                .withFullscreen(true)
                .withHeader(R.layout.header)
                .inflateMenu(R.menu.drawer_menu)
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        Class<?>  targetClass=null;
                        switch (position){
                            case 1:
                                targetClass=DownloadActivity.class;
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



    public int getNetState(){
        return MyApplication.currentNetworkType;
    }

    /**
     * 初始化控件
     */
    protected  void initialize(Bundle savedInstanceState){}

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
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    /*
    * 响应后退按键
    */
    public void keyBack() {
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
        showToast("数据获取失败，请检查网络状态...");
    }

}
