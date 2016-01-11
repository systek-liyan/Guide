package com.systekcn.guide.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.alibaba.fastjson.JSON;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.systekcn.guide.R;
import com.systekcn.guide.adapter.DownloadAdapter;
import com.systekcn.guide.biz.DataBiz;
import com.systekcn.guide.entity.MuseumBean;
import com.systekcn.guide.utils.ExceptionUtil;
import com.systekcn.guide.utils.LogUtil;
import com.systekcn.guide.utils.ViewUtils;

import java.util.ArrayList;
import java.util.List;

public class DownloadActivity extends BaseActivity {

    private ListView listViewDownload;
    private List<MuseumBean> museumList;
    private DownloadAdapter downloadAdapter;
    private Handler handler;
    private Drawer drawer;

    @Override
    protected void initialize(Bundle savedInstanceState) {
        ViewUtils.setStateBarColor(this, R.color.md_red_400);
        setContentView(R.layout.activity_download);
        initData();
        initView();
        initDrawer();
        addListener();
    }

    private void initDrawer() {
        drawer=new DrawerBuilder()
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
                        Intent intent=new Intent(DownloadActivity.this,targetClass);
                        startActivity(intent);
                        return false;
                    }
                }).build();
    }

    private void addListener() {
        listViewDownload.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MuseumBean museumBean=museumList.get(position);
                String museumStr= JSON.toJSONString(museumBean);
                Intent intent=new Intent(DownloadActivity.this,MuseumHomeActivity.class);
                intent.putExtra(INTENT_MUSEUM,museumStr);
                startActivity(intent);
                finish();
            }
        });
    }

    private void initData() {
        new Thread(){
            @Override
            public void run() {
                try{
                    if(application.currentNetworkType!=INTERNET_TYPE_NONE){
                        museumList= DataBiz.getEntityListFromNet(MuseumBean.class, URL_MUSEUM_LIST);
                    }
                    if(museumList!=null&&museumList.size()>0){
                        LogUtil.i("ZHANG", "数据获取成功");
                        //boolean isSaveTrue=DataBiz.deleteSQLiteDataFromClass(MuseumBean.class);
                        //LogUtil.i("ZHANG","数据删除"+isSaveTrue);
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
        handler=new MyHandler();
        museumList=new ArrayList<>();
        downloadAdapter=new DownloadAdapter(this,museumList);
        listViewDownload=(ListView)findViewById(R.id.listViewDownload);
        listViewDownload.setAdapter(downloadAdapter);
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
