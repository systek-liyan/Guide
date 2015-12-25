package com.systekcn.guide.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.systekcn.guide.MyApplication;
import com.systekcn.guide.R;
import com.systekcn.guide.activity.base.BaseActivity;
import com.systekcn.guide.adapter.NearlyGalleryAdapter;
import com.systekcn.guide.common.IConstants;
import com.systekcn.guide.common.utils.ImageLoaderUtil;
import com.systekcn.guide.common.utils.TimeUtil;
import com.systekcn.guide.common.utils.Tools;
import com.systekcn.guide.common.utils.ViewUtils;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class LockScreenActivity extends BaseActivity implements IConstants{


    private ImageView fullscreen_image;
    private MyApplication application;
    private View view;
    private TextView timeNow;
    private Button btn_unlock;
    private ImageView iv_play_ctrl;
    private NearlyGalleryAdapter nearlyGalleryAdapter;
    private RecyclerView recycle_nearly;

    private final int MSG_WHAT_UPDATE_DATA=1;
    private MyHandler handler;
    private ListChangeReceiver listChangeReceiver;

    @Override
    protected void initialize() {
        application= (MyApplication) getApplication();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        view =getLayoutInflater().inflate(R.layout.activity_lock,null);
        ViewUtils.setStateBarToAlpha(this);
        setContentView(view);
        handler=new MyHandler();
        initView();
    }


    private void initView() {
        fullscreen_image=(ImageView)view.findViewById(R.id.fullscreen_image);
        iv_play_ctrl=(ImageView)view.findViewById(R.id.iv_play_ctrl);
        timeNow=(TextView)view.findViewById(R.id.tv_lock_time);
        timeNow.setText(TimeUtil.getTime());

        recycle_nearly = (RecyclerView)findViewById(R.id.recycle_nearly);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        recycle_nearly.setLayoutManager(linearLayoutManager);
        nearlyGalleryAdapter=new NearlyGalleryAdapter(this,application.currentExhibitBeanList);
        recycle_nearly.setAdapter(nearlyGalleryAdapter);
        registerReceiver();
        iv_play_ctrl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Tools.virbate(200);
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
        if(application.currentExhibitBean!=null){
            String imgUrl=application.currentExhibitBean.getIconurl();
            String localName= Tools.changePathToName(imgUrl);
            String localPath=application.getCurrentImgDir()+localName;
            if(Tools.isFileExist(localPath)){
                //本地图片存在，将锁屏界面设为图片Icon
               /* Bitmap bitmap= BitmapFactory.decodeFile(localPath);
                Drawable drawable=new BitmapDrawable(bitmap);*/
                //view.setBackground(drawable);
                //view.setAlpha(0.8f);
                ImageLoaderUtil.displaySdcardImage(this, localPath, fullscreen_image);
            }else{
                ImageLoaderUtil.displayNetworkImage(this, BASEURL + imgUrl, fullscreen_image);
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
            if(msg.what==MSG_WHAT_UPDATE_DATA){
                if(nearlyGalleryAdapter!=null){
                    nearlyGalleryAdapter.updateData(application.currentExhibitBeanList);
                }
            }
        }
    }


    private  class ListChangeReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            handler.sendEmptyMessage(MSG_WHAT_UPDATE_DATA);
        }
    }

}
