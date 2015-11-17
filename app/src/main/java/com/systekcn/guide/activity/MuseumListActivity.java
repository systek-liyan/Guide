package com.systekcn.guide.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.magic.mapdemo.R;
import com.systekcn.guide.adapter.MuseumListAdapter;
import com.systekcn.guide.biz.BeansManageBiz;
import com.systekcn.guide.biz.BizFactory;
import com.systekcn.guide.common.utils.ExceptionUtil;
import com.systekcn.guide.entity.MuseumBean;
import com.systekcn.guide.widget.DrawerView;
import com.systekcn.guide.widget.slidingmenu.SlidingMenu;

import java.util.ArrayList;
import java.util.List;

public class MuseumListActivity extends BaseActivity {

    private ListView lvMuseum;
    /*当前所在城市*/
    private String city;
    /*侧滑菜单*/
    private SlidingMenu side_drawer;
    private ImageView iv_titleBar_switch;
    private List<MuseumBean> museumList;
    private MuseumListAdapter adapter;
    private final int MSG_WHAT_MUSEUMS = 1;
    /**
     * 侧滑菜单按钮
     */
    private ImageView iv_drawer;

    @SuppressLint("HandlerLeak")
    Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == MSG_WHAT_MUSEUMS) {
                adapter.updateData(museumList);
            }
        }
    };
    private ImageView home_page_guide_flower;
    private TextView tv_museum_search;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_museum_list);
        initialize();
    }

    private void initialize() {
        try{
            // 初始化数据
            initData();
            // 初始化视图
            initViews();
            //添加监听器
            addListener();
            //初始化侧滑菜单
            initSlidingMenu();
        }catch (Exception e){
            ExceptionUtil.handleException(e);
        }
    }

    private void initSlidingMenu() {
        DrawerView dv =new DrawerView(this);
        side_drawer = dv.initSlidingMenu();
    }

    private void initViews() {
        // 初始化头部
        iv_drawer = (ImageView) findViewById(R.id.iv_setting);
        iv_titleBar_switch = (ImageView) findViewById(R.id.iv_titleBar_switch);
        home_page_guide_flower = (ImageView) findViewById(R.id.home_page_guide_flower);
        lvMuseum = (ListView) findViewById(R.id.lv_museum_list);
        tv_museum_search=(TextView)findViewById(R.id.tv_museum_search);
        if (museumList == null) {
            museumList = new ArrayList<>();
        }
        adapter = new MuseumListAdapter(museumList, this);
        lvMuseum.setAdapter(adapter);
    }

    private void addListener() {
        tv_museum_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent =new Intent(MuseumListActivity.this,SearchActivity.class);
                startActivity(intent);
            }
        });

        iv_drawer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (side_drawer.isMenuShowing()) {
                    side_drawer.showContent();
                } else {
                    side_drawer.showMenu();
                }
            }
        });
        lvMuseum.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MuseumBean bean = (MuseumBean) adapter.getItem(position);
                Intent intent = new Intent(MuseumListActivity.this, MuseumHomePageActivity.class);
                intent.putExtra(INTENT_MUSEUM_ID, bean.getId());
                startActivity(intent);
            }
        });
        iv_titleBar_switch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MuseumListActivity.this, CityActivity.class);
                startActivity(intent);
            }
        });

        home_page_guide_flower.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(),"popupwindow",Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void initData() {

        try{
            city = getIntent().getStringExtra("city");
            new Thread() {
                public void run() {
                    BeansManageBiz biz = (BeansManageBiz) BizFactory.getBeansManageBiz(MuseumListActivity.this);
                    museumList = biz.getAllBeans(URL_TYPE_GET_MUSEUM_LIST, MuseumBean.class, "");
                    while (museumList == null) {
                    }
                    handler.sendEmptyMessage(MSG_WHAT_MUSEUMS);
                }
            }.start();
        }catch (Exception e){
            ExceptionUtil.handleException(e);
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
}
