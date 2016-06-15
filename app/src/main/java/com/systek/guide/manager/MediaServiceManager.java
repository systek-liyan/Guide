package com.systek.guide.manager;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.systek.guide.IConstants;
import com.systek.guide.MyApplication;
import com.systek.guide.callback.PlayChangeCallback;
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
    private static MediaServiceManager instance;

    private PlayChangeCallback stateChangeCallback;

    public PlayChangeCallback getStateChangeCallback() {
        return stateChangeCallback;
    }

    public void setStateChangeCallback(PlayChangeCallback stateChangeCallback) {
        this.stateChangeCallback = stateChangeCallback;
    }
    public void removeStateChangeCallback() {
        if(stateChangeCallback!=null){
            stateChangeCallback=null;
        }
    }

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
        if(stateChangeCallback!=null){
            stateChangeCallback.onStateChanged(mediaServiceBinder.getPlayState());
        }
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
        if (stateChangeCallback==null){return;}
        stateChangeCallback.onExhibitChanged(exhibitBean);
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

    public void onStateChange(){
        int state=0;
        if(mediaServiceBinder==null){return;}
        if (mediaServiceBinder.isPlaying()) {
            mediaServiceBinder.pause();
            state=PlayChangeCallback.STATE_PAUSE;
        } else {
            mediaServiceBinder.continuePlay();
            state=PlayChangeCallback.STATE_PLAYING;
        }
        if(stateChangeCallback!=null){
            stateChangeCallback.onStateChanged(state);
        }
    }

}
