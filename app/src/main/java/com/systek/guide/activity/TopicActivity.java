package com.systek.guide.activity;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.PauseOnScrollListener;
import com.systek.guide.R;
import com.systek.guide.adapter.ExhibitAdapter;
import com.systek.guide.biz.DataBiz;
import com.systek.guide.entity.ExhibitBean;
import com.systek.guide.manager.MediaServiceManager;
import com.systek.guide.utils.ExceptionUtil;

import java.util.ArrayList;
import java.util.List;


/**
 * 专题activity
 *
 *
 */
public class TopicActivity extends BaseActivity {

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

    private MediaServiceManager mediaServiceManager;
    private String currentMuseumId;
    private Handler handler;

    private TextView  tv_collection_dongwei,tv_collection_beiqi, tv_collection_beiwei, tv_collection_xizhou,
            tv_collection_shang, tv_collection_sui, tv_collection_tangdai, tv_collection_handai, tv_collection_chunqiu,
            tv_collection_zhanguo, tv_collection_qing, tv_collection_shixiang, tv_collection_qingtong,
            tv_collection_tongqi, tv_collection_shike;

    private TextView tvLabelClear;
    private LinearLayout ll_collection_years;
    private LinearLayout ll_collection_material;


    @Override
    protected void setView() {

        View view = View.inflate(this, R.layout.activity_topic, null);
        setContentView(view);
        initDrawer();
    }


    void initData() {
        Intent intent=getIntent();
        currentMuseumId =intent.getStringExtra(INTENT_MUSEUM_ID);
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
    void registerReceiver() {

    }

    @Override
    protected void onResume() {
        super.onResume();
        exhibitAdapter.notifyDataSetChanged();
    }


    @Override
    void initView() {
        setTitleBar();
        setTitleBarTitle(R.string.title_bar_topic);
        setHomeIcon();
        setHomeClickListener(backOnClickListener);

        tvList=new ArrayList<>();
        mediaServiceManager=MediaServiceManager.getInstance(this);

        ll_collection_has_choose=(LinearLayout)findViewById(R.id.ll_collection_has_choose);
        ll_collection_years=(LinearLayout)findViewById(R.id.ll_collection_years);
        ll_collection_material=(LinearLayout)findViewById(R.id.ll_collection_material);

        lv_collection_listView=(ListView)findViewById(R.id.lv_collection_listView);

        tvLabelClear=(TextView)findViewById(R.id.tvLabelClear);


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
        //去除滑动到末尾时的阴影
        lv_collection_listView.setOverScrollMode(ScrollView.OVER_SCROLL_NEVER);
    }
    @Override
    void addListener() {

        tvLabelClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ll_collection_has_choose.removeAllViewsInLayout();
                int count1=ll_collection_years.getChildCount();
                int count2=ll_collection_material.getChildCount();
                for(int i=0;i<count1;i++){
                    ll_collection_years.getChildAt(i).setVisibility(View.VISIBLE);
                }
                for(int i=0;i<count2;i++){
                    ll_collection_material.getChildAt(i).setVisibility(View.VISIBLE);
                }
                disPlayCheckExhibitList=new ArrayList<>();
                exhibitAdapter.updateData(totalExhibitList);
            }
        });

        //快速滑动停止加载图片
        lv_collection_listView.setOnScrollListener(new PauseOnScrollListener(ImageLoader.getInstance(), false, true));

        lv_collection_listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                ExhibitBean exhibitBean = exhibitAdapter.getItem(position);
                ExhibitBean bean = mediaServiceManager.getCurrentExhibit();

                if(bean==null||!bean.equals(exhibitBean)){
                    exhibitAdapter.setState(position,ExhibitAdapter.STATE_PLAYING);
                }
                mediaServiceManager.setPlayMode(PLAY_MODE_HAND);
                exhibitAdapter.notifyDataSetInvalidated();

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
        setManyBtnListener();
    }



    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if(drawer.isDrawerOpen()){
                drawer.closeDrawer();
                return true;
            }else{
                return super.onKeyDown(keyCode, event);
            }
        }
        return super.onKeyDown(keyCode,event);
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
            TextView textView=new TextView(getApplicationContext());
            String label= (String) tv.getText();
            textView.setText(label);
            textView.setTextSize(13);
            textView.setTextColor(getResources().getColor(R.color.md_grey_800));
            textView.setBackgroundResource(R.drawable.btn_checked_topic);
            textView.setGravity(Gravity.CENTER);
            textView.setOnClickListener(deleteLabelClickListener);
            LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMarginStart(15);
            params.setMarginEnd(15);
            ll_collection_has_choose.addView(textView, params);
            tv.setVisibility(View.GONE);
            try{
                checkExhibitList=DataBiz.getExhibitListByLabel(label);
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
                disPlayCheckExhibitList=new ArrayList<>();
                for(int i=0;i<ll_collection_has_choose.getChildCount();i++){
                    TextView tvLabel= (TextView) ll_collection_has_choose.getChildAt(i);
                    String text= (String) tvLabel.getText();
                    List<ExhibitBean> list=DataBiz.getExhibitListByLabel(text);
                    if(list!=null&&list.size()>0){
                        disPlayCheckExhibitList.removeAll(list);
                        disPlayCheckExhibitList.addAll(list);
                    }
                }
            }else{
                disPlayCheckExhibitList=new ArrayList<>();
                showToast("抱歉，没有符合您筛选条件的展品！");
            }
            exhibitAdapter.updateData(disPlayCheckExhibitList);
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.normal_menu, menu);
        menu.getItem(0).setIcon(R.drawable.iv_skip);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(disPlayCheckExhibitList==null||disPlayCheckExhibitList.size()==0){return false;}
        String exhibitListStr=JSON.toJSONString(disPlayCheckExhibitList);
        Intent intent=new Intent(TopicActivity.this,ListAndMapActivity.class);
        intent.putExtra(INTENT_FLAG_GUIDE_MAP, INTENT_FLAG_MAP);
        intent.putExtra(INTENT_EXHIBIT_LIST_STR,exhibitListStr);
        startActivity(intent);
        finish();
        return true;
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
