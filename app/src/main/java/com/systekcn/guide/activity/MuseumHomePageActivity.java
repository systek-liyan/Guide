package com.systekcn.guide.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.lidroid.xutils.DbUtils;
import com.lidroid.xutils.exception.DbException;
import com.systekcn.guide.MyApplication;
import com.systekcn.guide.R;
import com.systekcn.guide.adapter.ExhibitAdapter;
import com.systekcn.guide.adapter.OnListViewScrollListener;
import com.systekcn.guide.beacon.BeaconSearcher;
import com.systekcn.guide.beacon.NearestBeacon;
import com.systekcn.guide.biz.BeansManageBiz;
import com.systekcn.guide.biz.BizFactory;
import com.systekcn.guide.common.utils.ExceptionUtil;
import com.systekcn.guide.common.utils.ImageLoaderUtil;
import com.systekcn.guide.common.utils.LogUtil;
import com.systekcn.guide.common.utils.Tools;
import com.systekcn.guide.entity.BeaconBean;
import com.systekcn.guide.entity.ExhibitBean;
import com.systekcn.guide.entity.MuseumBean;
import com.systekcn.guide.widget.DrawerView;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.Identifier;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MuseumHomePageActivity extends BaseActivity {

    private String currentMuseumId;
    /* 侧滑菜单 */
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
    private MediaPlayer mediaPlayer;

    /**蓝牙扫描对象*/
    private static BeaconSearcher mBeaconSearcher;
    /**
     * Bluetooth 设备可见时间，单位：秒。
     */
    private static final int BLUETOOTH_DISCOVERABLE_DURATION = 250;
    /**
     * 自定义的打开 Bluetooth 的请求码，与 onActivityResult 中返回的 requestCode 匹配。
     */
    private static final int REQUEST_CODE_BLUETOOTH_ON = 1313;
    private ModelChangeBroadcastReceiver modelChangeBroadcastReceiver;

    public void setOnListViewScrollListener(OnListViewScrollListener onListViewScrollListener) {
        this.onListViewScrollListener = onListViewScrollListener;
    }
    @SuppressLint("HandlerLeak")
    Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == MSG_WHAT_UPDATE_DATA) {
                updateDate();
                application.totalExhibitBeanList=currentExhibitList;
            }
        }
    };
    /**listview的滚动开始和结束*/
    //private int start,end;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_museum_home_page);
        Intent intent = getIntent();
        currentMuseumId = intent.getStringExtra(INTENT_MUSEUM_ID);
        application= (MyApplication) getApplication();
        application.currentMuseumId=currentMuseumId;
        WindowManager windowManager = getWindowManager();
        Display display = windowManager.getDefaultDisplay();
        screenWidth = display.getWidth();
        if(application.guideModel==application.GUIDE_MODEL_AUTO){
            initBeaconSearcher();
        }
        initialize();
    }

    private void initialize() {
        registerReceiver();
        initView();
        addListener();
        initSlidingMenu();
        initData();
    }

    private void initView() {
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
        if(mediaPlayer.isPlaying()){
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
                    mediaPlayer.start();
                    imageView.setBackgroundResource(R.mipmap.headset_on);
                }
            }
        });

        home_page_guide_flower.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MuseumHomePageActivity.this,TopicActivity.class);
                startActivity(intent);
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
                application.currentExhibitBean = application.totalExhibitBeanList.get(0);
                application.currentExhibitBeanList.add(application.currentExhibitBean);
                application.refreshData();
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
                ExhibitBean bean = exhibitAdapter.getItem(position - 1);
                application.currentExhibitBean = bean;
                LogUtil.i("ZHANG","exhibitListView.setOnItemClickListener::::application.currentExhibitBean = bean");
                application.refreshData();
                Intent intent = new Intent(MuseumHomePageActivity.this, GuideActivity.class);
                application.dataFrom = application.DATA_FROM_HOME;
                startActivity(intent);
            }
        });
        ed_museum_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent =new Intent(MuseumHomePageActivity.this,SearchActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
                startActivity(intent);
            }
        });
        /*ed_museum_search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                List<ExhibitBean> list =null;
                DbUtils db=DbUtils.create(MuseumHomePageActivity.this);
                try {
                    list= db.findAll(Selector.from(ExhibitBean.class).where("name","like","%"+s+"%"));
                } catch (DbException e) {
                    ExceptionUtil.handleException(e);
                }
                exhibitAdapter.updateData(list);
            }
            @Override
            public void afterTextChanged(Editable s) {
                LogUtil.i("zhang","afterTextChanged");
            }
        });*/
    }

    private void initSlidingMenu() {
        DrawerView dv =new DrawerView(this);
        side_drawer = dv.initSlidingMenu();
    }

    private void initData() {
        new Thread() {
            @Override
            public void run() {
                currentMuseum = getMuseumBean(currentMuseumId);
                BeansManageBiz biz = (BeansManageBiz) BizFactory.getBeansManageBiz(MuseumHomePageActivity.this);
                currentExhibitList = biz.getAllBeans(URL_TYPE_GET_EXHIBITS_BY_MUSEUM_ID, ExhibitBean.class, currentMuseumId);
                while (currentExhibitList == null) {
                }
                handler.sendEmptyMessage(MSG_WHAT_UPDATE_DATA);
            }
        }.start();
    }

    private MuseumBean getMuseumBean(String museumId) {
        MuseumBean bean = null;
        DbUtils db = DbUtils.create(MuseumHomePageActivity.this);
        try {
            bean = db.findById(MuseumBean.class, museumId);
        } catch (DbException e) {
            ExceptionUtil.handleException(e);
        }finally{
            if(db!=null){
                db.close();
            }
        }
        return bean;
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
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
        registerReceiver(modelChangeBroadcastReceiver,intentFilter);
    }

    @Override
    protected void onDestroy() {
        if (mBeaconSearcher != null) {
            mBeaconSearcher.closeSearcher();
            mBeaconSearcher=null;
        }
        unregisterReceiver(modelChangeBroadcastReceiver);
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // requestCode 与请求开启 Bluetooth 传入的 requestCode 相对应
        if (requestCode == REQUEST_CODE_BLUETOOTH_ON) {
            switch (resultCode) {
                // 点击确认按钮
                case Activity.RESULT_OK:
                    LogUtil.i("ZHANG", "用户选择开启 Bluetooth，Bluetooth 会被开启");
                    initBeaconSearcher();
                    break;
                // 点击取消按钮或点击返回键
                case Activity.RESULT_CANCELED:
                    LogUtil.i("ZHANG", "用户拒绝打开 Bluetooth, Bluetooth 不会被开启");
                    break;
            }
        }
    }

    /**
     * 实现beacon搜索监听，或得BeaconSearcher搜索到的beacon对象
     */
    private BeaconSearcher.OnNearestBeaconListener onNearestBeaconListener=new BeaconSearcher.OnNearestBeaconListener(){

        @Override
        public void getNearestBeacon(int type,Beacon beacon) {
            if (beacon == null) {//&& application.guideModel==application.GUIDE_MODEL_AUTO
            } else {
                try{
                    Identifier major = beacon.getId2();
                    Identifier minor = beacon.getId3();
                    LogUtil.i("ZHANG","major===="+major+","+"minor==="+minor);
                    BeansManageBiz biz = (BeansManageBiz) BizFactory.getBeansManageBiz(MuseumHomePageActivity.this);
                    BeaconBean b = biz.getBeaconMinorAndMajor(minor, major);
                    if (b != null) {
                        String beaconId = b.getId();
                        LogUtil.i("ZHANG", beaconId);
                        if (beaconId != null &&
                                (application.getCurrentBeaconId().equals("")||!beaconId.equals(application.getCurrentBeaconId()))) {
                            List<ExhibitBean> nearlyExhibitsList = biz.getExhibitListByBeaconId(application.getCurrentMuseumId(), beaconId);
                            if (nearlyExhibitsList != null && nearlyExhibitsList.size() > 0) {
                                //LogUtil.i("ZHANG", nearlyExhibitsList.toString());
                                application.currentExhibitBeanList=nearlyExhibitsList;
                                application.currentExhibitBean=nearlyExhibitsList.get(0);
                                application.refreshData();
                                application.dataFrom=application.DATA_FROM_BEACON;// TODO: 2015/11/3
                                Intent intent =new Intent();
                                intent.setAction(ACTION_NOTIFY_CURRENT_EXHIBIT_CHANGE);
                                sendBroadcast(intent);
                            }
                        }
                    }
                }catch (Exception e){
                    ExceptionUtil.handleException(e);
                }
            }
        }
    };

    public void initBeaconSearcher() {
        if(mBeaconSearcher==null){
            // 设定用于展品定位的最小停留时间(ms)
            mBeaconSearcher = BeaconSearcher.getInstance(this);
            // NearestBeacon.GET_EXHIBIT_BEACON：展品定位beacon
            // NearestBeacon.GET_EXHIBIT_BEACON：游客定位beacon。可以不用设置上述的最小停留时间和最小距离
            mBeaconSearcher.setMin_stay_milliseconds(2000);
            // 设定用于展品定位的最小距离(m)
            mBeaconSearcher.setExhibit_distance(2.0);
            // 设置获取距离最近的beacon类型
            mBeaconSearcher.setNearestBeaconType(NearestBeacon.GET_EXHIBIT_BEACON);
            // 当蓝牙打开时，打开beacon搜索器，开始搜索距离最近的Beacon
            // 设置beacon监听器
            mBeaconSearcher.setNearestBeaconListener(onNearestBeaconListener);
            // 添加导游模式切换监听
            if (mBeaconSearcher!=null&&mBeaconSearcher.checkBLEEnable()) {
                mBeaconSearcher.openSearcher();
            } else {
                // 请求打开 Bluetooth
                Intent requestBluetoothOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                // 设置 Bluetooth 设备可以被其它 Bluetooth 设备扫描到
                requestBluetoothOn.setAction(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                // 设置 Bluetooth 设备可见时间
                requestBluetoothOn.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, BLUETOOTH_DISCOVERABLE_DURATION);
                // 请求开启 Bluetooth
                this.startActivityForResult(requestBluetoothOn, REQUEST_CODE_BLUETOOTH_ON);
            }
        }
    }

    class ModelChangeBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if(application.guideModel==application.GUIDE_MODEL_AUTO){
                initBeaconSearcher();
            }else{
                if (mBeaconSearcher != null) {
                    mBeaconSearcher.closeSearcher();
                    mBeaconSearcher=null;
                }
            }
        }
    }

}
