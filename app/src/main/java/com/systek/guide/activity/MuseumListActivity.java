package com.systek.guide.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.systek.guide.MyApplication;
import com.systek.guide.R;
import com.systek.guide.adapter.MuseumAdapter;
import com.systek.guide.biz.DataBiz;
import com.systek.guide.entity.MuseumBean;
import com.systek.guide.utils.ExceptionUtil;
import com.systek.guide.utils.LogUtil;
import com.systek.guide.utils.NetworkUtil;
import com.systek.guide.utils.Tools;

import java.lang.ref.WeakReference;
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

    private static final int MSG_WHAT_UPDATE_NO_DATA=2;
    private static final int MSG_WHAT_REFRESH_TITLE=4;
    private static final int MSG_WHAT_REFRESH_DATA=5;

    private BroadcastReceiver receiver=new  BroadcastReceiver(){

        @Override
        public void onReceive(Context context, Intent intent) {
            String action=intent.getAction();
            if(action.equals(ACTION_NET_IS_COMING)){
                handler.sendEmptyMessage(MSG_WHAT_REFRESH_DATA);
            }
        }
    };
    private DrawerLayout mDrawerLayout;
    private NavigationView navigationView;
    private ActionBarDrawerToggle mDrawerToggle;

    static class MyHandler extends Handler {

        WeakReference<MuseumListActivity> activityWeakReference;
        MyHandler(MuseumListActivity activity){
            this.activityWeakReference=new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {

            if(activityWeakReference==null){return;}
            MuseumListActivity activity=activityWeakReference.get();
            if(activity==null){return;}
            switch (msg.what){
                case MSG_WHAT_REFRESH_DATA:
                    activity.initData();
                    break;
                case MSG_WHAT_REFRESH_VIEW:
                    activity.refreshView();
                    break;
                case MSG_WHAT_REFRESH_TITLE:
                    activity.refreshTitle();
                    break;
                case MSG_WHAT_UPDATE_NO_DATA:
                    activity.onNoData();
                    break;
            }
            activity.closeDialog();
        }
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_museum_list);
        handler=new MyHandler(this);
        //加载抽屉
        initToolBar();
        initView();
        addListener();
        showDialog("正在加载...");
        initData();

    }


    private final DrawerLayout.DrawerListener mDrawerListener = new DrawerLayout.DrawerListener(){

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


    private void initToolBar() {
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        if (mToolbar == null) {
            throw new IllegalStateException("Layout is required to include a Toolbar with id 'toolbar'");
        }
        toolbarTitle = (TextView) mToolbar.findViewById(R.id.toolbar_title);
        mToolbar.inflateMenu(R.menu.museum_list_menu);
        mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Intent intent=new Intent(getActivity(),CityChooseActivity.class);
                startActivity(intent);
                return true;
            }
        });
        mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Intent intent=new Intent(getActivity(),CityChooseActivity.class);
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
                        /*case R.id.menu_4:
                            intent=new Intent(getActivity(),MuseumListActivity.class);
                            break;*/
                        case R.id.menu_5:
                            intent=new Intent(getActivity(),SettingActivity.class);
                            break;
                        default:break;
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




    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
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
    private void addListener() {
        museumListView.setOnItemClickListener(onItemClickListener);
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
                        city=cityStr;
                    }
                    museumList=DataBiz.getEntityListLocalByColumn(CITY,city,MuseumBean.class);
                    if(museumList==null){

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

                        if(MyApplication.currentNetworkType!=INTERNET_TYPE_NONE){
                            String url=BASE_URL+URL_MUSEUM_LIST;
                            museumList=DataBiz.getEntityListFromNet(MuseumBean.class,url);
                        }
                        if(museumList!=null&&museumList.size()>0){
                            DataBiz.saveListToSQLite(museumList);
                        }
                    }

                }catch (Exception e){
                    ExceptionUtil.handleException(e);
                    onDataError();
                }finally {
                    if(museumList==null){
                        onDataError();
                    }else if(museumList.size()==0){
                        handler.sendEmptyMessage(MSG_WHAT_UPDATE_NO_DATA);
                    } else{
                        handler.sendEmptyMessage(MSG_WHAT_REFRESH_VIEW);
                    }
                    handler.sendEmptyMessage(MSG_WHAT_REFRESH_TITLE);
                }
            }
        }.start();
    }


    private void refreshView() {
        if(museumList==null||museumList.size()==0){return;}
        if(adapter==null){return;}
        int mTheme= (int) Tools.getValue(this,THEME,R.style.AppTheme);
        if(mTheme!=theme){
            adapter=new MuseumAdapter(this,museumList);
            museumListView.setAdapter(adapter);
        }
        adapter.updateData(museumList);
    }


    private void refreshTitle() {
        setTitleBarTitle(city);
    }


    /**
     * 加载view
     */
    private void initView() {
        long startTime=System.currentTimeMillis();
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
    protected void onDestroy() {
        super.onDestroy();
    }
    /*用于计算点击返回键时间*/
    private long mExitTime=0;
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            /*if(drawer.isDrawerOpen()){
                drawer.closeDrawer();
            }else {  }*/
                if ((System.currentTimeMillis() - mExitTime) > 2000) {
                    Toast.makeText(this, "在按一次退出", Toast.LENGTH_SHORT).show();
                    mExitTime = System.currentTimeMillis();
                } else {
                    DataBiz.clearTempValues(this);
                    MyApplication.get().exit();

            }
            return true;
        }
        //拦截MENU按钮点击事件，让他无任何操作
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
