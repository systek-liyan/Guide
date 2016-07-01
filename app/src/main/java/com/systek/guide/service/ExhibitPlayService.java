package com.systek.guide.service;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

import com.systek.guide.IConstants;
import com.systek.guide.callback.PlayChangeCallback;
import com.systek.guide.entity.ExhibitBean;
import com.systek.guide.receiver.LockScreenReceiver;
import com.systek.guide.utils.LogUtil;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class ExhibitPlayService extends Service implements Playback.Callback,IConstants{

    private static final String TAG = LogUtil.makeLogTag(ExhibitPlayService.class);

    // Extra on MediaSession that contains the Cast device name currently connected to
    public static final String EXTRA_CONNECTED_CAST = "com.qiang.mediaProject.CAST_NAME";
    // The action of the incoming Intent indicating that it contains a command
    // to be executed (see {@link #onStartCommand})
    public static final String ACTION_CMD = "com.qiang.mediaProject.ACTION_CMD";
    // The key in the extras of the incoming Intent indicating the command that
    // should be executed (see {@link #onStartCommand})
    public static final String CMD_NAME = "CMD_NAME";
    // A value of a CMD_NAME key in the extras of the incoming Intent that
    // indicates that the music playback should be paused (see {@link #onStartCommand})
    public static final String CMD_PAUSE = "CMD_PAUSE";
    // A value of a CMD_NAME key that indicates that the music playback should switch
    // to local playback from cast playback.
    public static final String CMD_STOP_CASTING = "CMD_STOP_CASTING";

    // Action to thumbs up a media item
    private static final String CUSTOM_ACTION_THUMBS_UP = "com.qiang.mediaProject.THUMBS_UP";
    // Delay stopSelf by using a handler.
    private static final int STOP_DELAY = 30000;

    // Music catalog manager
    //private MusicProvider mMusicProvider;
    // "Now playing" queue:
    private ExhibitBean mCurrentExhibit;
    //private MediaNotificationManager mMediaNotificationManager;
    // Indicates whether the service was started.
    private boolean mServiceStarted;
    //private Bundle mSessionExtras;
    private final DelayedStopHandler mDelayedStopHandler = new DelayedStopHandler(this);
    private Playback mPlayback;
    private List<PlayChangeCallback> playChangeCallbackList;
    private Handler handler;
    private LockScreenReceiver mReceiver;
    private boolean isLockScreenReceiverRegistered;

    private ExhibitBean getCurrentPlayingMusic() {
        return mCurrentExhibit;
    }

    //private List<ExhibitBean> mPlayingQueue;
    private static final int MSG_WHAT_UPDATE_PROGRESS=1;

    static class MyHandler extends Handler{

        WeakReference<ExhibitPlayService> playServiceWeakReference;

        MyHandler(ExhibitPlayService mediaPlayService){
            this.playServiceWeakReference=new WeakReference<>(mediaPlayService);
        }

        @Override
        public void handleMessage(Message msg) {

            if(playServiceWeakReference==null){return;}
            ExhibitPlayService mediaPlayService=playServiceWeakReference.get();
            if(mediaPlayService==null){return;}
            switch (msg.what) {
                case MSG_WHAT_UPDATE_PROGRESS:
                    mediaPlayService.doUpdateProgress();
                    break;
            }
        }
    }


    private void doUpdateProgress() {
        if(playChangeCallbackList==null||playChangeCallbackList.size()==0){
            handler.sendEmptyMessageDelayed(MSG_WHAT_UPDATE_PROGRESS,500);
            return;}//||!isSendProgress
        if(mPlayback.isPlaying()){
            int currentPosition = mPlayback.getCurrentStreamPosition();
            int duration=mPlayback.getDuration();
            if(currentPosition!=0&&duration!=0){
                for(PlayChangeCallback callback:playChangeCallbackList){
                    if(callback!=null){
                        callback.onPositionChanged(duration,currentPosition);
                    }
                }
            }
        }
        handler.sendEmptyMessageDelayed(MSG_WHAT_UPDATE_PROGRESS,500);
    }


    public void addPlayChangeCallback(PlayChangeCallback playChangeCallback) {
        if(playChangeCallbackList==null){
            playChangeCallbackList=new ArrayList<>();
        }
        playChangeCallbackList.add(playChangeCallback);
    }

    public void removePlayChangeCallback(PlayChangeCallback playChangeCallback){
        if(playChangeCallbackList==null){return;}
        if(playChangeCallbackList.contains(playChangeCallback)){
            playChangeCallbackList.remove(playChangeCallback);
        }
    }

    private Binder binder = new MediaServiceBinder();///服务Binder*/

    public ExhibitPlayService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //mPlayingQueue = new ArrayList<>();
        mPlayback = new LocalPlayback(this);
        handler=new MyHandler(this);
        mPlayback.setState(Playback.STATE_NONE);
        mPlayback.setCallback(this);
        mPlayback.start();
        updatePlaybackState(null);

    }

    @Override
    public int onStartCommand(Intent startIntent, int flags, int startId) {
        if (startIntent != null) {
            String action = startIntent.getAction();
            String command = startIntent.getStringExtra(CMD_NAME);
            if (ACTION_CMD.equals(action)) {
                if (CMD_PAUSE.equals(command)) {
                    if (mPlayback != null && mPlayback.isPlaying()) {
                        handlePauseRequest();
                    }
                }
            }
        }
        // Reset the delay handler to enqueue a message to stop the service if
        // nothing is playing.
        mDelayedStopHandler.removeCallbacksAndMessages(null);
        mDelayedStopHandler.sendEmptyMessageDelayed(0, STOP_DELAY);
        return START_STICKY;
    }


    /**
     * (non-Javadoc)
     * @see android.app.Service#onDestroy()
     */
    @Override
    public void onDestroy() {
        LogUtil.d(TAG, "onDestroy");
        // Service is being killed, so make sure we release our resources
        handleStopRequest(null);

        mDelayedStopHandler.removeCallbacksAndMessages(null);
        this.playChangeCallbackList=null;
        // Always release the MediaSession to clean up resources
        // and notify associated MediaController(s).
        //mSession.release();
    }


    /**
     * Handle a request to pause music
     */
    private void handlePauseRequest() {
        LogUtil.d(TAG, "handlePauseRequest: mState=" + mPlayback.getState());
        mPlayback.pause();
        // reset the delayed stop handler.
        mDelayedStopHandler.removeCallbacksAndMessages(null);
        mDelayedStopHandler.sendEmptyMessageDelayed(0, STOP_DELAY);
    }


    /**
     * Handle a request to stop music
     */
    private void handleStopRequest(String withError) {
        LogUtil.d(TAG, "handleStopRequest: mState=" + mPlayback.getState() + " error=", withError);
        mPlayback.stop(true);
        // reset the delayed stop handler.
        mDelayedStopHandler.removeCallbacksAndMessages(null);
        mDelayedStopHandler.sendEmptyMessageDelayed(0, STOP_DELAY);

        updatePlaybackState(withError);

        // service is no longer necessary. Will be started again if needed.
        stopSelf();
        mServiceStarted = false;
        handler.removeCallbacksAndMessages(null);
        unRegisiterLockScreenReceiver();
    }

    /**
     * Handle a request to play music
     */
    private void handlePlayRequest() {
        LogUtil.d(TAG, "handlePlayRequest: mState=" + mPlayback.getState());

        mDelayedStopHandler.removeCallbacksAndMessages(null);
        if (!mServiceStarted) {
            LogUtil.v(TAG, "Starting service");
            // The MusicService needs to keep running even after the calling MediaBrowser
            // is disconnected. Call startService(Intent) and then stopSelf(..) when we no longer
            // need to play media.
            startService(new Intent(getApplicationContext(), ExhibitPlayService.class));
            mServiceStarted = true;
        }
        updateMetadata();
        mPlayback.play(mCurrentExhibit);
        doUpdateProgress();
        registerLockScreenReceiver();
    }
    private void registerLockScreenReceiver() {
        if(isLockScreenReceiverRegistered){return;}
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        mReceiver = new LockScreenReceiver();
        registerReceiver(mReceiver, filter);
        isLockScreenReceiverRegistered=true;
    }

    private void unRegisiterLockScreenReceiver(){
        if(isLockScreenReceiverRegistered){
            unregisterReceiver(mReceiver);
            isLockScreenReceiverRegistered=false;
        }
    }


    private void updateMetadata() {
    }

    /**
     * Update the current media player state, optionally showing an error message.
     *
     * @param error if not null, error message to present to the user.
     */
    private void updatePlaybackState(String error) {
        LogUtil.d(TAG, "updatePlaybackState, playback state=" + mPlayback.getState());
        long position = Playback.PLAYBACK_POSITION_UNKNOWN;
        if (mPlayback != null && mPlayback.isConnected()){
            position = mPlayback.getCurrentStreamPosition();
        }
        int state = mPlayback.getState();
        // If there is an error message, send it to the playback state:
        if (error != null) {
            // Error states are really only supposed to be used for errors that cause playback to
            // stop unexpectedly and persist until the user takes action to fix it.
            state = Playback.STATE_ERROR;
        }
        if(playChangeCallbackList==null){return;}
        for(PlayChangeCallback callback:playChangeCallbackList){
            if(callback!=null){
                callback.onStateChanged(state,(int)position,error);
            }
        }
      /* if (state == PlaybackState.STATE_PLAYING || state == PlaybackState.STATE_PAUSED) {
            mMediaNotificationManager.startNotification();
        }*/
    }


    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCompletion() {
        mPlayback.seekTo(0);
        mPlayback.setCurrentStreamPosition(0);
        if(playChangeCallbackList==null||playChangeCallbackList.size()==0){return;}
        for(PlayChangeCallback callback:playChangeCallbackList){
            if(callback!=null){
                callback.onPositionChanged(0,0);
            }
        }
        if(mPlayback.getMode()==PLAY_MODE_AUTO){
            handlePlayRequest();
        }else{
            handleStopRequest(null);
        }
        // The media player finished playing the current song, so we go ahead
        // and start the next.
       /* if (mPlayingQueue != null && !mPlayingQueue.isEmpty()) {
            // In this sample, we restart the playing queue when it gets to the end:
            mCurrentExhibit++;
            if (mCurrentExhibit >= mPlayingQueue.size()) {
                mCurrentExhibit = 0;
            }
            handlePlayRequest();
        } else {
            // If there is nothing to play, we stop and release the resources:
            handleStopRequest(null);
        }*/
    }

    @Override
    public void onPlaybackStatusChanged(int state) {
        if(playChangeCallbackList==null||playChangeCallbackList.size()==0||mPlayback==null){return;}
        for(PlayChangeCallback callback:playChangeCallbackList){
            if(callback!=null){
                callback.onStateChanged(state);
            }
        }
    }

    @Override
    public void onError(String error) {
        if(playChangeCallbackList==null||playChangeCallbackList.size()==0||mPlayback==null){return;}
        for(PlayChangeCallback callback:playChangeCallbackList){
            if(callback!=null){
                callback.onError(error);
            }
        }
    }

    @Override
    public void onMetadataChanged(ExhibitBean exhibit) {
        if(playChangeCallbackList==null||playChangeCallbackList.size()==0||mPlayback==null){return;}
        for(PlayChangeCallback callback:playChangeCallbackList){
            if(callback!=null){
                callback.onExhibitChanged(exhibit);
            }
        }
    }


    private long getAvailableActions() {
       /* long actions = Playback.ACTION_PLAY | Playback.ACTION_PLAY_FROM_MEDIA_ID |
                Playback.ACTION_PLAY_FROM_SEARCH;
        if (mPlayingQueue == null || mPlayingQueue.isEmpty()) {
            return actions;
        }
        if (mPlayback.isPlaying()) {
            actions |= Playback.ACTION_PAUSE;
        }
        if (mCurrentExhibit > 0) {
            actions |= Playback.ACTION_SKIP_TO_PREVIOUS;
        }
        if (mCurrentExhibit < mPlayingQueue.size() - 1) {
            actions |= Playback.ACTION_SKIP_TO_NEXT;
        }
        return actions;*/
        return 0;
    }



    /**
     * A simple handler that stops the service if playback is not active (playing)
     */
    private static class DelayedStopHandler extends Handler {
        private final WeakReference<ExhibitPlayService> mWeakReference;

        private DelayedStopHandler(ExhibitPlayService service) {
            mWeakReference = new WeakReference<>(service);
        }

        @Override
        public void handleMessage(Message msg) {
            ExhibitPlayService service = mWeakReference.get();
            if (service != null && service.mPlayback != null) {
                if (service.mPlayback.isPlaying()) {
                    return;
                }
                service.stopSelf();
                service.mServiceStarted = false;
            }
        }
    }


    public  class MediaServiceBinder extends Binder {

        public void onPlay() {
            handlePlayRequest();
        }

        public void onSkipToQueueItem(ExhibitBean exhibit){
            if(mPlayback==null||exhibit==null){return;}
            mCurrentExhibit=exhibit;
            handlePlayRequest();
        }

        public boolean onSeekTo(long position){
            if(mPlayback==null){return false;}
            mPlayback.seekTo((int) position);
            return true;
        }
        public boolean isPlaying() {
            return mPlayback != null && mPlayback.isPlaying();
        }

        public int getCurrentStreamPosition(){
            if(mPlayback==null){return 0;}
            return mPlayback.getCurrentStreamPosition();
        }

        public int getDuration(){
            if(mPlayback==null||!mPlayback.isPlaying()){return 0;}
            return mPlayback.getDuration();
        }
        public int getState(){
            if(mPlayback==null){return 0;}
            return mPlayback.getState();
        }
        public int getPlayMode(){
            if(mPlayback==null){return 0;}
            return mPlayback.getMode();
        }
        public void setPlayMode(int mode){
            if(mPlayback==null){return ;}
            mPlayback.setMode(mode);
        }

       /* public void onPause() {
            if (mPlayback == null) {return;}
            mPlayback.pause();
        }*/

        public void onStateChanged(int state){
            onPlaybackStatusChanged(state);
        }


        public ExhibitBean getCurrentExhibit(){
            if(mPlayback==null){return null;}
            return mPlayback.getCurrentExhibit();
        }

        public void onStop(){
            handleStopRequest(null);
        }

        public void addPlayback(PlayChangeCallback playChangeCallback){
            addPlayChangeCallback(playChangeCallback);
        }
        public void removePlayback(PlayChangeCallback playChangeCallback){
            removePlayChangeCallback(playChangeCallback);
        }

        public void onSkipToNext(){
            // TODO: 2016/6/27
        }
        public void onSkipToPrevious(){
            // TODO: 2016/6/27
        }
    }

}
