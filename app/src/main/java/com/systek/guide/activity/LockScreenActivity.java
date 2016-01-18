package com.systek.guide.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.systek.guide.R;
import com.systek.guide.adapter.NearlyGalleryAdapter;
import com.systek.guide.entity.ExhibitBean;
import com.systek.guide.manager.MediaServiceManager;
import com.systek.guide.utils.ImageLoaderUtil;
import com.systek.guide.utils.LogUtil;
import com.systek.guide.utils.TimeUtil;
import com.systek.guide.utils.Tools;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class LockScreenActivity extends BaseActivity {


    private ImageView fullscreenImage;
    private ImageView ivPlayCtrl;
    private ListChangeReceiver listChangeReceiver;
    private MediaServiceManager mediaServiceManager;
    private Handler handler;
    private ExhibitBean currentExhibit;
    private int currentDuration;
    private int currentProgress;
    private SeekBar seekBarProgress;
    private TextView tvPlayTime;
    private TextView tvTotalTime;
    private TextView tvLockTime;
    private String currentIconUrl;
    private RecyclerView recycleNearly;
    private List<ExhibitBean> nearlyExhibitList;
    private NearlyGalleryAdapter nearlyGalleryAdapter;
    private List<ExhibitBean> currentExhibitList;

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


    @Override
    protected void initialize(Bundle savedInstanceState) {
        setContentView(R.layout.activity_lock_screen);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD|
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        handler=new MyHandler();
        mediaServiceManager=MediaServiceManager.getInstance(this);
        initView();
        addListener();
        Intent intent=getIntent();
        String exhibitStr=intent.getStringExtra(INTENT_EXHIBIT);
        if(!TextUtils.isEmpty(exhibitStr)){
            ExhibitBean bean=JSON.parseObject(exhibitStr,ExhibitBean.class);
            if(currentExhibit==null){
                currentExhibit=bean;
                initData();
            }else{
                if(currentExhibit.equals(bean)){
                    refreshView();
                }else {
                    initData();
                }
            }

        }else{
            currentExhibit=mediaServiceManager.getCurrentExhibit();
            refreshView();
        }

        initView();
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(listChangeReceiver==null){
            registerReceiver();
        }

    }

    private void initData() {
        if(currentExhibit==null){return;}
        handler.sendEmptyMessage(MSG_WHAT_CHANGE_EXHIBIT);
    }

    private void addListener() {
        seekBarProgress.setOnSeekBarChangeListener(onSeekBarChangeListener);
    }


    private void refreshView() {
        LogUtil.i("ZHANG", "执行了refreshView");
        initIcon();
    }

    private void initView() {
        fullscreenImage=(ImageView)findViewById(R.id.fullscreenImage);
        ivPlayCtrl=(ImageView)findViewById(R.id.ivPlayCtrl);
        tvPlayTime=(TextView)findViewById(R.id.tvPlayTime);
        tvTotalTime=(TextView)findViewById(R.id.tvTotalTime);
        tvLockTime=(TextView)findViewById(R.id.tvLockTime);
        tvLockTime.setText(TimeUtil.getTime());

        seekBarProgress=(SeekBar)findViewById(R.id.seekBarProgress);

        recycleNearly = (RecyclerView)findViewById(R.id.recycleNearly);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        recycleNearly.setLayoutManager(linearLayoutManager);
        nearlyExhibitList=new ArrayList<>();
        nearlyGalleryAdapter=new NearlyGalleryAdapter(this,nearlyExhibitList);
        nearlyGalleryAdapter.setOnItemClickListener(new NearlyGalleryAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {

                LogUtil.i("zhang", "setOnItemClickLitener被点击了");
                nearlyGalleryAdapter.notifyItemChanged(position);
                ExhibitBean exhibitBean = currentExhibitList.get(position);
                ExhibitBean bean = mediaServiceManager.getCurrentExhibit();
                nearlyGalleryAdapter.setSelectIndex(exhibitBean);
                if (bean == null || !bean.equals(exhibitBean)) {
                    String str = JSON.toJSONString(exhibitBean);
                    Intent intent = new Intent();
                    intent.setAction(INTENT_EXHIBIT);
                    intent.putExtra(INTENT_EXHIBIT, str);
                    sendBroadcast(intent);
                }
            }
        });
        recycleNearly.setAdapter(nearlyGalleryAdapter);
        ivPlayCtrl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                unregisterReceiver(listChangeReceiver);
                handler.removeCallbacksAndMessages(null);
                finish();// TODO: 2016/1/14
            }
        });
    }


    private void registerReceiver() {
        listChangeReceiver = new ListChangeReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(INTENT_EXHIBIT);
        filter.addAction(INTENT_EXHIBIT_PROGRESS);
        filter.addAction(INTENT_EXHIBIT_DURATION);
        filter.addAction(INTENT_CHANGE_PLAY_PLAY);
        filter.addAction(INTENT_CHANGE_PLAY_STOP);
        filter.addAction(INTENT_EXHIBIT_LIST);
        registerReceiver(listChangeReceiver, filter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mediaServiceManager.isPlaying()){
            handler.sendEmptyMessage(MSG_WHAT_CHANGE_PLAY_START);
        }else{
            handler.sendEmptyMessage(MSG_WHAT_CHANGE_PLAY_STOP);
        }

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode==KeyEvent.KEYCODE_BACK){
            return true;
        }else if(keyCode==KeyEvent.KEYCODE_HOME){
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void initIcon() {
        if(currentExhibit==null){return;}
        currentIconUrl=currentExhibit.getIconurl();
        String imageName = Tools.changePathToName(currentIconUrl);
        String imgLocalUrl = LOCAL_ASSETS_PATH+currentExhibit.getMuseumId() + "/" + LOCAL_FILE_TYPE_IMAGE+"/"+imageName;
        File file = new File(imgLocalUrl);
        // 判断sdcard上有没有图片
        if (file.exists()) {
            // 显示sdcard
            ImageLoaderUtil.displaySdcardImage(this, imgLocalUrl, fullscreenImage);
        } else {
            ImageLoaderUtil.displayNetworkImage(this, BASE_URL + currentIconUrl, fullscreenImage);
        }
    }

    @Override
    protected void onDestroy() {
        //unregisterReceiver(listChangeReceiver);
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }


    class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_WHAT_UPDATE_PROGRESS:
                    seekBarProgress.setMax(currentDuration);
                    seekBarProgress.setProgress(currentProgress);
                    tvPlayTime.setText(TimeUtil.changeToTime(currentProgress).substring(3));
                    tvTotalTime.setText(TimeUtil.changeToTime(currentDuration).substring(3));
                    break;
                case MSG_WHAT_CHANGE_EXHIBIT:
                    refreshView();
                    break;
                case MSG_WHAT_CHANGE_PLAY_START:
                    ivPlayCtrl.setImageDrawable(getResources().getDrawable(R.drawable.iv_play_state_open_big));
                    break;
                case MSG_WHAT_CHANGE_PLAY_STOP:
                    ivPlayCtrl.setImageDrawable(getResources().getDrawable(R.drawable.iv_play_state_close_big));
                    break;
                case MSG_WHAT_UPDATE_DATA_SUCCESS:
                    if(nearlyGalleryAdapter ==null||currentExhibitList==null){return;}
                    nearlyGalleryAdapter.updateData(currentExhibitList);
                    break;
            }
        }
    }


    private  class ListChangeReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action) {
                case INTENT_EXHIBIT_PROGRESS:
                    currentDuration = intent.getIntExtra(INTENT_EXHIBIT_DURATION, 0);
                    currentProgress = intent.getIntExtra(INTENT_EXHIBIT_PROGRESS, 0);
                    handler.sendEmptyMessage(MSG_WHAT_UPDATE_PROGRESS);
                    break;
                case INTENT_EXHIBIT:
                    String exhibitStr = intent.getStringExtra(INTENT_EXHIBIT);
                    if (TextUtils.isEmpty(exhibitStr)) {
                        return;
                    }
                    ExhibitBean exhibitBean = JSON.parseObject(exhibitStr, ExhibitBean.class);
                    if (currentExhibit.equals(exhibitBean)) {
                        return;
                    }else{
                        currentExhibit=exhibitBean;
                    }
                    handler.sendEmptyMessage(MSG_WHAT_CHANGE_EXHIBIT);
                    break;
                case INTENT_CHANGE_PLAY_PLAY:
                    handler.sendEmptyMessage(MSG_WHAT_CHANGE_PLAY_START);
                    break;
                case INTENT_CHANGE_PLAY_STOP:
                    handler.sendEmptyMessage(MSG_WHAT_CHANGE_PLAY_STOP);
                    break;
                case INTENT_EXHIBIT_LIST:
                    String exhibitJson=intent.getStringExtra(INTENT_EXHIBIT_LIST);
                    currentExhibitList= JSON.parseArray(exhibitJson,ExhibitBean.class);
                    if(currentExhibitList==null){return;}
                    handler.sendEmptyMessage(MSG_WHAT_UPDATE_DATA_SUCCESS);
            }
        }
    }


}
