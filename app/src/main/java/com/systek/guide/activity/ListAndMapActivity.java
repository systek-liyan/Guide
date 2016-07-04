package com.systek.guide.activity;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.systek.guide.R;
import com.systek.guide.biz.MyBeaconTask;
import com.systek.guide.callback.BeaconChangeCallback;
import com.systek.guide.entity.BeaconBean;
import com.systek.guide.entity.ExhibitBean;
import com.systek.guide.fragment.ExhibitListFragment;
import com.systek.guide.fragment.MapFragment;
import com.systek.guide.service.PlayManager;
import com.systek.guide.service.Playback;
import com.systek.guide.utils.ExceptionUtil;
import com.systek.guide.utils.ImageUtil;
import com.systek.guide.utils.LogUtil;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 附近展品列表和地图activity
 */
public class ListAndMapActivity extends BaseActivity
        implements ExhibitListFragment.OnFragmentInteractionListener, BeaconConsumer {

    private RadioButton radioButtonList;
    private RadioButton radioButtonMap;
    private RadioGroup radioGroupTitle;
    private ExhibitListFragment exhibitListFragment;
    private MapFragment mapFragment;
    private ExhibitBean currentExhibit;
    private int currentProgress;
    private int currentDuration;
    private SeekBar seekBarProgress;
    private TextView exhibitName;
    private ImageView exhibitIcon;
    private ImageView ivPlayCtrl;
    private ImageView ivGuideMode;
    private TextView tvToast;
    private ThreadPoolExecutor executor ;
    private final static int  MSG_WHAT_UPDATE_PROGRESS=1;
    private final static int  MSG_WHAT_REFRESH_STATE=2;
    private final static int  MSG_WHAT_CHANGE_EXHIBIT=4;

    private BeaconManager beaconManager;
    long t=System.currentTimeMillis();
    @Override
    public void onBeaconServiceConnect() {

        beaconManager.setRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, org.altbeacon.beacon.Region region) {

                if(beacons==null||beacons.size()==0){return ; }
                executor.execute(new MyBeaconTask(beacons, new BeaconChangeCallback() {
                    @Override
                    public void getExhibits(List<ExhibitBean> exhibits) {

                        if(exhibitListFragment==null){return;}
                        if(exhibitListFragment.getCurrentExhibitList()==null||exhibitListFragment.getCurrentExhibitList().size()==0){
                            refreshFragmentExhibit(exhibits);
                        }else{
                            if(System.currentTimeMillis()-t<4000){return;}
                            t=System.currentTimeMillis();
                            refreshFragmentExhibit(exhibits);
                        }
                    }

                    @Override
                    public void getNearestExhibit(final ExhibitBean exhibit) {

                        if(exhibitListFragment==null||exhibit==null){return;}
                        if(PlayManager.getInstance().getPlayMode()==PLAY_MODE_AUTO&&PlayManager.getInstance().isPlaying()){//TODO: 2016/6/30 isPlaying?
                            if(currentExhibit!=null&&exhibit.equals(currentExhibit)){
                                    /*SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
                                    LogUtil.i("ZHANG","equals= "+df.format(new Date()));*/
                                return;}
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    PlayManager.getInstance().playFromBean(exhibit);
                                }
                            });
                        }
                    }

                    @Override
                    public void getNearestBeacon(BeaconBean bean) {
                        if(mapFragment==null||bean==null||mapFragment.getHandler()==null){return;}
                        mapFragment.setBeacon(bean);
                        mapFragment.getHandler().sendEmptyMessage(MapFragment.MSG_WHAT_DRAW_POINT);
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

    private void refreshFragmentExhibit(List<ExhibitBean> exhibits) {

        if(exhibitListFragment==null||exhibits==null||exhibitListFragment.getHandler()==null){return;}
        exhibitListFragment.setCurrentExhibitList(exhibits);
        exhibitListFragment.getHandler().sendEmptyMessage(ExhibitListFragment.MSG_WHAT_UPDATE_DATA_SUCCESS);
    }


    @Override
    public void onStateChanged(final int state) {
        super.onStateChanged(state);
        this.state=state;
        handler.sendEmptyMessage(MSG_WHAT_REFRESH_STATE);// TODO: 2016/5/26

    }

    @Override
    public void onExhibitChanged(ExhibitBean exhibit) {

        if(currentExhibit==null||!currentExhibit.equals(exhibit)){
            currentExhibit=exhibit;
            handler.sendEmptyMessage(MSG_WHAT_CHANGE_EXHIBIT);
        }else{
            LogUtil.i("ZHANG","onExhibitChanged : exhibit "+exhibit.equals(currentExhibit) );
        }

    }

    @Override
    public void onPositionChanged(int duration, int position) {
        super.onPositionChanged(duration, position);
        this.currentDuration=duration;
        this.currentProgress=position;
        handler.sendEmptyMessage(MSG_WHAT_UPDATE_PROGRESS);

    }


    static class MyHandler extends Handler {

        WeakReference<ListAndMapActivity> activityWeakReference;
        MyHandler(ListAndMapActivity activity){
            this.activityWeakReference=new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {

            if(activityWeakReference==null){return;}
            ListAndMapActivity activity=activityWeakReference.get();
            if(activity==null){return;}
            switch (msg.what){
                case MSG_WHAT_UPDATE_PROGRESS:
                    activity.refreshProgress();
                    break;
                case MSG_WHAT_REFRESH_STATE:
                    activity.refreshState();
                    break;
                case MSG_WHAT_CHANGE_EXHIBIT:
                    activity.refreshViewBottomTab();
                    break;

                default:break;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        switch (theme){
            case R.style.AppTheme:
                setContentView(R.layout.activity_list_and_map);
                break;
            case R.style.BlueAppTheme:
                setContentView(R.layout.activity_list_and_map_blue);
                break;
        }
        beaconManager = BeaconManager.getInstanceForApplication(this);
        executor= (ThreadPoolExecutor) Executors.newFixedThreadPool(4);
        PlayManager.getInstance().bindToService(this,this);
        //绑定蓝牙扫描服务
        beaconManager.bind(this);
        //加载播放器
        handler=new MyHandler(this);
        initView();
        addListener();
        initData();
        refreshModeIcon();
    }

    @Override
    protected void onStart() {
        super.onStart();
        ExhibitBean exhibitBean=PlayManager.getInstance().getCurrentExhibit();
        if(exhibitBean!=null){
            currentExhibit=exhibitBean;
            exhibitName.setText(currentExhibit.getName());
            refreshViewBottomTab();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshState();
        registerReceiver();
        if (beaconManager.isBound(this)) beaconManager.setBackgroundMode(false);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unRegisterReceiver();
        if (beaconManager.isBound(this)) beaconManager.setBackgroundMode(true);
    }

    @Override
    protected void onDestroy() {
        exhibitListFragment=null;
        mapFragment=null;
        super.onDestroy();
        PlayManager.getInstance().unbindService(this,this);
        beaconManager.unbind(this);
    }

    /**
     * 注册广播
     */
    private void registerReceiver() {
        registerBluetoothReceiver();
    }


    private void unRegisterReceiver() {
        unRegisterBluetoothReceiver();
    }

    /**
     * 加载播放器

     private void initMediaManager() {
     mediaServiceManager= MediaServiceManager.getInstance(this);
     }*/

    private void refreshViewBottomTab() {
        if(currentExhibit==null||exhibitName==null||exhibitIcon==null){return;}
        exhibitName.setText(currentExhibit.getName());
        String iconPath=currentExhibit.getIconurl();
        ImageUtil.displayImage(iconPath, exhibitIcon, currentExhibit.getMuseumId(),true,false);
    }

    private void refreshProgress() {
        if(seekBarProgress!=null&&currentDuration>=0&&currentProgress>=0){
            seekBarProgress.setMax(currentDuration);
            seekBarProgress.setProgress(currentProgress);
        }
    }


    private void refreshState() {
        switch (PlayManager.getInstance().getPlayMode()){
            case PLAY_MODE_AUTO:
                ivGuideMode.setBackgroundResource(R.drawable.play_mode_auto);
                break;
            case PLAY_MODE_HAND:
                ivGuideMode.setBackgroundResource(R.drawable.play_mode_hand);
                break;
            case PLAY_MODE_AUTO_PAUSE:
                ivGuideMode.setBackgroundResource(R.drawable.play_auto_pause);
                break;
        }
        //}
        state=PlayManager.getInstance().getState();
        if(state== Playback.STATE_PLAYING||state==Playback.STATE_BUFFERING) {
            ivPlayCtrl.setImageDrawable(getResources().getDrawable(R.drawable.uamp_ic_pause_white_24dp));
        }else{
            ivPlayCtrl.setImageDrawable(getResources().getDrawable(R.drawable.uamp_ic_play_arrow_white_24dp));
        }

    }


    private void addListener() {
        radioGroupTitle.setOnCheckedChangeListener(radioButtonCheckListener);
        ivPlayCtrl.setOnClickListener(onClickListener);
        seekBarProgress.setOnSeekBarChangeListener(onSeekBarChangeListener);
        exhibitIcon.setOnClickListener(onClickListener);
        ivGuideMode.setOnClickListener(onClickListener);
    }

    private void initData() {
        //设置默认fragment
        setDefaultFragment();

        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter != null) {
            if (!mBluetoothAdapter.isEnabled()) {
                mBluetoothAdapter.enable();
            }
        }
    }

    private void initView() {
        setMyTitleBar();
        setHomeIcon();
        toolbar.setNavigationOnClickListener(backOnClickListener);
        radioButtonList=(RadioButton)findViewById(R.id.radioButtonList);
        radioButtonMap=(RadioButton)findViewById(R.id.radioButtonMap);
        radioGroupTitle=(RadioGroup)findViewById(R.id.radioGroupTitle);
        seekBarProgress=(SeekBar)findViewById(R.id.seekBarProgress);
        exhibitName=(TextView)findViewById(R.id.exhibitName);
        exhibitIcon=(ImageView)findViewById(R.id.exhibitIcon);
        ivPlayCtrl=(ImageView)findViewById(R.id.ivPlayCtrl);
        ivGuideMode=(ImageView)findViewById(R.id.ivGuideMode);
        tvToast=(TextView)findViewById(R.id.tvToast);
    }

    private void setMyTitleBar() {
        View v = findViewById(R.id.toolbar_radio);
        if (v != null) {
            toolbar = (Toolbar) v;
            setSupportActionBar(toolbar);
            if(theme==R.style.AppTheme){
                toolbar.setBackgroundColor(getResources().getColor(R.color.my_own_red_300));
            }else{
                toolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimaryBlue));
            }
            ActionBar actionBar= getSupportActionBar();
            if(actionBar==null){ return;}
            actionBar.setDisplayShowTitleEnabled(false);
        }
    }


    /**
     * 根据状态切换模式图标
     */
    private void refreshModeIcon() {
        switch (PlayManager.getInstance().getPlayMode()){
            case PLAY_MODE_AUTO:
                ivGuideMode.setBackgroundResource(R.drawable.play_mode_auto);
                break;
            case PLAY_MODE_HAND:
                ivGuideMode.setBackgroundResource(R.drawable.play_mode_hand);
                break;
            case PLAY_MODE_AUTO_PAUSE:
                ivGuideMode.setBackgroundResource(R.drawable.play_auto_pause);
                break;
        }
        //}
    }

    /**
     * 点击监听器
     */
    View.OnClickListener onClickListener=new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.ivPlayCtrl:
                    if(PlayManager.getInstance().isPlaying()){
                        PlayManager.getInstance().pause();
                    }else{
                        PlayManager.getInstance().play();
                    }
                    break;
                case R.id.exhibitIcon:
                    Intent intent1=new Intent(ListAndMapActivity.this,PlayActivity.class);
                    startActivity(intent1);
                    finish();
                    break;
                case R.id.ivGuideMode:
                    tvToast.setVisibility(View.VISIBLE);
                    int  mode = PlayManager.getInstance().getPlayMode();
                    switch (mode){
                        case PLAY_MODE_AUTO:
                            PlayManager.getInstance().setPlayMode(PLAY_MODE_HAND);
                            tvToast.setText("手动模式");
                            break;
                        case PLAY_MODE_HAND:
                            PlayManager.getInstance().setPlayMode(PLAY_MODE_AUTO);
                            tvToast.setText("自动模式");
                            break;
                        case PLAY_MODE_AUTO_PAUSE:
                            PlayManager.getInstance().setPlayMode(PLAY_MODE_AUTO);
                            break;
                    }
                    refreshModeIcon();
                    new Thread(){
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(1500);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    tvToast.setVisibility(View.GONE);
                                }
                            });
                        }
                    }.start();
                    break;

                case R.id.titleBarDrawer:
                    finish();
                    break;
                default:break;
            }
        }
    };

    /**
     * SeekBar 进度改变监听器
     */
    SeekBar.OnSeekBarChangeListener onSeekBarChangeListener=new SeekBar.OnSeekBarChangeListener() {

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if(!fromUser){return;}
            PlayManager.getInstance().seekTo(progress);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };

    /**
     * 设置默认fragment
     */
    private void setDefaultFragment() {
        String flag=getIntent().getStringExtra(INTENT_FLAG_GUIDE_MAP);
        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
        android.support.v4.app.FragmentTransaction transaction = fm.beginTransaction();
        exhibitListFragment = ExhibitListFragment.newInstance();
        mapFragment = MapFragment.newInstance();
        if (flag.equals(INTENT_FLAG_GUIDE)){
            transaction.replace(R.id.llExhibitListContent, exhibitListFragment);
            radioButtonList.setChecked(true);
        }else{
            String exhibitListStr=getIntent().getStringExtra(INTENT_EXHIBIT_LIST_STR);
            if(!TextUtils.isEmpty(exhibitListStr)){
                List<ExhibitBean> topicExhibitList=JSON.parseArray(exhibitListStr,ExhibitBean.class);
                mapFragment.setTopicExhibitList(topicExhibitList);
            }
            transaction.replace(R.id.llExhibitListContent, mapFragment);
            radioButtonMap.setChecked(true);
        }
        transaction.commit();
    }

    /**
     * RadioButton 监听器
     */
    private RadioGroup.OnCheckedChangeListener radioButtonCheckListener=new RadioGroup.OnCheckedChangeListener(){

        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {

            android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
            // 开启Fragment事务
            android.support.v4.app.FragmentTransaction transaction = fm.beginTransaction();
            switch(checkedId){
                case R.id.radioButtonList:
                    if (exhibitListFragment == null) {
                        exhibitListFragment = ExhibitListFragment.newInstance();
                    }
                    // 使用当前Fragment的布局替代id_content的控件
                    transaction.replace(R.id.llExhibitListContent, exhibitListFragment);
                    break;
                case R.id.radioButtonMap:
                    if (mapFragment == null) {
                        mapFragment = MapFragment.newInstance();
                    }
                    transaction.replace(R.id.llExhibitListContent, mapFragment);
                    break;
            }
            // 事务提交
            transaction.commit();
        }
    };


    /**
     * 回调方法，用于反给activity数据
     * @param bean 返回给activity展品对象
     */
    @Override
    public void onFragmentInteraction(ExhibitBean bean) {
        this.currentExhibit=bean;
        refreshViewBottomTab();
    }

}
