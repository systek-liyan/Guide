package com.systek.guide.activity;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
import com.systek.guide.entity.ExhibitBean;
import com.systek.guide.fragment.ExhibitListFragment;
import com.systek.guide.fragment.MapFragment;
import com.systek.guide.manager.MediaServiceManager;
import com.systek.guide.utils.ImageLoaderUtil;
import com.systek.guide.utils.Tools;

import java.util.List;

/**
 * 附近展品列表和地图activity
 */
public class ListAndMapActivity extends BaseActivity implements ExhibitListFragment.OnFragmentInteractionListener{

    private RadioButton radioButtonList;
    private RadioButton radioButtonMap;
    private RadioGroup radioGroupTitle;
    private ExhibitListFragment exhibitListFragment;
    private MapFragment mapFragment;
    private ExhibitBean currentExhibit;
    private int currentProgress;
    private int currentDuration;
    private SeekBar seekBarProgress;
    private Handler handler;
    private PlayStateReceiver receiver;
    private String currentMuseumId;
    private TextView exhibitName;
    private ImageView exhibitIcon;
    private ImageView ivPlayCtrl;
    private MediaServiceManager mediaServiceManager;
    private ImageView ivGuideMode;

    private TextView tvToast;


    @Override
    protected void initialize(Bundle savedInstanceState) {
        setContentView(R.layout.activity_list_and_map);
        handler=new MyHandler();
        //加载播放器
        initMediaManager();
        //加载view
        initView();
        //添加监听器
        addListener();
        //设置默认fragment
        setDefaultFragment();
        //注册广播接收器
        registerReceiver();
    }

    /**
     * 加载播放器
     */
    private void initMediaManager() {
        mediaServiceManager= MediaServiceManager.getInstance(this);
    }

    /**
     * 注册广播
     */
    private void registerReceiver() {
        receiver=new PlayStateReceiver();
        IntentFilter filter=new IntentFilter();
        filter.addAction(INTENT_EXHIBIT_PROGRESS);
        filter.addAction(INTENT_EXHIBIT_DURATION);
        filter.addAction(INTENT_CHANGE_PLAY_PLAY);
        filter.addAction(INTENT_CHANGE_PLAY_STOP);
        filter.addAction(INTENT_EXHIBIT);
        registerReceiver(receiver, filter);
    }

    /**
     * 添加监听器
     */
    private void addListener() {
        radioGroupTitle.setOnCheckedChangeListener(radioButtonCheckListener);
        ivPlayCtrl.setOnClickListener(onClickListener);
        seekBarProgress.setOnSeekBarChangeListener(onSeekBarChangeListener);
        exhibitIcon.setOnClickListener(onClickListener);
        ivGuideMode.setOnClickListener(onClickListener);
    }

    /**
     * 加载视图
     */
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
            ActionBar actionBar= getSupportActionBar();
            if(actionBar==null){ return;}
            actionBar.setDisplayShowTitleEnabled(false);
        }
    }


    /**
     * 根据状态切换模式图标
     */
    private void refreshModeIcon() {
        switch (mediaServiceManager.getPlayMode()){
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
    }

    /**
     * 点击监听器
     */
    View.OnClickListener onClickListener=new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.ivPlayCtrl:
                    Intent intent=new Intent();
                    intent.setAction(INTENT_CHANGE_PLAY_STATE);
                    sendBroadcast(intent);
                    break;
                case R.id.exhibitIcon:
                    Intent intent1=new Intent(ListAndMapActivity.this,PlayActivity.class);
                    startActivity(intent1);
                    break;

                case R.id.ivGuideMode:
                    tvToast.setVisibility(View.VISIBLE);
                    int  mode = mediaServiceManager.getPlayMode();
                    switch (mode){
                        case PLAY_MODE_AUTO:
                            mediaServiceManager.setPlayMode(PLAY_MODE_HAND);
                            tvToast.setText("手动模式");
                            break;
                        case PLAY_MODE_HAND:
                            mediaServiceManager.setPlayMode(PLAY_MODE_AUTO);
                            tvToast.setText("自动模式");
                            break;
                        case PLAY_MODE_AUTO_PAUSE:
                            mediaServiceManager.setPlayMode(PLAY_MODE_AUTO_PAUSE);
                            break;
                    }
                    refreshModeIcon();
                    new Thread(){
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(500);
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
            Intent intent=new Intent();
            intent.setAction(INTENT_SEEK_BAR_CHANG);
            intent.putExtra(INTENT_SEEK_BAR_CHANG,progress);
            sendBroadcast(intent);
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
        FragmentManager fm = getFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
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

            FragmentManager fm = getFragmentManager();
            // 开启Fragment事务
            FragmentTransaction transaction = fm.beginTransaction();
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

    @Override
    protected void onResume() {
        super.onResume();
        ExhibitBean exhibitBean=mediaServiceManager.getCurrentExhibit();
        if(exhibitBean!=null){
            exhibitName.setText(exhibitBean.getName());
            refreshBottomTab(exhibitBean);
        }

        if(mediaServiceManager.isPlaying()){
            handler.sendEmptyMessage(MSG_WHAT_CHANGE_PLAY_START);
        }else{
            handler.sendEmptyMessage(MSG_WHAT_CHANGE_PLAY_STOP);
        }
        refreshModeIcon();
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(receiver);
        handler.removeCallbacksAndMessages(null);
        exhibitListFragment=null;
        mapFragment=null;
        super.onDestroy();
    }


    /**
     * 回调方法，用于反给activity数据
     * @param bean 返回给activity展品对象
     */
    @Override
    public void onFragmentInteraction(ExhibitBean bean) {
        this.currentExhibit=bean;
        refreshBottomTab(bean);
    }

    private class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case MSG_WHAT_CHANGE_EXHIBIT:
                    refreshBottomTab(currentExhibit);
                    break;
                case MSG_WHAT_UPDATE_PROGRESS:
                    seekBarProgress.setMax(currentDuration);
                    seekBarProgress.setProgress(currentProgress);
                    break;
                case MSG_WHAT_PAUSE_MUSIC:
                    break;
                case MSG_WHAT_CONTINUE_MUSIC:
                    break;
                case MSG_WHAT_CHANGE_PLAY_START:
                    ivPlayCtrl.setImageDrawable(getResources().getDrawable(R.drawable.uamp_ic_pause_white_24dp));
                    break;
                case MSG_WHAT_CHANGE_PLAY_STOP:
                    ivPlayCtrl.setImageDrawable(getResources().getDrawable(R.drawable.uamp_ic_play_arrow_white_24dp));
                    break;
            }
        }
    }

    private void refreshBottomTab(ExhibitBean exhibitBean) {
        if(exhibitBean==null){return;}
        currentExhibit=exhibitBean;
        String iconPath=currentExhibit.getIconurl();
        String name= Tools.changePathToName(iconPath);
        exhibitName.setText(currentExhibit.getName());
        String path=LOCAL_ASSETS_PATH+currentExhibit.getMuseumId()+"/"+name;
        if(Tools.isFileExist(path)){
            ImageLoaderUtil.displaySdcardImage(ListAndMapActivity.this, path, exhibitIcon);
        }else{
            ImageLoaderUtil.displayNetworkImage(ListAndMapActivity.this, BASE_URL + iconPath, exhibitIcon);
        }
    }

    /**
     * 广播接受器，监听播放状态
     */
    class PlayStateReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action) {

                //更新进度
                case INTENT_EXHIBIT_PROGRESS:
                    currentDuration = intent.getIntExtra(INTENT_EXHIBIT_DURATION, 0);
                    currentProgress = intent.getIntExtra(INTENT_EXHIBIT_PROGRESS, 0);
                    handler.sendEmptyMessage(MSG_WHAT_UPDATE_PROGRESS);
                    break;
                //停止播放
                case INTENT_CHANGE_PLAY_STOP:
                    handler.sendEmptyMessage(MSG_WHAT_CHANGE_PLAY_STOP);
                    break;
                //继续播放
                case INTENT_CHANGE_PLAY_PLAY:
                    handler.sendEmptyMessage(MSG_WHAT_CHANGE_PLAY_START);
                    break;
                //切换展品
                case INTENT_EXHIBIT:
                    String exhibitStr = intent.getStringExtra(INTENT_EXHIBIT);
                    if (TextUtils.isEmpty(exhibitStr)) {
                        break;
                    }
                    ExhibitBean exhibitBean = JSON.parseObject(exhibitStr, ExhibitBean.class);
                    if (exhibitBean == null) {
                        break;
                    }
                    if (currentExhibit == null || !currentExhibit.equals(exhibitBean)) {
                        currentExhibit = exhibitBean;
                        currentMuseumId = currentExhibit.getMuseumId();
                        handler.sendEmptyMessage(MSG_WHAT_CHANGE_EXHIBIT);
                    }
                    break;
            }
        }
    }


}
