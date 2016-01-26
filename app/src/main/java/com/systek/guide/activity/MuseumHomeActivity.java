package com.systek.guide.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PersistableBundle;
import android.support.v7.widget.SwitchCompat;
import android.text.TextUtils;
import android.view.Display;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.systek.guide.MyApplication;
import com.systek.guide.R;
import com.systek.guide.biz.DataBiz;
import com.systek.guide.entity.MuseumBean;
import com.systek.guide.manager.MediaServiceManager;
import com.systek.guide.utils.ExceptionUtil;
import com.systek.guide.utils.ImageLoaderUtil;
import com.systek.guide.utils.LogUtil;
import com.systek.guide.utils.Tools;

import java.io.IOException;

public class MuseumHomeActivity extends BaseActivity {

    /*当前博物馆ID*/
    private String currentMuseumId;
    private MuseumBean currentMuseum;
    /*当前屏幕宽度*/
    private int screenWidth;
    /*对话框*/
    private AlertDialog progressDialog;
    private ImageView titleBarDrawer;
    private TextView titleBarTopic;
    private LinearLayout llMuseumLargestIcon;
    private TextView tvMuseumIntroduce;
    private RelativeLayout rlGuideHome;
    private RelativeLayout rlMapHome;
    private RelativeLayout rlTopicHome;
    private Handler handler;
    private String currentMuseumStr;
    private MediaPlayer mediaPlayer;
    private ImageView ivPlayStateCtrl;
    private RelativeLayout rlCollectionHome;
    private ImageView titleBarSearch;
    private RelativeLayout rlNearlyHome;
    private SwitchCompat auto_switch;
    private MediaServiceManager mediaServiceManager;


    @Override
    protected void initialize(Bundle savedInstanceState) {
        init();
        initDrawer();
        initData();
        initView();
        addListener();
        /**数据初始化好之前显示加载对话框*/
        //showProgressDialog();
    }

    private void init() {
        mediaServiceManager=MediaServiceManager.getInstance(this);
        setContentView(R.layout.activity_museum_home);
        WindowManager windowManager = getWindowManager();
        Display display = windowManager.getDefaultDisplay();
        screenWidth = display.getWidth();
        handler=new MyHandler();
        Intent intent =getIntent();
        currentMuseumStr=intent.getStringExtra(INTENT_MUSEUM);

    }

    @Override
    protected void onNewIntent(Intent intent) {
        currentMuseumStr=intent.getStringExtra(INTENT_MUSEUM);
        initData();
    }


    @Override
    protected void onRestart() {
        super.onRestart();
        initAudio();
        refreshPlayState();
    }

    private void refreshPlayState() {
        if(mediaPlayer!=null&&mediaPlayer.isPlaying()){
            setPlayStateImageToOpen();
        }else{
            setPlayStateImageToOpen();
        }
    }

    private void addListener() {
        titleBarSearch.setOnClickListener(onClickListener);
        rlGuideHome.setOnClickListener(onClickListener);
        rlMapHome.setOnClickListener(onClickListener);
        rlTopicHome.setOnClickListener(onClickListener);
        titleBarDrawer.setOnClickListener(onClickListener);
        ivPlayStateCtrl.setOnClickListener(onClickListener);
        rlCollectionHome.setOnClickListener(onClickListener);
        rlNearlyHome.setOnClickListener(onClickListener);
        auto_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mediaServiceManager.setPlayMode(PLAY_MODE_AUTO);
                } else {
                    mediaServiceManager.setPlayMode(PLAY_MODE_HAND);
                }
            }
        });
    }


    private View.OnClickListener onClickListener=new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent=null;
            switch (v.getId()){
                case R.id.rlGuideHome:
                    intent=new Intent(MuseumHomeActivity.this,ListAndMapActivity.class);
                    intent.putExtra(INTENT_FLAG_GUIDE_MAP, INTENT_FLAG_GUIDE);
                    startActivity(intent);
                    break;
                case R.id.rlMapHome:
                    intent=new Intent(MuseumHomeActivity.this,ListAndMapActivity.class);
                    intent.putExtra(INTENT_FLAG_GUIDE_MAP, INTENT_FLAG_MAP);
                    startActivity(intent);
                    break;
                case R.id.titleBarRightImg:
                    intent=new Intent(MuseumHomeActivity.this,SearchActivity.class);
                    startActivity(intent);
                    break;
                case R.id.rlTopicHome:
                    intent=new Intent(MuseumHomeActivity.this,TopicActivity.class);
                    intent.putExtra(INTENT_MUSEUM_ID,currentMuseumId);
                    startActivity(intent);
                    break;
                case R.id.rlCollectionHome:
                    intent=new Intent(MuseumHomeActivity.this,CollectionActivity.class);
                    intent.putExtra(INTENT_MUSEUM_ID,currentMuseumId);
                    startActivity(intent);
                    break;
                case R.id.titleBarDrawer:
                    if (drawer.isDrawerOpen()) {
                        drawer.closeDrawer();
                    } else {
                        drawer.openDrawer();
                    }
                    break;
                case R.id.ivPlayStateCtrl:
                    if(mediaPlayer!=null){
                        if(mediaPlayer.isPlaying()){
                            mediaPlayer.pause();
                            setPlayStateImageToClose();
                        }else{
                            if(application.mServiceManager!=null&&application.mServiceManager.isPlaying()){
                                application.mServiceManager.pause();
                            }
                            mediaPlayer.start();
                            setPlayStateImageToOpen();
                        }
                    }
                    break;
            }
        }
    };

    private void setPlayStateImageToClose() {
        if(ivPlayStateCtrl==null){return;}
        ivPlayStateCtrl.setImageDrawable(getResources().getDrawable(R.drawable.iv_sound_close));
    }
    private void setPlayStateImageToOpen() {
        if(ivPlayStateCtrl==null){return;}
        ivPlayStateCtrl.setImageDrawable(getResources().getDrawable(R.drawable.iv_sound_open));
    }

    private void initData() {
        try{
            new Thread(){
                @Override
                public void run() {
                    if(!TextUtils.isEmpty(currentMuseumStr)){
                        currentMuseum=JSON.parseObject(currentMuseumStr, MuseumBean.class);
                        currentMuseumId=currentMuseum.getId();
                        if(handler!=null){
                            handler.sendEmptyMessage(MSG_WHAT_UPDATE_DATA_SUCCESS);
                        }
                    }
                    if(TextUtils.isEmpty(currentMuseumId)){return;}
                    LogUtil.i("ZHANG",currentMuseumId);
                    DataBiz.saveTempValue(MuseumHomeActivity.this, SP_MUSEUM_ID,currentMuseumId);
                    boolean flag=DataBiz.saveAllJsonData(currentMuseumId);
                    if(flag){
                        LogUtil.i("ZHANG","DataBiz.saveAllJsonData 数据更新成功");
                    }else{
                        showToast("抱歉，数据获取失败！");
                    }
                }
            }.start();
        }catch (Exception e){
            ExceptionUtil.handleException(e);
        }


    }

    private void showProgressDialog() {
       /* progressDialog = new AlertDialog.Builder(MuseumHomeActivity.this).create();
        progressDialog.show();
        Window window = progressDialog.getWindow();
        window.setContentView(R.layout.dialog_progress);
        TextView dialog_title=(TextView)window.findViewById(R.id.dialog_title);
        dialog_title.setText("正在加载...");*/
    }

    private void initView() {
        titleBarDrawer = (ImageView) findViewById(R.id.titleBarDrawer);
        titleBarSearch = (ImageView) findViewById(R.id.titleBarRightImg);
        ivPlayStateCtrl = (ImageView) findViewById(R.id.ivPlayStateCtrl);
        titleBarTopic = (TextView) findViewById(R.id.titleBarTopic);
        llMuseumLargestIcon = (LinearLayout) findViewById(R.id.llMuseumLargestIcon);
        tvMuseumIntroduce = (TextView) findViewById(R.id.tvMuseumIntroduce);
        rlGuideHome = (RelativeLayout) findViewById(R.id.rlGuideHome);
        rlMapHome = (RelativeLayout) findViewById(R.id.rlMapHome);
        rlTopicHome = (RelativeLayout) findViewById(R.id.rlTopicHome);
        rlCollectionHome = (RelativeLayout) findViewById(R.id.rlCollectionHome);
        rlNearlyHome = (RelativeLayout) findViewById(R.id.rlNearlyHome);
        auto_switch = (SwitchCompat) findViewById(R.id.auto_switch);
    }

    private void showData(){
        if(currentMuseum!=null){
            titleBarTopic.setText(currentMuseum.getName());
            /**加载博物馆介绍*/
            tvMuseumIntroduce.setText("      " + currentMuseum.getTextUrl());
            initAudio();
            /*加载多个Icon图片*/
            if(llMuseumLargestIcon.getChildCount()>0){
                llMuseumLargestIcon.removeAllViews();
            }
            String imgStr = currentMuseum.getImgUrl();
            String[] imgs = imgStr.split(",");
            for (int i = 0; i < imgs.length; i++) {
                String imgUrl = imgs[i];
                String imgName = imgUrl.replaceAll("/", "_");
                String localPath = LOCAL_ASSETS_PATH + currentMuseumId +"/"+ LOCAL_FILE_TYPE_IMAGE+"/"+imgName;
                boolean flag = Tools.isFileExist(localPath);
                ImageView iv = new ImageView(this);
                iv.setLayoutParams(new LinearLayout.LayoutParams(screenWidth, ViewGroup.LayoutParams.MATCH_PARENT));
                iv.setScaleType(ImageView.ScaleType.FIT_XY);
                llMuseumLargestIcon.addView(iv);
                if (flag) {
                    ImageLoaderUtil.displaySdcardImage(this, localPath, iv);
                } else {
                    if (MyApplication.currentNetworkType != INTERNET_TYPE_NONE) {
                        ImageLoaderUtil.displayNetworkImage(this, BASE_URL + imgUrl, iv);
                    }
                }
            }
        }
    }

    private void initAudio() {
        new Thread(){
            @Override
            public void run() {
                mediaPlayer=new MediaPlayer();
                String audioPath = currentMuseum.getAudioUrl();
                String audioName = Tools.changePathToName(audioPath);
                String audioUrl = LOCAL_ASSETS_PATH + currentMuseumId + "/" + LOCAL_FILE_TYPE_AUDIO + "/"+ audioName;
                String dataUrl="";
                // 判断sdcard上有没有图片
                if (Tools.isFileExist(audioUrl)) {
                    dataUrl=audioUrl;
                } else {
                    dataUrl = BASE_URL + audioPath;
                }
                try {
                    mediaPlayer.setDataSource(dataUrl);
                    mediaPlayer.setOnPreparedListener(mediaListener);
                    mediaPlayer.prepareAsync();
                } catch (IllegalStateException | IOException e) {
                    ExceptionUtil.handleException(e);
                }
            }
        }.start();
    }

    MediaPlayer.OnPreparedListener mediaListener=new MediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(MediaPlayer mp) {
            if(application.mServiceManager==null){
                mp.start();
            }
        }
    };


    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {

        super.onSaveInstanceState(outState, outPersistentState);
    }

    /*用于计算点击返回键时间*/
    private long mExitTime=0;
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if(drawer.isDrawerOpen()){
                drawer.closeDrawer();
            }else {
                if ((System.currentTimeMillis() - mExitTime) > 2000) {
                    Toast.makeText(this, "在按一次退出", Toast.LENGTH_SHORT).show();
                    mExitTime = System.currentTimeMillis();
                } else {
                    DataBiz.clearTempValues(this);
                    MyApplication.get().exit();
                }
            }
            return true;
        }
        //拦截MENU按钮点击事件，让他无任何操作
       else if (keyCode == KeyEvent.KEYCODE_MENU) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    @Override
    protected void onStop() {
        super.onStop();
        if(mediaPlayer!=null&&mediaPlayer.isPlaying()){
            mediaPlayer.stop();
            setPlayStateImageToClose();
        }
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MSG_WHAT_UPDATE_DATA_SUCCESS) {
                showData();
            }
        }
    }
}
