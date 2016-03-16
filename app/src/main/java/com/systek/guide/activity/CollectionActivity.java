package com.systek.guide.activity;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ScrollView;

import com.alibaba.fastjson.JSON;
import com.systek.guide.R;
import com.systek.guide.adapter.ExhibitAdapter;
import com.systek.guide.biz.DataBiz;
import com.systek.guide.entity.ExhibitBean;
import com.systek.guide.manager.MediaServiceManager;

import java.util.ArrayList;
import java.util.List;


/**
 * 收藏展品的activity
 */
public class CollectionActivity extends BaseActivity {


    private ListView collectionListView;//展品列表ListView
    private List<ExhibitBean> collectionExhibitList;//展品集合
    private ExhibitAdapter exhibitAdapter;//适配器
    private MyHandler handler;
    private String museumId;
    private MediaServiceManager mediaServiceManager;

    @Override
    protected void setView() {

        View view = View.inflate(this, R.layout.activity_collection, null);
        setContentView(view);
        //加载抽屉
        initDrawer();
    }

    /**
     * 给控件添加监听器
     */
    void addListener() {
        collectionListView.setOnItemClickListener(adapterViewListener);
    }
    AdapterView.OnItemClickListener adapterViewListener =new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            ExhibitBean exhibitBean = exhibitAdapter.getItem(position);
            ExhibitBean bean = mediaServiceManager.getCurrentExhibit();
            if(bean!=null&&!bean.equals(exhibitBean)){
                mediaServiceManager.setPlayMode(PLAY_MODE_HAND);
            }
            exhibitAdapter.setSelectItem(position);
            exhibitAdapter.notifyDataSetInvalidated();

            Intent intent1 = new Intent(CollectionActivity.this, PlayActivity.class);
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
    };

    /**
     * 加载数据
     */
    void initData() {
        museumId =getIntent().getStringExtra(INTENT_MUSEUM_ID);
        new Thread(){
            @Override
            public void run() {
                if(!TextUtils.isEmpty(museumId)){
                    collectionExhibitList=DataBiz.getCollectionExhibitListFromDBById(museumId);
                }else{
                    collectionExhibitList=DataBiz.getCollectionExhibitListFromDB();
                }
                if(collectionExhibitList==null){return;}
                if(collectionExhibitList.size()==0){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showToast("您暂未收藏展品。");
                        }
                    });
                }else{
                    handler.sendEmptyMessage(MSG_WHAT_UPDATE_DATA_SUCCESS);
                }
            }
        }.start();
    }

    @Override
    void registerReceiver() {

    }

    /**
     * 加载视图
     */
    void initView() {

        setTitleBar();
        setTitleBarTitle("收藏");
        setHomeIcon();
        toolbar.setNavigationOnClickListener(backOnClickListener);
        mediaServiceManager=MediaServiceManager.getInstance(this);
        handler=new MyHandler();
        collectionListView=(ListView)findViewById(R.id.collectionListView);

        mErrorView=findViewById(R.id.mErrorView);
        refreshBtn=(Button)mErrorView.findViewById(R.id.refreshBtn);

        collectionExhibitList=new ArrayList<>();
        exhibitAdapter=new ExhibitAdapter(this,collectionExhibitList);
        collectionListView.setAdapter(exhibitAdapter);
        //去除阴影
        collectionListView.setOverScrollMode(ScrollView.OVER_SCROLL_NEVER);
    }

    class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            if(msg.what==MSG_WHAT_UPDATE_DATA_SUCCESS){
                exhibitAdapter.updateData(collectionExhibitList);
            }
        }
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}
