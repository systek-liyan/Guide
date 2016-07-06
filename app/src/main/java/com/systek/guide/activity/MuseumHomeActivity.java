package com.systek.guide.activity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PersistableBundle;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.systek.guide.MyApplication;
import com.systek.guide.R;
import com.systek.guide.adapter.MuseumIconAdapter;
import com.systek.guide.biz.DataBiz;
import com.systek.guide.entity.MuseumBean;
import com.systek.guide.service.DownloadService;
import com.systek.guide.service.PlayManager;
import com.systek.guide.utils.ExceptionUtil;
import com.systek.guide.utils.LogUtil;
import com.systek.guide.utils.MyHttpUtil;
import com.systek.guide.utils.NetworkUtil;
import com.systek.guide.utils.Tools;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class MuseumHomeActivity extends BaseActivity {

    /*当前博物馆ID*/
    private String currentMuseumId;
    private MuseumBean currentMuseum;
    private TextView tvMuseumIntroduce;
    private RelativeLayout rlGuideHome;
    private RelativeLayout rlMapHome;
    private RelativeLayout rlTopicHome;
    private MediaPlayer mediaPlayer;
    private ImageView ivPlayStateCtrl;
    private RelativeLayout rlCollectionHome;
    private RelativeLayout rlNearlyHome;
    private ArrayList<String> iconUrlList;
    private MuseumIconAdapter iconAdapter;
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;
    private NavigationView navigationView;

    static class MyHandler extends Handler {

        WeakReference<MuseumHomeActivity> activityWeakReference;
        MyHandler(MuseumHomeActivity activity){
            this.activityWeakReference=new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {

            if(activityWeakReference==null){return;}
            MuseumHomeActivity activity=activityWeakReference.get();
            if(activity==null){return;}
            switch (msg.what){
                case MSG_WHAT_REFRESH_VIEW:
                    activity.refreshView();
                    break;
                default:break;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_museum_home);
        initToolBar();
        initView();
        addListener();
        initData();
    }

    private void initToolBar() {
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        if (mToolbar == null) {
            throw new IllegalStateException("Layout is required to include a Toolbar with id 'toolbar'");
        }
        toolbarTitle = (TextView) mToolbar.findViewById(R.id.toolbar_title);
        mToolbar.inflateMenu(R.menu.normal_menu);
        mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Intent intent=new Intent(getActivity(),SearchActivity.class);
                startActivity(intent);
                return true;
            }
        });
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (mDrawerLayout != null) {
            navigationView = (NavigationView) findViewById(R.id.nav_view);
            if (navigationView == null) {
                throw new IllegalStateException("Layout requires a NavigationView with id 'nav_view'");
            }

            navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(MenuItem item) {

                    Intent intent=null;
                    switch (item.getItemId()){
                        case R.id.menu_1:
                            intent=new Intent(getActivity(),DownloadManagerActivity.class);
                            break;
                        case R.id.menu_2:
                            intent=new Intent(getActivity(),CollectionActivity.class);
                            break;
                        case R.id.menu_3:
                            intent=new Intent(getActivity(),CityChooseActivity.class);
                            break;
                        case R.id.menu_4:
                            intent=new Intent(getActivity(),MuseumListActivity.class);
                            break;
                        case R.id.menu_5:
                            intent=new Intent(getActivity(),SettingActivity.class);
                            break;
                    }
                    if(intent!=null){
                        startActivity(intent);
                    }
                    closeDrawer();
                    return true;
                }
            });
            // Create an ActionBarDrawerToggle that will handle opening/closing of the drawer:
            mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar,R.string.app_name, R.string.app_name);
            mDrawerLayout.addDrawerListener(mDrawerListener);
            //populateDrawerItems(navigationView);
            //setSupportActionBar(mToolbar);
            updateDrawerToggle();
        } else {
            setSupportActionBar(mToolbar);
        }
    }

    private void closeDrawer(){
        if(mDrawerLayout==null||navigationView==null){return;}
        if(mDrawerLayout.isDrawerOpen(navigationView)){
            mDrawerLayout.closeDrawer(navigationView);
        }
    }
    protected void updateDrawerToggle() {
        if (mDrawerToggle == null) {
            return;
        }
        boolean isRoot = getFragmentManager().getBackStackEntryCount() == 0;
        mDrawerToggle.setDrawerIndicatorEnabled(isRoot);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowHomeEnabled(!isRoot);
            getSupportActionBar().setDisplayHomeAsUpEnabled(!isRoot);
            getSupportActionBar().setHomeButtonEnabled(!isRoot);
        }
        if (isRoot) {
            mDrawerToggle.syncState();
        }
    }


    public final DrawerLayout.DrawerListener mDrawerListener = new DrawerLayout.DrawerListener(){

        @Override
        public void onDrawerSlide(View drawerView, float slideOffset) {

        }

        @Override
        public void onDrawerOpened(View drawerView) {

        }

        @Override
        public void onDrawerClosed(View drawerView) {

        }

        @Override
        public void onDrawerStateChanged(int newState) {

        }
    };


    @Override
    protected void onNewIntent(Intent intent) {
        currentMuseumId=intent.getStringExtra(INTENT_MUSEUM_ID);
        initData();
    }


    @Override
    protected void onRestart() {
        super.onRestart();
        initData();
        refreshPlayState();
    }


    @Override
    protected void onStop() {
        super.onStop();
        if(mediaPlayer!=null&&mediaPlayer.isPlaying()){
            mediaPlayer.stop();
            setPlayStateImageToClose();
        }
    }

    private void refreshPlayState() {
        if(mediaPlayer!=null&&mediaPlayer.isPlaying()){
            setPlayStateImageToOpen();
        }else{
            setPlayStateImageToClose();
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

    private void initData() {

        Intent intent =getIntent();
        currentMuseumId=intent.getStringExtra(INTENT_MUSEUM_ID);
        showDialog("正在加载...");
        if(TextUtils.isEmpty(currentMuseumId)){
            onError();
            return;
        }
        new Thread(){
            @Override
            public void run() {
                currentMuseum = DataBiz.getEntityLocalById(MuseumBean.class, currentMuseumId);
                if (currentMuseum == null) {
                    if (!NetworkUtil.isOnline(MuseumHomeActivity.this)) {
                        onError();
                    } else {
                        String url = BASE_URL + URL_GET_MUSEUM_BY_ID + currentMuseumId;
                        List<MuseumBean> museumBeanList = DataBiz.getEntityListFromNet(MuseumBean.class, url);
                        if (museumBeanList != null && museumBeanList.size() > 0) {
                            currentMuseum = museumBeanList.get(0);
                        }
                    }
                }
                if(currentMuseum==null){
                    onError();
                    return;
                }
                initAudio();
                //保存临时数据，当前博物馆id
                DataBiz.saveTempValue(MuseumHomeActivity.this, SP_MUSEUM_ID, currentMuseumId);
                String imgStr = currentMuseum.getImgUrl();
                String[] imgs = imgStr.split(",");
                for (String imgUrl : imgs) {
                    if (!TextUtils.isEmpty(imgUrl)) {
                        iconUrlList.add(imgUrl);
                    }
                }

                //判断当前博物馆基本数据是否已经加载
                boolean isBasicDataSave = DataBiz.isBasicDataSave(currentMuseumId);
                if (!isBasicDataSave) {
                    DataBiz.saveAllJsonData(currentMuseumId);
                }
                handler.sendEmptyMessage(MSG_WHAT_REFRESH_VIEW);
                //initAllData();
            }
        }.start();
    }


    private void initAllData(){
        new Thread(){
            @Override
            public void run() {
                //已经下载当前博物馆基本数据，通知更新显示数据
                //判断当前是否在博物馆，根据beacon回调时存储的Boolean值
                boolean isInMuseum = (boolean) DataBiz.getTempValue(MuseumHomeActivity.this, SP_IS_IN_MUSEUM, false);
                //如果在博物馆
                if (isInMuseum) {
                    //判断博物馆数据是否已经下载
                    boolean isDownload = (boolean) Tools.getValue(MyApplication.get(), currentMuseumId, false);
                    //没有下载则启动下载服务去下载数据
                    if (!isDownload) {
                        DownloadService.startActionBaz(MuseumHomeActivity.this, currentMuseumId);
                    }
                }

            }
        }.start();
    }


    private void refreshView() {
        if(currentMuseum!=null){
            setTitleBarTitle(currentMuseum.getName());
            /**加载博物馆介绍*/
            tvMuseumIntroduce.setText("      " + currentMuseum.getTextUrl());
            iconAdapter.updateData(iconUrlList);
            closeDialog();
        }
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
                            if(PlayManager.getInstance().isPlaying()){
                                PlayManager.getInstance().pause();
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

    private void onError() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showErrors(true);
                refreshBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mErrorView.setVisibility(View.GONE);
                        initData();
                    }
                });
                closeDialog();
            }
        });
    }

    protected void initView() {

        handler=new MyHandler(this);
        mErrorView=findViewById(R.id.mErrorView);
        if (mErrorView != null) {
            refreshBtn=(Button)mErrorView.findViewById(R.id.refreshBtn);
        }

        ivPlayStateCtrl = (ImageView) findViewById(R.id.ivPlayStateCtrl);
        //llMuseumLargestIcon = (LinearLayout) findViewById(R.id.llMuseumLargestIcon);
        tvMuseumIntroduce = (TextView) findViewById(R.id.tvMuseumIntroduce);
        rlGuideHome = (RelativeLayout) findViewById(R.id.rlGuideHome);
        rlMapHome = (RelativeLayout) findViewById(R.id.rlMapHome);
        rlTopicHome = (RelativeLayout) findViewById(R.id.rlTopicHome);
        rlCollectionHome = (RelativeLayout) findViewById(R.id.rlCollectionHome);
        rlNearlyHome = (RelativeLayout) findViewById(R.id.rlNearlyHome);

        RecyclerView recycleViewMuseumIcon = (RecyclerView) findViewById(R.id.recycleViewMuseumIcon);

         /*设置为横向*/
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        if (recycleViewMuseumIcon != null) {
            recycleViewMuseumIcon.setLayoutManager(linearLayoutManager);
            iconUrlList=new ArrayList<>();
            iconAdapter=new MuseumIconAdapter(this,iconUrlList);
            recycleViewMuseumIcon.setAdapter(iconAdapter);
            recycleViewMuseumIcon.setOverScrollMode(ScrollView.OVER_SCROLL_NEVER);
        }

    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.normal_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent=new Intent(MuseumHomeActivity.this,SearchActivity.class);
        if(!TextUtils.isEmpty(currentMuseumId)){
            intent.putExtra(MUSEUM_ID,currentMuseumId);
        }
        startActivity(intent);
        return true;
    }


    private synchronized void initAudio() {
        new Thread(){
            @Override
            public void run() {
                mediaPlayer=new MediaPlayer();
                String audioPath = currentMuseum.getAudioUrl();
                String audioName = Tools.changePathToName(audioPath);
                String audioUrl = LOCAL_ASSETS_PATH + currentMuseumId  + "/"+ audioName;
                String dataUrl=null;
                // 判断sdcard上有没有图片
                if (Tools.isFileExist(audioUrl)) {
                    dataUrl=audioUrl;
                    try {
                        mediaPlayer.setDataSource(dataUrl);
                        mediaPlayer.setOnPreparedListener(mediaListener);
                        mediaPlayer.prepareAsync();
                    } catch (IllegalStateException | IOException e) {
                        ExceptionUtil.handleException(e);
                    }

                } else {
                    dataUrl = BASE_URL + audioPath;
                    String savePath= LOCAL_ASSETS_PATH + currentMuseumId;
                    int error=0;
                    try {
                        MyHttpUtil.downLoadFromUrl(dataUrl,savePath,audioName);
                    } catch (IOException e) {
                        ExceptionUtil.handleException(e);
                        error++;
                    }
                    if(error<3){
                        initAudio();
                    }else{
                        LogUtil.i(TAG,"博物馆音频加载失败");
                    }
                }
            }
        }.start();
    }

    MediaPlayer.OnPreparedListener mediaListener=new MediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(MediaPlayer mp) {
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
            if(mDrawerLayout.isDrawerOpen(navigationView)){
                mDrawerLayout.closeDrawer(navigationView);
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


}