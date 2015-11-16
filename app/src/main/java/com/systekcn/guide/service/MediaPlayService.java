package com.systekcn.guide.service;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

import com.systekcn.guide.MyApplication;
import com.systekcn.guide.common.IConstants;
import com.systekcn.guide.common.utils.ExceptionUtil;
import com.systekcn.guide.entity.ExhibitBean;

import java.io.File;

public class MediaPlayService extends Service implements IConstants{

    private static final String TAG = "com.systekcn.guide.MediaPlay_Service";
    /**
     * 播放器
     */
    private MediaPlayer mediaPlayer;
    /**
     * 是否正在播放
     */
    private boolean isPlaying = false;
    /**当前播放展品列表*/
    /**
     * 服务Binder
     */
    private Binder mediaServiceBinder = new MediaServiceBinder();
    /**
     * 当前展品
     */
    private ExhibitBean currentExhibit;
    /**
     * 当前位置
     */
    private int currentPosition;
    /**
     * message类型之更新进度
     */
    private static final int updateProgress = 1;
    /**
     * message类型之更新展品
     */
    private static final int updateCurrentMusic = 2;
    /**
     * message类型之更新播放长度
     */
    private static final int updateDuration = 3;
    private MyApplication application;
    private int duration;

    public static final String ACTION_UPDATE_PROGRESS = "com.systekcn.guide.UPDATE_PROGRESS";
    public static final String ACTION_UPDATE_DURATION = "com.systekcn.guide.UPDATE_DURATION";
    public static final String ACTION_UPDATE_CURRENT_EXHIBIT = "com.systekcn.guide.UPDATE_CURRENT_MUSIC";

    private Handler handler = new Handler() {

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case updateProgress:
                    toUpdateProgress();
                    break;
                case updateDuration:
                    toUpdateDuration(duration);
                    break;
                case updateCurrentMusic:
                    toUpdateCurrentExhibit();
                    break;
            }
        }
    };

    private void toUpdateProgress() {
        if (mediaPlayer != null && isPlaying) {
            int progress = mediaPlayer.getCurrentPosition();
            Intent intent = new Intent();
            intent.setAction(ACTION_UPDATE_PROGRESS);
            intent.putExtra(ACTION_UPDATE_PROGRESS, progress);
            sendBroadcast(intent);
            handler.sendEmptyMessageDelayed(updateProgress, 1000);
            System.gc();
        }
    }

    private void toUpdateDuration(int time) {
        Intent intent = new Intent();
        intent.setAction(ACTION_UPDATE_DURATION);
        intent.putExtra(ACTION_UPDATE_DURATION, time);
        sendBroadcast(intent);
    }

    private void toUpdateCurrentExhibit() {
        if(application.currentExhibitBean!=null){
            play(application.currentExhibitBean);
        }
    }

    public void onCreate() {
        super.onCreate();
        application= (MyApplication) getApplication();
        initMediaPlayer();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    public void onDestroy() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        super.onDestroy();
    }

    /**
     * initialize the MediaPlayer
     */
    private void initMediaPlayer() {
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
            }
        });
        mediaPlayer.setOnCompletionListener(
                new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {

                        if (isPlaying) {// TODO: 2015/11/9
                            int index = application.nearlyExhibitBeanList.indexOf(currentExhibit) + 1;
                            if (index == application.nearlyExhibitBeanList.size()) {
                                index = 0;
                            }
                            try{
                                application.currentExhibitBean = application.nearlyExhibitBeanList.get(index);
                                play(application.currentExhibitBean);
                                Intent intent = new Intent();
                                intent.setAction(ACTION_UPDATE_CURRENT_EXHIBIT);
                                sendBroadcast(intent);
                            }catch (Exception e){ExceptionUtil.handleException(e);}

                        }
                    }

                }

        );
    }

    private void  setCurrentExhibit(ExhibitBean bean) {
        currentExhibit = bean;
    }

    private void play(ExhibitBean bean) {
        setCurrentExhibit(bean);
        mediaPlayer.reset();
        try {
            String url = "";
            String exURL = currentExhibit.getAudiourl();
            String localName = exURL.replaceAll("/", "_");
            String localUrl = LOCAL_ASSETS_PATH +application.getCurrentMuseumId() + "/"+LOCAL_FILE_TYPE_AUDIO+"/"+ localName;
            File file = new File(localUrl);
            if (file.exists()) {
                url = localUrl;
            } else {
                url = BASEURL + exURL;
            }
            mediaPlayer.setDataSource(url);
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (Exception e) {
            ExceptionUtil.handleException(e);
        }
        duration=mediaPlayer.getDuration();
        handler.sendEmptyMessage(updateDuration);
        handler.sendEmptyMessage(updateProgress);
        isPlaying = true;
        addRecord(bean);
    }

    private void addRecord(ExhibitBean bean) {
        if(!application.everSeenExhibitBeanList.contains(bean)){
            application.everSeenExhibitBeanList.add(bean);
        }
    }

    private void stop() {
        mediaPlayer.stop();
        isPlaying = false;
    }
    @Override
    public IBinder onBind(Intent intent) {
        return mediaServiceBinder;
    }

    public class MediaServiceBinder extends Binder {

        public void stopPlay() {
            stop();
        }
        /**
         * The service is playing the music
         *
         * @return
         */
        public boolean isPlaying() {
            return mediaPlayer.isPlaying();
        }

        public void toContinue(){
            mediaPlayer.start();
            isPlaying=true;
            handler.sendEmptyMessage(updateProgress);
        }
        public void pause(){
            mediaPlayer.pause();
            isPlaying=false;
        }
        /**
         * Notify Activities to update the current music and duration when
         * current activity changes.
         */
        public void notifyAllDataChange() {
            currentExhibit = application.currentExhibitBean;
            toUpdateCurrentExhibit();
        }


        public int getCurrentPosition(){
            return mediaPlayer.getCurrentPosition();
        }

        /**
         * Seekbar changes
         *
         * @param progress
         */
        public void seekTo(int progress) {
            if (mediaPlayer != null) {
                currentPosition = progress;
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.seekTo(currentPosition);
                } else {
                    play(currentExhibit);
                }
            }
        }

        /*public void startPlay() {
            play(currentExhibit);

        }*/

        /* public int getDuration(){
            return mediaPlayer.getDuration();
        }*/

       /* public void reset(){
            mediaPlayer.reset();
        }

        public void refreshExhibitList(List<ExhibitBean> exhibitBeanList){
        }

        public void rePlay(){
            mediaPlayer.seekTo(0);
        }

        public void prepare(){
            try {
                mediaPlayer.prepare();
            } catch (IOException e) {
                ExceptionUtil.handleException(e);
            }
        }*/
    }
}
