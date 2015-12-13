package com.systekcn.guide.activity;

import android.content.Intent;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.lidroid.xutils.DbUtils;
import com.lidroid.xutils.db.sqlite.Selector;
import com.lidroid.xutils.exception.DbException;
import com.systekcn.guide.R;
import com.systekcn.guide.activity.base.BaseActivity;
import com.systekcn.guide.adapter.ExhibitAdapter;
import com.systekcn.guide.adapter.OnListViewScrollListener;
import com.systekcn.guide.common.utils.ExceptionUtil;
import com.systekcn.guide.common.utils.LogUtil;
import com.systekcn.guide.common.utils.Tools;
import com.systekcn.guide.common.utils.ViewUtils;
import com.systekcn.guide.custom.DrawerView;
import com.systekcn.guide.custom.slidingmenu.SlidingMenu;
import com.systekcn.guide.entity.ExhibitBean;

import java.util.ArrayList;
import java.util.List;

public class TopicActivity extends BaseActivity {

    private TextView tv_collection_pig,tv_collection_tiger,tv_collection_bull,
            tv_collection_monkey, tv_collection_dongwei,tv_collection_beiqi,
            tv_collection_beiwei, tv_collection_xizhou, tv_collection_shang,
            tv_collection_sui, tv_collection_tangdai, tv_collection_handai,
            tv_collection_chunqiu, tv_collection_zhanguo, tv_collection_qing,
            tv_collection_shixiang, tv_collection_qingtong,tv_collection_tongqi,
            tv_collection_shike;
    /**展品总列表*/
    private  List<ExhibitBean> totalExhibitList;
    /**单个标签搜索结果列表*/
    private  List<ExhibitBean> checkExhibitList;
    /**展示列表*/
    private  List<ExhibitBean> disPlayCheckExhibitList;
    /**已选标签布局*/
    private LinearLayout ll_collection_has_choose;
    /**展示集listview*/
    private ListView lv_collection_listView;
    /**适配器*/
    private ExhibitAdapter exhibitAdapter;
    /**滚动监听*/
    private OnListViewScrollListener onListViewScrollListener;
    /**图片是否显示标签*/
    private boolean isPictureShow=true;
    /**已选标签控件集合*/
    private List<TextView> tvList;
    /**右上角导览按钮*/
    private TextView iv_titleBar_toGuide;
    /**侧边栏*/
    private SlidingMenu side_drawer;
    /**侧边栏按钮*/
    private ImageView iv_topic_drawer;

    public void setOnListViewScrollListener(OnListViewScrollListener onListViewScrollListener) {
        this.onListViewScrollListener = onListViewScrollListener;
    }

    @Override
    public void initialize() {
        ViewUtils.setStateBarColor(this, R.color.myOrange);
        setContentView(R.layout.activity_topic);
        totalExhibitList =application.totalExhibitBeanList;
        /*初始化*/
        init();
    }
    private void init() {
        initViews();
        initSlidingMenu();
        exhibitAdapter=new ExhibitAdapter(this, totalExhibitList);
        setOnListViewScrollListener(exhibitAdapter);
        lv_collection_listView.setAdapter(exhibitAdapter);
        addListener();
    }

    private void initSlidingMenu() {
        DrawerView dv =new DrawerView(this);
        side_drawer = dv.initSlidingMenu();
    }

    private void addListener() {

        iv_topic_drawer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (side_drawer.isMenuShowing()) {
                    side_drawer.showContent();
                } else {
                    side_drawer.showMenu();
                }
            }
        });

        lv_collection_listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                application.currentExhibitBean= exhibitAdapter.getItem(position);
                application.refreshData();
                Intent intent =new Intent(TopicActivity.this,GuideActivity.class);
                application.dataFrom=application.DATA_FROM_HOME;
                startActivity(intent);
                finish();
            }
        });

        iv_titleBar_toGuide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /**点击导览时，判断，如果当前筛选列表不为为空，向专题列表赋值，启动导览界面*/
                if (disPlayCheckExhibitList == null || disPlayCheckExhibitList.size() <= 0) {
                    application.topicExhibitBeanList = new ArrayList<>();
                    application.currentExhibitBean = application.totalExhibitBeanList.get(0);
                } else {
                    application.topicExhibitBeanList = disPlayCheckExhibitList;
                    application.currentExhibitBean = application.topicExhibitBeanList.get(0);
                }
                application.isTopicOpen = true;
                Intent intent = new Intent(TopicActivity.this, GuideActivity.class);
                startActivity(intent);
                finish();
            }
        });

        lv_collection_listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                isPictureShow = false;
                switch (scrollState) {
                    case AbsListView.OnScrollListener.SCROLL_STATE_IDLE:
                        isPictureShow = true;
                        LogUtil.i("OnScrollListener", "停止滚动:SCROLL_STATE_IDLE" + isPictureShow);
                        onListViewScrollListener.onScroll(isPictureShow);
                        break;
                    case AbsListView.OnScrollListener.SCROLL_STATE_FLING:
                        LogUtil.i("OnScrollListener", "手指离开屏幕，屏幕惯性滚动:SCROLL_STATE_FLING");
                        break;
                    case AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
                        LogUtil.i("OnScrollListener", "屏幕手指正在滚动：SCROLL_STATE_TOUCH_SCROLL");
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            }
        });

        setManyBtnListener();

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

    private View.OnClickListener labelClickListener =new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            TextView tv= (TextView) v;
            TextView textView=new TextView(TopicActivity.this);
            String label= (String) tv.getText();
            textView.setText(label);
            textView.setTextSize(12);
            textView.setGravity(Gravity.CENTER);
            textView.setOnClickListener(deleteLabelClickListener);
            LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMarginStart(15);
            ll_collection_has_choose.addView(textView, params);
            tv.setVisibility(View.GONE);
            try{
                checkExhibitList=getList(label);
                if(checkExhibitList!=null&&checkExhibitList.size()>0){
                    if(disPlayCheckExhibitList!=null&&disPlayCheckExhibitList.size()>0){
                        disPlayCheckExhibitList.removeAll(checkExhibitList);
                        disPlayCheckExhibitList.addAll(checkExhibitList);
                    }else{
                        disPlayCheckExhibitList=checkExhibitList;
                    }
                }else{
                    disPlayCheckExhibitList=new ArrayList<>();
                }
                exhibitAdapter.updateData(disPlayCheckExhibitList);
            }catch (Exception e){
                ExceptionUtil.handleException(e);
            }

        }
    };

    private View.OnClickListener deleteLabelClickListener=new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            TextView tv=(TextView)v;
            CharSequence charsTop=tv.getText();
            ll_collection_has_choose.removeView(v);
            CharSequence charDown =null;
            for(int i=0;i<tvList.size();i++){
                charDown=tvList.get(i).getText();
                if(charDown.equals(charsTop)){
                    tvList.get(i).setVisibility(View.VISIBLE);
                }
            }
            if(ll_collection_has_choose.getChildCount()>0){
                disPlayCheckExhibitList.clear();
                for(int i=0;i<ll_collection_has_choose.getChildCount();i++){
                    TextView tvLabel= (TextView) ll_collection_has_choose.getChildAt(i);
                    String text= (String) tvLabel.getText();
                    List<ExhibitBean> list=getList(text);
                    if(list!=null&&list.size()>0){
                        disPlayCheckExhibitList.removeAll(list);
                        disPlayCheckExhibitList.addAll(list);
                    }
                }
            }else{
                disPlayCheckExhibitList=new ArrayList<>();
                //disPlayCheckExhibitList=totalExhibitList;
                Tools.showMessage(TopicActivity.this,"抱歉，没有符合您筛选条件的展品！");
            }
            /*List<ExhibitBean> removeList=getList((String)charsTop);
            if(removeList!=null&&removeList.size()>0){
                if(disPlayCheckExhibitList!=null&&removeList.size()>0){
                    disPlayCheckExhibitList.removeAll(removeList);
                }
                if(disPlayCheckExhibitList==null||disPlayCheckExhibitList.size()==0){
                    disPlayCheckExhibitList=totalExhibitList;
                }
            }*/
            exhibitAdapter.updateData(disPlayCheckExhibitList);
        }
    };

    public List<ExhibitBean> getList(String label){
        List<ExhibitBean> list = null;
        DbUtils db=DbUtils.create(this);
        try {
            list=  db.findAll(Selector.from(ExhibitBean.class).where("labels","like","%"+label+"%"));
        } catch (DbException e) {
            ExceptionUtil.handleException(e);
        }finally {
            if(db!=null){
                db.close();
            }
        }
        return list;
    }

    private void initViews() {
        tvList=new ArrayList<>();
        iv_topic_drawer=(ImageView)findViewById(R.id.iv_topic_drawer);

        ll_collection_has_choose=(LinearLayout)findViewById(R.id.ll_collection_has_choose);
        lv_collection_listView=(ListView)findViewById(R.id.lv_collection_listView);
        iv_titleBar_toGuide =(TextView)findViewById(R.id.iv_titlebar_toGuide);

        tv_collection_pig=(TextView)findViewById(R.id.tv_collection_pig);
        tv_collection_tiger=(TextView)findViewById(R.id.tv_collection_tiger);
        tv_collection_bull=(TextView)findViewById(R.id.tv_collection_bull);
        tv_collection_monkey=(TextView)findViewById(R.id.tv_collection_monkey);

        tv_collection_dongwei=(TextView)findViewById(R.id.tv_collection_dongwei);
        tv_collection_beiqi=(TextView)findViewById(R.id.tv_collection_beiqi);
        tv_collection_beiwei=(TextView)findViewById(R.id.tv_collection_beiwei);
        tv_collection_xizhou=(TextView)findViewById(R.id.tv_collection_xizhou);
        tv_collection_shang=(TextView)findViewById(R.id.tv_collection_shang);
        tv_collection_sui=(TextView)findViewById(R.id.tv_collection_sui);
        tv_collection_tangdai=(TextView)findViewById(R.id.tv_collection_tangdai);
        tv_collection_handai=(TextView)findViewById(R.id.tv_collection_handai);
        tv_collection_chunqiu=(TextView)findViewById(R.id.tv_collection_chunqiu);
        tv_collection_zhanguo=(TextView)findViewById(R.id.tv_collection_zhanguo);
        tv_collection_qing=(TextView)findViewById(R.id.tv_collection_qing);

        tv_collection_shixiang=(TextView)findViewById(R.id.tv_collection_shixiang);
        tv_collection_qingtong=(TextView)findViewById(R.id.tv_collection_qingtong);
        tv_collection_tongqi=(TextView)findViewById(R.id.tv_collection_tongqi);
        tv_collection_shike=(TextView)findViewById(R.id.tv_collection_shike);

        //tvList.add(tv_collection_pig);
        //tvList.add(tv_collection_tiger);
        //tvList.add(tv_collection_bull);
        //tvList.add(tv_collection_monkey);
        tvList.add(tv_collection_dongwei);
        tvList.add(tv_collection_beiqi);
        tvList.add(tv_collection_xizhou);
        tvList.add(tv_collection_shang);
        tvList.add(tv_collection_sui);
        tvList.add(tv_collection_tangdai);
        tvList.add(tv_collection_handai);
        tvList.add(tv_collection_chunqiu);
        tvList.add(tv_collection_zhanguo);
        tvList.add(tv_collection_qing);
        tvList.add(tv_collection_shixiang);
        tvList.add(tv_collection_qingtong);
        tvList.add(tv_collection_tongqi);
        tvList.add(tv_collection_shike);
    }

    private void setManyBtnListener() {
        //tv_collection_pig.setOnClickListener(labelClickListener);
        //tv_collection_tiger.setOnClickListener(labelClickListener);
        //tv_collection_bull.setOnClickListener(labelClickListener);
        //tv_collection_monkey.setOnClickListener(labelClickListener);
        tv_collection_dongwei.setOnClickListener(labelClickListener);
        tv_collection_beiqi.setOnClickListener(labelClickListener);
        tv_collection_beiwei.setOnClickListener(labelClickListener);
        tv_collection_xizhou.setOnClickListener(labelClickListener);
        tv_collection_shang.setOnClickListener(labelClickListener);
        tv_collection_sui.setOnClickListener(labelClickListener);
        tv_collection_tangdai.setOnClickListener(labelClickListener);
        tv_collection_handai.setOnClickListener(labelClickListener);
        tv_collection_chunqiu.setOnClickListener(labelClickListener);
        tv_collection_zhanguo.setOnClickListener(labelClickListener);
        tv_collection_qing.setOnClickListener(labelClickListener);
        tv_collection_shixiang.setOnClickListener(labelClickListener);
        tv_collection_qingtong.setOnClickListener(labelClickListener);
        tv_collection_tongqi.setOnClickListener(labelClickListener);
        tv_collection_shike.setOnClickListener(labelClickListener);
    }
}
