package com.systekcn.guide.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.systekcn.guide.R;
import com.systekcn.guide.activity.base.BaseActivity;
import com.systekcn.guide.common.IConstants;
import com.systekcn.guide.common.utils.ViewUtils;

public class MuseumHomeActivity extends BaseActivity implements IConstants{

    private String currentMuseumId;
    private int screenWidth;
    private MyHandler handler;
    private final int MSG_WHAT_UPDATE_DATA=1;
    private AlertDialog progressDialog;
    private LayoutInflater inflater;

    @Override
    protected void initialize() {
        ViewUtils.setStateBarColor(this, R.color.myOrange);
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


        /**数据初始化好之前显示加载对话框*/
        showProgressDialog();
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

        /*inflater = LayoutInflater.from(this);
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
        exhibitListView.setAdapter(exhibitAdapter);*/
    }




    class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MSG_WHAT_UPDATE_DATA) {
                if(progressDialog!=null&&progressDialog.isShowing()){
                    progressDialog.dismiss();
                }
                /*application.totalExhibitBeanList=currentExhibitList;
                updateDate();*/
            }
        }
    }
}
