package com.systekcn.guide.service;

import android.app.KeyguardManager;
import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.widget.Toast;

import com.systekcn.guide.MyApplication;
import com.systekcn.guide.common.IConstants;
import com.systekcn.guide.common.utils.ExceptionUtil;
import com.systekcn.guide.common.utils.LogUtil;
import com.systekcn.guide.common.utils.Tools;
import com.systekcn.guide.entity.ExhibitBean;
import com.systekcn.guide.entity.MuseumBean;
import com.systekcn.guide.receiver.LockScreenReceiver;

import java.io.File;
import java.io.IOException;

public class MediaPlayService extends Service implements IConstants{

    /** 播放器*/
    private MediaPlayer mediaPlayer;
    /*是否正在播放*/
    private boolean isPlaying = false;
    /**服务Binder*/
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
    private DownloadAudioTask downloadAudioTask;
    private LockScreenReceiver mReceiver;
    private KeyguardManager.KeyguardLock kl;

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
                    toNotifyAllDataChange();
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
            //System.gc();
        }
    }

    private void toUpdateDuration(int time) {
        Intent intent = new Intent();
        intent.setAction(ACTION_UPDATE_DURATION);
        intent.putExtra(ACTION_UPDATE_DURATION, time);
        sendBroadcast(intent);
    }

    private void toNotifyAllDataChange() {
        currentExhibit = application.currentExhibitBean;
        if(application.currentExhibitBean!=null){
            play(application.currentExhibitBean);
        }
    }

    public void onCreate() {
        super.onCreate();
        application= (MyApplication) getApplication();
        initMediaPlayer();
        // 禁用系统锁屏页
        KeyguardManager km = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
        kl = km.newKeyguardLock("IN");
        kl.disableKeyguard();
        /**锁屏广播接收器*/
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_SCREEN_ON);
        mReceiver = new LockScreenReceiver();
        registerReceiver(mReceiver, filter);
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
        unregisterReceiver(mReceiver);
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
                mediaPlayer.start();
                try {
                    duration = mediaPlayer.getDuration();
                    handler.sendEmptyMessage(updateDuration);
                    handler.sendEmptyMessage(updateProgress);
                } catch (Exception e) {
                    ExceptionUtil.handleException(e);
                }
            }
        });
        mediaPlayer.setOnCompletionListener(
                new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {

                        if (isPlaying&&hasPlay) {// TODO: 2015/11/9
                            hasPlay=false;
                            int index = application.currentExhibitBeanList.indexOf(currentExhibit) + 1;
                            if (index == application.currentExhibitBeanList.size()) {
                                index = 0;
                            }
                            try {
                                application.currentExhibitBean = application.currentExhibitBeanList.get(index);
                                play(application.currentExhibitBean);
                                Intent intent = new Intent();
                                intent.setAction(ACTION_UPDATE_CURRENT_EXHIBIT);
                                sendBroadcast(intent);
                            } catch (Exception e) {
                                ExceptionUtil.handleException(e);
                            }
                        }
                    }

                }
        );

        mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                return false;
            }
        });

    }

    private void  setCurrentExhibit(ExhibitBean bean) {
        currentExhibit = bean;
    }
    private boolean hasPlay;

    private void play(ExhibitBean bean) {
        setCurrentExhibit(bean);
        isPlaying=false;
        mediaPlayer.reset();
        String url = "";
        String exURL = currentExhibit.getAudiourl();
        String localName = exURL.replaceAll("/", "_");
        String localUrl = LOCAL_ASSETS_PATH +application.getCurrentMuseumId() + "/"+LOCAL_FILE_TYPE_AUDIO+"/"+ localName;
        File file = new File(localUrl);
        if (!file.exists()) {
            url = BASEURL + exURL;
            downloadAudioTask=new DownloadAudioTask();
            downloadAudioTask.execute(url,localName);
        } else {
            url = localUrl;
            completePlay(bean, url);
        }
    }

    private int errorCount;


    /**完成播放*/
    private void completePlay(ExhibitBean bean, String url) {
        try {
            LogUtil.i("ZHANG", url);
            if(Tools.isFileExist(url)){
                mediaPlayer.setDataSource(url);
                mediaPlayer.prepareAsync();
                addRecord(bean);
                isPlaying = true;
                hasPlay=true;
                errorCount=0;
            }
        } catch (IOException e) {
            ExceptionUtil.handleException(e);
            errorCount++;
            if(errorCount<=5){
                play(currentExhibit);
            }else{
                Toast.makeText(this,"数据获取异常",Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * 用于下载音频类
     */
    class DownloadAudioTask extends AsyncTask<String,Void,String>{

        @Override
        protected String doInBackground(String... params) {

            String audioUrl=params[0];
            String audioName=params[1];
            String saveDir=application.getCurrentAudioDir();
            try {
                Tools.downLoadFromUrl(audioUrl,audioName,saveDir);
            } catch (IOException e) {
                ExceptionUtil.handleException(e);
            }
            return saveDir+audioName;
        }

        @Override
        protected void onPostExecute(String aVoid) {
            try{
                completePlay(currentExhibit,aVoid);
            }catch (Exception e){
                ExceptionUtil.handleException(e);
            }
        }
    }

    /**播放博物馆讲解*/
    private void pMuseum(MuseumBean museumBean){
        mediaPlayer.reset();
        try {
            String url = "";
            String exURL = museumBean.getAudioUrl();
            String localName = exURL.replaceAll("/", "_");
            String localUrl = LOCAL_ASSETS_PATH + application.getCurrentMuseumId() + "/" + LOCAL_FILE_TYPE_AUDIO + "/" + localName;
            File file = new File(localUrl);
            if (file.exists()) {
                url = localUrl;
            } else {
                url = BASEURL + exURL;
            }
            mediaPlayer.setDataSource(url);
            mediaPlayer.prepare();
            mediaPlayer.start();
        }catch(Exception e){
            ExceptionUtil.handleException(e);
        }
    }

    /**添加讲解过的记录*/
    private void addRecord(ExhibitBean bean) {
        if(!application.everSeenExhibitBeanList.contains(bean)){
            application.everSeenExhibitBeanList.add(bean);
        }
    }

    /**停止播放*/
    private void stop() {
        mediaPlayer.stop();
        isPlaying = false;
    }
    @Override
    public IBinder onBind(Intent intent) {
        return mediaServiceBinder;
    }

    public class MediaServiceBinder extends Binder {
        /**播放博物馆*/
        public void playMuseum(MuseumBean museumBean){
            pMuseum(museumBean);
        }

        public void stopPlay() {
            stop();
        }
        public boolean isPlaying() {
            if(mediaPlayer!=null){
                return mediaPlayer.isPlaying();
            }else{
                return false;
            }
        }
        /**暂停后开始播放*/
        public void toContinue(){
            mediaPlayer.start();
            isPlaying=true;
            handler.sendEmptyMessage(updateProgress);
        }
        /**暂停*/
        public void pause(){
            mediaPlayer.pause();
            isPlaying=false;
        }
        /**通知切换展品*/
        public void notifyAllDataChange() {
            toNotifyAllDataChange();
        }
        /**获取当前播放时长*/
        public int getCurrentPosition(){
            return mediaPlayer.getCurrentPosition();
        }
        /**播放时长跳至参数中时间*/
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
    }



}
