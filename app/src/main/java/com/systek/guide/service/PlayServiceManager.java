package com.systek.guide.service;

import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.systek.guide.IConstants;
import com.systek.guide.MyApplication;
import com.systek.guide.callback.PlayChangeCallback;
import com.systek.guide.entity.ExhibitBean;

/**
 * Created by Qiang on 2016/6/27.
 */
public class PlayServiceManager implements IConstants{

    private Context mContext;
    private ServiceConnection mConn;
    public ExhibitPlayService.MediaServiceBinder mediaServiceBinder;
    private static PlayServiceManager instance;

    public PlayChangeCallback getStateChangeCallback() {
        return mediaServiceBinder.getPlayback();
    }

    public void setStateChangeCallback(PlayChangeCallback stateChangeCallback) {
        mediaServiceBinder.setPlayback(stateChangeCallback);
    }

    /**
     * 单例构造方法
     * @param context 上下文
     * @return
     */
    public static PlayServiceManager getInstance(Context context){
        if(instance==null){
            synchronized (MyApplication.class){
                if(instance==null){
                    instance=new PlayServiceManager(context);
                }
            }
        }
        return instance;
    }

    /**
     * 私有构造方法，连接并启动播放service
     * @param context
     */
    private PlayServiceManager(Context context) {
        this.mContext = context.getApplicationContext();
        init();
    }

    /**
     * 连接播放service，注册广播
     */
    public void init(){
        mConn = new ServiceConnection() {
            @Override
            public void onServiceDisconnected(ComponentName name) {
            }
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                mediaServiceBinder = (ExhibitPlayService.MediaServiceBinder) service;
            }
        };
    }

    public int getState(){
        return mediaServiceBinder.getState();
    }
    /**
     * 获取当前播放进度
     * @return
     */
    public int getPosition(){
        if (mediaServiceBinder == null) {return 0;}
        return mediaServiceBinder.getCurrentStreamPosition();
    }

    public void onSkipToQueueItem(ExhibitBean exhibit){
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
        if(mediaServiceBinder.getPlayback()!=null){
            mediaServiceBinder.getPlayback().onStateChanged(mediaServiceBinder.getState());
        }
    }

    /**
     * 判断是否正在播放
     * @return
     */
    public boolean isPlaying() {
        if (mediaServiceBinder == null) {return false;}
        return mediaServiceBinder.isPlaying();
    }

    /**
     * 播放进度跳转至progre
     * @param progress 进度
     */
    public void seekTo(int progress) {
        if (mediaServiceBinder == null) {return;}
        mediaServiceBinder.onSeekTo(progress);
    }

    /**
     * 通知展品切换
     * @param exhibitBean
     */
    public void skipToQueueItem(ExhibitBean exhibitBean){
        if (mediaServiceBinder == null) {return;}
        mediaServiceBinder.onSkipToQueueItem(exhibitBean);

        if(mediaServiceBinder.getPlayback()!=null){
            mediaServiceBinder.getPlayback().onExhibitChanged(exhibitBean);
        }
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

   /* public void onStateChange(){
        int state=0;
        if(mediaServiceBinder==null){return;}
        if (mediaServiceBinder.isPlaying()) {
            mediaServiceBinder.onPause();
            state=PlayChangeCallback.STATE_PAUSE;
        } else {
            mediaServiceBinder.onPlay();
            state=PlayChangeCallback.STATE_PLAYING;
        }

        if(mediaServiceBinder.getPlayback()!=null){
            mediaServiceBinder.getPlayback().onStateChanged(state);
        }
    }
*/

}
