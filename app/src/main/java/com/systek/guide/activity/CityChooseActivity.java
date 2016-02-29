package com.systek.guide.activity;

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
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.systek.guide.R;
import com.systek.guide.adapter.CityAdapter;
import com.systek.guide.biz.DataBiz;
import com.systek.guide.custom.ClearEditText;
import com.systek.guide.custom.SideBar;
import com.systek.guide.entity.CityBean;
import com.systek.guide.parser.CharacterParser;
import com.systek.guide.utils.ExceptionUtil;
import com.systek.guide.utils.LogUtil;
import com.systek.guide.utils.PinyinComparator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CityChooseActivity extends BaseActivity  implements AMapLocationListener {


    private ListView cityListView;//城市列表
    private SideBar sideBar;//自定义控件，右侧abc...
    private CityAdapter adapter;
    private ClearEditText mClearEditText;
    private List<CityBean> cities;
    private CharacterParser characterParser;
    private PinyinComparator pinyinComparator;
    private Handler handler;
    private String currentCity;
    private AMapLocationClient locationClient;
    private AMapLocationClientOption locationOption;


    @Override
    protected void initialize(Bundle savedInstanceState) {
        setContentView(R.layout.activity_city_choose);
        setTitleBar();
        initDrawer();
        initView();
        addListener();
        initData();
        //加载高德地图
        initLocation();

    }

    private void initLocation() {// TODO: 2016/2/16
        locationClient = new AMapLocationClient(this.getApplicationContext());
        locationOption = new AMapLocationClientOption();
        // 设置定位模式为高精度模式
        locationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        // 设置定位监听
        locationClient.setLocationListener(this);
        // 设置定位参数
        locationClient.setLocationOption(locationOption);
        // 启动定位
        locationClient.startLocation();
    }

    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        LogUtil.i("ZHANG", "执行了onLocationChanged");
        LogUtil.i("ZHANG", aMapLocation.getCity());
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
        finish();
    }


    /**
     * 根据输入框中的值来过滤数据并更新ListView
     * @param filterStr edittext 输入的关键字
     */
    private void filterData(String filterStr){
        List<CityBean> filterDateList = new ArrayList<>();
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

        setTitleBarTitle("城市选择");
        handler=new MyHandler();
        setHomeIcon();
        toolbar.setNavigationOnClickListener(backOnClickListener);
        //实例化汉字转拼音类
        characterParser = CharacterParser.getInstance();
        pinyinComparator = new PinyinComparator();
        sideBar = (SideBar) findViewById(R.id.sidebar);
        TextView dialog = (TextView) findViewById(R.id.dialog);
        sideBar.setTextView(dialog);
        cityListView = (ListView) findViewById(R.id.country_lvcountry);
        mClearEditText = (ClearEditText) findViewById(R.id.filter_edit);
        mClearEditText.clearFocus();
        cities = new ArrayList<>();
        // 根据a-z进行排序源数据
        Collections.sort(cities, pinyinComparator);
        adapter = new CityAdapter(this, cities);
        cityListView.setAdapter(adapter);
        //去除滑动到末尾时的阴影
        cityListView.setOverScrollMode(ScrollView.OVER_SCROLL_NEVER);

    }


    private void initData() {
        new Thread(){
            @Override
            public void run() {
                cities=DataBiz.getEntityListLocal(CityBean.class);
                if(cities==null||cities.size()==0){
                    String url=BASE_URL+URL_CITY_LIST;
                    cities=DataBiz.getEntityListFromNet(CityBean.class,url);
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
        if (null != locationClient) {
            /**
             * 如果AMapLocationClient是在当前Activity实例化的，
             * 在Activity的onDestroy中一定要执行AMapLocationClient的onDestroy
             */
            locationClient.onDestroy();
            locationClient = null;
            locationOption = null;
        }

    }
}
