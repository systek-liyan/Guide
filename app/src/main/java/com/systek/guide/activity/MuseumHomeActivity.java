package com.systek.guide.activity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PersistableBundle;
import android.text.TextUtils;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
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
import com.systek.guide.service.DownloadService;
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
    private RelativeLayout rlNearlyHome;
    private MediaServiceManager mediaServiceManager;


    @Override
    protected void initialize(Bundle savedInstanceState) {

        setContentView(R.layout.activity_museum_home);
        mediaServiceManager=MediaServiceManager.getInstance(this);
        WindowManager windowManager = getWindowManager();
        Display display = windowManager.getDefaultDisplay();
        screenWidth = display.getWidth();
        handler=new MyHandler();
        Intent intent =getIntent();
        currentMuseumStr=intent.getStringExtra(INTENT_MUSEUM);
        initView();
        initDrawer();
        addListener();
        initBasicData();
        showDialog("加载中，请稍后...");
    }


    @Override
    protected void onNewIntent(Intent intent) {
        currentMuseumStr=intent.getStringExtra(INTENT_MUSEUM);
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
        rlGuideHome.setOnClickListener(onClickListener);
        rlMapHome.setOnClickListener(onClickListener);
        rlTopicHome.setOnClickListener(onClickListener);
        ivPlayStateCtrl.setOnClickListener(onClickListener);
        rlCollectionHome.setOnClickListener(onClickListener);
        rlNearlyHome.setOnClickListener(onClickListener);
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
                case R.id.ivPlayStateCtrl:
                    if(mediaPlayer!=null){
                        if(mediaPlayer.isPlaying()){
                            mediaPlayer.pause();
                            setPlayStateImageToClose();
                        }else{
                            MyApplication application=MyApplication.get();
                            MediaServiceManager serviceManager=application.getmServiceManager();
                            if(serviceManager!=null&&serviceManager.isPlaying()){
                                serviceManager.pause();
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

    private void initBasicData() {
        new Thread(){
            @Override
            public void run() {
                try{
                    //当前博物馆json为空，返回数据加载失败
                    if(TextUtils.isEmpty(currentMuseumStr)){
                        onDataError();
                        return;
                    }
                    //解析当前你博物馆
                    currentMuseum=JSON.parseObject(currentMuseumStr, MuseumBean.class);
                    currentMuseumId=currentMuseum.getId();
                    if(TextUtils.isEmpty(currentMuseumId)){
                        LogUtil.i("ZHANG","currentMuseumId"+currentMuseumId);
                        return;}
                    //保存临时数据，当前博物馆id
                    DataBiz.saveTempValue(MuseumHomeActivity.this, SP_MUSEUM_ID, currentMuseumId);
                    //判断当前博物馆数据是否已经下载
                    boolean isDataSave= (boolean) Tools.getValue(MuseumHomeActivity.this, SP_IS_MUSEUM_DATA_SAVE, false);
                    //未下载则去下载
                    if(!isDataSave){
                        new Thread(){
                            @Override
                            public void run() {
                                DataBiz.saveAllJsonData(currentMuseumId);
                                Tools.saveValue(MuseumHomeActivity.this,SP_IS_MUSEUM_DATA_SAVE,true);
                                if(handler!=null){
                                    handler.sendEmptyMessage(MSG_WHAT_UPDATE_DATA_SUCCESS);
                                }
                            }
                        }.start();
                    }else{
                        //已经下载当前博物馆基本数据，通知更新显示数据
                        if(handler!=null){
                            handler.sendEmptyMessage(MSG_WHAT_UPDATE_DATA_SUCCESS);
                        }
                    }
                    //判断当前是否在博物馆，根据beacon回调时存储的Boolean值
                    boolean isInMuseum= (boolean) DataBiz.getTempValue(MuseumHomeActivity.this,SP_IS_IN_MUSEUM,false);
                    LogUtil.i("ZHANG","isInMuseum"+isInMuseum);
                    //如果在博物馆
                    if(isInMuseum){
                        //判断博物馆数据是否已经下载
                        boolean isDownload= (boolean) Tools.getValue(MyApplication.get(), currentMuseumId, false);
                        LogUtil.i("ZHANG","isDownload"+isDownload);
                        //没有下载则启动下载服务去下载数据
                        if(!isDownload){
                            /*Intent intent =new Intent(MuseumHomeActivity.this, TestService.class);
                            intent.putExtra(INTENT_MUSEUM_ID,currentMuseumId);
                            startService(intent);*/
                           DownloadService.startActionBaz(MuseumHomeActivity.this, currentMuseumId);
                           /* Intent intent =new Intent (MuseumHomeActivity.this, TempDownloadService.class);
                            intent.putExtra(INTENT_MUSEUM_ID,currentMuseumId);
                            startService(intent);*/
                        }
                    }
                }catch (Exception e){
                    ExceptionUtil.handleException(e);
                    onDataError();
                }
            }
        }.start();



    }

    private void initView() {

        setTitleBar();
        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_menu));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (drawer == null) {
                    return;
                }
                if (drawer.isDrawerOpen()) {
                    drawer.closeDrawer();
                } else {
                    drawer.openDrawer();
                }
            }
        });
        ivPlayStateCtrl = (ImageView) findViewById(R.id.ivPlayStateCtrl);
        llMuseumLargestIcon = (LinearLayout) findViewById(R.id.llMuseumLargestIcon);
        tvMuseumIntroduce = (TextView) findViewById(R.id.tvMuseumIntroduce);
        rlGuideHome = (RelativeLayout) findViewById(R.id.rlGuideHome);
        rlMapHome = (RelativeLayout) findViewById(R.id.rlMapHome);
        rlTopicHome = (RelativeLayout) findViewById(R.id.rlTopicHome);
        rlCollectionHome = (RelativeLayout) findViewById(R.id.rlCollectionHome);
        rlNearlyHome = (RelativeLayout) findViewById(R.id.rlNearlyHome);
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.normal_menu, menu);
        menu.getItem(0).setIcon(R.drawable.iv_search);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent=new Intent(MuseumHomeActivity.this,SearchActivity.class);
        startActivity(intent);
        return true;
    }

    private void showData(){
        if(currentMuseum!=null){
            if(dialog!=null&&dialog.isShowing()){
                dialog.dismiss();
            }
            setTitleBarTitle(currentMuseum.getName());
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
                String localPath = LOCAL_ASSETS_PATH + currentMuseumId +"/"+imgName;
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
                String audioUrl = LOCAL_ASSETS_PATH + currentMuseumId  + "/"+ audioName;
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
            MyApplication application=MyApplication.get();
            MediaServiceManager serviceManager=application.getmServiceManager();
            if(serviceManager==null){
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
