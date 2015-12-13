package com.systekcn.guide.activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.lidroid.xutils.DbUtils;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.DbException;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;
import com.systekcn.guide.MyApplication;
import com.systekcn.guide.R;
import com.systekcn.guide.activity.base.BaseActivity;
import com.systekcn.guide.adapter.ExhibitAdapter;
import com.systekcn.guide.adapter.OnListViewScrollListener;
import com.systekcn.guide.biz.BeansManageBiz;
import com.systekcn.guide.biz.BizFactory;
import com.systekcn.guide.common.IConstants;
import com.systekcn.guide.common.utils.ExceptionUtil;
import com.systekcn.guide.common.utils.ImageLoaderUtil;
import com.systekcn.guide.common.utils.LogUtil;
import com.systekcn.guide.common.utils.Tools;
import com.systekcn.guide.common.utils.ViewUtils;
import com.systekcn.guide.custom.DrawerView;
import com.systekcn.guide.custom.slidingmenu.SlidingMenu;
import com.systekcn.guide.entity.ExhibitBean;
import com.systekcn.guide.entity.MuseumBean;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MuseumHomePageActivity extends BaseActivity implements IConstants{

    private String currentMuseumId;
    /**侧滑菜单 */
    private SlidingMenu side_drawer;
    /**展品列表*/
    private ListView exhibitListView;
    /**顶部最大图片的布局*/
    private LinearLayout ll_museum_largest_icon;
    /**博物馆介绍的小*/
    private TextView tv_museum_introduce_short;
    /**博物馆介绍大*/
    private TextView tv_museum_introduce_long;
    /**搜索框*/
    private TextView ed_museum_search;
    /**博物馆介绍音乐开关*/
    private ImageView iv_home_page_ctrl_sound;
    /**当期博物馆*/
    private MuseumBean currentMuseum;
    /**展品列表适配器*/
    private ExhibitAdapter exhibitAdapter;
    /**展品列表*/
    private List<ExhibitBean> currentExhibitList;
    /**标题*/
    private TextView tv_title;
    /**导览按钮*/
    private TextView iv_home_page_guide;
    /**侧滑菜单按钮*/
    private ImageView iv_Drawer;
    /**listview的header*/
    private View lv_header;
    /**布局解析器*/
    private LayoutInflater inflater;
    /**控制博物馆介绍内同大小的图片按钮开*/
    private ImageView iv_home_page_introduce_open;
    /**控制博物馆介绍内同大小的图片按钮关*/
    private ImageView iv_home_page_introduce_close;
    /**快速滑动时，判断图片是否加载*/
    public boolean isPictureShow=true;
    /**handler信息类型，更新数据*/
    private final int MSG_WHAT_UPDATE_DATA = 1;
    /**listview的滑动监听器*/
    private OnListViewScrollListener onListViewScrollListener;
    /**右下角花瓣*/
    private ImageView home_page_guide_flower;
    /**屏幕宽度*/
    private int screenWidth;
    /**播放器*/
    private MediaPlayer mediaPlayer;
    private ModelChangeBroadcastReceiver modelChangeBroadcastReceiver;
    private AlertDialog progressDialog;
    private DrawerView drawerView;

    public void setOnListViewScrollListener(OnListViewScrollListener onListViewScrollListener) {
        this.onListViewScrollListener = onListViewScrollListener;
    }
    @SuppressLint("HandlerLeak")
    Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == MSG_WHAT_UPDATE_DATA) {
                if(progressDialog!=null&&progressDialog.isShowing()){
                    progressDialog.dismiss();
                }
                application.totalExhibitBeanList=currentExhibitList;
                updateDate();
            }
        }
    };
    /**listview的滚动开始和结束*/
    //private int start,end;

    @Override
    public void initialize() {
        ViewUtils.setStateBarColor(this,R.color.myOrange);
        setContentView(R.layout.activity_museum_home_page);
        application.mServiceManager.connectService();/**启动播放服务*/
        Intent intent = getIntent();
        currentMuseumId = intent.getStringExtra(INTENT_MUSEUM_ID);
        application.currentMuseumId=currentMuseumId;
        WindowManager windowManager = getWindowManager();
        Display display = windowManager.getDefaultDisplay();
        screenWidth = display.getWidth();
        init();
        initData();
        //registerReceiver();
        addListener();
        initSlidingMenu();
        /**数据初始化好之前显示加载对话框*/
        showProgressDialog();
    }

    private void showProgressDialog() {
        progressDialog = new AlertDialog.Builder(MuseumHomePageActivity.this).create();
        progressDialog.show();
        Window window = progressDialog.getWindow();
        window.setContentView(R.layout.dialog_progress);
        TextView dialog_title=(TextView)window.findViewById(R.id.dialog_title);
        dialog_title.setText("正在加载...");
    }

    private void init() {
        inflater = LayoutInflater.from(this);
        iv_Drawer = (ImageView) findViewById(R.id.iv_setting);
        tv_title = (TextView) findViewById(R.id.tv_title);
        iv_home_page_guide = (TextView) findViewById(R.id.iv_home_page_guide);
        exhibitListView = (ListView) findViewById(R.id.lv_home_page_list);
        home_page_guide_flower=(ImageView)findViewById(R.id.home_page_guide_flower);
        lv_header = inflater.inflate(R.layout.item_home_page_museum, null);
        ll_museum_largest_icon = (LinearLayout) lv_header.findViewById(R.id.ll_museum_largest_icon);
        ed_museum_search = (TextView) lv_header.findViewById(R.id.ed_museum_search);// TODO: 2015/11/6
        iv_home_page_ctrl_sound = (ImageView) lv_header.findViewById(R.id.iv_home_page_ctrl_sound);
        tv_museum_introduce_short = (TextView) lv_header.findViewById(R.id.tv_museum_introduce_short);
        tv_museum_introduce_long = (TextView) lv_header.findViewById(R.id.tv_museum_introduce_long);
        iv_home_page_introduce_open = (ImageView) lv_header.findViewById(R.id.iv_home_page_introduce_open);
        iv_home_page_introduce_close = (ImageView) lv_header.findViewById(R.id.iv_home_page_introduce_close);
        exhibitListView.addHeaderView(lv_header, null, false);

        currentExhibitList = new ArrayList<>();
        exhibitAdapter = new ExhibitAdapter(this, currentExhibitList);
        setOnListViewScrollListener(exhibitAdapter);
        exhibitListView.setAdapter(exhibitAdapter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(mediaPlayer!=null&&mediaPlayer.isPlaying()){
            mediaPlayer.pause();
            iv_home_page_ctrl_sound.setBackgroundResource(R.mipmap.headset_off);
        }
    }

    private void addListener() {

        iv_home_page_ctrl_sound.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImageView imageView=(ImageView)v;
                if(mediaPlayer.isPlaying()){
                    mediaPlayer.pause();
                    imageView.setBackgroundResource(R.mipmap.headset_off);
                }else{
                    if(application.mServiceManager!=null&&application.mServiceManager.isPlaying()){
                        application.mServiceManager.pause();
                    }
                    mediaPlayer.start();
                    imageView.setBackgroundResource(R.mipmap.headset_on);
                }
            }
        });

        home_page_guide_flower.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopupWindow(v);
                /*Intent intent = new Intent(MuseumHomePageActivity.this,TopicActivity.class);
                startActivity(intent);*/
            }
        });

        iv_Drawer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (side_drawer.isMenuShowing()) {
                    side_drawer.showContent();
                } else {
                    side_drawer.showMenu();
                }
            }
        });

        iv_home_page_introduce_open.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setVisibility(View.GONE);
                tv_museum_introduce_short.setVisibility(View.GONE);
                iv_home_page_introduce_close.setVisibility(View.VISIBLE);
                tv_museum_introduce_long.setVisibility(View.VISIBLE);

            }
        });
        iv_home_page_introduce_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setVisibility(View.GONE);
                tv_museum_introduce_long.setVisibility(View.GONE);
                iv_home_page_introduce_open.setVisibility(View.VISIBLE);
                tv_museum_introduce_short.setVisibility(View.VISIBLE);
            }
        });

        iv_home_page_guide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MuseumHomePageActivity.this, GuideActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
                startActivity(intent);
            }
        });

        exhibitListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                isPictureShow = false;
                switch (scrollState) {
                    case AbsListView.OnScrollListener.SCROLL_STATE_IDLE:
                        isPictureShow = true;
                        //LogUtil.i("OnScrollListener","停止滚动:SCROLL_STATE_IDLE" + isPictureShow);
                        onListViewScrollListener.onScroll(isPictureShow);
                        break;
                    case AbsListView.OnScrollListener.SCROLL_STATE_FLING:
                        isPictureShow = false;
                        // LogUtil.i("OnScrollListener", "手指离开屏幕，屏幕惯性滚动:SCROLL_STATE_FLING");
                        break;
                    case AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
                        isPictureShow = false;
                        // LogUtil.i("OnScrollListener", "屏幕手指正在滚动：SCROLL_STATE_TOUCH_SCROLL");
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                // start = firstVisibleItem;
                //end = firstVisibleItem + visibleItemCount;
                // LogUtil.i("MainActivity", "第一个显示的item:" + firstVisibleItem+ "当前显示：" + visibleItemCount + "尾巴位置:"+ (firstVisibleItem + visibleItemCount) + "总个数"+ totalItemCount);
            }
        });

        exhibitListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                application.currentExhibitBean = exhibitAdapter.getItem(position - 1);
                LogUtil.i("ZHANG", "exhibitListView.setOnItemClickListener::::application.currentExhibitBean = bean");
                application.refreshData();
                Intent intent = new Intent(MuseumHomePageActivity.this, GuideActivity.class);
                application.dataFrom = application.DATA_FROM_HOME;
                startActivity(intent);
            }
        });
        ed_museum_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MuseumHomePageActivity.this, SearchActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
                startActivity(intent);
            }
        });
    }

    private void showPopupWindow(View view) {
        // 一个自定义的布局，作为显示的内容
        View contentView = LayoutInflater.from(this).inflate(R.layout.pop_window, null);
        // 设置按钮的点击事件
        final PopupWindow popupWindow = new PopupWindow(contentView,
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, true);

        popupWindow.setTouchable(true);

        popupWindow.setTouchInterceptor(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                return false;
                // 这里如果返回true的话，touch事件将被拦截
                // 拦截后 PopupWindow的onTouchEvent不被调用，这样点击外部区域无法dismiss
            }
        });
        LinearLayout button = (LinearLayout) contentView.findViewById(R.id.lv_topic_btn);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MuseumHomePageActivity.this, TopicActivity.class);
                startActivity(intent);
                popupWindow.dismiss();
            }
        });
        // 如果不设置PopupWindow的背景，无论是点击外部区域还是Back键都无法dismiss弹框
        // 我觉得这里是API的一个bug
        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        //设置popwindow出现和消失动画
        //popupWindow.setAnimationStyle(R.style.PopMenuAnimation);
        // 设置好参数之后再show
        //popupWindow.showAsDropDown(view);
        /**显示在父控件上方*/
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        popupWindow.showAtLocation(view, Gravity.NO_GRAVITY, location[0] - view.getWidth() - 50, location[1] - view.getHeight() - 50);
    }

    private void initSlidingMenu() {
        drawerView =new DrawerView(this);
        side_drawer = drawerView.initSlidingMenu();
    }

    private void initData() {
        new Thread() {
            @Override
            public void run() {
                currentMuseum= getBeanById(currentMuseumId);
                BeansManageBiz biz = (BeansManageBiz) BizFactory.getBeansManageBiz(MuseumHomePageActivity.this);
                //currentMuseum = getMuseumBean(currentMuseumId);
                currentExhibitList = biz.getAllBeans(URL_TYPE_GET_EXHIBITS_BY_MUSEUM_ID, ExhibitBean.class, currentMuseumId);
                while (currentExhibitList == null) {
                }
                handler.sendEmptyMessage(MSG_WHAT_UPDATE_DATA);
            }
        }.start();
    }


    private MuseumBean tempMuseum;
    private MuseumBean getBeanById(String museumId) {
        long startTime=System.currentTimeMillis();
        DbUtils db=DbUtils.create(this);
        try {
            tempMuseum=db.findById(MuseumBean.class,museumId);
        } catch (DbException e) {
            ExceptionUtil.handleException(e);
        }
        if(tempMuseum==null){
            HttpUtils http=new HttpUtils();
            http.send(HttpRequest.HttpMethod.GET, URL_GET_MUSEUM_BY_ID + museumId, new RequestCallBack<String>() {

                @Override
                public void onSuccess(ResponseInfo<String> responseInfo) {
                   List<MuseumBean> museumList= JSON.parseArray(responseInfo.result,MuseumBean.class);
                    if(museumList!=null&&museumList.size()>0){
                        tempMuseum=museumList.get(0);
                    }

                }

                @Override
                public void onFailure(HttpException error, String msg) {

                }
            });
        }
        while(tempMuseum==null){
            if(System.currentTimeMillis()-startTime>10000){
                break;
            }
        }
        return tempMuseum;
    }

    private void updateDate() {

        if (currentMuseum != null) {
            tv_title.setText(currentMuseum.getName());
            tv_museum_introduce_short.setText(currentMuseum.getTextUrl());
            tv_museum_introduce_long.setText(currentMuseum.getTextUrl());
            /**加载博物馆介绍*/
            initAudio();
            /*加载多个Icon图片*/
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
        exhibitAdapter.updateData(currentExhibitList);
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
                    dataUrl = BASEURL + audioPath;
                }
                try {
                    mediaPlayer.setDataSource(dataUrl);
                    mediaPlayer.prepare();
                    mediaPlayer.start();
                } catch (IllegalStateException e) {
                    ExceptionUtil.handleException(e);
                } catch (IOException e) {
                    ExceptionUtil.handleException(e);
                }
            }
        }.start();

    }

    /*用于计算点击返回键时间*/
    private long mExitTime=0;
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if(side_drawer.isMenuShowing() ||side_drawer.isSecondaryMenuShowing()){
                side_drawer.showContent();
            }else {
                if ((System.currentTimeMillis() - mExitTime) > 2000) {
                    Toast.makeText(this, "在按一次退出", Toast.LENGTH_SHORT).show();
                    mExitTime = System.currentTimeMillis();
                } else {
                    application.exit();
                }
            }
            return true;
        }
        //拦截MENU按钮点击事件，让他无任何操作
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void registerReceiver() {
        modelChangeBroadcastReceiver=new ModelChangeBroadcastReceiver();
        IntentFilter intentFilter=new IntentFilter();
        intentFilter.addAction(ACTION_MODEL_CHANGED);
        registerReceiver(modelChangeBroadcastReceiver, intentFilter);
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(modelChangeBroadcastReceiver);
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }



    class ModelChangeBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
           /* if(application.guideModel==application.GUIDE_MODEL_AUTO){
                initBeaconSearcher();
            }else{
                if (mBeaconSearcher != null) {
                    mBeaconSearcher.closeSearcher();
                    mBeaconSearcher=null;
                }
            }*/
        }
    }



}
