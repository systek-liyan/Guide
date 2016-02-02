package com.systek.guide;

import android.os.Environment;

/**
 * Created by Qiang on 2015/11/26.
 *
 * 公共接口类，用于存放所有常量
 */
public interface IConstants {

    String SP_NOT_FIRST_LOGIN="sp_not_first_login";//是否首次登陆
    String SP_MUSEUM_ID="sp_museum_id";//博物馆id
    String SP_DOWNLOAD_MUSEUM_COUNT="sp_download_museum_count";//下载博物馆数据个数

    /**消息类型*/

    int MSG_WHAT_UPDATE_DATA_SUCCESS = 1;//数据获取成功
    int MSG_WHAT_UPDATE_NO_DATA = 2;//数据获取成功

    int MSG_WHAT_UPDATE_DATA_FAIL = 3;//数据获取失败
    int MSG_WHAT_REFRESH_DATA = 4;//刷新数据
    int MSG_WHAT_UPDATE_PROGRESS = 5;//message类型之更新进度
    int MSG_WHAT_UPDATE_CURRENT_MUSEUM = 6;//message类型之更新展品
    int MSG_WHAT_UPDATE_DURATION = 7;//message类型之更新播放长度
    int MSG_WHAT_CHANGE_ICON=8;
    int MSG_WHAT_CHANGE_EXHIBIT=9;
    int MSG_WHAT_PAUSE_MUSIC=10;
    int MSG_WHAT_CONTINUE_MUSIC=11;
    int MSG_WHAT_CHANGE_PLAY_STOP=12;
    int MSG_WHAT_CHANGE_PLAY_START=13;
    int MSG_WHAT_REFRESH_VIEW=14;
    /**关于URL*/

    String BASE_URL ="http://182.92.82.70";
    //String BASE_URL ="http://192.168.1.108:8080";
    String URL_CITY_LIST="http://182.92.82.70/api/cityService/cityList"; //城市路径
    String URL_MUSEUM_LIST="http://182.92.82.70/api/museumService/museumList";//city下博物馆列表
    String URL_GET_MUSEUM_BY_ID="http://182.92.82.70/api/museumService/museumList?museumId="; //博物馆列表下博物馆*/
    String URL_ALL_MUSEUM_ASSETS ="http://182.92.82.70/api/assetsService/assetsList?museumId=";//资源路径*/
    String URL_ALL_BEACON_LIST="http://182.92.82.70/api/beaconService/beaconList";//beacon列表
    String URL_EXHIBIT_LIST="http://182.92.82.70/api/exhibitService/exhibitList?museumId="; //博物馆下展品列表*/
    String URL_MUSEUM_MAP_LIST ="http://182.92.82.70/api/museumMapService/museumMapList?museumId=";//地图列表
    String URL_BEACON_LIST ="http://182.92.82.70/api/beaconService/beaconList?museumId=";//beacon列表
    String URL_LABELS_LIST ="http://182.92.82.70/api/labelsService/labelsList?museumId=";//标签列表
    String URL_CHECK_FOR_UPDATE ="http://182.92.82.70/api/version/latest";//检查更新


    /**网络相关*/

    String ACTION_NET_IS_COMMING ="net_is_comming";//断网后又连接到网络
    String ACTION_NET_IS_OUT ="net_is_out";//断网
    int INTERNET_TYPE_WIFI=1;//网络状态--WIFI
    int INTERNET_TYPE_MOBILE=2;//网络状态--数据网络
    int INTERNET_TYPE_NONE=3;//网络状态--无网络

    /**播放模式*/
    int PLAY_MODE_HAND=1;
    int PLAY_MODE_AUTO=2;
    int PLAY_MODE_AUTO_PAUSE=3;

    /**intent 传递数据*/
    String INTENT_CITY="intent_city";//城市对象
    String INTENT_MUSEUM="intent_museum";//博物馆对象
    String INTENT_EXHIBIT="intent_exhibit";//展品对象
    String INTENT_MUSEUM_ID="intent_museum_id";//博物馆ID
    String INTENT_FLAG_GUIDE_MAP="intent_flag_guide_map";//数据标记，进入附近列表或者地图
    String INTENT_FLAG_MAP="intent_flag_map";//数据标记--进入地图
    String INTENT_FLAG_GUIDE ="intent_flag_guide";//数据标记--进入附近列表
    String INTENT_EXHIBIT_LIST ="intent_exhibit_list";//展品列表
    String INTENT_EXHIBIT_LIST_STR ="intent_exhibit_list_str";//展品列表json


    String INTENT_START_PLAY_SERVICE ="intent_start_play_service";//启动播放服务
    String INTENT_EXHIBIT_PROGRESS ="intent_exhibit_progress";//播放进度
    String INTENT_EXHIBIT_DURATION ="intent_exhibit_duration";//播放总时长
    String INTENT_EXHIBIT_CHANG ="intent_exhibit_chang";//切换展品
    String INTENT_SEEK_BAR_CHANG ="intent_seek_bar_chang";//进度条拖动

    String INTENT_CHANGE_PLAY_STATE="intent_change_play_state";
    String INTENT_CHANGE_PLAY_STOP="intent_change_play_stop";
    String INTENT_CHANGE_PLAY_PLAY="intent_change_play_play";
    String INTENT_BEACON="intent_beacon";



    /**欢迎界面图片路径*/
    String WELCOME_IMAGES="welcome_images";


    /**本地文件位置*/

    String APP_ROOT= MyApplication.get().getFilesDir().getAbsolutePath();//存储至本地sdcard位置
    String SDCARD_ROOT= Environment.getExternalStorageDirectory().getAbsolutePath();//存储至本地sdcard位置
    //String LOCAL_ASSETS_PATH=SDCARD_ROOT+"/Guide/";//sdcard存储图片的位置*/
    String LOCAL_ASSETS_PATH=APP_ROOT+"/";
    String APP_ASSETS_PATH=APP_ROOT+"/";//sdcard存储图片的位置*/
    String LOCAL_FILE_TYPE_IMAGE="image";
    String LOCAL_FILE_TYPE_AUDIO="audio";
    String LOCAL_FILE_TYPE_LYRIC="lyric";


    /**数据库字段*/

    String MUSEUM_ID="museumId";
    String EXHIBIT_ID="exhibitId";
    String CITY ="city";
    String LABELS="labels";
    String SAVE_FOR_PERSON ="saveForPerson";
    String NAME="name";
    String ID="id";
    String LIKE="like";
    String BEACON_ID="beaconId";

    /**WIFI SSID PASSWORD*/

    String WIFI_SSID="systek";
    String WIFI_PASSWORD="systek2015";


    /**以下准备废弃*/
    int URL_TYPE_GET_CITY=1;
    int URL_TYPE_GET_MUSEUM_LIST=2;
    int URL_TYPE_GET_EXHIBITS_BY_MUSEUM_ID =3;
    int URL_TYPE_GET_EXHIBIT_BY_EXHIBIT_ID=4; /**博物馆下某个展品*/
    int URL_TYPE_GET_MUSEUM_BY_ID=5;
    /*用于下载后传递数据*/
    String DOWNLOAD_ASSETS_KEY="download_assets_key";
    String DOWNLOAD_MUSEUMID_KEY="download_museumId_key";
    /*用于下载过程中传递消息的广播过滤信息*/
    String ACTION_DOWNLOAD="download";
    String ACTION_DOWNLOAD_PAUSE ="action_download_pause";
    String ACTION_DOWNLOAD_CONTINUE ="action_download_continue";
    String ACTION_PROGRESS="action_download_progress";
    /*用于广播传递更新展品集合*/
    String ACTION_NOTIFY_CURRENT_EXHIBIT_CHANGE="action_notify_current_exhibit_change";
    String ACTION_NOTIFY_NEARLY_EXHIBIT_LIST_CHANGE="action_notify_nearly_exhibit_list_change";
    /*用于导游模式的切换*/
    String ACTION_MODEL_CHANGED="action_model_changed";
    /*用于APP配置信息存储*/
    String GUIDE_MODEL_KEY="guide_model_key";
    String GUIDE_MODEL_AUTO="guide_model_auto";
    String GUIDE_MODEL_HAND="guide_model_hand";
    /*用于存储配置*/
    String APP_SETTING ="setting";
    /*用于APP下载信息的存储*/
    String HAS_DOWNLOAD="has_download";

}
