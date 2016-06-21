package com.systek.guide.activity;

import android.content.Intent;
import android.os.Bundle;
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

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;


/**
 * 收藏展品的activity
 */
public class CollectionActivity extends BaseActivity {


    private ListView collectionListView;//展品列表ListView
    private List<ExhibitBean> collectionExhibitList;//展品集合
    private ExhibitAdapter exhibitAdapter;//适配器
    private String museumId;
    private MediaServiceManager mediaServiceManager;

    static class MyHandler extends Handler {

        WeakReference<CollectionActivity> activityWeakReference;
        MyHandler(CollectionActivity activity){
            this.activityWeakReference=new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {

            if(activityWeakReference==null){return;}
            CollectionActivity activity=activityWeakReference.get();
            if(activity==null){return;}
            switch (msg.what){
                case MSG_WHAT_REFRESH_VIEW:
                    activity.refreshView();
                    break;
                default:break;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collection);
        handler=new MyHandler(this);
        //加载抽屉
        initDrawer();
        initView();
        addListener();
        initData();
    }

    /**
     * 给控件添加监听器
     */
    private void addListener() {
        collectionListView.setOnItemClickListener(adapterViewListener);
    }

    AdapterView.OnItemClickListener adapterViewListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            ExhibitBean exhibitBean = exhibitAdapter.getItem(position);
            ExhibitBean bean = mediaServiceManager.getCurrentExhibit();
            //exhibitAdapter.setSelectItem(position);
            exhibitAdapter.setSelectExhibit(exhibitBean);
            if(bean==null||!bean.equals(exhibitBean)){
                exhibitAdapter.setState(position,ExhibitAdapter.STATE_PLAYING);
            }
            mediaServiceManager.setPlayMode(PLAY_MODE_HAND);
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
    private void initData() {
        museumId = getIntent().getStringExtra(INTENT_MUSEUM_ID);
        new Thread() {
            @Override
            public void run() {
                if (!TextUtils.isEmpty(museumId)) {
                    collectionExhibitList = DataBiz.getCollectionExhibitListFromDBById(museumId);
                } else {
                    collectionExhibitList = DataBiz.getCollectionExhibitListFromDB();
                }
                if (collectionExhibitList == null) {
                    return;
                }
                if (collectionExhibitList.size() == 0) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showToast("您暂未收藏展品。");
                        }
                    });
                } else {
                    handler.sendEmptyMessage(MSG_WHAT_REFRESH_VIEW);
                }
            }
        }.start();
    }


    private void refreshView() {
        if (exhibitAdapter != null && collectionExhibitList != null && collectionExhibitList.size() > 0) {
            exhibitAdapter.updateData(collectionExhibitList);
        }
    }




    /**
     * 加载视图
     */
    private void initView() {

        setTitleBar();
        setTitleBarTitle("收藏");
        setHomeIcon();
        toolbar.setNavigationOnClickListener(backOnClickListener);
        mediaServiceManager = MediaServiceManager.getInstance(this);
        collectionListView = (ListView) findViewById(R.id.collectionListView);

        mErrorView = findViewById(R.id.mErrorView);
        if (mErrorView != null) {
            refreshBtn = (Button) mErrorView.findViewById(R.id.refreshBtn);
        }
        collectionExhibitList = new ArrayList<>();
        exhibitAdapter = new ExhibitAdapter(this, collectionExhibitList);
        collectionListView.setAdapter(exhibitAdapter);
        //去除阴影
        collectionListView.setOverScrollMode(ScrollView.OVER_SCROLL_NEVER);
    }
}