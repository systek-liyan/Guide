package com.systekcn.guide.manager;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.systekcn.guide.common.IConstants;
import com.systekcn.guide.service.MediaPlayService;

/**
 * Created by Qiang on 2015/10/29.
 */
public class MediaServiceManager implements IConstants {

    private Context mContext;
    private ServiceConnection mConn;
    public MediaPlayService.MediaServiceBinder mediaServiceBinder;

    public MediaServiceManager(Context context) {
        this.mContext = context;
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

    public void connectService() {
        Intent intent = new Intent(mContext, MediaPlayService.class);
        mContext.bindService(intent, mConn, Context.BIND_AUTO_CREATE);
    }

    public void disConnectService() {
        if (mediaServiceBinder != null) {
            mContext.unbindService(mConn);
            mContext.stopService(new Intent(mContext, MediaPlayService.class));
        }
    }

    public int getCurrentPosition(){
        if (mediaServiceBinder != null) {
            return mediaServiceBinder.getCurrentPosition();
        }
        return 0;
    }

    public void stop() {
        mediaServiceBinder.stopPlay();
    }

    public boolean isPlaying() {
        return mediaServiceBinder.isPlaying();
    }

    public boolean pause() {
        if (mediaServiceBinder != null) {
            mediaServiceBinder.pause();
        }
        return false;
    }

    public boolean next() {
        if (mediaServiceBinder != null) {
            // return mediaPlayService.next();
        }
        return false;
    }
    public void seekTo(int progress) {
        if (mediaServiceBinder != null) {
            mediaServiceBinder.seekTo(progress);
        }
    }

    public void toContinue(){
        mediaServiceBinder.toContinue();
    }

    public void  notifyAllDataChange(){
        mediaServiceBinder.notifyAllDataChange();
    }

    private class PlayCtrlReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO: 2015/12/25  
        }
    }
    /*public List<ExhibitBean> getExhibitList() {
        List<ExhibitBean> exhibitList = new ArrayList<ExhibitBean>();
        if (mediaServiceBinder != null) {
            //mediaServiceBinder.getExhibitList(exhibitList);
        }
        return exhibitList;
    }

    public void refreshMusicList(List<ExhibitBean> exhibitList) {
        if (exhibitList != null && mediaServiceBinder != null) {
            try {
                mediaServiceBinder.refreshExhibitList(exhibitList);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public boolean play() {
        if (mediaServiceBinder != null) {
            mediaServiceBinder.startPlay();
            return true;
        }
        return false;
    }

    public boolean rePlay() {
        if (mediaServiceBinder != null) {
            mediaServiceBinder.rePlay();
            return true;
        }
        return false;
    }*/


    /*public boolean prev() {
        if (mediaServiceBinder != null) {
            //  return mediaPlayService.prev();
        }
        return false;
    }*/


   /* public int position() {
        if (mediaServiceBinder != null) {
            //return mediaServiceBinder.position();
        }
        return 0;
    }*/

    /*public int duration() {
        if (mediaServiceBinder != null) {
            return mediaServiceBinder.getDuration();
        }
        return 0;
    }*/

    /*  public void reset(){
          mediaServiceBinder.reset();
      }*/

    /*public int getPlayState() {
        if (mediaServiceBinder != null) {
            // int mode = mediaServiceBinder.getPlayState();
            // return mediaServiceBinder.getPlayState();
        }
        return 0;
    }

    public void setPlayMode(int mode) {
        if (mediaServiceBinder != null) {
            //    mediaServiceBinder.setPlayMode(mode);
        }
    }

    public int getPlayMode() {
        if (mediaServiceBinder != null) {
            //   return mediaPlayService.getPlayMode();
        }
        return 0;
    }

    public int getCurMusicId() {
        if (mediaServiceBinder != null) {
            //   return mediaServiceBinder.getCurMusicId();
        }
        return -1;
    }*/
   /* public ExhibitBean getCurMusic() {
        if (mediaServiceBinder != null) {
            //   return mediaServiceBinder.getCurMusic();
        }
        return null;
    }

    public void sendBroadcast() {
        if (mediaServiceBinder != null) {
            //     mediaServiceBinder.sendPlayStateBrocast();
        }
    }

    public void exit() {
        mContext.unbindService(mConn);
        mContext.stopService(new Intent(SERVICE_NAME));
    }*/

}
