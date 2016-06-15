package com.systek.guide.activity;

import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.systek.guide.IConstants;
import com.systek.guide.R;
import com.systek.guide.adapter.NearlyGalleryAdapter;
import com.systek.guide.biz.MyBeaconTask;
import com.systek.guide.callback.BeaconChangeCallback;
import com.systek.guide.callback.PlayChangeCallback;
import com.systek.guide.entity.BeaconBean;
import com.systek.guide.entity.ExhibitBean;
import com.systek.guide.manager.MediaServiceManager;
import com.systek.guide.utils.ExceptionUtil;
import com.systek.guide.utils.ImageUtil;
import com.systek.guide.utils.TimeUtil;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class LockScreenActivity extends SwipeBackActivity implements BeaconConsumer,IConstants{


    private ImageView fullscreenImage;
    private ImageView ivPlayCtrl;
    private int currentDuration;
    private int currentProgress;
    private TextView tvPlayTime;
    private TextView tvTotalTime;
    private NearlyGalleryAdapter nearlyGalleryAdapter;
    private ExhibitBean currentExhibit;
    private List<ExhibitBean> currentExhibitList;
    private SeekBar seekBarProgress;
    private TextView tvExhibitName;
    private AnimationDrawable animArrowDrawable;

    private static final int MSG_WHAT_CHANGE_EXHIBIT=1;
    private static final int MSG_WHAT_UPDATE_PROGRESS=2;
    private static final int MSG_WHAT_REFRESH_STATE=3;
    private static final int MSG_WHAT_CHANGE_EXHIBIT_LIST=4;

    private BeaconManager beaconManager;
    private ThreadPoolExecutor executor;
    private long t;
    private RecyclerView recycleNearly;


    @Override
    public void onBeaconServiceConnect() {
        beaconManager.setRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, org.altbeacon.beacon.Region region) {

                if(beacons==null||beacons.size()==0){return ; }
                executor.execute(new MyBeaconTask(beacons, new BeaconChangeCallback() {
                    @Override
                    public void getExhibits(List<ExhibitBean> exhibits) {

                        if(nearlyGalleryAdapter==null||exhibits==null){return;}
                        if(System.currentTimeMillis()-t<4000){
                            return;
                        }
                        t=System.currentTimeMillis();
                        currentExhibitList=exhibits;
                        handler.sendEmptyMessage(MSG_WHAT_CHANGE_EXHIBIT_LIST);

                    }

                    @Override
                    public void getNearestExhibit(final ExhibitBean exhibit) {

                        if(exhibit==null){return;}
                        MediaServiceManager mediaServiceManager=MediaServiceManager.getInstance(getActivity());
                        if(mediaServiceManager.getPlayMode()==MediaServiceManager.PLAY_MODE_AUTO&&!mediaServiceManager.isPause()){
                            if(currentExhibit!=null&&exhibit.equals(currentExhibit)){
                                /*SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
                                LogUtil.i("ZHANG","equals= "+df.format(new Date()));*/
                                return;}
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    MediaServiceManager.getInstance(getActivity()).notifyExhibitChange(exhibit);
                                }
                            });
                        }
                    }


                    @Override
                    public void getNearestBeacon(BeaconBean bean) {

                    }

                }));
            }
        });
        try {
            beaconManager.startRangingBeaconsInRegion(new Region(BEACON_LAYOUT, null, null, null));
        } catch (RemoteException e) {
            ExceptionUtil.handleException(e);
        }
    }

    static class MyHandler extends Handler {

        WeakReference<LockScreenActivity> activityWeakReference;
        MyHandler(LockScreenActivity activity){
            this.activityWeakReference=new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {

            if(activityWeakReference==null){return;}
            LockScreenActivity activity=activityWeakReference.get();
            if(activity==null){return;}
            switch (msg.what){
                case MSG_WHAT_CHANGE_EXHIBIT:
                    activity.refreshExhibit();
                    break;
                case MSG_WHAT_UPDATE_PROGRESS:
                    activity.refreshProgress();
                    break;
                case MSG_WHAT_REFRESH_STATE:
                    activity.refreshState();
                    break;
                case MSG_WHAT_CHANGE_EXHIBIT_LIST:
                    activity.refreshExhibitList();
                    break;
                default:break;
            }
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        toggleHideBar();
        setContentView(R.layout.activity_lock_screen);
        executor= (ThreadPoolExecutor) Executors.newFixedThreadPool(3);
        beaconManager = BeaconManager.getInstanceForApplication(this);
        beaconManager.bind(this);
        handler=new MyHandler(this);
        initView();
        addListener();
        initData();

    }

    /**
     * 隐藏状态栏
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public void toggleHideBar() {

        // BEGIN_INCLUDE (get_current_ui_flags)
        // The UI options currently enabled are represented by a bitfield.
        // getSystemUiVisibility() gives us that bitfield.
        int uiOptions = getWindow().getDecorView().getSystemUiVisibility();
        int newUiOptions = uiOptions;
        // END_INCLUDE (get_current_ui_flags)
        // BEGIN_INCLUDE (toggle_ui_flags)
        boolean isImmersiveModeEnabled = ((uiOptions | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY) == uiOptions);
        if (isImmersiveModeEnabled) {
            Log.i("123", "Turning immersive mode mode off. ");
        } else {
            Log.i("123", "Turning immersive mode mode on.");
        }

        // Navigation bar hiding:  Backwards compatible to ICS.
        if (Build.VERSION.SDK_INT >= 14) {
            newUiOptions ^= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        }

        // Status bar hiding: Backwards compatible to Jellybean
        if (Build.VERSION.SDK_INT >= 16) {
            newUiOptions ^= View.SYSTEM_UI_FLAG_FULLSCREEN;
        }

        // Immersive mode: Backward compatible to KitKat.
        // Note that this flag doesn't do anything by itself, it only augments the behavior
        // of HIDE_NAVIGATION and FLAG_FULLSCREEN.  For the purposes of this sample
        // all three flags are being toggled together.
        // Note that there are two immersive mode UI flags, one of which is referred to as "sticky".
        // Sticky immersive mode differs in that it makes the navigation and status bars
        // semi-transparent, and the UI flag does not get cleared when the user interacts with
        // the screen.
        if (Build.VERSION.SDK_INT >= 18) {
            newUiOptions ^= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        }

        getWindow().getDecorView().setSystemUiVisibility(newUiOptions);//上边状态栏和底部状态栏滑动都可以调出状态栏
        //getWindow().getDecorView().setSystemUiVisibility(4108);//这里的4108可防止从底部滑动调出底部导航栏
        //END_INCLUDE (set_ui_flags)
    }


    @Override
    public void onStateChanged(int state) {
        this.state=state;
        handler.sendEmptyMessage(MSG_WHAT_REFRESH_STATE);// TODO: 2016/5/26
    }

    @Override
    public void onExhibitChanged(ExhibitBean exhibit) {
        if(currentExhibit==null||!currentExhibit.equals(exhibit)){
            currentExhibit=exhibit;
            handler.sendEmptyMessage(MSG_WHAT_CHANGE_EXHIBIT);
        }
    }

    @Override
    public void onPositionChanged(int duration, int position) {
        super.onPositionChanged(duration, position);
        this.currentDuration=duration;
        this.currentProgress=position;
        handler.sendEmptyMessage(MSG_WHAT_UPDATE_PROGRESS);

    }


    protected void initView() {
        fullscreenImage = (ImageView) findViewById(R.id.fullscreenImage);
        ivPlayCtrl = (ImageView) findViewById(R.id.ivPlayCtrl);
        tvPlayTime = (TextView) findViewById(R.id.tvPlayTime);
        tvTotalTime = (TextView) findViewById(R.id.tvTotalTime);
        TextView tvLockTime = (TextView) findViewById(R.id.tvLockTime);
        tvExhibitName = (TextView) findViewById(R.id.tvExhibitName);
        if (tvLockTime != null) {
            tvLockTime.setText(TimeUtil.getTime());
        }

        seekBarProgress=(SeekBar)findViewById(R.id.seekBarProgress);
        recycleNearly = (RecyclerView) findViewById(R.id.recycleNearly);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        if (recycleNearly != null) {
            recycleNearly.setLayoutManager(linearLayoutManager);
        }

        List<ExhibitBean> nearlyExhibitList = new ArrayList<>();
        nearlyGalleryAdapter = new NearlyGalleryAdapter(getActivity(), nearlyExhibitList);
        if (recycleNearly != null) {
            recycleNearly.setAdapter(nearlyGalleryAdapter);
            recycleNearly.setOverScrollMode(ScrollView.OVER_SCROLL_NEVER);
        }

        ImageView imgView_getup_arrow = (ImageView) findViewById(R.id.getup_arrow);
        animArrowDrawable = (AnimationDrawable) (imgView_getup_arrow != null ? imgView_getup_arrow.getBackground() : null);

    }

    private void initIcon() {
        if(currentExhibit==null){return;}
        String currentIconUrl = currentExhibit.getIconurl();
        ImageUtil.displayImage(currentIconUrl, fullscreenImage);
    }


    @Override
    protected void onResume() {
        super.onResume();
        if(MediaServiceManager.getInstance(this).isPlaying()){
            //state=PLAY_STATE_START;
            state= PlayChangeCallback.STATE_PLAYING;
        }else{
            //state=PLAY_STATE_STOP;
            state= PlayChangeCallback.STATE_STOP;
        }
        refreshState();
        handler.postDelayed(animationDrawableTask, 300);
        if (beaconManager.isBound(this)) beaconManager.setBackgroundMode(false);
        MediaServiceManager.getInstance(this).setStateChangeCallback(this);
    }

    //通过延时控制当前绘制bitmap的位置坐标
    private Runnable animationDrawableTask = new Runnable(){

        public void run(){
            animArrowDrawable.start();
            handler.postDelayed(animationDrawableTask, 300);
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        animArrowDrawable.stop();
        if (beaconManager.isBound(this)) beaconManager.setBackgroundMode(true);
        MediaServiceManager.getInstance(this).removeStateChangeCallback();

    }


    /*//屏蔽掉Home键
    public void onAttachedToWindow() {
        this.getWindow().setType(WindowManager.LayoutParams.TYPE_KEYGUARD_DIALOG);
        super.onAttachedToWindow();
    }*/
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return event.getKeyCode() == KeyEvent.KEYCODE_BACK || super.onKeyDown(keyCode, event);
    }


    private void addListener() {

        nearlyGalleryAdapter.setOnItemClickListener(new NearlyGalleryAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {

                ExhibitBean clickExhibit = nearlyGalleryAdapter.getEntity(position);
                ExhibitBean bean = MediaServiceManager.getInstance(getActivity()).getCurrentExhibit();

                if(bean==null||!bean.equals(clickExhibit)){
                    nearlyGalleryAdapter.setSelectIndex(clickExhibit);
                }
                MediaServiceManager.getInstance(getActivity()).setPlayMode(PLAY_MODE_HAND);
                nearlyGalleryAdapter.notifyDataSetChanged();

                if(bean==null||!bean.equals(clickExhibit)){
                    MediaServiceManager.getInstance(getActivity()).setPlayMode(PLAY_MODE_HAND);
                    nearlyGalleryAdapter.notifyDataSetChanged();
                    MediaServiceManager.getInstance(getActivity()).notifyExhibitChange(clickExhibit);
                }

            }
        });

        ivPlayCtrl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MediaServiceManager.getInstance(getActivity()).onStateChange();

            }
        });
        seekBarProgress.setOnSeekBarChangeListener(onSeekBarChangeListener);



    }


    private void initData() {
        new Thread(){
            @Override
            public void run() {

                Intent intent=getIntent();
                String exhibitStr=intent.getStringExtra(INTENT_EXHIBIT);
                if(!TextUtils.isEmpty(exhibitStr)){
                    ExhibitBean bean= JSON.parseObject(exhibitStr, ExhibitBean.class);
                    if(currentExhibit==null) {
                        currentExhibit = bean;
                    }
                }else{
                    currentExhibit=MediaServiceManager.getInstance(getActivity()).getCurrentExhibit();
                }
                handler.sendEmptyMessage(MSG_WHAT_CHANGE_EXHIBIT);
            }
        }.start();

    }

    private void refreshExhibitList() {
        if(nearlyGalleryAdapter!=null&&currentExhibitList!=null){
            nearlyGalleryAdapter.updateData(currentExhibitList);
        }
    }

    private void refreshExhibit() {
        if(currentExhibit==null){return;}
        //String currentMuseumId = currentExhibit.getMuseumId();
        tvExhibitName.setText(currentExhibit.getName());
        initIcon();
    }

    private void refreshProgress() {
        seekBarProgress.setMax(currentDuration);
        seekBarProgress.setProgress(currentProgress);
        tvPlayTime.setText(TimeUtil.changeToTime(currentProgress).substring(3));
        tvTotalTime.setText(TimeUtil.changeToTime(currentDuration).substring(3));
    }


    private void refreshState() {

        if(state == PlayChangeCallback.STATE_PLAYING){//state==PLAY_STATE_START
            ivPlayCtrl.setImageDrawable(getResources().getDrawable(R.drawable.uamp_ic_pause_white_48dp));//iv_play_state_open_big,ic_pause_black_36dp
        }else{
            ivPlayCtrl.setImageDrawable(getResources().getDrawable(R.drawable.uamp_ic_play_arrow_white_48dp));
        }
    }

    SeekBar.OnSeekBarChangeListener onSeekBarChangeListener=new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if(!fromUser){return;}
            MediaServiceManager.getInstance(getActivity()).seekTo(progress);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };

}