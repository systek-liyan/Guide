package com.systek.guide.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.systek.guide.MyApplication;
import com.systek.guide.R;
import com.systek.guide.adapter.MuseumAdapter;
import com.systek.guide.biz.DataBiz;
import com.systek.guide.entity.CityBean;
import com.systek.guide.entity.MuseumBean;
import com.systek.guide.utils.ExceptionUtil;
import com.systek.guide.utils.LogUtil;

import java.util.ArrayList;
import java.util.List;


/**
 * 博物馆列表 Activity
 */
public class MuseumListActivity extends BaseActivity {

    private boolean isDataShow;
    private ListView museumListView;
    private String city;//当前所在城市
    private List<MuseumBean> museumList;//展品列表
    private MuseumAdapter adapter;//适配器
    private Handler handler;
    private static final int MSG_WHAT_REFRESH_CITY=22;


    @Override
    protected void initialize(Bundle savedInstanceState) {
        setContentView(R.layout.activity_museum_list);
        handler=new MyHandler();
        setIntent(getIntent());
        //加载视图
        initView();
        //添加监听器
        addListener();
        //加载抽屉
        initDrawer();
        //添加广播接收器
        addReceiver();
        //加载数据
        initData();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        //加载数据
        initData();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    /**
     * 加载广播接收器
     */
    private void addReceiver() {
        IntentFilter filter=new IntentFilter(ACTION_NET_IS_COMING);
        filter.addAction(ACTION_NET_IS_OUT);
        registerReceiver(receiver, filter);
    }

    /**
     * 添加监听器
     */
    private void addListener() {
        museumListView.setOnItemClickListener(onItemClickListener);
        setHomeClickListener(new View.OnClickListener() {
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
    }

    /**
     * item点击监听器
     */
    private AdapterView.OnItemClickListener  onItemClickListener= new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            MuseumBean museumBean = museumList.get(position - 1);
            Intent intent = new Intent(MuseumListActivity.this, MuseumHomeActivity.class);
            String museumStr=JSON.toJSONString(museumBean);
            intent.putExtra(INTENT_MUSEUM,museumStr);
            startActivity(intent);
            finish();
        }
    };

    /**
     * 加载数据
     */
    private void initData() {
        new Thread(){
            @Override
            public void run() {
                try{
                    Intent intent=getIntent();
                    String cityStr = intent.getStringExtra(INTENT_CITY);
                    if(TextUtils.isEmpty(cityStr)){
                        city="北京市";
                    }else{
                        CityBean bean=JSON.parseObject(cityStr,CityBean.class);
                        if(bean!=null){
                            city=bean.getName();
                        }else{
                            city="北京市";
                        }
                    }
                    handler.sendEmptyMessage(MSG_WHAT_REFRESH_CITY);

                    if(MyApplication.currentNetworkType!=INTERNET_TYPE_NONE){
                        String url=BASE_URL+URL_MUSEUM_LIST;
                        museumList=DataBiz.getEntityListFromNet(MuseumBean.class,url);
                    }
                    if(museumList!=null&&museumList.size()>0){
                        DataBiz.saveListToSQLite(museumList);
                    }
                    museumList=DataBiz.getEntityListLocalByColumn(CITY,city,MuseumBean.class);
                }catch (Exception e){
                    ExceptionUtil.handleException(e);
                    onDataError();
                }finally {
                    if(museumList==null){
                        onDataError();
                    }else if(museumList.size()==0){
                        handler.sendEmptyMessage(MSG_WHAT_UPDATE_NO_DATA);
                    } else{
                        handler.sendEmptyMessage(MSG_WHAT_UPDATE_DATA_SUCCESS);
                    }
                }
            }
        }.start();
    }

    /**
     * 加载view
     */
    private void initView() {
        long startTime=System.currentTimeMillis();
        setTitleBar();
        setHomeIcon();
        setHomeIcon(R.drawable.ic_menu);
        museumListView=(ListView)findViewById(R.id.museumListView);
        View header=getLayoutInflater().inflate(R.layout.header_museum_list,null);
        museumListView.addHeaderView(header,null,false);
        museumList=new ArrayList<>();
        adapter=new MuseumAdapter(this,museumList);
        museumListView.setAdapter(adapter);
        museumListView.setOverScrollMode(ScrollView.OVER_SCROLL_NEVER);
        LogUtil.i(getTag(), "initView执行用时==" + (System.currentTimeMillis() - startTime));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.normal_menu,menu);
        menu.getItem(0).setIcon(R.drawable.iv_tab);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent=new Intent(MuseumListActivity.this,CityChooseActivity.class);
        startActivity(intent);
        return true;
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(receiver);
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
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
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case MSG_WHAT_UPDATE_DATA_SUCCESS:
                    if(museumList==null||museumList.size()==0){return;}
                    adapter.updateData(museumList);
                    isDataShow=true;
                    break;
                case MSG_WHAT_UPDATE_DATA_FAIL:
                    showToast("数据获取失败，请检查网络...");
                    break;
                case MSG_WHAT_REFRESH_DATA:
                    initData();
                    break;
                case MSG_WHAT_UPDATE_NO_DATA:
                    adapter.updateData(museumList);
                    showToast("暂无该城市数据...");
                    break;
                case MSG_WHAT_REFRESH_CITY:
                    setTitleBarTitle(city);
                    break;
            }
        }
    }

    private BroadcastReceiver receiver=new  BroadcastReceiver(){

        @Override
        public void onReceive(Context context, Intent intent) {
            String action=intent.getAction();
            if(action.equals(ACTION_NET_IS_COMING)){
                handler.sendEmptyMessage(MSG_WHAT_REFRESH_DATA);
            }
        }
    };

}
