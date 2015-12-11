package com.systekcn.guide.common;

import android.os.Environment;

/**
 * Created by Qiang on 2015/11/26.
 */
public interface IConstants {

    /*服务器上的域名加端口号*/
    String BASEURL="http://182.92.82.70";
    /**城市路径*/
    String URL_CITY_LIST="http://182.92.82.70/api/cityService/cityList";
    int URL_TYPE_GET_CITY=1;
    /**city下博物馆列表 TODO */
    String URL_MUSEUM_LIST="http://182.92.82.70/api/museumService/museumList";
    int URL_TYPE_GET_MUSEUM_LIST=2;
    /**博物馆下展品列表*/
    String URL_EXHIBIT_LIST="http://182.92.82.70/api/exhibitService/exhibitList?museumId=";
    int URL_TYPE_GET_EXHIBITS_BY_MUSEUM_ID =3;
    /**博物馆下某个展品*/// TODO: 2015/10/30
    int URL_TYPE_GET_EXHIBIT_BY_EXHIBIT_ID=4;

    String URL_GET_MUSEUM_BY_ID="http://182.92.82.70/api/museumService/museumList?museumId=";
    int URL_TYPE_GET_MUSEUM_BY_ID=5;

    /*资源路径*/
    String URL_ALL_MUSEUM_ASSETS ="http://182.92.82.70/api/assetsService/assetsList?museumId=";

    String URL_ALL_BEACON_LIST="http://182.92.82.70/api/beaconService/beaconList";


    String EXHIBIT_LIST_URL=BASEURL+ "/api/exhibitService/exhibitList?museumId=";
    String MUSEUM_MAP_URL=BASEURL+ "/api/museumMapService/museumMapList?museumId=";
    String BEACON_URL=BASEURL+ "/api/beaconService/beaconList?museumId=";
    String LABELS_URL=BASEURL+ "/api/labelsService/labelsList?museumId=";

    /*网络状态*/
    int INTERNET_TYPE_WIFI=1;
    int INTERNET_TYPE_MOBILE=2;
    int INTERNET_TYPE_NONE=3;
    /*存储至本地sdcard位置*/
    String SDCARD_ROOT= Environment.getExternalStorageDirectory().getAbsolutePath();
    /*sdcard存储图片的位置*/
    String LOCAL_ASSETS_PATH=SDCARD_ROOT+"/Guide/";
    String LOCAL_FILE_TYPE_IMAGE="image";
    String LOCAL_FILE_TYPE_AUDIO="audio";
    String LOCAL_FILE_TYPE_LYRIC="lyric";


    /*用于下载后传递数据*/
    String DOWNLOAD_ASSETS_KEY="download_assets_key";
    String DOWNLOAD_MUSEUMID_KEY="download_museumId_key";

    /*用于下载过程中传递消息的广播过滤信息*/
    String ACTION_DOWNLOAD="download";
    String ACTION_DOWNLOAD_PAUSE ="action_download_pause";
    String ACTION_DOWNLOAD_CONTINUE ="action_download_continue";
    String ACTION_PROGRESS="action_download_progress";

    /*用于标记Activity间跳转至主页所传数据*/
    String INTENT_MUSEUM_ID="intent_museum_id";

    /*用于APP配置信息存储*/
    String GUIDE_MODEL_KEY="guide_model_key";
    String GUIDE_MODEL_AUTO="guide_model_auto";
    String GUIDE_MODEL_HAND="guide_model_hand";

    /*用于存储配置*/
    String APP_SETTING ="setting";

    /*用于APP下载信息的存储*/
    String HAS_DOWNLOAD="has_download";

    /*用于广播传递更新展品集合*/
    String ACTION_NOTIFY_CURRENT_EXHIBIT_CHANGE="action_notify_current_exhibit_change";
    String ACTION_NOTIFY_NEARLY_EXHIBIT_LIST_CHANGE="action_notify_nearly_exhibit_list_change";

    /*用于导游模式的切换*/
    String ACTION_MODEL_CHANGED="action_model_changed";

    /**欢迎界面图片路径*/
    String WELCOME_IMAGES="welcome_images";

}
