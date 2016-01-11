package com.systekcn.guide.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.widget.Toast;

import com.systekcn.guide.IConstants;
import com.systekcn.guide.MyApplication;
import com.systekcn.guide.utils.ExceptionUtil;

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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            application=MyApplication.get();
            netState=MyApplication.currentNetworkType;
            //MyApplication.listActivity.add(this);
        } catch (Exception e) {
            ExceptionUtil.handleException(e);
        }
        initialize(savedInstanceState);
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
