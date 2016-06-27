package com.systek.guide.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.session.PlaybackState;
import android.net.wifi.WifiManager;
import android.os.PowerManager;

import com.systek.guide.IConstants;
import com.systek.guide.callback.PlayChangeCallback;
import com.systek.guide.entity.ExhibitBean;
import com.systek.guide.utils.LogUtil;

import java.io.File;
import java.io.IOException;

/**
 * Created by Qiang on 2016/6/24.
 */
public class LocalPlayback implements Playback, IConstants,AudioManager.OnAudioFocusChangeListener,
        MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener, MediaPlayer.OnPreparedListener, MediaPlayer.OnSeekCompleteListener {

    private static final String TAG = LogUtil.makeLogTag(LocalPlayback.class);

    // The volume we set the media player to when we lose audio focus, but are
    // allowed to reduce the volume instead of stopping playback.
    public static final float VOLUME_DUCK = 0.2f;
    // The volume we set the media player when we have audio focus.
    public static final float VOLUME_NORMAL = 1.0f;

    // we don't have audio focus, and can't duck (play at a low volume)
    private static final int AUDIO_NO_FOCUS_NO_DUCK = 0;
    // we don't have focus, but can duck (play at a low volume)
    private static final int AUDIO_NO_FOCUS_CAN_DUCK = 1;
    // we have full audio focus
    private static final int AUDIO_FOCUSED  = 2;

    private final ExhibitPlayService mService;
    private final WifiManager.WifiLock mWifiLock;
    private int mState;
    private boolean mPlayOnFocusGain;
    private Callback mCallback;
   // private final MusicProvider mMusicProvider;
    private volatile boolean mAudioNoisyReceiverRegistered;
    private volatile int mCurrentPosition;
    private volatile ExhibitBean mCurrentExhibit;

    // Type of audio focus we have:
    private int mAudioFocus = AUDIO_NO_FOCUS_NO_DUCK;
    private final AudioManager mAudioManager;
    private MediaPlayer mMediaPlayer;

    public PlayChangeCallback getPlayChangeCallback() {
        return playChangeCallback;
    }

    public void setPlayChangeCallback(PlayChangeCallback playChangeCallback) {
        this.playChangeCallback = playChangeCallback;
    }

    private PlayChangeCallback playChangeCallback;


    private int playMode=PLAY_MODE_HAND ; //默认设置自动点击播放


    private final IntentFilter mAudioNoisyIntentFilter =
            new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);


    private final BroadcastReceiver mAudioNoisyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intent.getAction())) {
                if (isPlaying()) {
                    Intent i = new Intent(context, ExhibitPlayService.class);
                    i.setAction(ExhibitPlayService.ACTION_CMD);
                    i.putExtra(ExhibitPlayService.CMD_NAME, ExhibitPlayService.CMD_PAUSE);
                    mService.startService(i);
                }
            }
        }
    };

    public LocalPlayback(ExhibitPlayService service) {//, MusicProvider musicProvider
        this.mService = service;
        //this.mMusicProvider = musicProvider;
        this.mAudioManager = (AudioManager) service.getSystemService(Context.AUDIO_SERVICE);
        // Create the Wifi lock (this does not acquire the lock, this just creates it)
        this.mWifiLock = ((WifiManager) service.getSystemService(Context.WIFI_SERVICE))
                .createWifiLock(WifiManager.WIFI_MODE_FULL, "qiang_lock");
    }

    @Override
    public void start() {

    }

    @Override
    public void stop(boolean notifyListeners) {
        mState = STATE_STOPPED;
        if (notifyListeners && mCallback != null) {
            mCallback.onPlaybackStatusChanged(mState);
        }
        mCurrentPosition = getCurrentStreamPosition();
        // Give up Audio focus
        giveUpAudioFocus();
        unregisterAudioNoisyReceiver();
        // Relax all resources
        relaxResources(true);
        if (mWifiLock.isHeld()) {
            mWifiLock.release();
        }
    }


    @Override
    public void setState(int state) {
        this.mState = state;
    }

    @Override
    public void setMode(int mode) {
        this.playMode=mode;
    }

    @Override
    public int getMode() {
       return playMode;
    }

    @Override
    public int getState() {
        return mState;
    }

    @Override
    public boolean isConnected() {
        return true;
    }

    @Override
    public boolean isPlaying() {
        return mPlayOnFocusGain || (mMediaPlayer != null && mMediaPlayer.isPlaying());
    }

    @Override
    public int getCurrentStreamPosition() {
        return mMediaPlayer != null ?
                mMediaPlayer.getCurrentPosition() : mCurrentPosition;
    }

    @Override
    public int getDuration() {
        return  mMediaPlayer != null ?
                mMediaPlayer.getDuration() : 0;
    }

    @Override
    public ExhibitBean getCurrentExhibit() {
        return mCurrentExhibit;
    }

    @Override
    public void setCurrentStreamPosition(int pos) {
        this.mCurrentPosition = pos;
    }

    @Override
    public void updateLastKnownStreamPosition() {
        if (mMediaPlayer != null) {
            mCurrentPosition = mMediaPlayer.getCurrentPosition();
        }
    }

    @Override
    public void play(ExhibitBean item) {
        mPlayOnFocusGain = true;
        tryToGetAudioFocus();
        registerAudioNoisyReceiver();
        boolean mediaHasChanged = mCurrentExhibit==null||!mCurrentExhibit.equals(item);
        if (mediaHasChanged) {
            mCurrentPosition = 0;
            mCurrentExhibit = item;
        }

        if (mState == PlaybackState.STATE_PAUSED && !mediaHasChanged && mMediaPlayer != null) {
            configMediaPlayerState();
        } else {
            mState = STATE_STOPPED;
            relaxResources(false); // release everything except MediaPlayer
            try {
                createMediaPlayerIfNeeded();
                mState = STATE_BUFFERING;
                mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                String exURL = item.getAudiourl();
                String localName = exURL.replaceAll("/", "_");
                String localUrl = getCurrentAudioPath()+"/"+ localName;
                File file = new File(localUrl);
                if (file.exists()) {
                    mMediaPlayer.setDataSource(localUrl);
                } else {
                    String  url = BASE_URL + exURL;
                    mMediaPlayer.setDataSource(url);
                }

                // Starts preparing the media player in the background. When
                // it's done, it will call our OnPreparedListener (that is,
                // the onPrepared() method on this class, since we set the
                // listener to 'this'). Until the media player is prepared,
                // we *cannot* call start() on it!
                mMediaPlayer.prepareAsync();

                // If we are streaming from the internet, we want to hold a
                // Wifi lock, which prevents the Wifi radio from going to
                // sleep while the song is playing.
                mWifiLock.acquire();

                if (mCallback != null) {
                    mCallback.onPlaybackStatusChanged(mState);
                }

            } catch (IOException ex) {
                LogUtil.e(TAG, ex, "Exception playing song");
                if (mCallback != null) {
                    mCallback.onError(ex.getMessage());
                }
            }
        }
    }

    private String getCurrentAudioPath() {
        if(mCurrentExhibit==null){return null;}
            return LOCAL_ASSETS_PATH+mCurrentExhibit.getMuseumId()+"/";
    }

    /**
     * Try to get the system audio focus.
     */
    private void tryToGetAudioFocus() {
        LogUtil.d(TAG, "tryToGetAudioFocus");
        if (mAudioFocus != AUDIO_FOCUSED) {
            int result = mAudioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC,
                    AudioManager.AUDIOFOCUS_GAIN);
            if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                mAudioFocus = AUDIO_FOCUSED;
            }
        }
    }

    private void registerAudioNoisyReceiver() {
        if (!mAudioNoisyReceiverRegistered) {
            mService.registerReceiver(mAudioNoisyReceiver, mAudioNoisyIntentFilter);
            mAudioNoisyReceiverRegistered = true;
        }
    }

    @Override
    public void pause() {
        if (mState == PlaybackState.STATE_PLAYING) {
            // Pause media player and cancel the 'foreground service' state.
            if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
                mMediaPlayer.pause();
                mCurrentPosition = mMediaPlayer.getCurrentPosition();
            }
            // while paused, retain the MediaPlayer but give up audio focus
            relaxResources(false);
            giveUpAudioFocus();
        }
        mState = STATE_PAUSED;
        if (mCallback != null) {
            mCallback.onPlaybackStatusChanged(mState);
        }
        unregisterAudioNoisyReceiver();
    }

    @Override
    public void seekTo(int position) {
        LogUtil.d(TAG, "seekTo called with ", position);

        if (mMediaPlayer == null) {
            // If we do not have a current media player, simply update the current position
            mCurrentPosition = position;
        } else {
            if (mMediaPlayer.isPlaying()) {
                mState = STATE_BUFFERING;
            }
            mMediaPlayer.seekTo(position);
            if (mCallback != null) {
                mCallback.onPlaybackStatusChanged(mState);
            }
        }
    }

    @Override
    public void setCurrentMediaId(ExhibitBean exhibit) {
        this.mCurrentExhibit = exhibit;
    }

    @Override
    public void setCallback(Callback callback) {
        this.mCallback = callback;
    }


    /**
     * Give up the audio focus.
     */
    private void giveUpAudioFocus() {
        if (mAudioFocus == AUDIO_FOCUSED) {
            if (mAudioManager.abandonAudioFocus(this) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                mAudioFocus = AUDIO_NO_FOCUS_NO_DUCK;
            }
        }
    }

    private void unregisterAudioNoisyReceiver() {
        if (mAudioNoisyReceiverRegistered) {
            mService.unregisterReceiver(mAudioNoisyReceiver);
            mAudioNoisyReceiverRegistered = false;
        }
    }


    /**
     * Releases resources used by the service for playback. This includes the
     * "foreground service" status, the wake locks and possibly the MediaPlayer.
     *
     * @param releaseMediaPlayer Indicates whether the Media Player should also
     *            be released or not
     */
    private void relaxResources(boolean releaseMediaPlayer) {

        mService.stopForeground(true);

        // stop and release the Media Player, if it's available
        if (releaseMediaPlayer && mMediaPlayer != null) {
            mMediaPlayer.reset();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }

        // we can also release the Wifi lock, if we're holding it
        if (mWifiLock.isHeld()) {
            mWifiLock.release();
        }
    }

    /**
     * Makes sure the media player exists and has been reset. This will create
     * the media player if needed, or reset the existing media player if one
     * already exists.
     */
    private void createMediaPlayerIfNeeded() {
        LogUtil.d(TAG, "createMediaPlayerIfNeeded. needed? ", (mMediaPlayer==null));
        if (mMediaPlayer == null) {
            mMediaPlayer = new MediaPlayer();

            // Make sure the media player will acquire a wake-lock while
            // playing. If we don't do that, the CPU might go to sleep while the
            // song is playing, causing playback to stop.
            mMediaPlayer.setWakeMode(mService.getApplicationContext(),
                    PowerManager.PARTIAL_WAKE_LOCK);

            // we want the media player to notify us when it's ready preparing,
            // and when it's done playing:
            mMediaPlayer.setOnPreparedListener(this);
            mMediaPlayer.setOnCompletionListener(this);
            mMediaPlayer.setOnErrorListener(this);
            mMediaPlayer.setOnSeekCompleteListener(this);
        } else {
            mMediaPlayer.reset();
        }
    }


    @Override
    public void onAudioFocusChange(int focusChange) {
        if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
            // We have gained focus:
            mAudioFocus = AUDIO_FOCUSED;

        } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS ||
                focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT ||
                focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {
            // We have lost focus. If we can duck (low playback volume), we can keep playing.
            // Otherwise, we need to pause the playback.
            boolean canDuck = focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK;
            mAudioFocus = canDuck ? AUDIO_NO_FOCUS_CAN_DUCK : AUDIO_NO_FOCUS_NO_DUCK;

            // If we are playing, we need to reset media player by calling configMediaPlayerState
            // with mAudioFocus properly set.
            if (mState == PlaybackState.STATE_PLAYING && !canDuck) {
                // If we don't have audio focus and can't duck, we save the information that
                // we were playing, so that we can resume playback once we get the focus back.
                mPlayOnFocusGain = true;
            }
        } else {
            LogUtil.e(TAG, "onAudioFocusChange: Ignoring unsupported focusChange: ", focusChange);
        }
        configMediaPlayerState();
    }

    /**
     * Reconfigures MediaPlayer according to audio focus settings and
     * starts/restarts it. This method starts/restarts the MediaPlayer
     * respecting the current audio focus state. So if we have focus, it will
     * play normally; if we don't have focus, it will either leave the
     * MediaPlayer paused or set it to a low volume, depending on what is
     * allowed by the current focus settings. This method assumes mPlayer !=
     * null, so if you are calling it, you have to do so from a context where
     * you are sure this is the case.
     */
    private void configMediaPlayerState() {
        if (mAudioFocus == AUDIO_NO_FOCUS_NO_DUCK) {
            // If we don't have audio focus and can't duck, we have to pause,
            if (mState == PlaybackState.STATE_PLAYING) {
                pause();
            }
        } else {  // we have audio focus:
            if (mAudioFocus == AUDIO_NO_FOCUS_CAN_DUCK) {
                mMediaPlayer.setVolume(VOLUME_DUCK, VOLUME_DUCK); // we'll be relatively quiet
            } else {
                if (mMediaPlayer != null) {
                    mMediaPlayer.setVolume(VOLUME_NORMAL, VOLUME_NORMAL); // we can be loud again
                } // else do something for remote client.
            }
            // If we were playing when we lost focus, we need to resume playing.
            if (mPlayOnFocusGain) {
                if (mMediaPlayer != null && !mMediaPlayer.isPlaying()) {
                    if (mCurrentPosition == mMediaPlayer.getCurrentPosition()) {
                        mMediaPlayer.start();
                        mState = STATE_PLAYING;
                    } else {
                        mMediaPlayer.seekTo(mCurrentPosition);
                        mState = STATE_BUFFERING;
                    }
                }
                mPlayOnFocusGain = false;
            }
        }
        if (mCallback != null) {
            mCallback.onPlaybackStatusChanged(mState);
        }
    }

    /**
     * Called when media player is done playing current song.
     *
     * @see MediaPlayer.OnCompletionListener
     */
    @Override
    public void onCompletion(MediaPlayer mp) {
        if (mCallback != null) {
            mCallback.onCompletion();
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        LogUtil.e(TAG, "Media player error: what=" + what + ", extra=" + extra);
        if (mCallback != null) {
            mCallback.onError("MediaPlayer error " + what + " (" + extra + ")");
        }
        return true; // true indicates we handled the error
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        LogUtil.d(TAG, "onPrepared from MediaPlayer");
        // The media player is done preparing. That means we can start playing if we
        // have audio focus.
        configMediaPlayerState();
    }

    /**
     * Called when MediaPlayer has completed a seek
     *
     * @see MediaPlayer.OnSeekCompleteListener
     */
    @Override
    public void onSeekComplete(MediaPlayer mp) {
        LogUtil.d(TAG, "onSeekComplete from MediaPlayer:", mp.getCurrentPosition());
        mCurrentPosition = mp.getCurrentPosition();
        if (mState == PlaybackState.STATE_BUFFERING) {
            mMediaPlayer.start();
            mState = STATE_PLAYING;
        }
        if (mCallback != null) {
            mCallback.onPlaybackStatusChanged(mState);
        }
    }


}