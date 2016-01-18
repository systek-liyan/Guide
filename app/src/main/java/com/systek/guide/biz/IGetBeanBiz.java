package com.systek.guide.biz;

import android.content.Context;

import java.util.List;

/**
 * Created by Qiang on 2015/10/22.
 */
public interface IGetBeanBiz {
    <T>List<T> getAllBeans(Context context, int type, String url, String Id);
    <T>List<T> getAllBeans(int type, String url, String Id);
    <T> T  getBeanById(Context context, int type, String url, String Id);
}
