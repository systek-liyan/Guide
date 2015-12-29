package com.systekcn.guide.biz;

import android.content.Context;

import com.alibaba.fastjson.JSON;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;
import com.systekcn.guide.common.utils.LogUtil;
import com.systekcn.guide.common.utils.MyHttpUtil;
import com.systekcn.guide.common.utils.Tools;

import java.util.List;

/**
 * Created by Qiang on 2015/10/22.
 */
public class GetBeansFromNet implements IGetBeanBiz {

    private List<?> list;
    private Object obj;

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
                LogUtil.i("ZHANG", "HTTP...onSuccess");
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
                LogUtil.i("ZHANG", error.toString());
            }
        });
        /**判断超时20秒*/
        while ( list == null) {
            if((System.currentTimeMillis() - startTime) > 20000){
                break;
            }
        }
        return (List<T>) list;
    }


    public List<?> getAllBeans(int type, String url, String id) {
        Class clazz = Tools.checkTypeForClass(type);
        String response=MyHttpUtil.get(url+id);
        list = JSON.parseArray(response, clazz);
        return list;
    }

    @Override
    public Object getBeanById(Context context, int type, String url, String Id) {
        long startTime=System.currentTimeMillis();
        final Class clazz = Tools.checkTypeForClass(type);
        HttpUtils http = new HttpUtils();
        http.send(HttpRequest.HttpMethod.GET, url, new RequestCallBack<String>() {

            @Override
            public void onLoading(long total, long current, boolean isUploading) {
            }

            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                list = JSON.parseArray(responseInfo.result, clazz);
            }

            @Override
            public void onStart() {
            }

            @Override
            public void onFailure(HttpException error, String msg) {
            }
        });
        while (list == null) {
            if((System.currentTimeMillis() - startTime) > 20000){
                break;
            }
        }
        return list.get(0);
    }
}
