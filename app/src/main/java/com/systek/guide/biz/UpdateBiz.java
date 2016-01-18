package com.systek.guide.biz;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.systek.guide.IConstants;
import com.systek.guide.entity.base.VersionBean;
import com.systek.guide.utils.MyHttpUtil;

/**
 * Created by Qiang on 2016/1/18.
 *
 *
 */
public class UpdateBiz implements IConstants {


    /**
     * 检查最新版本号
     * @return 版本号
     */
    public static VersionBean checkVersion(){
        String response= MyHttpUtil.sendGet(URL_CHECK_FOR_UPDATE);
        if(TextUtils.isEmpty(response)||response.equals("[]")){return null;}
        return JSON.parseObject(response, VersionBean.class);
    }

    /**
     2  * 获取版本号
     3  * @return 当前应用的版本号
     4  */
    public String getVersion(Context context) {
        try {
            PackageManager manager = context.getPackageManager();
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
            return info.versionName;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static void downloadNearestVersion(VersionBean version){
        MyHttpUtil.sendGet(BASE_URL+version.getUrl());
    }

}
