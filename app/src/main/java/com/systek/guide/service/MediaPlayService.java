package com.systek.guide.service;

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

import com.systek.guide.IConstants;
import com.systek.guide.callback.PlayChangeCallback;
import com.systek.guide.entity.ExhibitBean;
import com.systek.guide.entity.MuseumBean;
import com.systek.guide.manager.MediaServiceManager;
import com.systek.guide.receiver.LockScreenReceiver;
import com.systek.guide.utils.ExceptionUtil;
import com.systek.guide.utils.MyHttpUtil;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 *  播放器服务类
 *
 */
public class MediaPlayService extends Service implements IConstants {


    private MediaPlayer mediaPlayer; // 播放器*/
    private ExhibitBean currentExhibit; //当前展品*/
    private String  currentMuseumId;//当前博物馆id*/
    private int currentPosition;//当前位置*/
    private int duration;//当前展品总时长
    private LockScreenReceiver mReceiver;//锁屏监听器
    private Handler handler ;
    private List<ExhibitBean> recordExhibitList;
    private List<ExhibitBean> playExhibitList;
    private int playMode ; //默认设置自动点击播放
    private Binder mediaServiceBinder = new MediaServiceBinder();///服务Binder*/
    private boolean isSendProgress;
    //private int errorCount;
    //private boolean isPause;
    //private boolean hasPlay; /*是否播放过*/
    //private boolean isPlaying ; //是否正在播放*/
    private static final int MSG_WHAT_UPDATE_PROGRESS=1;
    private int playState;


    public void onCreate() {
        super.onCreate();
        handler=new MyHandler(this);
        playMode= PLAY_MODE_HAND;
        recordExhibitList=new ArrayList<>();
        playExhibitList=new ArrayList<>();
        initMediaPlayer();
        registerReceiver();// TODO: 2016/2/26 暂时不开启锁屏界面
    }

    public int toGetDuration() {
        return duration;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mediaServiceBinder;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_STICKY;
    }

    /**
     * 锁屏广播接收器
     * */
    private void registerReceiver() {

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        mReceiver = new LockScreenReceiver();
        registerReceiver(mReceiver, filter);
    }

    public int toGetPlayMode() {
        return playMode;
    }

    public void toSetPlayMode(int playMode) {
        this.playMode = playMode;
    }

    /**初始化播放器*/
    private void initMediaPlayer() {
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setOnPreparedListener(preparedListener);
        mediaPlayer.setOnCompletionListener(completionListener);
        mediaPlayer.setOnErrorListener(errorListener);

    }
    /**
     * 播放完成监听器
     */
    private MediaPlayer.OnCompletionListener completionListener=new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mp) {
            try{}catch (Exception e){
                ExceptionUtil.handleException(e);
            }
            if(playMode==PLAY_MODE_AUTO){
                toStartPlay();
            }else if(playMode==PLAY_MODE_AUTO_PAUSE){
                toSetPlayMode(PLAY_MODE_AUTO);
                toStartPlay();
            }else{
                mp.seekTo(0);
                mp.pause();
                //isPause=true;
                playState= PlayChangeCallback.STATE_PAUSE;
                PlayChangeCallback callback=MediaServiceManager.getInstance(getApplicationContext()).getStateChangeCallback();
                if(callback!=null){
                    callback.onStateChanged(playState);
                }
                /*Intent intent=new Intent();
                intent.setAction(INTENT_CHANGE_PLAY_STOP);
                sendBroadcast(intent);*/
            }
        }
    };

    /**
     * 资源准备监听器
     */
    private  MediaPlayer.OnPreparedListener preparedListener=new MediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(MediaPlayer mp) {
            //if(playMode==PLAY_MODE_AUTO){// TODO: 2016/1/6  }
            toStartPlay();
        }
    };

    private boolean toStartPlay() {
        boolean flag=false;
        if(mediaPlayer==null){return false;}
        try {
            mediaPlayer.start();
            playState= PlayChangeCallback.STATE_PLAYING;
            PlayChangeCallback callback=MediaServiceManager.getInstance(getApplicationContext()).getStateChangeCallback();
            if(callback!=null){
                callback.onStateChanged(playState);
            }
            /*Intent intent=new Intent();
            intent.setAction(INTENT_CHANGE_PLAY_PLAY);
            sendBroadcast(intent);*/
            //isPlaying=true;
            addRecord(currentExhibit);
            duration = mediaPlayer.getDuration();
            handler.sendEmptyMessage(MSG_WHAT_UPDATE_PROGRESS);
            isSendProgress=true;
            flag=true;
        } catch (Exception e) {
            ExceptionUtil.handleException(e);
        }
        return flag;
    }

    /**
     * 异常监听器
     */
    private MediaPlayer.OnErrorListener errorListener=new MediaPlayer.OnErrorListener() {
        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            if(mp==null){return false;}
            mp.reset();
            return false;
        }
    };

    /**
     * 设置当前展品
     * @param bean
     */
    private void  setCurrentExhibit(ExhibitBean bean) {
        currentExhibit = bean;
    }

    private void toUpdateProgress() {
        if (mediaPlayer == null ) {return;}
        handler.sendEmptyMessage(MSG_WHAT_UPDATE_PROGRESS);
    }

    public boolean toPause(){
        if(playState== PlayChangeCallback.STATE_PAUSE||mediaPlayer==null){return true;}
        //if(!isPlaying||mediaPlayer==null){return true;}
        mediaPlayer.pause();
        playState= PlayChangeCallback.STATE_PAUSE;
        //isPlaying=false;
        //isPause=true;
        return true;
    }

    public boolean isPauseOrNot(){
        //return isPause;
        return playState== PlayChangeCallback.STATE_PAUSE;
    }


    private void play(ExhibitBean bean) {
        setCurrentExhibit(bean);
        currentMuseumId=bean.getMuseumId();
        //isPlaying=false;
        currentPosition=0;
        mediaPlayer.reset();
        handler.removeCallbacksAndMessages(null);
        String url = "";
        String exURL = currentExhibit.getAudiourl();
        String localName = exURL.replaceAll("/", "_");
        String localUrl = getCurrentAudioPath()+"/"+ localName;
        File file = new File(localUrl);
        if (file.exists()) {
            try {
                mediaPlayer.setDataSource(localUrl);
            } catch (IOException e) {
                ExceptionUtil.handleException(e);
            }
            mediaPlayer.prepareAsync();
        } else {
            url = BASE_URL + exURL;
            DownloadAudioTask downloadAudioTask = new DownloadAudioTask();
            downloadAudioTask.execute(url, localName);// TODO: 2016/1/6 修改加载地址
            Toast.makeText(this,"正在加载...",Toast.LENGTH_LONG).show();
        }
    }

    public void onDestroy() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        unregisterReceiver(mReceiver);
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    public void toContinuePlay(){
        toStartPlay();
    }

    public void toSeekTo(int progress){
        if (mediaPlayer == null) {return;}
        mediaPlayer.seekTo(progress);
    }

    public int toGetCurrentPosition(){
        return currentPosition;
    }

    private String  getCurrentAudioPath(){
        return LOCAL_ASSETS_PATH+currentMuseumId+"/";
    }

    /**播放博物馆讲解*/
    private void playMuseum(MuseumBean museumBean){
        mediaPlayer.reset();
        try {
            String url = "";
            String exURL = museumBean.getAudioUrl();
            String localName = exURL.replaceAll("/", "_");
            String localUrl = getCurrentAudioPath()+ "/" + localName;
            File file = new File(localUrl);
            if (file.exists()) {
                url = localUrl;
            } else {
                url = BASE_URL + exURL;
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
        if(recordExhibitList.contains(bean)){return;}
        recordExhibitList.add(bean);
    }

    /**停止播放*/
    private void stop() {
        if(mediaPlayer!=null&&mediaPlayer.isPlaying()){
            mediaPlayer.stop();
            //isPlaying = false;
            playState= PlayChangeCallback.STATE_STOP;
        }
    }

    public void toSetPlayList(List<ExhibitBean> list){
        this.playExhibitList=list;
    }
    public List<ExhibitBean> toGetPlayList(){
        return playExhibitList;
    }

    static class MyHandler extends Handler{

        WeakReference<MediaPlayService> mediaPlayServiceWeakReference;

        MyHandler(MediaPlayService mediaPlayService){
            this.mediaPlayServiceWeakReference=new WeakReference<>(mediaPlayService);
        }

        @Override
        public void handleMessage(Message msg) {

            if(mediaPlayServiceWeakReference==null){return;}
            MediaPlayService mediaPlayService=mediaPlayServiceWeakReference.get();
            if(mediaPlayService==null){return;}
            switch (msg.what) {
                case MSG_WHAT_UPDATE_PROGRESS:
                    mediaPlayService.doUpdateProgress();
                    break;
            }
        }
    }

    private void doUpdateDuration(){
        if(mediaPlayer==null||!isSendProgress){ return;}
        duration=mediaPlayer.getDuration();
        Intent intent=new Intent();
        intent.setAction(INTENT_EXHIBIT_DURATION);
        intent.putExtra(INTENT_EXHIBIT_DURATION,duration);
        sendBroadcast(intent);
    }

    private void doUpdateProgress() {
        if(mediaPlayer==null||!isSendProgress){ return;}
        currentPosition = mediaPlayer.getCurrentPosition();
        duration=mediaPlayer.getDuration();
        PlayChangeCallback callback=MediaServiceManager.getInstance(getApplicationContext()).getStateChangeCallback();
        if(callback!=null){
            callback.onPositionChanged(duration,currentPosition);
        }
       /* Intent intent=new Intent();
        intent.setAction(INTENT_EXHIBIT_PROGRESS);
        intent.putExtra(INTENT_EXHIBIT_PROGRESS, currentPosition);
        intent.putExtra(INTENT_EXHIBIT_DURATION,duration);
        sendBroadcast(intent);*/
        handler.sendEmptyMessageDelayed(MSG_WHAT_UPDATE_PROGRESS,800);
    }

    public class MediaServiceBinder extends Binder {

        public void stopPlay() {
            stop();
        }
        public boolean isPlaying() {
            return mediaPlayer != null && playState== PlayChangeCallback.STATE_PLAYING;
        }

        public int getPlayState(){
            return playState;
        }


        public boolean isPause() {
            return mediaPlayer != null && isPauseOrNot();
        }

        /**暂停后开始播放*/
        public void continuePlay(){
            toContinuePlay();
        }
        /**暂停*/
        public boolean pause(){
            isSendProgress=false;
            return toPause();
        }
        /**获取当前播放时长*/
        public int getCurrentPosition(){
            return toGetCurrentPosition();
        }
        /**播放时长跳至参数中时间*/
        public void seekTo(int progress) {
            toSeekTo(progress);
        }
        /**设置播放列表*/
        public void setPlayList(List<ExhibitBean> list) {
            toSetPlayList(list);
        }
        /**获得播放列表*/
        public List<ExhibitBean>  getPlayList() {
            return toGetPlayList();
        }

        /**通知切换展品*/
        public void notifyExhibitChange(ExhibitBean exhibitBean) {
            setCurrentExhibit(exhibitBean);
            play(exhibitBean);
        }

        public int getPlayMode() {
            return toGetPlayMode();
        }

        public void setPlayMode(int mode) {
            toSetPlayMode(mode);
        }

        public boolean startPlay(){
            return toStartPlay();
        }

        public int getDuration(){
            return toGetDuration();
        }

        public ExhibitBean getCurrentExhibit(){
            return currentExhibit;
        }

        public boolean next(){
            // TODO: 2016/1/6  
            return false;
        }
    }
    /**
     * 用于下载音频类
     */
    int count;

    class DownloadAudioTask extends AsyncTask<String,Void,String>{

        @Override
        protected String doInBackground(String... params) {
            String audioUrl=params[0];
            String audioName=params[1];
            String saveDir=getCurrentAudioPath();
            try {
                MyHttpUtil.downLoadFromUrl(audioUrl, saveDir,audioName);
            } catch (IOException e) {
                ExceptionUtil.handleException(e);
            }
            count++;
            return saveDir+audioName;
        }
        @Override
        protected void onPostExecute(String savePath) {
            if(count<=5){
                play(currentExhibit);
            }
        }
    }

}
