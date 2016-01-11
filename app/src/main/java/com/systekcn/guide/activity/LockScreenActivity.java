package com.systekcn.guide.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.systekcn.guide.R;
import com.systekcn.guide.adapter.NearlyGalleryAdapter;
import com.systekcn.guide.entity.ExhibitBean;
import com.systekcn.guide.manager.MediaServiceManager;
import com.systekcn.guide.utils.ImageLoaderUtil;
import com.systekcn.guide.utils.TimeUtil;
import com.systekcn.guide.utils.Tools;

import java.util.ArrayList;
import java.util.List;

public class LockScreenActivity extends BaseActivity {


    private ImageView fullscreenImage;
    private TextView tvLockTime;
    private RecyclerView recycleNearly;
    private NearlyGalleryAdapter nearlyGalleryAdapter;
    private ImageView ivPlayCtrl;
    private ListChangeReceiver listChangeReceiver;
    private Handler handler;
    private List<ExhibitBean> nearlyExhibitList;
    private ExhibitBean currentExhibitBean;
    private MediaServiceManager mediaServiceManager;


    @Override
    protected void initialize(Bundle savedInstanceState) {
        setContentView(R.layout.activity_lock_screen);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        handler=new MyHandler();
        mediaServiceManager= MediaServiceManager.getInstance(this);



        initView();
    }

    private void initView() {
        fullscreenImage=(ImageView)findViewById(R.id.fullscreenImage);
        ivPlayCtrl=(ImageView)findViewById(R.id.ivPlayCtrl);
        tvLockTime=(TextView)findViewById(R.id.tvLockTime);
        tvLockTime.setText(TimeUtil.getTime());

        recycleNearly = (RecyclerView)findViewById(R.id.recycleNearly);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        recycleNearly.setLayoutManager(linearLayoutManager);
        nearlyExhibitList=new ArrayList<>();
        nearlyGalleryAdapter=new NearlyGalleryAdapter(this,nearlyExhibitList);
        recycleNearly.setAdapter(nearlyGalleryAdapter);
        registerReceiver();// TODO: 2016/1/7
        ivPlayCtrl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }


    private void registerReceiver() {
        listChangeReceiver = new ListChangeReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_NOTIFY_NEARLY_EXHIBIT_LIST_CHANGE);
        registerReceiver(listChangeReceiver, intentFilter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        displayLockImage();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode==KeyEvent.KEYCODE_BACK){
            return true;
        }else if(keyCode==KeyEvent.KEYCODE_HOME){
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void displayLockImage() {
        if(currentExhibitBean!=null){
            String imgUrl=currentExhibitBean.getIconurl();
            String localName= Tools.changePathToName(imgUrl);
            String localPath= currentExhibitBean.getMuseumId()+localName;
            if(Tools.isFileExist(localPath)){
                ImageLoaderUtil.displaySdcardImage(this, localPath, fullscreenImage);
            }else{
                ImageLoaderUtil.displayNetworkImage(this, BASE_URL + imgUrl, fullscreenImage);
            }
        }
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(listChangeReceiver);
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }


    class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            if(msg.what==MSG_WHAT_UPDATE_DATA_SUCCESS){
                if(nearlyGalleryAdapter!=null){
                    //nearlyGalleryAdapter.updateData(application.currentExhibitBeanList);
                }
            }
        }
    }


    private  class ListChangeReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action =intent.getAction();
            switch (action){
                case "":
                    break;

            }
            handler.sendEmptyMessage(MSG_WHAT_UPDATE_DATA_SUCCESS);
        }
    }


}
