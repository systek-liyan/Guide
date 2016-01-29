package com.systek.guide.manager;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.systek.guide.IConstants;
import com.systek.guide.MyApplication;
import com.systek.guide.entity.ExhibitBean;
import com.systek.guide.service.MediaPlayService;
import com.systek.guide.utils.ExceptionUtil;

import java.util.List;

/**
 * Created by Qiang on 2015/10/29.
 *
 * 播放器控制类，接收全局播放控制广播，控制播放状态等
 *
 */
public class MediaServiceManager implements IConstants {


    private Context mContext;
    private ServiceConnection mConn;
    public MediaPlayService.MediaServiceBinder mediaServiceBinder;
    private PlayCtrlReceiver playCtrlReceiver;
    private static MediaServiceManager instance;

    /**
     * 单例构造方法
     * @param context
     * @return
     */
    public static MediaServiceManager getInstance(Context context){
        if(instance==null){
            synchronized (MyApplication.class){
                if(instance==null){
                    instance=new MediaServiceManager(context);
                }
            }
        }
        return instance;
    }

    /**
     * 私有构造方法，连接并启动播放service
     * @param context
     */
    private MediaServiceManager(Context context) {
        this.mContext = context.getApplicationContext();
        init();
    }

    /**
     * 连接播放service，注册广播
     */
    public void init(){
        initConn();
        registerReceiver();
    }

    /**
     * 注册广播
     */
    private void registerReceiver(){
        playCtrlReceiver=new PlayCtrlReceiver();
        IntentFilter filter=new IntentFilter();
        filter.addAction(INTENT_EXHIBIT);
        filter.addAction(INTENT_CHANGE_PLAY_STATE);
        filter.addAction(INTENT_SEEK_BAR_CHANG);
        filter.addAction(INTENT_EXHIBIT_LIST);
        mContext.registerReceiver(playCtrlReceiver, filter);
    }

    private void initConn() {
        mConn = new ServiceConnection() {
            @Override
            public void onServiceDisconnected(ComponentName name) {
            }
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                mediaServiceBinder = (MediaPlayService.MediaServiceBinder) service;
            }
        };
    }

    /**
     * 连接播放service
     */
    public void connectService() {
        Intent intent = new Intent(mContext, MediaPlayService.class);
        mContext.bindService(intent, mConn, Context.BIND_AUTO_CREATE);
    }

    /**
     * 断开播放service连接并结束service
     */
    public void disConnectService() {
        if (mediaServiceBinder == null) {return;}
        mContext.unregisterReceiver(playCtrlReceiver);
        mContext.unbindService(mConn);
        mContext.stopService(new Intent(mContext, MediaPlayService.class));

    }

    /**
     * 获取当前播放进度
     * @return
     */
    public int getCurrentPosition(){
        if (mediaServiceBinder == null) {return 0;}
        return mediaServiceBinder.getCurrentPosition();
    }

    /**
     * 停止播放
     */
    public void stop() {
        if (mediaServiceBinder == null) {return;}
        mediaServiceBinder.stopPlay();
    }

    /**
     * 判断是否正在播放
     * @return
     */
    public boolean isPlaying() {
        return mediaServiceBinder != null && mediaServiceBinder.isPlaying();
    }

    /**
     * 判断是否为暂停状态
     * @return
     */
    public boolean isPause() {
        return mediaServiceBinder != null && mediaServiceBinder.isPause();
    }

    /**
     * 暂停播放
     * @return
     */
    public boolean pause() {
        if (mediaServiceBinder == null) {return false;}
        return mediaServiceBinder.pause();
    }

    /**
     * 播放下一个
     * @return
     */
    public boolean next() {
        if (mediaServiceBinder == null) {return false;}
        return mediaServiceBinder.next();
    }

    /**
     * 播放进度跳转至progre
     * @param progress 进度
     */
    public void seekTo(int progress) {
        if (mediaServiceBinder == null) {return;}
        mediaServiceBinder.seekTo(progress);
    }

    /**
     * 继续播放
     */
    public void toContinue(){
        if (mediaServiceBinder == null) {return;}
        mediaServiceBinder.continuePlay();
    }

    /**
     * 通知展品切换
     * @param exhibitBean
     */
    public void notifyExhibitChange(ExhibitBean exhibitBean){
        if (mediaServiceBinder == null) {return;}
        mediaServiceBinder.notifyExhibitChange(exhibitBean);
    }

    /**
     * 获取当前播放展品集合
     * @return
     */
    public List<ExhibitBean> getExhibitList() {
        if (mediaServiceBinder == null) {return null;}
        return mediaServiceBinder.getPlayList();
    }

    /**
     * 刷新播放列表
     * @param exhibitList
     */
    public void refreshExhibitBeanList(List<ExhibitBean> exhibitList) {
        if (exhibitList == null || mediaServiceBinder == null) {return;}
        try {
            mediaServiceBinder.setPlayList(exhibitList);
        } catch (Exception e) {
            ExceptionUtil.handleException(e);
        }
    }

    /**
     * 播放
     * @return
     */
    public boolean play() {
        if (mediaServiceBinder == null) {return false;}
        return mediaServiceBinder.startPlay();
    }

    /**
     * 获取当前播放展品的总长度
     * @return
     */
    public int duration() {
        if (mediaServiceBinder == null) {return 0;}
        return mediaServiceBinder.getDuration();
    }

    /**
     * 设置播放模式
     * @param mode
     */
    public void setPlayMode(int mode) {
        if (mediaServiceBinder == null) {return;}
        mediaServiceBinder.setPlayMode(mode);
    }

    /**
     * 获取当前播放模式
     * @return
     */
    public int getPlayMode() {
        if (mediaServiceBinder == null) {return 0;}
        return mediaServiceBinder.getPlayMode();
    }

    /**
     * 获取当前正在播放的展品
     * @return
     */
    public ExhibitBean getCurrentExhibit() {
        if (mediaServiceBinder == null) {return null;}
        return mediaServiceBinder.getCurrentExhibit();
    }

    /**
     * 退出播放服务
     */
    public void exit() {
        if(mConn==null||mContext==null){return;}
        mContext.unbindService(mConn);
        mContext.stopService(new Intent(mContext, MediaPlayService.class));
    }

    /**
     * 播放控制广播接受器
     */
    private class PlayCtrlReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            String action=intent.getAction();
            Intent intent1=null;
            switch (action) {

                /*广播为展品时，通知切换展品*/
                case INTENT_EXHIBIT:
                    String exhibitStr = intent.getStringExtra(INTENT_EXHIBIT);
                    if (TextUtils.isEmpty(exhibitStr)) {return;}
                    ExhibitBean exhibitBean = JSON.parseObject(exhibitStr, ExhibitBean.class);
                    if (exhibitBean == null || mediaServiceBinder == null) {return;}
                    mediaServiceBinder.notifyExhibitChange(exhibitBean);
                    break;
                /*广播为切换播放状态，判断当前状态，切换暂停或播放*/
                case INTENT_CHANGE_PLAY_STATE:
                    intent1 = new Intent();
                    if (mediaServiceBinder.isPlaying()) {
                        mediaServiceBinder.pause();
                        intent1.setAction(INTENT_CHANGE_PLAY_STOP);
                    } else {
                        mediaServiceBinder.continuePlay();
                        intent1.setAction(INTENT_CHANGE_PLAY_PLAY);
                    }
                    context.sendBroadcast(intent1);
                    break;
                /*广播为跳转播放进度*/
                case INTENT_SEEK_BAR_CHANG:
                    int progress = intent.getIntExtra(INTENT_SEEK_BAR_CHANG, 0);
                    mediaServiceBinder.seekTo(progress);
                    break;
                /*广播为展品集合*/
                case INTENT_EXHIBIT_LIST:
                    /*如果不是自动模式，不做处理*/
                    if(mediaServiceBinder.getPlayMode()!=PLAY_MODE_AUTO){break;}
                    /*获取并解析展品集合*/
                    String exhibitJson=intent.getStringExtra(INTENT_EXHIBIT_LIST);
                    List<ExhibitBean> currentExhibitList= JSON.parseArray(exhibitJson,ExhibitBean.class);
                    /*当展品集合不为空并且为自动播放模式，没有暂停的情况下自动播放*/
                    if(currentExhibitList==null ||currentExhibitList.size()==0
                            ||mediaServiceBinder.getPlayMode()!=PLAY_MODE_AUTO){
                        break;
                    }
                    if(!mediaServiceBinder.isPlaying()&&mediaServiceBinder.getCurrentExhibit()!=null){
                        break;
                    }
                        /*获取展品集合第一个，发送广播，通知播放*/
                    ExhibitBean exhibit=currentExhibitList.get(0);
                    ExhibitBean currentExhibit=getCurrentExhibit();
                    if(currentExhibit==null||!currentExhibit.equals(exhibit)){
                        String str= JSON.toJSONString(exhibit);
                        Intent intent2 =new Intent();
                        intent2.setAction(INTENT_EXHIBIT);
                        intent2.putExtra(INTENT_EXHIBIT, str);
                        context.sendBroadcast(intent2);
                    }
                    break;
            }
        }
    }

//暂无以下方法
    /*public boolean rePlay() {
        if (mediaServiceBinder != null) {
            mediaServiceBinder.rePlay();
            return true;
        }
        return false;
    }
    public boolean prev() {
        if (mediaServiceBinder != null) {
            return mediaPlayService.prev();
        }
        return false;
    }
    public void reset(){
        mediaServiceBinder.reset();
    }
    public String getCurMusicId() {
        if (mediaServiceBinder != null) {
            return mediaServiceBinder.getCurMusicId();
        }
        return -1;
    }
 public void sendBroadcast() {
        if (mediaServiceBinder != null) {
            mediaServiceBinder.sendPlayStateBrocast();
        }
    }

    */
}
