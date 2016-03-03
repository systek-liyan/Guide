package com.systek.guide.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ScrollView;

import com.alibaba.fastjson.JSON;
import com.systek.guide.MyApplication;
import com.systek.guide.R;
import com.systek.guide.adapter.DownloadAdapter;
import com.systek.guide.biz.DataBiz;
import com.systek.guide.entity.MuseumBean;
import com.systek.guide.utils.ExceptionUtil;
import com.systek.guide.utils.LogUtil;

import java.util.ArrayList;
import java.util.List;

public class DownloadActivity extends BaseActivity {

    private ListView listViewDownload;
    private List<MuseumBean> museumList;
    private DownloadAdapter downloadAdapter;
    private Handler handler;

    @Override
    protected void initialize(Bundle savedInstanceState) {
        setContentView(R.layout.activity_download);
        initView();
        initDrawer();
        addListener();
        initData();
    }


    private void addListener() {
        toolbar.setNavigationOnClickListener(backOnClickListener);
        listViewDownload.setOnItemClickListener(onItemClickListener);
    }

    AdapterView.OnItemClickListener onItemClickListener=new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            MuseumBean museumBean=museumList.get(position);
            String museumStr= JSON.toJSONString(museumBean);
            Intent intent=new Intent(DownloadActivity.this,MuseumHomeActivity.class);
            intent.putExtra(INTENT_MUSEUM,museumStr);
            startActivity(intent);
            finish();
        }
    };

    private void initData() {
        new Thread(){
            @Override
            public void run() {
                try{
                    if(MyApplication.getCurrentNetworkType()!=INTERNET_TYPE_NONE){
                        String url=BASE_URL+URL_MUSEUM_LIST;
                        museumList= DataBiz.getEntityListFromNet(MuseumBean.class,url );
                    }
                    if(museumList!=null&&museumList.size()>0){
                        LogUtil.i("ZHANG", "数据获取成功");
                        boolean isSaveTrue=DataBiz.saveListToSQLite(museumList);
                        LogUtil.i("ZHANG","数据更新"+isSaveTrue);
                    }else{
                        museumList=DataBiz.getEntityListLocal(MuseumBean.class);
                    }
                }catch (Exception e){
                    ExceptionUtil.handleException(e);
                }finally {
                    if(museumList==null||museumList.size()==0){
                        onDataError();
                    }else{
                        handler.sendEmptyMessage(MSG_WHAT_UPDATE_DATA_SUCCESS);
                    }
                }
            }
        }.start();
    }

    private void initView() {

        setTitleBar();
        setTitleBarTitle(R.string.title_bar_download_center);
        setHomeIcon();
        handler=new MyHandler();
        museumList=new ArrayList<>();
        downloadAdapter=new DownloadAdapter(this,museumList);
        listViewDownload=(ListView)findViewById(R.id.listViewDownload);
        listViewDownload.setAdapter(downloadAdapter);
        //去除阴影
        listViewDownload.setOverScrollMode(ScrollView.OVER_SCROLL_NEVER);
    }


    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        downloadAdapter.onDestroy();
        super.onDestroy();
    }

    class MyHandler  extends Handler{

        @Override
        public void handleMessage(Message msg) {
            if(msg.what==MSG_WHAT_UPDATE_DATA_SUCCESS){
                downloadAdapter.updateData(museumList);
            }
        }
    };
}
