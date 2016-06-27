package com.systek.guide.service;

import com.systek.guide.entity.ExhibitBean;

/**
 * Created by Qiang on 2016/6/24.
 */
public interface Playback {
    public final static long PLAYBACK_POSITION_UNKNOWN = -1;
    int STATE_NONE = 0;
    int STATE_STOPPED = 1;
    int STATE_PAUSED = 2;
    int STATE_PLAYING = 3;

    int STATE_BUFFERING = 6;
    int STATE_ERROR = 7;
    long ACTION_PLAY = 1 << 2;
    long ACTION_PLAY_FROM_MEDIA_ID = 1 << 10;
    long ACTION_PLAY_FROM_SEARCH = 1 << 11;
    long ACTION_PAUSE = 1 << 1;
    long ACTION_SKIP_TO_PREVIOUS = 1 << 4;
    long ACTION_SKIP_TO_NEXT = 1 << 5;

    /**
     * Start/setup the playback.
     * Resources/listeners would be allocated by implementations.
     */
    void start();

    /**
     * Stop the playback. All resources can be de-allocated by implementations here.
     * @param notifyListeners if true and a callback has been set by setCallback,
     *                        callback.onPlaybackStatusChanged will be called after changing
     *                        the state.
     */
    void stop(boolean notifyListeners);

    /**
     * Set the latest playback state as determined by the caller.
     */
    void setState(int state);

    /**
     * 设置播放模式
     */
    void setMode(int mode);
    /**
     * 设置播放模式
     */
    int getMode();

    /**
     * Get the current {@link android.media.session.PlaybackState#getState()}
     */
    int getState();

    /**
     * @return boolean that indicates that this is ready to be used.
     */
    boolean isConnected();

    /**
     * @return boolean indicating whether the player is playing or is supposed to be
     * playing when we gain audio focus.
     */
    boolean isPlaying();

    /**
     * @return pos if currently playing an item
     */
    int getCurrentStreamPosition();
    /**
     * @return duration if currently playing an item
     */
    int getDuration();


    ExhibitBean getCurrentExhibit();

    /**
     * Set the current position. Typically used when switching players that are in
     * paused state.
     *
     * @param pos position in the stream
     */
    void setCurrentStreamPosition(int pos);

    /**
     * Query the underlying stream and update the internal last known stream position.
     */
    void updateLastKnownStreamPosition();

    /**
     * @param item to play
     */
    void play(ExhibitBean item);

    /**
     * Pause the current playing item
     */
    void pause();

    /**
     * Seek to the given position
     */
    void seekTo(int position);

    /**
     * Set the current mediaId. This is only used when switching from one
     * playback to another.
     *
     * @param exhibit to be set as the current.
     */
    void setCurrentMediaId(ExhibitBean exhibit);


    interface Callback {
        /**
         * On current music completed.
         */
        void onCompletion();
        /**
         * on Playback status changed
         * Implementations can use this callback to update
         * playback state on the media sessions.
         */
        void onPlaybackStatusChanged(int state);

        /**
         * @param error to be added to the PlaybackState
         */
        void onError(String error);

        /**
         * @param exhibit being currently played
         */
        void onMetadataChanged(ExhibitBean exhibit);
    }

    /**
     * @param callback to be called
     */
    void setCallback(Callback callback);
}
