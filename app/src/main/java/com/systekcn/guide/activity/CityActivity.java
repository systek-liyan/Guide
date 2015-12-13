package com.systekcn.guide.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.systekcn.guide.R;
import com.systekcn.guide.activity.base.BaseActivity;
import com.systekcn.guide.adapter.CityAdapter;
import com.systekcn.guide.biz.BeansManageBiz;
import com.systekcn.guide.biz.BizFactory;
import com.systekcn.guide.common.IConstants;
import com.systekcn.guide.common.utils.ExceptionUtil;
import com.systekcn.guide.common.utils.NetworkUtil;
import com.systekcn.guide.common.utils.ViewUtils;
import com.systekcn.guide.custom.DrawerView;
import com.systekcn.guide.custom.SideBar;
import com.systekcn.guide.custom.slidingmenu.SlidingMenu;
import com.systekcn.guide.entity.CityBean;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class CityActivity extends BaseActivity implements IConstants{
    /**加载数据对话框*/
    private AlertDialog progressDialog;
    /**定位连接*/
    private LocationClient mLocationClient;
    /**右侧显示字母控件*/
    private SideBar sideBar;
    /**选中字母的对话框*/
    /**侧边栏*/
    private SlidingMenu side_drawer;
    /***/
    private CityAdapter adapter;
    /** 汉字转换成拼音的类*/
    //private CharacterParser characterParser;
    private List<CityBean> cities;
    private  final int MSG_WHAT_CITIES=1;
    /**
     * 根据拼音来排列ListView里面的数据类
     */
    private PinyinComparator pinyinComparator;
    private ListView cityListView;
    private TextView locationButton;
    // private View view;
    private ImageView iv_city_drawer;
    private String currentCity;
    private Handler handler;


    @Override
    public void initialize() {
        ViewUtils.setStateBarColor(this, R.color.myOrange);
        setContentView(R.layout.activity_city);
        try{
            initHandler();
            initData();
            initViews();
            initAdapter();
            addListener();
            initSlidingView();
            /**数据初始化好之前显示加载对话框*/
            showProgressDialog();
            initLocationConnect();
        }catch (Exception e){
            ExceptionUtil.handleException(e);
        }

    }

    private void initHandler() {
        handler=new MyHandler();
    }

    private void initLocationConnect() {
        int netState= NetworkUtil.checkNet(this);
        if(netState==INTERNET_TYPE_NONE){
            if(progressDialog!=null&&progressDialog.isShowing()){
                progressDialog.dismiss();
            }
            Toast.makeText(this, "当前无网络连接", Toast.LENGTH_SHORT).show();
        }else{
            initLocation();
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        disConnectBaiduSDK();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        disConnectBaiduSDK();
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
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


    private void showProgressDialog() {
        progressDialog = new AlertDialog.Builder(CityActivity.this).create();
        progressDialog.show();
        Window window = progressDialog.getWindow();
        window.setContentView(R.layout.dialog_progress);
        TextView dialog_title=(TextView)window.findViewById(R.id.dialog_title);
        dialog_title.setText("正在加载...");
    }

    private void initSlidingView() {
        DrawerView dv =new DrawerView(this);
        side_drawer = dv.initSlidingMenu();
    }

    private void initAdapter() {

        try{
            cities=new ArrayList<>();
            pinyinComparator = new PinyinComparator();
            // 自定义Adapter
            adapter = new CityAdapter(this, cities);
            cityListView.setAdapter(adapter);
        }catch (Exception e){
            ExceptionUtil.handleException(e);
        }
    }

    private void initViews() {
        // 实例化汉字转拼音类
        //characterParser = CharacterParser.getInstance();
        cityListView = (ListView) findViewById(R.id.city_list);
        sideBar = (SideBar) findViewById(R.id.sidebar);
        TextView dialog = (TextView) findViewById(R.id.city_dialog);
        iv_city_drawer = (ImageView) findViewById(R.id.iv_city_drawer);
        sideBar.setTextView(dialog);
        locationButton = (TextView) findViewById(R.id.city_btn_location);
		/*
		 * mClearEditText = (ClearEditText) findViewById(R.id.filter_edit);
		 *
		 * //根据输入框输入值的改变来过滤搜索 mClearEditText.addTextChangedListener(new
		 * TextWatcher() {
		 *
		 * @Override public void onTextChanged(CharSequence s, int start, int
		 * before, int count) { //当输入框里面的值为空，更新为原来的列表，否则为过滤数据列表
		 * filterData(s.toString()); }
		 *
		 * @Override public void beforeTextChanged(CharSequence s, int start,
		 * int count, int after) { }
		 *
		 * @Override public void afterTextChanged(Editable s) { } });
		 */
    }

    private void addListener() {
        // 设置右侧触摸监听
        sideBar.setOnTouchingLetterChangedListener(new SideBar.OnTouchingLetterChangedListener() {

            @Override
            public void onTouchingLetterChanged(String s) {
                // 该字母首次出现的位置
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
                    CityBean city = (CityBean) cityListView.getAdapter().getItem(position);
                    // 这里要利用adapter.getItem(position)来获取当前position所对应的对象
                    currentCity = city.getName();
                    Toast.makeText(getApplication(), currentCity, Toast.LENGTH_SHORT).show();
                    gotoMuseumActivity();
                    finish();
                } catch (Exception e) {
                    ExceptionUtil.handleException(e);
                }
            }
        });

        iv_city_drawer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (side_drawer.isMenuShowing()) {
                        side_drawer.showContent();
                    } else {
                        side_drawer.showMenu();
                    }
                } catch (Exception e) {
                    ExceptionUtil.handleException(e);
                }
            }
        });

        locationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView tv=(TextView)v;
                String text= (String) ((TextView) v).getText();
                if(!text.equals("正在定位...")){
                    gotoMuseumActivity();
                    finish();
                }
            }
        });


    }

    /* 获取数据 */
    private void initData() {

        try{
            new Thread(){
                public void run() {
                    BeansManageBiz biz=(BeansManageBiz) BizFactory.getBeansManageBiz(CityActivity.this);
                    cities=biz.getAllBeans(URL_TYPE_GET_CITY,CityBean.class,"");
                    while(cities==null){}
                    handler.sendEmptyMessage(MSG_WHAT_CITIES);
                }
            }.start();
        }catch (Exception e){
            ExceptionUtil.handleException(e);
        }
    }

    /**
     * 为ListView填充数据
     *
     * @param data
     * @return
     *
     * 		private List<CityModule> filledData(String [] data){ List
     *         <CityModule> mSortList = new ArrayList<CityModule>();
     *
     *         for(int i=0; i<data.length; i++){ CityModule cityModule = new
     *         CityModule(); cityModule.setName(data[i]); //汉字转换成拼音 String
     *         pinyin = characterParser.getSelling(data[i]); String sortString =
     *         pinyin.substring(0, 1).toUpperCase();
     *
     *         // 正则表达式，判断首字母是否是英文字母 if(sortString.matches("[A-Z]")){
     *         cityModule.setAlpha(sortString.toUpperCase()); }else{
     *         cityModule.setAlpha("#"); } mSortList.add(cityModule); } return
     *         mSortList;
     *
     *         }
     */

    /**
     * 根据输入框中的值来过滤数据并更新ListView 待用
     *
     *
     *            private void filterData(String filterStr) { List
     *            <CityModule> filterDateList = new ArrayList<CityModule>();
     *
     *            if (TextUtils.isEmpty(filterStr)) { filterDateList = cities; }
     *            else { filterDateList.clear(); for (CityModule sortModel :
     *            cities) { String name = sortModel.getName(); if
     *            (name.toUpperCase().indexOf(
     *            filterStr.toString().toUpperCase()) != -1 ||
     *            characterParser.getSelling(name).toUpperCase()
     *            .startsWith(filterStr.toString().toUpperCase())) {
     *            filterDateList.add(sortModel); } } }
     *
     *            // 根据a-z进行排序 Collections.sort(filterDateList,
     *            pinyinComparator); adapter.updateListView(filterDateList); }
     */

    public class PinyinComparator implements Comparator<CityBean> {

        public int compare(CityBean o1, CityBean o2) {
            // 这里主要是用来对ListView里面的数据根据ABCDEFG...来排序
            if (o2.getAlpha().equals("#")) {
                return -1;
            } else if (o1.getAlpha().equals("#")) {
                return 1;
            } else {
                return o1.getAlpha().compareTo(o2.getAlpha());
            }
        }
    }

    private void initLocation() {
        try {
            LocationClientOption option = new LocationClientOption();
            option.setScanSpan(1);
            option.setLocationNotify(true);//可选，默认false，设置是否当gps有效时按照1S1次频率输出GPS结果
            option.setOpenGps(true);//可选，默认false,设置是否使用gps
            option.setIsNeedAddress(true);
            option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
            option.setCoorType("bd0911");
            mLocationClient = new LocationClient(this);
            mLocationClient.setLocOption(option);
            mLocationClient.registerLocationListener(bdLocationListener);
            mLocationClient.start();
        } catch (Exception e) {
            ExceptionUtil.handleException(e);
        }
    }

    /**
     * 更新ListView中的数据
     * @param cityList
     */
    public void updateListView(List<CityBean> cityList) {
        this.cities = cityList;
        try{
            if (cities!=null) {
                Collections.sort(cities, pinyinComparator);
                adapter.updateListView(cities);
                if(progressDialog!=null&&progressDialog.isShowing()){
                    progressDialog.dismiss();
                }
            }
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
                    locationButton.setEnabled(false);
                    //Toast.makeText(CityActivity.this, "定位失败，请手动选择城市", Toast.LENGTH_SHORT).show();
                    mLocationClient.stop();
                } else {
                    locationButton.setText(currentCity);
                    //buildDialog();
                }
            }catch (Exception e){
                ExceptionUtil.handleException(e);
            }

        }
    };


    private void buildDialog() {
        try{
            AlertDialog.Builder builder = new AlertDialog.Builder(CityActivity.this);
            builder.setMessage("当期城市为"+currentCity+",是否选择？");
            builder.setTitle("提示");
            builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    gotoMuseumActivity();
                    dialog.dismiss();
                    finish();
                }

            });

            builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            AlertDialog dialog=builder.create();
            dialog.setCanceledOnTouchOutside(true);
            dialog.show();
        }catch (Exception e){
            ExceptionUtil.handleException(e);
        }
    }

    private void gotoMuseumActivity() {
        Intent intent = new Intent(CityActivity.this, MuseumListActivity.class);
        intent.putExtra("city", currentCity);
        startActivity(intent);
        disConnectBaiduSDK();
        finish();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if(side_drawer.isMenuShowing() ||side_drawer.isSecondaryMenuShowing()){
                side_drawer.showContent();
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    class MyHandler extends Handler{
        @Override
        public void handleMessage(Message msg) {
            if(msg.what==MSG_WHAT_CITIES){
                updateListView(cities);
            }
        }
    }


}
