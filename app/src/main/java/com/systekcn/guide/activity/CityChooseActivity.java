package com.systekcn.guide.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.systekcn.guide.R;
import com.systekcn.guide.adapter.CityAdapter;
import com.systekcn.guide.biz.DataBiz;
import com.systekcn.guide.custom.ClearEditText;
import com.systekcn.guide.custom.SideBar;
import com.systekcn.guide.entity.CityBean;
import com.systekcn.guide.parser.CharacterParser;
import com.systekcn.guide.utils.ExceptionUtil;
import com.systekcn.guide.utils.PinyinComparator;
import com.systekcn.guide.utils.ViewUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CityChooseActivity extends BaseActivity{

    private Drawer drawer;


    private ListView cityListView;
    private SideBar sideBar;
    private TextView dialog;
    private CityAdapter adapter;
    private ClearEditText mClearEditText;
    private List<CityBean> cities;
    private CharacterParser characterParser;
    private PinyinComparator pinyinComparator;
    private Handler handler;
    private TextView title_bar_topic;
    private String currentCity;
    /**定位连接*/
    private LocationClient mLocationClient;

    @Override
    protected void initialize(Bundle savedInstanceState) {
        ViewUtils.setStateBarColor(this, R.color.md_red_400);
        setContentView(R.layout.activity_city_choose);
        initDrawer();
        initView();
        addListener();
        initData();

    }

    private void addListener() {

        //设置右侧触摸监听
        sideBar.setOnTouchingLetterChangedListener(new SideBar.OnTouchingLetterChangedListener() {
            @Override
            public void onTouchingLetterChanged(String s) {
                //该字母首次出现的位置
                int position = adapter.getPositionForSection(s.charAt(0));
                if (position != -1) {
                    cityListView.setSelection(position);
                }
            }
        });
        cityListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                try {
                    CityBean city = adapter.getItem(position);
                    // 这里要利用adapter.getItem(position)来获取当前position所对应的对象
                    currentCity = JSON.toJSONString(city);
                    Toast.makeText(getApplication(), city.getName(), Toast.LENGTH_SHORT).show();
                    gotoMuseumActivity();
                    finish();
                } catch (Exception e) {
                    ExceptionUtil.handleException(e);
                }


            }
        });
        //根据输入框输入值的改变来过滤搜索
        mClearEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //当输入框里面的值为空，更新为原来的列表，否则为过滤数据列表
                filterData(s.toString());
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

    }



    private void gotoMuseumActivity() {
        Intent intent = new Intent(CityChooseActivity.this, MuseumListActivity.class);
        intent.putExtra(INTENT_CITY, currentCity);
        startActivity(intent);
        disConnectBaiduSDK();
        finish();
    }

    private void disConnectBaiduSDK() {
        try{
            if(mLocationClient!=null&&mLocationClient.isStarted()){
                mLocationClient.unRegisterLocationListener(bdLocationListener);
                mLocationClient.stop();
            }
            bdLocationListener=null;
            mLocationClient=null;
        }catch (Exception e){
            ExceptionUtil.handleException(e);
        }
    }


    BDLocationListener bdLocationListener=new BDLocationListener() {

        @Override
        public void onReceiveLocation(BDLocation bdLocation) {

            try{
                currentCity = bdLocation.getCity();
			/*
			 * // 纬度 double latitude = bdLocation.getLatitude(); // 经度
			 * double longitude = bdLocation.getLongitude();
			 * LogUtil.i("定位", "纬度=" + latitude + ",经度=" + longitude);
			 */
                if (currentCity == null) {
                    //locationButton.setEnabled(false);
                    //Toast.makeText(CityActivity.this, "定位失败，请手动选择城市", Toast.LENGTH_SHORT).show();
                    // mLocationClient.stop();
                } else {
                    //locationButton.setText(currentCity);
                    //buildDialog();
                }
            }catch (Exception e){
                ExceptionUtil.handleException(e);
            }

        }
    };

    /**
     * 根据输入框中的值来过滤数据并更新ListView
     * @param filterStr edittext 输入的关键字
     */
    private void filterData(String filterStr){
        List<CityBean> filterDateList = new ArrayList<CityBean>();
        if(cities==null){return;}
        if(TextUtils.isEmpty(filterStr)){
            filterDateList = cities;
        }else{
            filterDateList.clear();
            for(CityBean city : cities){
                String name = city.getName();
                if(name.contains(filterStr) || characterParser.getSelling(name).startsWith(filterStr)){
                    filterDateList.add(city);
                }
            }
        }
        // 根据a-z进行排序
        Collections.sort(filterDateList, pinyinComparator);
        adapter.updateListView(filterDateList);
    }


    private void initView() {
        handler=new MyHandler();
        //实例化汉字转拼音类
        characterParser = CharacterParser.getInstance();
        pinyinComparator = new PinyinComparator();
        sideBar = (SideBar) findViewById(R.id.sidrbar);
        dialog = (TextView) findViewById(R.id.dialog);
        title_bar_topic = (TextView) findViewById(R.id.titleBarTopic);
        sideBar.setTextView(dialog);
        cityListView = (ListView) findViewById(R.id.country_lvcountry);
        mClearEditText = (ClearEditText) findViewById(R.id.filter_edit);
        cities = new ArrayList<>();
        // 根据a-z进行排序源数据
        Collections.sort(cities, pinyinComparator);
        adapter = new CityAdapter(this, cities);
        cityListView.setAdapter(adapter);
    }

    private void initData() {
        new Thread(){
            @Override
            public void run() {
                cities=DataBiz.getEntityListLocal(CityBean.class);
                if(cities==null||cities.size()==0){
                    cities=DataBiz.getEntityListFromNet(CityBean.class,URL_CITY_LIST);
                    if(cities!=null&&cities.size()>0){DataBiz.saveListToSQLite(cities);}
                }
                int msg=MSG_WHAT_UPDATE_DATA_SUCCESS;
                if(cities==null||cities.size()==0){
                    msg=MSG_WHAT_UPDATE_DATA_FAIL;
                }
                handler.sendEmptyMessage(msg);
            }
        }.start();
    }

    private void initDrawer() {
        drawer = new DrawerBuilder()
                .withActivity(this)
                .withFullscreen(true)
                .withHeader(R.layout.header)
                .inflateMenu(R.menu.drawer_menu)
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        Class<?>  targetClass=null;
                        switch (position){
                            case 1:
                                targetClass=DownloadActivity.class;
                                break;
                            case 2:
                                targetClass=CollectionActivity.class;
                                break;
                            case 3:
                                targetClass=CityChooseActivity.class;
                                break;
                            case 4:
                                targetClass=MuseumListActivity.class;
                                break;
                            case 5:
                                targetClass=SettingActivity.class;
                                break;
                        }
                        Intent intent=new Intent(CityChooseActivity.this,targetClass);
                        startActivity(intent);
                        return false;
                    }
                }).build();
    }

    class MyHandler extends  Handler{
        @Override
        public void handleMessage(Message msg) {
            if(msg.what==MSG_WHAT_UPDATE_DATA_SUCCESS){
                adapter.updateListView(cities);
            }
        }
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}
