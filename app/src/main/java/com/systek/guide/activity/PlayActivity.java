package com.systek.guide.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.systek.guide.R;
import com.systek.guide.adapter.MultiAngleImgAdapter;
import com.systek.guide.adapter.base.ViewPagerAdapter;
import com.systek.guide.entity.ExhibitBean;
import com.systek.guide.entity.MultiAngleImg;
import com.systek.guide.fragment.BaseFragment;
import com.systek.guide.fragment.IconImageFragment;
import com.systek.guide.fragment.LyricFragment;
import com.systek.guide.manager.MediaServiceManager;
import com.systek.guide.utils.ExceptionUtil;
import com.systek.guide.utils.ImageUtil;
import com.systek.guide.utils.TimeUtil;

import java.util.ArrayList;

public class PlayActivity extends BaseActivity implements LyricFragment.OnFragmentInteractionListener,IconImageFragment.OnFragmentInteractionListener {

    private ImageView ivExhibitIcon;//歌词背景大图
    private ArrayList<MultiAngleImg> multiAngleImgs;//多角度图片
    private String currentMuseumId;
    private ExhibitBean currentExhibit;
    private MultiAngleImgAdapter mulTiAngleImgAdapter;//多角度图片adapter
    private ArrayList<Integer> imgsTimeList;
    private ImageView ivPlayCtrl;//,ivTitleBarBack
    private TextView tvPlayTime;//tvExhibitName
    private SeekBar seekBarProgress;
    private int currentProgress;
    private int currentDuration;
    private RecyclerView recycleMultiAngle;
    private String currentIconUrl;
    private MediaServiceManager mediaServiceManager;
    private TextView tvTotalTime;
    private ViewPager viewpagerWordImage;
    private LyricFragment lyricFragment;
    private IconImageFragment iconImageFragment;

    @Override
    void setView() {
        View view = View.inflate(this, R.layout.activity_play, null);
        setContentView(view);
    }
    @Override
    void initData() {
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
        handler.sendEmptyMessage(MSG_WHAT_UPDATE_DATA_SUCCESS);
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        initData();
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


    /**
     * 添加监听器
     */
    void addListener() {
        ivPlayCtrl.setOnClickListener(onClickListener);
        seekBarProgress.setOnSeekBarChangeListener(onSeekBarChangeListener);
        mulTiAngleImgAdapter.setOnItemClickListener(new MultiAngleImgAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                MultiAngleImg multiAngleImg = multiAngleImgs.get(position);
                currentIconUrl = multiAngleImg.getUrl();
                Intent intent = new Intent();
                intent.setAction(INTENT_SEEK_BAR_CHANG);
                intent.putExtra(INTENT_SEEK_BAR_CHANG, multiAngleImg.getTime());
                sendBroadcast(intent);
            }
        });

        viewpagerWordImage.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                initIcon();
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

    }

    View.OnClickListener onClickListener=new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.ivPlayCtrl:
                    Intent intent=new Intent();
                    intent.setAction(INTENT_CHANGE_PLAY_STATE);
                    sendBroadcast(intent);
                    break;
            }

        }
    };

    /**
     * 注册广播接收器
     */
    void registerReceiver() {
        registerBluetoothReceiver();
        IntentFilter filter=new IntentFilter();
        filter.addAction(INTENT_EXHIBIT);
        filter.addAction(INTENT_EXHIBIT_PROGRESS);
        filter.addAction(INTENT_EXHIBIT_DURATION);
        filter.addAction(INTENT_CHANGE_PLAY_PLAY);
        filter.addAction(INTENT_CHANGE_PLAY_STOP);
        registerReceiver(receiver, filter);
    }

    @Override
    void unRegisterReceiver() {
        unregisterReceiver(receiver);
        unRegisterBluetoothReceiver();
    }

    /**
     * 刷新界面
     */
    @Override
    void refreshView() {
        if(currentExhibit==null){return;}
        currentMuseumId=currentExhibit.getMuseumId();
        if(iconImageFragment==null){
            iconImageFragment=new IconImageFragment();
        }
        if(lyricFragment==null){
            lyricFragment=new LyricFragment();
        }
        lyricFragment.setExhibit(currentExhibit);
        ArrayList<BaseFragment> fragments=new ArrayList<>();
        fragments.add(lyricFragment);
        fragments.add(iconImageFragment);
        ViewPagerAdapter viewPagerAdapter=new ViewPagerAdapter(getSupportFragmentManager(),fragments);
        viewpagerWordImage.setAdapter(viewPagerAdapter);
        setTitleBarTitle(currentExhibit.getName());
        initMultiImgs();
        if(currentExhibit!=null){
            currentIconUrl=currentExhibit.getIconurl();
        }
        initIcon();
    }

    @Override
    void refreshExhibit() {
        refreshView();
    }

    @Override
    void refreshTitle() {

    }

    @Override
    void refreshViewBottomTab() {

    }


    @Override
    void refreshState() {
        if(state==PLAY_STATE_START){
            ivPlayCtrl.setImageDrawable(getResources().getDrawable(R.drawable.uamp_ic_pause_white_48dp));//iv_play_state_open_big,ic_pause_black_36dp
        }else{
            ivPlayCtrl.setImageDrawable(getResources().getDrawable(R.drawable.uamp_ic_play_arrow_white_48dp));
        }
    }

    @Override
    void refreshProgress() {
        seekBarProgress.setMax(currentDuration);
        seekBarProgress.setProgress(currentProgress);
        lyricFragment.notifyTime(currentProgress);
        tvPlayTime.setText(TimeUtil.changeToTime(currentProgress).substring(3));
        tvTotalTime.setText(TimeUtil.changeToTime(currentDuration).substring(3));
        refreshIcon();
    }

    /**
     * 初始化icon图标
     */
    private void initIcon() {

        if(TextUtils.isEmpty(currentIconUrl)){return;}
        if (viewpagerWordImage.getCurrentItem()==0) {
            ImageUtil.displayImage(currentIconUrl,ivExhibitIcon,false,true);
        }else{
            ImageUtil.displayImage(currentIconUrl,ivExhibitIcon,false,false);
        }
    }

    /*初始化界面控件*/
    void initView() {

        setMyTitleBar();
        setHomeIcon();
        setHomeClickListener(backOnClickListener);
        viewpagerWordImage=(ViewPager)findViewById(R.id.viewpagerWordImage);
        mediaServiceManager=MediaServiceManager.getInstance(this);
        //去除滑动到末尾时的阴影
        viewpagerWordImage.setOverScrollMode(ScrollView.OVER_SCROLL_NEVER);
        seekBarProgress=(SeekBar)findViewById(R.id.seekBarProgress);
        tvPlayTime=(TextView)findViewById(R.id.tvPlayTime);
        tvTotalTime=(TextView)findViewById(R.id.tvTotalTime);
        recycleMultiAngle = (RecyclerView) findViewById(R.id.recycleMultiAngle);
        ivPlayCtrl=(ImageView)findViewById(R.id.ivPlayCtrl);
        ivExhibitIcon=(ImageView)findViewById(R.id.ivExhibitIcon);
        multiAngleImgs=new ArrayList<>();
        mulTiAngleImgAdapter=new MultiAngleImgAdapter(this,multiAngleImgs);
        /*设置为横向*/
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        recycleMultiAngle.setLayoutManager(linearLayoutManager);
        recycleMultiAngle.setAdapter(mulTiAngleImgAdapter);
        recycleMultiAngle.setOverScrollMode(ScrollView.OVER_SCROLL_NEVER);
    }

    private void setMyTitleBar() {
        View v = findViewById(R.id.toolbar_alpha);
        if (v != null) {
            toolbar = (Toolbar) v;
            setSupportActionBar(toolbar);
            toolbarTitle = (TextView) v.findViewById(R.id.toolbar_title);
            if (toolbarTitle == null) {return;}
            ActionBar actionBar= getSupportActionBar();
            if(actionBar==null){ return;}
            actionBar.setDisplayShowTitleEnabled(false);
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
    public void onFragmentInteraction(ExhibitBean exhibit) {
        // TODO: 2016/2/16  
    }

    //加载多角度图片
    private void initMultiImgs() {
        multiAngleImgs.clear();
        //当前展品为空，返回
        if(currentExhibit==null){return;}
        String imgStr=currentExhibit.getImgsurl();
        // 没有多角度图片，返回
        if(TextUtils.isEmpty(imgStr)){
            MultiAngleImg multiAngleImg=new MultiAngleImg();
            multiAngleImg.setUrl(currentExhibit.getIconurl());
            multiAngleImgs.add(multiAngleImg);
        }else{//获取多角度图片地址数组
            String[] imgs = imgStr.split(",");
            imgsTimeList=new ArrayList<>();
            for (String singleUrl : imgs) {
                String[] nameTime = singleUrl.split("\\*");
                MultiAngleImg multiAngleImg=new MultiAngleImg();
                int time=Integer.valueOf(nameTime[1]);
                multiAngleImg.setTime(time);
                multiAngleImg.setUrl(nameTime[0]);
                imgsTimeList.add(time);
                multiAngleImgs.add(multiAngleImg);
            }
        }
        mulTiAngleImgAdapter.updateData(multiAngleImgs);
    }

    @Override
    protected void onDestroy() {
        viewpagerWordImage.removeAllViewsInLayout();
        super.onDestroy();
    }


    BroadcastReceiver receiver=new BroadcastReceiver(){

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
                    if (currentExhibit==null||!currentExhibit.equals(exhibitBean)) {
                        currentExhibit=exhibitBean;
                        handler.sendEmptyMessage(MSG_WHAT_CHANGE_EXHIBIT);
                    }
                    break;
                case INTENT_CHANGE_PLAY_PLAY:
                    state=PLAY_STATE_START;
                    handler.sendEmptyMessage(MSG_WHAT_CHANGE_PLAY_START);
                    break;
                case INTENT_CHANGE_PLAY_STOP:
                    state=PLAY_STATE_STOP;
                    handler.sendEmptyMessage(MSG_WHAT_CHANGE_PLAY_STOP);
                    break;
            }
        }
    };

    /**
     * 刷新icon图标
     */
    public void refreshIcon(){
        if (imgsTimeList==null||imgsTimeList.size() == 0) {return;}
        for (int i = 0; i < imgsTimeList.size()-1; i++) {
            int imgTime = imgsTimeList.get(i);
            int overTime= imgsTimeList.get(i+1);
            if (currentProgress > imgTime && currentProgress <= overTime) {
                if(multiAngleImgs==null||multiAngleImgs.size()==0){return;}
                for(MultiAngleImg angleImg:multiAngleImgs){
                    if(angleImg.getTime()==imgTime){
                        currentIconUrl=angleImg.getUrl();
                        initIcon();
                    }
                }
            }else if(currentProgress>overTime){
                try{
                    currentIconUrl=multiAngleImgs.get(imgsTimeList.size()-1).getUrl();
                    initIcon();
                }catch (Exception e){ExceptionUtil.handleException(e);}
            }
        }
    }

}
