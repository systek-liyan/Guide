package com.systek.guide.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.systek.guide.R;
import com.systek.guide.adapter.ExhibitAdapter;
import com.systek.guide.biz.DataBiz;
import com.systek.guide.entity.ExhibitBean;

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
    private TextView titleBarTopic;

    @Override
    protected void initialize(Bundle savedInstanceState) {
        setContentView(R.layout.activity_collection);
        //加载view
        initView();
        //加载抽屉
        initDrawer();
        //加载数据
        initData();
        //添加监听器
        addListener();
    }

    /**
     * 给控件添加监听器
     */
    private void addListener() {
        collectionListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ExhibitBean exhibitBean= exhibitAdapter.getItem(position);
                String str= JSON.toJSONString(exhibitBean);
                Intent intent =new Intent();
                intent.setAction(INTENT_EXHIBIT);
                intent.putExtra(INTENT_EXHIBIT, str);
                sendBroadcast(intent);
                Intent intent1 =new Intent(CollectionActivity.this,PlayActivity.class);
                intent1.putExtra(INTENT_EXHIBIT,str);
                startActivity(intent1);
               // finish();
            }
        });
    }

    /**
     * 加载数据
     */
    private void initData() {
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
                    showToast("您暂未收藏展品。");
                }else{
                    handler.sendEmptyMessage(MSG_WHAT_UPDATE_DATA_SUCCESS);
                }
            }
        }.start();
    }

    /**
     * 加载视图
     */
    private void initView() {

        titleBarTopic =(TextView)findViewById(R.id.titleBarTopic);
        titleBarTopic.setText(R.string.title_bar_collection);
        handler=new MyHandler();
        collectionListView=(ListView)findViewById(R.id.collectionListView);
        collectionExhibitList=new ArrayList<>();
        exhibitAdapter=new ExhibitAdapter(this,collectionExhibitList);
        collectionListView.setAdapter(exhibitAdapter);
    }

    class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            if(msg.what==MSG_WHAT_UPDATE_DATA_SUCCESS){
                exhibitAdapter.updateData(collectionExhibitList);
            }
        }
    }

}
