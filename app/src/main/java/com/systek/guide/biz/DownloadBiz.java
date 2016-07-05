package com.systek.guide.biz;

import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.systek.guide.IConstants;
import com.systek.guide.utils.MyHttpUtil;

import java.util.List;

/**
 * Created by Qiang on 2015/11/17.
 *
 * 下载业务类
 */
public class DownloadBiz implements IConstants {


    /** 获得assets资源json */
    public String getAssetsJSON(String id) {
        String url=BASE_URL+URL_ALL_MUSEUM_ASSETS + id;
        return MyHttpUtil.sendGet(url);
    }

    /** 获得assets资源json */
    public String getAssetsJSON(String id,String baseUrl) {
        String url=baseUrl+URL_ALL_MUSEUM_ASSETS + id;
        return MyHttpUtil.sendGet(url);
    }


    /** 解析assets资源json */
    public List<String> parseAssetsJson(String assetsJson) {
        if(TextUtils.isEmpty(assetsJson)){return null;}
        JSONObject jsonObj = JSON.parseObject(assetsJson);
        String jsonString = jsonObj.getString("url");
        if(TextUtils.isEmpty(jsonString)){return null;}
        return JSON.parseArray(jsonString, String.class);
    }

    /** 解析assets资源json */
    public long parseAssetsSize(String assetsJson) {
        JSONObject jsonObj = JSON.parseObject(assetsJson);
        String size = jsonObj.getString("size");
        if(size==null){return 0;}
        return Long.valueOf(size);
    }

}
