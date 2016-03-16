package com.systek.guide.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.Toast;

import com.systek.guide.MyApplication;
import com.systek.guide.R;
import com.systek.guide.adapter.MuseumAdapter;
import com.systek.guide.biz.DataBiz;
import com.systek.guide.download.TasksManager;
import com.systek.guide.entity.MuseumBean;
import com.systek.guide.utils.ExceptionUtil;
import com.systek.guide.utils.LogUtil;
import com.systek.guide.utils.NetworkUtil;

import java.util.ArrayList;
import java.util.List;


/**
 * 博物馆列表 Activity
 */
public class MuseumListActivity extends BaseActivity {

    private ListView museumListView;
    private String city;//当前所在城市
    private List<MuseumBean> museumList;//展品列表
    private MuseumAdapter adapter;//适配器
    private static final int MSG_WHAT_REFRESH_CITY=22;


    @Override
    protected void setView() {
        View view = View.inflate(this, R.layout.activity_museum_list, null);
        setContentView(view);
        //加载抽屉
        initDrawer();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        //加载数据
        initData();
    }



    /**
     * 添加监听器
     */
    @Override
    void addListener() {
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
           // String museumStr=JSON.toJSONString(museumBean);
            String museumId= museumBean.getId();
            //intent.putExtra(INTENT_MUSEUM,museumStr);
            intent.putExtra(INTENT_MUSEUM_ID,museumId);
            startActivity(intent);
            finish();
        }
    };

    /**
     * 加载数据
     */
    @Override
    void initData() {
        showDialog("正在加载...");
        if(!NetworkUtil.isOnline(MuseumListActivity.this)){
            showErrors(true);
            refreshBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mErrorView.setVisibility(View.GONE);
                    initData();
                }
            });
            closeDialog();
            return;
        }
        new Thread(){
            @Override
            public void run() {
                try{
                    Intent intent=getIntent();
                    String cityStr = intent.getStringExtra(INTENT_CITY);
                    if(TextUtils.isEmpty(cityStr)){
                        city="北京市";
                    }else{
                        city=cityStr;
                    }
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
                    handler.sendEmptyMessage(MSG_WHAT_REFRESH_TITLE);
                }
            }
        }.start();
    }

    @Override
    void registerReceiver() {
        IntentFilter filter=new IntentFilter(ACTION_NET_IS_COMING);
        filter.addAction(ACTION_NET_IS_OUT);
        registerReceiver(receiver, filter);
    }

    @Override
    void unRegisterReceiver() {
        unregisterReceiver(receiver);
    }

    @Override
    void refreshView() {
        if(museumList==null||museumList.size()==0){return;}
        adapter.updateData(museumList);

        TasksManager.getImpl().addTask(museumList.get(0)); // TODO: 2016/3/9
    }

    @Override
    void refreshExhibit() {

    }

    @Override
    void refreshTitle() {
        setTitleBarTitle(city);
    }

    @Override
    void refreshViewBottomTab() {

    }

    @Override
    void refreshProgress() {

    }

    @Override
    void refreshIcon() {

    }

    @Override
    void refreshState() {

    }

    /**
     * 加载view
     */
    @Override
    void initView() {
        long startTime=System.currentTimeMillis();
        setTitleBar();
        setHomeIcon();
        setHomeIcon(R.drawable.ic_menu);
        museumListView=(ListView)findViewById(R.id.museumListView);
        mErrorView=findViewById(R.id.mErrorView);
        refreshBtn=(Button)mErrorView.findViewById(R.id.refreshBtn);
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
        getMenuInflater().inflate(R.menu.normal_menu, menu);
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
