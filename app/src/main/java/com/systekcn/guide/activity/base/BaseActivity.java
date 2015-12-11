package com.systekcn.guide.activity.base;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Toast;

import com.systekcn.guide.MyApplication;
import com.systekcn.guide.common.utils.ExceptionUtil;

/**
 * Created by Qiang on 2015/11/26.
 *
 */
public class BaseActivity extends FragmentActivity implements View.OnClickListener {
    /**
     * 类唯一标记
     */
    private String TAG = getClass().getSimpleName();

    protected MyApplication application;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        application=(MyApplication)getApplication();
        try {
            MyApplication.listActivity.add(this);
        } catch (Exception e) {
            ExceptionUtil.handleException(e);
        }
        initialize();
    }

    /**
     * 初始化控件
     */
    protected void initialize() {
    }

    @Override
    public void onClick(View v) {

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
    public void showToast(String msg) {
        Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

   /* *//**
     * 响应后退按键
     *//*
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

    *//**
     * 控制当前activity中的网络请求的生周期
     *//*
    @Override
    protected void onDestroy() {
        super.onDestroy();
        JVolley.getInstance(this).cancelFromRequestQueue(getTag());
    }

    *//**
     * 控制当前activity中的网络请求的生命周期
     *//*
    @Override
    protected void onStop() {
        super.onStop();
        JVolley.getInstance(this).cancelFromRequestQueue(getTag());
    }*/
}
