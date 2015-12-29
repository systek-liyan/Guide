package com.systekcn.guide.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.systekcn.guide.MyApplication;
import com.systekcn.guide.R;
import com.systekcn.guide.activity.base.BaseActivity;
import com.systekcn.guide.biz.BizFactory;
import com.systekcn.guide.biz.GetDataBiz;
import com.systekcn.guide.common.IConstants;
import com.systekcn.guide.common.utils.ExceptionUtil;
import com.systekcn.guide.common.utils.ImageLoaderUtil;
import com.systekcn.guide.common.utils.Tools;
import com.systekcn.guide.common.utils.ViewUtils;
import com.systekcn.guide.custom.DrawerView;
import com.systekcn.guide.custom.slidingmenu.SlidingMenu;
import com.systekcn.guide.entity.ExhibitBean;

import java.util.List;

public class MuseumHomeActivity extends BaseActivity implements IConstants{

    private String currentMuseumId;
    private int screenWidth;
    private MyHandler handler;
    private AlertDialog progressDialog;
    private ImageView iv_Drawer;
    private TextView title_bar_topic;

    private final int MSG_WHAT_UPDATE_DATA=1;
    private LinearLayout ll_museum_largest_icon;
    private TextView tv_museum_introduce;
    private RelativeLayout rl_guide_home;
    /**侧滑菜单*/
    private SlidingMenu side_drawer;
    private DrawerView drawerView;
    private RelativeLayout rl_topic_home;

    @Override
    protected void initialize() {
        ViewUtils.setStateBarToAlpha(this);
        setContentView(R.layout.activity_museum_home);
        application.mServiceManager.connectService();/**启动播放服务*/
        Intent intent = getIntent();
        currentMuseumId = intent.getStringExtra(INTENT_MUSEUM_ID);
        application.currentMuseumId=currentMuseumId;
        WindowManager windowManager = getWindowManager();
        Display display = windowManager.getDefaultDisplay();
        screenWidth = display.getWidth();
        handler=new MyHandler();
        initView();
        //initSlidingMenu();
        addListener();
        /**数据初始化好之前显示加载对话框*/
        showProgressDialog();
        initData();
    }


    private void initSlidingMenu() {
        drawerView =new DrawerView(this);
        side_drawer = drawerView.initSlidingMenu();
    }

    private void addListener() {
        rl_guide_home.setOnClickListener(onClickListener);
        rl_topic_home.setOnClickListener(onClickListener);
        iv_Drawer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    private View.OnClickListener onClickListener=new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent=null;
            switch (v.getId()){
                case R.id.rl_guide_home:
                     intent=new Intent(MuseumHomeActivity.this,ListAndMapActivity.class);
                    startActivity(intent);
                    break;
                case R.id.rl_topic_home:
                    intent=new Intent(MuseumHomeActivity.this,TopicActivity.class);
                    startActivity(intent);
                    break;
            }
        }
    };

    private void initData() {

        try{
            new Thread(){
                @Override
                public void run() {
                    if(application.currentMuseum!=null){
                        currentMuseumId=application.currentMuseum.getId();
                        handler.sendEmptyMessage(MSG_WHAT_UPDATE_DATA);
                    }
                    GetDataBiz biz= (GetDataBiz) BizFactory.getDataBiz();
                    List<ExhibitBean> exhibitBeans= (List<ExhibitBean>) biz.getAllBeans(MuseumHomeActivity.this,
                            IConstants.URL_TYPE_GET_EXHIBITS_BY_MUSEUM_ID, "deadccf89ef8412a9c8a2628cee28e18");
                    if(exhibitBeans!=null&&exhibitBeans.size()>0){
                        application.totalExhibitBeanList=exhibitBeans;
                    }
                }
            }.start();

        }catch (Exception e) {
            showToast("抱歉，数据获取失败！");
            ExceptionUtil.handleException(e);
        }
    }


    private void showProgressDialog() {
        progressDialog = new AlertDialog.Builder(MuseumHomeActivity.this).create();
        progressDialog.show();
        Window window = progressDialog.getWindow();
        window.setContentView(R.layout.dialog_progress);
        TextView dialog_title=(TextView)window.findViewById(R.id.dialog_title);
        dialog_title.setText("正在加载...");
    }

    private void initView() {

        iv_Drawer = (ImageView) findViewById(R.id.title_bar_more);
        title_bar_topic = (TextView) findViewById(R.id.title_bar_topic);
        ll_museum_largest_icon = (LinearLayout) findViewById(R.id.ll_museum_largest_icon);
        tv_museum_introduce = (TextView) findViewById(R.id.tv_museum_introduce);
        rl_guide_home = (RelativeLayout) findViewById(R.id.rl_guide_home);
        rl_topic_home = (RelativeLayout) findViewById(R.id.rl_topic_home);


    }


    private void showData(){
        if(application.currentMuseum!=null){
                title_bar_topic.setText(application.currentMuseum.getName());
                /**加载博物馆介绍*/
            tv_museum_introduce.setText("      "+application.currentMuseum.getTextUrl());
                //initAudio();
            /*加载多个Icon图片*/
                String imgStr = application.currentMuseum.getImgUrl();
                String[] imgs = imgStr.split(",");
                for (int i = 0; i < imgs.length; i++) {
                    String imgUrl = imgs[i];
                    String imgName = imgUrl.replaceAll("/", "_");
                    String localPath = LOCAL_ASSETS_PATH + currentMuseumId +"/"+ LOCAL_FILE_TYPE_IMAGE+"/"+imgName;
                    boolean flag = Tools.isFileExist(localPath);
                    ImageView iv = new ImageView(this);
                    iv.setLayoutParams(new LinearLayout.LayoutParams(screenWidth, ViewGroup.LayoutParams.MATCH_PARENT));
                    iv.setScaleType(ImageView.ScaleType.FIT_XY);
                    ll_museum_largest_icon.addView(iv);
                    if (flag) {
                        ImageLoaderUtil.displaySdcardImage(this, localPath, iv);
                    } else {
                        if (MyApplication.currentNetworkType != INTERNET_TYPE_NONE) {
                            ImageLoaderUtil.displayNetworkImage(this, BASEURL + imgUrl, iv);
                        }
                    }
            }
        }
    }

    class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MSG_WHAT_UPDATE_DATA) {
                if(progressDialog!=null&&progressDialog.isShowing()){
                    progressDialog.dismiss();
                }
                showData();
            }
        }
    }
}
