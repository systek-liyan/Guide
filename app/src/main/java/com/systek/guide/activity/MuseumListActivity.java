package com.systek.guide.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
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
    private Receiver receiver;//广播接收器
    private TextView titleBarTopic;//标题
    private ImageView titleBarDrawer;
    private ImageView titleBarTab;

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
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        LogUtil.i("ZHANG", "onNewIntent");
    }

    @Override
    protected void onStart() {
        super.onStart();
        initData();
        LogUtil.i("ZHANG", "执行了onStart");
    }

    /**
     * 加载广播接收器
     */
    private void addReceiver() {
        receiver=new Receiver();
        IntentFilter filter=new IntentFilter(ACTION_NET_IS_COMMING);
        filter.addAction(ACTION_NET_IS_OUT);
        registerReceiver(receiver,filter);
    }

    /**
     * 添加监听器
     */
    private void addListener() {
        museumListView.setOnItemClickListener(onItemClickListener);
        titleBarDrawer.setOnClickListener(onClickListener);
        titleBarTab.setOnClickListener(onClickListener);
    }

    /**
     * view点击监听器
     */
    private View.OnClickListener onClickListener=new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.titleBarDrawer:
                    if(drawer==null){return;}
                    if(drawer.isDrawerOpen()){
                        drawer.closeDrawer();
                    }else {
                        drawer.openDrawer();
                    }
                    break;
                case R.id.titleBarRightImg:
                    Intent intent=new Intent(MuseumListActivity.this,CityChooseActivity.class);
                    startActivity(intent);
                    break;

            }
        }
    };

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
        Intent intent=getIntent();
        String cityStr=intent.getStringExtra(INTENT_CITY);
        CityBean bean=JSON.parseObject(cityStr,CityBean.class);
        if(bean!=null){
            city=bean.getName();
        }else{
            city="北京市";
        }
        titleBarTopic.setText(city);
        LogUtil.i("ZHANG", "当前城市为" + city);
        new Thread(){
            @Override
            public void run() {
                try{
                    if(MyApplication.currentNetworkType!=INTERNET_TYPE_NONE){
                        museumList=DataBiz.getEntityListFromNet(MuseumBean.class,URL_MUSEUM_LIST);
                    }
                    if(museumList!=null&&museumList.size()>0){
                        //LogUtil.i("ZHANG", "数据获取成功");
                        //boolean isSaveTrue=DataBiz.deleteSQLiteDataFromClass(MuseumBean.class);
                        //LogUtil.i("ZHANG","数据删除"+isSaveTrue);
                        boolean isSaveTrue2=DataBiz.saveListToSQLite(museumList);
                        LogUtil.i("ZHANG","数据保存"+isSaveTrue2);
                    }
                    museumList=DataBiz.getEntityListLocalByColumn(CITY,city,MuseumBean.class);

                }catch (Exception e){
                    ExceptionUtil.handleException(e);
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
        titleBarTopic=(TextView)findViewById(R.id.titleBarTopic);
        titleBarTab=(ImageView)findViewById(R.id.titleBarRightImg);
        titleBarDrawer=(ImageView)findViewById(R.id.titleBarDrawer);
        titleBarTab.setImageDrawable(getResources().getDrawable(R.drawable.iv_tab));
        titleBarDrawer.setImageDrawable(getResources().getDrawable(R.drawable.setting));
        museumListView=(ListView)findViewById(R.id.museumListView);
        View header=getLayoutInflater().inflate(R.layout.header_museum_list,null);
        museumListView.addHeaderView(header,null,false);
        museumList=new ArrayList<>();
        adapter=new MuseumAdapter(this,museumList);
        museumListView.setAdapter(adapter);
        museumListView.setOverScrollMode(ScrollView.OVER_SCROLL_NEVER);
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
                   showToast("暂无改城市数据...");
                   break;
           }
        }
    }

    class Receiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            String action=intent.getAction();
            if(action.equals(ACTION_NET_IS_COMMING)){
                handler.sendEmptyMessage(MSG_WHAT_REFRESH_DATA);
            }
        }
    }

}
