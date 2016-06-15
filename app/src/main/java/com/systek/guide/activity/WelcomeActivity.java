package com.systek.guide.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.systek.guide.R;
import com.systek.guide.adapter.base.ViewPagerAdapter;
import com.systek.guide.custom.Dot;
import com.systek.guide.fragment.BaseFragment;
import com.systek.guide.fragment.ImageFragment;
import com.systek.guide.utils.ExceptionUtil;

import java.io.IOException;
import java.util.ArrayList;

public class WelcomeActivity extends BaseActivity implements ViewPager.OnPageChangeListener{

    private int lastPage = 0;
    private int dotWidth = 40;
    private int dotHeight = 35;
    private ArrayList<Dot> mDots;
    private Button btn_into_app;
    private ViewPager viewPager;
    private LinearLayout linearLayout_dots;
    private String[] list_image;
    private Class<?> targetClass;
    private Intent intent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        initView();
        addListener();
        initData();
    }

    private void initView() {
        btn_into_app=(Button)findViewById(R.id.btn_into_app);
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        linearLayout_dots = (LinearLayout) findViewById(R.id.linearLayout_dots);
    }

    private void addListener() {
        // 设置page切换监听
        viewPager.addOnPageChangeListener(this);
        //去除阴影
        viewPager.setOverScrollMode(ScrollView.OVER_SCROLL_NEVER);
        btn_into_app.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent = new Intent(WelcomeActivity.this, targetClass);
                startActivity(intent);
                finish();
            }
        });
    }

    private void initData() {
        targetClass=MuseumListActivity.class;
        mDots = new ArrayList<>();
        //得到assets/welcome_images/目录下的所有文件的文件名，以便后面打开操作时使用
        try {
            list_image = getAssets().list("welcome_images");
        } catch (IOException e) {
            ExceptionUtil.handleException(e);
        }
        // 遍历图片数组
        ArrayList<BaseFragment> baseFragments = new ArrayList<>();
        for (String aList_image : list_image) {
            Dot dot = new Dot(this);
            int unSelectColorResId = android.R.color.darker_gray;
            int selectColorResId = android.R.color.white;
            dot.setColor(unSelectColorResId, selectColorResId);
            dot.setLayoutParams(new LinearLayout.LayoutParams(dotWidth, dotHeight));
            mDots.add(dot);
            linearLayout_dots.addView(dot);
            baseFragments.add(ImageFragment.newInstance(aList_image));
        }
        // 设置适配器
        viewPager.setAdapter(new ViewPagerAdapter(getSupportFragmentManager(), baseFragments));
        // 默认选中第一位
        dotSelect(0);
    }

    /**
     * 页面选择
     *
     * @param index
     */
    private void dotSelect(int index) {
        for (int i = 0; i < mDots.size(); i++) {
            mDots.get(i).setSelected(i == index);
        }
    }

    @Override
    public void onPageScrollStateChanged(int arg0) {

    }

    @Override
    public void onPageScrolled(int arg0, float arg1, int arg2) {
        if (arg0 == lastPage && (lastPage + 1) < mDots.size()) {
            mDots.get(lastPage).setScale(1.0f - arg1);
            mDots.get(lastPage + 1).setScale(arg1);
        } else {
            this.lastPage = arg0;
        }
    }
    @Override
    public void onPageSelected(int arg0) {
        dotSelect(arg0);
        if(arg0==mDots.size()-1){
            btn_into_app.setVisibility(View.VISIBLE);
        }else{
            btn_into_app.setVisibility(View.GONE);
        }
    }

}
