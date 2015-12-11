package com.systekcn.guide.fragment.base;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.volley.JVolley;

/**
 * Created by Qiang on 2015/11/26.
 */
public class BaseFragment extends Fragment {
    /**
     * 主视图
     */
    View contentView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return contentView;
    }

    protected void setContentView(int res) {
        if (contentView == null) {
            contentView = LinearLayout.inflate(getActivity(), res, null);
        }
    }

    /**
     * 初始化控件
     */
    protected void initView() {

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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        JVolley.getInstance(getActivity()).cancelFromRequestQueue(getTag());
    }

    @Override
    public void onStop() {
        super.onStop();
        JVolley.getInstance(getActivity()).cancelFromRequestQueue(getTag());
    }
}
