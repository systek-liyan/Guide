package com.systek.guide.service;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.annotation.NonNull;

import com.systek.guide.IConstants;
import com.systek.guide.MyApplication;
import com.systek.guide.callback.PlayChangeCallback;
import com.systek.guide.entity.ExhibitBean;

/**
 * Created by Qiang on 2016/6/27.
 */
public class PlayManager implements IConstants{

    //private Context mContext;
    private static PlayManager instance;
    private ExhibitPlayService.MediaServiceBinder mediaServiceBinder;
    private PlayChangeCallback playChangeCallback;

    /**
     * 私有构造方法，连接并启动播放service
     */
    private PlayManager() {
        /*this.mContext = context.getApplicationContext();*/
    }

    /**
     * 单例构造方法
     * @return
     */
    public static PlayManager getInstance(){
        if(instance==null){
            synchronized (MyApplication.class){
                if(instance==null){
                    instance=new PlayManager();
                }
            }
        }
        return instance;
    }

    public void addPlayChangeCallback(PlayChangeCallback playChangeCallback){
        if(mediaServiceBinder==null){return;}
        mediaServiceBinder.addPlayback(playChangeCallback);
    }



    public void bindToService(@NonNull Context context,PlayChangeCallback playChangeCallback){
        this.playChangeCallback=playChangeCallback;
        context.bindService(new Intent(context,ExhibitPlayService.class),mConn,Context.BIND_AUTO_CREATE);
    }
    public void unbindService(@NonNull Context context,PlayChangeCallback playChangeCallback){
        context.unbindService(mConn);
        mediaServiceBinder.removePlayback(playChangeCallback);
    }


    private ServiceConnection mConn = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mediaServiceBinder = (ExhibitPlayService.MediaServiceBinder) service;
            mediaServiceBinder.addPlayback(playChangeCallback);
        }
    };

    public int getState(){
        return mediaServiceBinder==null?0:mediaServiceBinder.getState();
    }
    /**
     * 获取当前播放进度
     * @return
     */
    public int getPosition(){
        if (mediaServiceBinder == null) {return 0;}
        return mediaServiceBinder.getCurrentStreamPosition();
    }

    /**
     * 展品切换
     * @param exhibit
     */
    public void playFromBean(ExhibitBean exhibit){
        if (mediaServiceBinder == null||exhibit==null) {return;}
        mediaServiceBinder.onSkipToQueueItem(exhibit);
    }


    /**
     * 播放
     * @return
     */
    public void play() {
        if (mediaServiceBinder == null) {return;}
        mediaServiceBinder.onPlay();
    }



    /**
     * 停止播放
     */
    public void pause() {
        if (mediaServiceBinder == null) {return;}
        mediaServiceBinder.onStop();
    }

    /**
     * 判断是否正在播放
     * @return
     */
    public boolean isPlaying() {
        return mediaServiceBinder != null && mediaServiceBinder.isPlaying();
    }

    /**
     * 播放进度跳转至progre
     * @param progress 进度
     */
    public boolean seekTo(int progress) {
        return mediaServiceBinder != null && mediaServiceBinder.onSeekTo(progress);
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
        if (mediaServiceBinder == null) {return PLAY_MODE_HAND;}
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

    public void onStateChange(int state){
        if(mediaServiceBinder==null){return;}
        mediaServiceBinder.onStateChanged(state);
    }


}
