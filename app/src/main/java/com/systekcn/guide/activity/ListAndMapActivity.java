package com.systekcn.guide.activity;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.systekcn.guide.R;
import com.systekcn.guide.activity.base.BaseActivity;
import com.systekcn.guide.common.utils.ViewUtils;
import com.systekcn.guide.fragment.ExhibitListFragment;


public class ListAndMapActivity extends BaseActivity {

    private ImageView iv_search;
    private ExhibitListFragment exhibitListFragment;
    private RadioButton rb_guide_list;
    private RadioButton rb_guide_map;
    private RadioGroup rg_nearly_page_title;

    @Override
    protected void initialize() {
        ViewUtils.setStateBarToAlpha(this);
        setContentView(R.layout.activity_list_and_map);
        initView();
        addListener();
        setDefaultFragment();
    }

    private void addListener() {
        rg_nearly_page_title.setOnCheckedChangeListener(radioButtonCheckListener);
    }

    private void initView() {
        iv_search=(ImageView)findViewById(R.id.title_bar_search);
        rb_guide_list=(RadioButton)findViewById(R.id.rb_guide_list);
        rb_guide_map=(RadioButton)findViewById(R.id.rb_guide_map);
        rg_nearly_page_title=(RadioGroup)findViewById(R.id.rg_nearly_page_title);
    }

    private void setDefaultFragment()
    {
        FragmentManager fm = getFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        exhibitListFragment = ExhibitListFragment.newInstance();
        transaction.replace(R.id.ll_exhibit_list_content, exhibitListFragment);
        transaction.commit();
    }


    private RadioGroup.OnCheckedChangeListener radioButtonCheckListener=new RadioGroup.OnCheckedChangeListener(){

        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {

            FragmentManager fm = getFragmentManager();
            // 开启Fragment事务
            FragmentTransaction transaction = fm.beginTransaction();
            switch(checkedId){
                case R.id.rb_guide_list:
                    if (exhibitListFragment == null)
                    {
                        exhibitListFragment = ExhibitListFragment.newInstance();
                    }
                    // 使用当前Fragment的布局替代id_content的控件
                    transaction.replace(R.id.ll_exhibit_list_content, exhibitListFragment);
                    break;
                case R.id.rb_guide_map:
                    break;
            }
            // 事务提交
            transaction.commit();
        }
    };

}
