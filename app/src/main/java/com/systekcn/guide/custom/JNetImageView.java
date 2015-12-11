package com.systekcn.guide.custom;

import android.content.Context;
import android.util.AttributeSet;

import com.android.volley.JVolley;
import com.android.volley.toolbox.NetworkImageView;

/**
 * Created by Qiang on 2015/11/26.
 * 重写的网络图片加载控件
 */
public class JNetImageView extends NetworkImageView {

    public JNetImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public JNetImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public JNetImageView(Context context) {
        super(context);
    }

    /**
     * 显示网络图片
     *
     * @param imgUrl
     *            图片地址
     */
    public void displayImage(String imgUrl) {
        displayImage(imgUrl, 0, 0);
    }

    /**
     * 显示网络图片
     *
     * @param imgUrl
     *            图片地址
     * @param defaultImage
     *            默认图片
     * @param errorImage
     *            错误图片
     */
    public void displayImage(String imgUrl, int defaultImage, int errorImage) {
        // 设置错误图片
        setErrorImageResId(errorImage);
        // 设置默认图片
        setDefaultImageResId(defaultImage);
        // 加载图片
        setImageUrl(imgUrl, JVolley.getInstance(getContext()).getImageLoader());
    }
}
