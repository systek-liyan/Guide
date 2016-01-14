package com.systekcn.guide.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.lidroid.xutils.DbUtils;
import com.lidroid.xutils.db.sqlite.Selector;
import com.lidroid.xutils.exception.DbException;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.systekcn.guide.R;
import com.systekcn.guide.adapter.ExhibitAdapter;
import com.systekcn.guide.biz.DataBiz;
import com.systekcn.guide.entity.ExhibitBean;
import com.systekcn.guide.manager.MediaServiceManager;
import com.systekcn.guide.utils.ExceptionUtil;
import com.systekcn.guide.utils.ViewUtils;

import java.util.ArrayList;
import java.util.List;

public class TopicActivity extends BaseActivity {

    private Drawer drawer;
    private String currentMuseumId;
    private Handler handler;
    private ImageView titleBarSkip;

    @Override
    protected void initialize(Bundle savedInstanceState) {
        ViewUtils.setStateBarColor(this, R.color.md_red_400);
        setContentView(R.layout.activity_topic);
        Intent intent=getIntent();
        currentMuseumId =intent.getStringExtra(INTENT_MUSEUM_ID);
        init();
    }
    private TextView  tv_collection_dongwei,tv_collection_beiqi,
            tv_collection_beiwei, tv_collection_xizhou, tv_collection_shang,
            tv_collection_sui, tv_collection_tangdai, tv_collection_handai,
            tv_collection_chunqiu, tv_collection_zhanguo, tv_collection_qing,
            tv_collection_shixiang, tv_collection_qingtong,tv_collection_tongqi,
            tv_collection_shike;
    /**展品总列表*/
    private List<ExhibitBean> totalExhibitList;
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
    /**已选标签控件集合*/
    private List<TextView> tvList;
    /**右上角导览按钮*/
    //private TextView iv_titleBar_toGuide;
    /**侧边栏按钮*/
    private ImageView titleBarDrawer;

    private MediaServiceManager mediaServiceManager;

    private void init() {
        initViews();
        initDrawer();
        addListener();
        initData();
    }

    private void initData() {
        new Thread(){
            @Override
            public void run() {
                if(TextUtils.isEmpty(currentMuseumId)){return;}
                totalExhibitList=DataBiz.getLocalListById(ExhibitBean.class, currentMuseumId);
                if(totalExhibitList!=null&&totalExhibitList.size()>0){
                    handler.sendEmptyMessage(MSG_WHAT_UPDATE_DATA_SUCCESS);
                }
            }
        }.start();

    }

    @Override
    protected void onResume() {
        super.onResume();
        exhibitAdapter.notifyDataSetChanged();
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
                        Intent intent=new Intent(TopicActivity.this,targetClass);
                        startActivity(intent);
                        return false;
                    }
                }).build();
    }

    private void addListener() {

        titleBarDrawer.setOnClickListener(onClickListener);
        lv_collection_listView.setOnScrollListener(onScrollListener);
        lv_collection_listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                exhibitAdapter.setSelectItem(position);
                exhibitAdapter.notifyDataSetInvalidated();
                ExhibitBean exhibitBean = exhibitAdapter.getItem(position);
                ExhibitBean bean = mediaServiceManager.getCurrentExhibit();
                Intent intent1 = new Intent(TopicActivity.this, PlayActivity.class);
                if (bean == null || !bean.equals(exhibitBean)) {
                    String str = JSON.toJSONString(exhibitBean);
                    Intent intent = new Intent();
                    intent.setAction(INTENT_EXHIBIT);
                    intent.putExtra(INTENT_EXHIBIT, str);
                    sendBroadcast(intent);
                    intent1.putExtra(INTENT_EXHIBIT, str);
                }
                startActivity(intent1);
            }
        });

        titleBarSkip.setOnClickListener(onClickListener);

        /*iv_titleBar_toGuide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                *//**点击导览时，判断，如果当前筛选列表不为为空，向专题列表赋值，启动导览界面*//*
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
        });*/

        lv_collection_listView.setOnScrollListener(onScrollListener);
        setManyBtnListener();
    }


    private AbsListView.OnScrollListener onScrollListener=new AbsListView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            switch (scrollState){
                case  AbsListView.OnScrollListener.SCROLL_STATE_IDLE://停止滚动
                    exhibitAdapter.setScrollState(false);
                    exhibitAdapter.notifyDataSetChanged();
                    break;
                case AbsListView.OnScrollListener.SCROLL_STATE_FLING://滚动做出了抛的动作
                    //设置为正在滚动
                    exhibitAdapter.setScrollState(true);
                    break;
                case AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL://正在滚动
                    //设置为正在滚动
                    exhibitAdapter.setScrollState(true);
                    break;
            }
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        }
    };

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if(drawer.isDrawerOpen()){
                drawer.closeDrawer();
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
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

    private View.OnClickListener onClickListener=new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.titleBarDrawer:
                    if (drawer.isDrawerOpen()) {
                        drawer.closeDrawer();
                    } else {
                        drawer.openDrawer();
                    }
                    break;
                case  R.id.titleBarRightImg:
                    if(disPlayCheckExhibitList==null||disPlayCheckExhibitList.size()==0){return;}
                    String exhibitListStr=JSON.toJSONString(disPlayCheckExhibitList);
                    Intent intent=new Intent(TopicActivity.this,ListAndMapActivity.class);
                    intent.putExtra(INTENT_FLAG_GUIDE_MAP, INTENT_FLAG_MAP);
                    intent.putExtra(INTENT_EXHIBIT_LIST_STR,exhibitListStr);
                    startActivity(intent);

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
                showToast("抱歉，没有符合您筛选条件的展品！");
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
        mediaServiceManager=MediaServiceManager.getInstance(this);
        titleBarDrawer =(ImageView)findViewById(R.id.titleBarDrawer);
        titleBarSkip =(ImageView)findViewById(R.id.titleBarRightImg);
        titleBarSkip.setImageDrawable(getResources().getDrawable(R.drawable.iv_skip));
        ll_collection_has_choose=(LinearLayout)findViewById(R.id.ll_collection_has_choose);
        lv_collection_listView=(ListView)findViewById(R.id.lv_collection_listView);
        //iv_titleBar_toGuide =(TextView)findViewById(R.id.iv_titlebar_toGuide);

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
        totalExhibitList=new ArrayList<>();
        exhibitAdapter=new ExhibitAdapter(this, totalExhibitList);
        lv_collection_listView.setAdapter(exhibitAdapter);
    }

    private void setManyBtnListener() {
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
        handler=new MyHandler();
    }

    class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MSG_WHAT_UPDATE_DATA_SUCCESS) {
                refreshView();
            }
        }
    }

    private void refreshView() {
        exhibitAdapter.updateData(totalExhibitList);// TODO: 2016/1/3
    }

}
