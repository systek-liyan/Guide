package com.systek.guide.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.TextUtils;

import com.systek.guide.callback.PlayChangeCallback;
import com.systek.guide.entity.ExhibitBean;
import com.systek.guide.utils.LogUtil;

import java.lang.ref.WeakReference;

public class ExhibitPlayService extends Service implements Playback.Callback{

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
    private PlayChangeCallback playChangeCallback;

    //private List<ExhibitBean> mPlayingQueue;

    public void setPlayChangeCallback(PlayChangeCallback playChangeCallback) {
        this.playChangeCallback = playChangeCallback;
    }

    public PlayChangeCallback getPlayChangeCallback() {
        return playChangeCallback;
    }


    private Binder binder = new MediaServiceBinder();///服务Binder*/


    public ExhibitPlayService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //mPlayingQueue = new ArrayList<>();
        mPlayback = new LocalPlayback(this);
        mPlayback.setState(Playback.STATE_NONE);
        mPlayback.setCallback(this);
        mPlayback.start();
        //updatePlaybackState(null);

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
        // Always release the MediaSession to clean up resources
        // and notify associated MediaController(s).
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
        //}
    }

    private ExhibitBean getCurrentPlayingMusic() {
        return mCurrentExhibit;
    }



    private void updateMetadata() {
    }

    /**
     * Update the current media player state, optionally showing an error message.
     *
     * @param error if not null, error message to present to the user.
     */
    private void updatePlaybackState(String error) {
        if(playChangeCallback==null|| TextUtils.isEmpty(error)){return;}
        playChangeCallback.onError(error);
        //playChangeCallback.onExhibitChanged(mPlayback.getCurrentExhibit());
      /*   LogUtil.d(TAG, "updatePlaybackState, playback state=" + mPlayback.getState());
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

        //mSession.setPlaybackState(stateBuilder.build());

       if (state == PlaybackState.STATE_PLAYING || state == PlaybackState.STATE_PAUSED) {
            mMediaNotificationManager.startNotification();
        }*/
    }


    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCompletion() {
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
        if(playChangeCallback==null||mPlayback==null){return;}
        playChangeCallback.onStateChanged(state);
    }

    @Override
    public void onError(String error) {
        if(playChangeCallback==null||mPlayback==null){return;}
        playChangeCallback.onError(error);
    }

    @Override
    public void onMetadataChanged(ExhibitBean exhibit) {
        if(playChangeCallback==null||mPlayback==null){return;}
        playChangeCallback.onExhibitChanged(exhibit);
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
            mPlayback.play(exhibit);
        }

        public void onSeekTo(long position){
            if(mPlayback==null){return;}
            mPlayback.seekTo((int) position);
        }
        public boolean isPlaying() {
            return mPlayback != null && mPlayback.isPlaying();
        }

        public int getCurrentStreamPosition(){
            if(mPlayback==null){return 0;}
            return mPlayback.getCurrentStreamPosition();
        }

        public int getDuration(){
            if(mPlayback==null){return 0;}
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

        public void onPause() {
            if (mPlayback == null) {return;}
            mPlayback.pause();
        }

        public ExhibitBean getCurrentExhibit(){
            if(mPlayback==null){return null;}
            return mPlayback.getCurrentExhibit();
        }

        public void onStop(){
            handleStopRequest(null);
        }
        public void setPlayback(PlayChangeCallback playChangeCallback){
            setPlayChangeCallback(playChangeCallback);
        }
        public PlayChangeCallback getPlayback(){
            return getPlayChangeCallback();
        }

        public void onSkipToNext(){
            // TODO: 2016/6/27
        }
        public void onSkipToPrevious(){
            // TODO: 2016/6/27
        }
    }

}
