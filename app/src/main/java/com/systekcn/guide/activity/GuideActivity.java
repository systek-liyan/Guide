package com.systekcn.guide.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.view.Display;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.systekcn.guide.R;
import com.systekcn.guide.adapter.GuideMapFragmentAdapter;
import com.systekcn.guide.common.IConstants;
import com.systekcn.guide.fragment.GuideFragment;
import com.systekcn.guide.fragment.MapFragment;

import java.util.ArrayList;
import java.util.List;

public class GuideActivity extends BaseActivity implements IConstants{
    /**放置导览和地图的fragment集合*/
    private List<Fragment> fragmentList;
    /**fragment适配器*/
    private GuideMapFragmentAdapter guideMapFragmentAdapter;
    /**fragment的父容器*/
    private ViewPager viewPager;
    /**导览fragment*/
    private GuideFragment guideFragment;
    /**地体fragment*/
    private MapFragment mapFragment;
    /**顶部返回键*/
    private ImageView iv_titleBar_back;
    /**顶部更多键*/
    private TextView iv_titleBar_more;
    /**顶部tab键*/
    private RadioButton rb_guide_guide;
    private RadioButton rb_guide_map;
    private RadioGroup rg_guide_page_title;
    /**fragment管理者*/
    private FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guide);
        Display display = getWindowManager().getDefaultDisplay();
        if (display.getWidth() < display.getHeight()) {}
        fragmentManager = getSupportFragmentManager();
        fragmentList=new ArrayList<>();
        initialize();
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

    private void initialize() {
        initView();
        addListener();
    }

    private void initView() {
        iv_titleBar_back=(ImageView)findViewById(R.id.iv_titleBar_back);
        iv_titleBar_more=(TextView)findViewById(R.id.iv_titleBar_more);
        rb_guide_guide=(RadioButton)findViewById(R.id.rb_guide_guide);
        rb_guide_map=(RadioButton)findViewById(R.id.rb_guide_map);
        rg_guide_page_title=(RadioGroup)findViewById(R.id.rg_guide_page_title);
        viewPager=(ViewPager)findViewById(R.id.guide_map_container);
        guideFragment = GuideFragment.newInstance();
        mapFragment=new MapFragment();
        fragmentList.add(guideFragment);
        fragmentList.add(mapFragment);
        guideMapFragmentAdapter=new GuideMapFragmentAdapter(fragmentManager,fragmentList);
        viewPager.setAdapter(guideMapFragmentAdapter);
    }

    private void addListener() {
        rg_guide_page_title.setOnCheckedChangeListener(radioButtonCheckListener);

        iv_titleBar_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        iv_titleBar_more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "popupwindow", Toast.LENGTH_SHORT).show();
            }
        });

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }
            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case 0:
                        rb_guide_guide.setChecked(true);
                        rb_guide_map.setChecked(false);
                        break;
                    case 1:
                        rb_guide_guide.setChecked(false);
                        rb_guide_map.setChecked(true);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    private RadioGroup.OnCheckedChangeListener radioButtonCheckListener=new RadioGroup.OnCheckedChangeListener(){

        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            switch(checkedId){
                case R.id.rb_guide_guide:
                    viewPager.setCurrentItem(0);
                    break;
                case R.id.rb_guide_map:
                    viewPager.setCurrentItem(1);
                    break;
            }
        }
    };

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            moveTaskToBack(true);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

}