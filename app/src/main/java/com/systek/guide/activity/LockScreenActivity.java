package com.systek.guide.activity;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.systek.guide.IConstants;
import com.systek.guide.R;
import com.systek.guide.adapter.NearlyGalleryAdapter;
import com.systek.guide.callback.PlayChangeCallback;
import com.systek.guide.entity.ExhibitBean;
import com.systek.guide.manager.MediaServiceManager;
import com.systek.guide.utils.ImageUtil;
import com.systek.guide.utils.TimeUtil;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class LockScreenActivity extends SwipeBackActivity implements IConstants{


    private ImageView fullscreenImage;
    private ImageView ivPlayCtrl;
    private MediaServiceManager mediaServiceManager;
    private int currentDuration;
    private int currentProgress;
    private TextView tvPlayTime;
    private TextView tvTotalTime;
    private NearlyGalleryAdapter nearlyGalleryAdapter;
    private ExhibitBean currentExhibit;
    private List<ExhibitBean> currentExhibitList;
    private SeekBar seekBarProgress;
    private TextView tvExhibitName;
    private AnimationDrawable animArrowDrawable;

    private static final int MSG_WHAT_CHANGE_EXHIBIT=1;
    private static final int MSG_WHAT_UPDATE_PROGRESS=2;
    private static final int MSG_WHAT_CHANGE_PLAY_START=3;
    private static final int MSG_WHAT_CHANGE_PLAY_STOP=4;
    private static final int MSG_WHAT_UPDATE_DATA_SUCCESS=5;



    static class MyHandler extends Handler {

        WeakReference<LockScreenActivity> activityWeakReference;
        MyHandler(LockScreenActivity activity){
            this.activityWeakReference=new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {

            if(activityWeakReference==null){return;}
            LockScreenActivity activity=activityWeakReference.get();
            if(activity==null){return;}
            switch (msg.what){
                case MSG_WHAT_CHANGE_EXHIBIT:
                    activity.refreshExhibit();
                    break;
                case MSG_WHAT_UPDATE_PROGRESS:
                    activity.refreshProgress();
                    break;
                case MSG_WHAT_CHANGE_PLAY_START:
                    activity.refreshState();
                    break;
                case MSG_WHAT_CHANGE_PLAY_STOP:
                    activity.refreshState();
                    break;
                case MSG_WHAT_UPDATE_DATA_SUCCESS:
                    activity.refreshView();
                    break;
                default:break;
            }
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        setContentView(R.layout.activity_lock_screen);
        handler=new MyHandler(this);
        initView();
        addListener();
        initData();

    }

    protected void initView() {
        mediaServiceManager=MediaServiceManager.getInstance(this);
        fullscreenImage = (ImageView) findViewById(R.id.fullscreenImage);
        ivPlayCtrl = (ImageView) findViewById(R.id.ivPlayCtrl);
        tvPlayTime = (TextView) findViewById(R.id.tvPlayTime);
        tvTotalTime = (TextView) findViewById(R.id.tvTotalTime);
        TextView tvLockTime = (TextView) findViewById(R.id.tvLockTime);
        tvExhibitName = (TextView) findViewById(R.id.tvExhibitName);
        tvLockTime.setText(TimeUtil.getTime());

        seekBarProgress=(SeekBar)findViewById(R.id.seekBarProgress);
        RecyclerView recycleNearly = (RecyclerView) findViewById(R.id.recycleNearly);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        recycleNearly.setLayoutManager(linearLayoutManager);

        List<ExhibitBean> nearlyExhibitList = new ArrayList<>();
        nearlyGalleryAdapter = new NearlyGalleryAdapter(getActivity(), nearlyExhibitList);
        recycleNearly.setAdapter(nearlyGalleryAdapter);
        recycleNearly.setOverScrollMode(ScrollView.OVER_SCROLL_NEVER);

        ImageView imgView_getup_arrow = (ImageView) findViewById(R.id.getup_arrow);
        animArrowDrawable = (AnimationDrawable) imgView_getup_arrow.getBackground() ;

    }

    private void initIcon() {
        if(currentExhibit==null){return;}
        String currentIconUrl = currentExhibit.getIconurl();
        ImageUtil.displayImage(currentIconUrl, fullscreenImage);
    }


    @Override
    protected void onResume() {
        super.onResume();
        if(mediaServiceManager.isPlaying()){
            //state=PLAY_STATE_START;
            state= PlayChangeCallback.STATE_PLAYING;
        }else{
            //state=PLAY_STATE_STOP;
            state= PlayChangeCallback.STATE_STOP;
        }
        refreshState();
        handler.postDelayed(animationDrawableTask, 300);
    }

    //通过延时控制当前绘制bitmap的位置坐标
    private Runnable animationDrawableTask = new Runnable(){

        public void run(){
            animArrowDrawable.start();
            handler.postDelayed(animationDrawableTask, 300);
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        animArrowDrawable.stop();
    }


    /*//屏蔽掉Home键
    public void onAttachedToWindow() {
        this.getWindow().setType(WindowManager.LayoutParams.TYPE_KEYGUARD_DIALOG);
        super.onAttachedToWindow();
    }*/
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return event.getKeyCode() == KeyEvent.KEYCODE_BACK || super.onKeyDown(keyCode, event);
    }


    private void addListener() {

        nearlyGalleryAdapter.setOnItemClickListener(new NearlyGalleryAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {

                ExhibitBean exhibitBean = nearlyGalleryAdapter.getEntity(position);
                ExhibitBean bean = mediaServiceManager.getCurrentExhibit();

                if(bean==null||!bean.equals(exhibitBean)){
                    nearlyGalleryAdapter.setSelectIndex(exhibitBean);
                }
                mediaServiceManager.setPlayMode(PLAY_MODE_HAND);
                nearlyGalleryAdapter.notifyDataSetChanged();

                Intent intent1 = new Intent(LockScreenActivity.this, PlayActivity.class);
                if (bean == null || !bean.equals(exhibitBean)) {
                    String str = JSON.toJSONString(exhibitBean);
                    Intent intent = new Intent();
                    intent.setAction(INTENT_EXHIBIT);
                    intent.putExtra(INTENT_EXHIBIT, str);
                    sendBroadcast(intent);
                    intent1.putExtra(INTENT_EXHIBIT, str);
                }
            }
        });

        ivPlayCtrl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(INTENT_CHANGE_PLAY_STATE);
                sendBroadcast(intent);

            }
        });
        seekBarProgress.setOnSeekBarChangeListener(onSeekBarChangeListener);



    }


    private void initData() {
        new Thread(){
            @Override
            public void run() {

                Intent intent=getIntent();
                String exhibitStr=intent.getStringExtra(INTENT_EXHIBIT);
                if(!TextUtils.isEmpty(exhibitStr)){
                    ExhibitBean bean= JSON.parseObject(exhibitStr, ExhibitBean.class);
                    if(currentExhibit==null) {
                        currentExhibit = bean;
                    }
                }else{
                    currentExhibit=mediaServiceManager.getCurrentExhibit();
                }
                handler.sendEmptyMessage(MSG_WHAT_CHANGE_EXHIBIT);
            }
        }.start();

    }





    private void refreshView() {
        if(nearlyGalleryAdapter!=null&&currentExhibitList!=null){
            nearlyGalleryAdapter.updateData(currentExhibitList);
        }
    }

    private void refreshExhibit() {
        if(currentExhibit==null){return;}
        //String currentMuseumId = currentExhibit.getMuseumId();
        tvExhibitName.setText(currentExhibit.getName());
        initIcon();
    }

    private void refreshProgress() {
        seekBarProgress.setMax(currentDuration);
        seekBarProgress.setProgress(currentProgress);
        tvPlayTime.setText(TimeUtil.changeToTime(currentProgress).substring(3));
        tvTotalTime.setText(TimeUtil.changeToTime(currentDuration).substring(3));
    }


    private void refreshState() {

        if(state == PlayChangeCallback.STATE_PLAYING){//state==PLAY_STATE_START
            ivPlayCtrl.setImageDrawable(getResources().getDrawable(R.drawable.uamp_ic_pause_white_48dp));//iv_play_state_open_big,ic_pause_black_36dp
        }else{
            ivPlayCtrl.setImageDrawable(getResources().getDrawable(R.drawable.uamp_ic_play_arrow_white_48dp));
        }
    }

    SeekBar.OnSeekBarChangeListener onSeekBarChangeListener=new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if(!fromUser){return;}
            Intent intent=new Intent();
            intent.setAction(INTENT_SEEK_BAR_CHANG);
            intent.putExtra(INTENT_SEEK_BAR_CHANG,progress);
            sendBroadcast(intent);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };

}
   /* BroadcastReceiver listChangeReceiver = new  BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action) {
                case INTENT_EXHIBIT_PROGRESS:
                    currentDuration = intent.getIntExtra(INTENT_EXHIBIT_DURATION, 0);
                    currentProgress = intent.getIntExtra(INTENT_EXHIBIT_PROGRESS, 0);
                    handler.sendEmptyMessage(MSG_WHAT_UPDATE_PROGRESS);
                    break;
                case INTENT_EXHIBIT:
                    String exhibitStr = intent.getStringExtra(INTENT_EXHIBIT);
                    if (TextUtils.isEmpty(exhibitStr)) {
                        return;
                    }
                    ExhibitBean exhibitBean = JSON.parseObject(exhibitStr, ExhibitBean.class);
                    if (currentExhibit.equals(exhibitBean)) {
                        return;
                    }else{
                        currentExhibit=exhibitBean;
                    }
                    handler.sendEmptyMessage(MSG_WHAT_CHANGE_EXHIBIT);
                    break;
                case INTENT_CHANGE_PLAY_PLAY:
                    //state=PLAY_STATE_START;
                    state= PlayChangeCallback.STATE_PLAYING;

                    handler.sendEmptyMessage(MSG_WHAT_CHANGE_PLAY_START);
                    break;
                case INTENT_CHANGE_PLAY_STOP:
                    //state=PLAY_STATE_STOP;
                    state= PlayChangeCallback.STATE_PLAYING;
                    handler.sendEmptyMessage(MSG_WHAT_CHANGE_PLAY_STOP);
                    break;
                case INTENT_EXHIBIT_LIST:
                    String exhibitJson=intent.getStringExtra(INTENT_EXHIBIT_LIST);
                    currentExhibitList= JSON.parseArray(exhibitJson,ExhibitBean.class);
                    if(currentExhibitList==null){return;}
                    handler.sendEmptyMessage(MSG_WHAT_UPDATE_DATA_SUCCESS);
                default:break;
            }
        }
    };*/
