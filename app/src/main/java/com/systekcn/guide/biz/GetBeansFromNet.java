package com.systekcn.guide.biz;

import android.content.Context;

import com.alibaba.fastjson.JSON;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;
import com.systekcn.guide.common.utils.LogUtil;
import com.systekcn.guide.common.utils.Tools;

import java.util.List;

/**
 * Created by Qiang on 2015/10/22.
 */
public class GetBeansFromNet implements IGetBeanBiz {

    private List<?> list;
    private Object obj;

    @SuppressWarnings("unchecked")
    @Override
    public <T> List<T> getAllBeans(Context context, int type, String url, String id) {
        long startTime = System.currentTimeMillis();
        final Class clazz = Tools.checkTypeForClass(type);
        HttpUtils http = new HttpUtils();
        url=url+id;
        http.send(HttpRequest.HttpMethod.GET, url, new RequestCallBack<String>() {

            @Override
            public void onLoading(long total, long current, boolean isUploading) {
            }

            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                String str=responseInfo.result;
                try{
                    list = JSON.parseArray(str, clazz);
                }catch (Exception e){
                    LogUtil.i("tag",e.toString());
                }
            }

            @Override
            public void onStart() {
            }

            @Override
            public void onFailure(HttpException error, String msg) {
                LogUtil.i("测试信息", error.toString());
            }
        });
        /**判断超时10秒*/
        while ((System.currentTimeMillis() - startTime) < 10000 || list == null) {
        }
        return (List<T>) list;
    }

    @Override
    public <T> T getBeanById(Context context, int type, String url, String Id) {
        final Class clazz = Tools.checkTypeForClass(type);
        HttpUtils http = new HttpUtils();
        http.send(HttpRequest.HttpMethod.GET, url, new RequestCallBack<String>() {

            @Override
            public void onLoading(long total, long current, boolean isUploading) {
            }

            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                obj = JSON.parseObject(responseInfo.result, clazz);
            }

            @Override
            public void onStart() {
            }

            @Override
            public void onFailure(HttpException error, String msg) {
            }
        });
        while (obj == null) {
        }
        return (T) obj;
    }
}
