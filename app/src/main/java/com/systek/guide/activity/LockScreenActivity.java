package com.systek.guide.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
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
import com.systek.guide.entity.ExhibitBean;
import com.systek.guide.manager.MediaServiceManager;
import com.systek.guide.utils.ImageLoaderUtil;
import com.systek.guide.utils.TimeUtil;

import java.util.ArrayList;
import java.util.List;

public class LockScreenActivity extends SwipeBackActivity implements IConstants{



    private String currentMuseumId;
    private String currentIconUrl;
    private TextView tvLockTime;
    private RecyclerView recycleNearly;
    private List<ExhibitBean> nearlyExhibitList;
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

    private static final int PLAY_STATE_START=1;
    private static final int PLAY_STATE_STOP=2;
    private   int state=PLAY_STATE_STOP;

    @Override
    protected void setView() {
        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        setContentView(R.layout.activity_lock_screen);
    }

    @Override
    protected void initView() {
        mediaServiceManager=MediaServiceManager.getInstance(this);
        fullscreenImage = (ImageView) findViewById(R.id.fullscreenImage);
        ivPlayCtrl = (ImageView) findViewById(R.id.ivPlayCtrl);
        tvPlayTime = (TextView) findViewById(R.id.tvPlayTime);
        tvTotalTime = (TextView) findViewById(R.id.tvTotalTime);
        tvLockTime = (TextView) findViewById(R.id.tvLockTime);
        tvExhibitName = (TextView) findViewById(R.id.tvExhibitName);
        tvLockTime.setText(TimeUtil.getTime());

        seekBarProgress=(SeekBar)findViewById(R.id.seekBarProgress);
        recycleNearly = (RecyclerView) findViewById(R.id.recycleNearly);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        recycleNearly.setLayoutManager(linearLayoutManager);

        nearlyExhibitList = new ArrayList<>();
        nearlyGalleryAdapter = new NearlyGalleryAdapter(getActivity(), nearlyExhibitList);
        recycleNearly.setAdapter(nearlyGalleryAdapter);
        recycleNearly.setOverScrollMode(ScrollView.OVER_SCROLL_NEVER);
    }

    private void initIcon() {
        if(currentExhibit==null){return;}
        currentIconUrl=currentExhibit.getIconurl();
        ImageLoaderUtil.displayImage(currentIconUrl, fullscreenImage);
    }


    @Override
    protected void onResume() {
        super.onResume();
        if(mediaServiceManager.isPlaying()){
            state=PLAY_STATE_START;
        }else{
            state=PLAY_STATE_STOP;
        }
        refreshState();
    }


    /*//屏蔽掉Home键
    public void onAttachedToWindow() {
        this.getWindow().setType(WindowManager.LayoutParams.TYPE_KEYGUARD_DIALOG);
        super.onAttachedToWindow();
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return event.getKeyCode() == KeyEvent.KEYCODE_BACK || super.onKeyDown(keyCode, event);
    }*/


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void addListener() {

        nearlyGalleryAdapter.setOnItemClickListener(new NearlyGalleryAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {


                ExhibitBean exhibitBean = currentExhibitList.get(position);
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
                startActivity(intent1);
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


    @Override
    void initData() {
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



    @Override
    void registerReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(INTENT_EXHIBIT);
        filter.addAction(INTENT_EXHIBIT_PROGRESS);
        filter.addAction(INTENT_EXHIBIT_DURATION);
        filter.addAction(INTENT_CHANGE_PLAY_PLAY);
        filter.addAction(INTENT_CHANGE_PLAY_STOP);
        filter.addAction(INTENT_EXHIBIT_LIST);
        getActivity().registerReceiver(listChangeReceiver, filter);
    }

    @Override
    void unRegisterReceiver() {
        unregisterReceiver(listChangeReceiver);
    }

    @Override
    void refreshView() {
        if(nearlyGalleryAdapter!=null&&currentExhibitList!=null){
            nearlyGalleryAdapter.updateData(currentExhibitList);
        }
    }

    @Override
    void refreshExhibit() {
        if(currentExhibit==null){return;}
        currentMuseumId=currentExhibit.getMuseumId();
        tvExhibitName.setText(currentExhibit.getName());
        initIcon();
    }

    @Override
    void refreshTitle() {

    }

    @Override
    void refreshViewBottomTab() {

    }

    @Override
    void refreshProgress() {
        seekBarProgress.setMax(currentDuration);
        seekBarProgress.setProgress(currentProgress);
        tvPlayTime.setText(TimeUtil.changeToTime(currentProgress).substring(3));
        tvTotalTime.setText(TimeUtil.changeToTime(currentDuration).substring(3));
    }

    @Override
    void refreshIcon() {

    }

    @Override
    void refreshState() {

        if(state==PLAY_STATE_START){
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



    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


     BroadcastReceiver listChangeReceiver = new  BroadcastReceiver() {

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
                    handler.sendEmptyMessage(MSG_WHAT_CHANGE_PLAY_START);
                    break;
                case INTENT_CHANGE_PLAY_STOP:
                    handler.sendEmptyMessage(MSG_WHAT_CHANGE_PLAY_STOP);
                    break;
                case INTENT_EXHIBIT_LIST:
                    String exhibitJson=intent.getStringExtra(INTENT_EXHIBIT_LIST);
                    currentExhibitList= JSON.parseArray(exhibitJson,ExhibitBean.class);
                    if(currentExhibitList==null){return;}
                    handler.sendEmptyMessage(MSG_WHAT_UPDATE_DATA_SUCCESS);
            }
        }
    };


}
