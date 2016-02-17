package com.systek.guide.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
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
import com.systek.guide.lyric.LyricAdapter;
import com.systek.guide.lyric.LyricDownloadManager;
import com.systek.guide.lyric.LyricLoadHelper;
import com.systek.guide.lyric.LyricSentence;
import com.systek.guide.manager.MediaServiceManager;
import com.systek.guide.utils.ExceptionUtil;
import com.systek.guide.utils.ImageLoaderUtil;
import com.systek.guide.utils.LogUtil;
import com.systek.guide.utils.TimeUtil;
import com.systek.guide.utils.Tools;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PlayActivity extends BaseActivity implements LyricFragment.OnFragmentInteractionListener,IconImageFragment.OnFragmentInteractionListener {

    private Handler handler;
    private ListView lvLyric;//歌词listview
    private ImageView ivExhibitIcon;//歌词背景大图
    private ImageView imgWordCtrl;//歌词imageview
    private ArrayList<MultiAngleImg> multiAngleImgs;//多角度图片
    private MultiAngleImgAdapter mulTiAngleImgAdapter;//多角度图片adapter
    private String currentLyricUrl;/*当前歌词路径*/
    private LyricLoadHelper mLyricLoadHelper;
    private LyricAdapter mLyricAdapter;
    private String currentMuseumId;
    private ExhibitBean currentExhibit;
    private ArrayList<Integer> imgsTimeList;
    private ImageView ivPlayCtrl,ivTitleBarBack;
    private TextView tvPlayTime,tvExhibitName;
    private SeekBar seekBarProgress;
    private int currentProgress;
    private int currentDuration;
    private PlayStateReceiver playStateReceiver;
    private RecyclerView recycleMultiAngle;
    private String currentExhibitStr;
    private String currentIconUrl;
    private MediaServiceManager mediaServiceManager;
    private TextView tvTotalTime;
    private boolean hasMultiImage;
    private ViewPager viewpagerWordImage;
    private LyricFragment lyricFragment;
    private IconImageFragment iconImageFragment;


    @Override
    protected void initialize(Bundle savedInstanceState) {
        setContentView(R.layout.activity_play);
        handler =new MyHandler();
        mediaServiceManager=MediaServiceManager.getInstance(this);
        initView();
        addListener();
        registerReceiver();
        Intent intent=getIntent();
        String exhibitStr=intent.getStringExtra(INTENT_EXHIBIT);
        if(!TextUtils.isEmpty(exhibitStr)){
            ExhibitBean bean=JSON.parseObject(exhibitStr,ExhibitBean.class);
            if(currentExhibit==null){
                currentExhibit=bean;
                initData();
            }else{
                if(currentExhibit.equals(bean)){
                    refreshView();
                }else {
                    initData();
                }
            }
        }else{
            currentExhibit=mediaServiceManager.getCurrentExhibit();
            refreshView();
        }
        LogUtil.i("ZHANG","执行了initialize");
    }


    @Override
    protected void onNewIntent(Intent intent) {
        LogUtil.i("ZHANG", "执行了onNewIntent");
        super.onNewIntent(intent);

        String exhibitStr=intent.getStringExtra(INTENT_EXHIBIT);
        if(!TextUtils.isEmpty(exhibitStr)){
            ExhibitBean bean=JSON.parseObject(exhibitStr,ExhibitBean.class);
            if(currentExhibit==null){
                currentExhibit=bean;
                initData();
            }else{
                if(currentExhibit.equals(bean)){
                    refreshView();
                }else {
                    initData();
                }
            }

        }else{
            currentExhibit=mediaServiceManager.getCurrentExhibit();
            refreshView();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        LogUtil.i("ZHANG", "执行了onStart");
    }

    @Override
    protected void onResume() {
        LogUtil.i("ZHANG", "执行了onResume");
        super.onResume();
        if(mediaServiceManager.isPlaying()){
            handler.sendEmptyMessage(MSG_WHAT_CHANGE_PLAY_START);
        }else{
            handler.sendEmptyMessage(MSG_WHAT_CHANGE_PLAY_STOP);
        }

    }

    private void addListener() {
        ivPlayCtrl.setOnClickListener(onClickListener);
        ivTitleBarBack.setOnClickListener(onClickListener);
        seekBarProgress.setOnSeekBarChangeListener(onSeekBarChangeListener);
        mulTiAngleImgAdapter.setOnItemClickListener(new MultiAngleImgAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                MultiAngleImg multiAngleImg = multiAngleImgs.get(position);
                currentIconUrl = multiAngleImg.getUrl();
                //initIcon();
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


    private void registerReceiver() {
        playStateReceiver=new PlayStateReceiver();
        IntentFilter filter=new IntentFilter();
        filter.addAction(INTENT_EXHIBIT);
        filter.addAction(INTENT_EXHIBIT_PROGRESS);
        filter.addAction(INTENT_EXHIBIT_DURATION);
        filter.addAction(INTENT_CHANGE_PLAY_PLAY);
        filter.addAction(INTENT_CHANGE_PLAY_STOP);
        registerReceiver(playStateReceiver, filter);

    }

    private void refreshView() {
        LogUtil.i("ZHANG", "执行了refreshView");
        currentMuseumId=currentExhibit.getMuseumId();
        tvExhibitName.setText(currentExhibit.getName());
        initMultiImgs();
        loadLyricByHand();
        if(currentExhibit!=null){
            currentIconUrl=currentExhibit.getIconurl();
        }
        initIcon();
    }

    private void initIcon() {

        if(currentIconUrl==null){return;}
        String imageName = Tools.changePathToName(currentIconUrl);
        String imgLocalUrl = LOCAL_ASSETS_PATH+currentMuseumId + "/" + LOCAL_FILE_TYPE_IMAGE+"/"+imageName;
        File file = new File(imgLocalUrl);
        // 判断sdcard上有没有图片
        if (file.exists()) {
            // 显示sdcard
            if (viewpagerWordImage.getCurrentItem()==0) {
                ImageLoaderUtil.displaySdcardBlurImage(this, imgLocalUrl, ivExhibitIcon);
            }else{
                ImageLoaderUtil.displaySdcardImage(this, imgLocalUrl, ivExhibitIcon);
            }
        } else {
            if (viewpagerWordImage.getCurrentItem()==0) {
                ImageLoaderUtil.displayNetworkBlurImage(this, BASE_URL + currentIconUrl, ivExhibitIcon);
            }else{
                ImageLoaderUtil.displayNetworkBlurImage(this, BASE_URL + currentIconUrl, ivExhibitIcon);
            }
        }
    }

    /*初始化界面控件*/
    private void initView() {

        viewpagerWordImage=(ViewPager)findViewById(R.id.viewpagerWordImage);

        iconImageFragment=IconImageFragment.newInstance(null,null);
        lyricFragment=LyricFragment.newInstance(null);
        ArrayList<BaseFragment> fragments=new ArrayList<>();
        fragments.add(lyricFragment);
        fragments.add(iconImageFragment);
        ViewPagerAdapter viewPagerAdapter=new ViewPagerAdapter(getSupportFragmentManager(),fragments);
        viewpagerWordImage.setAdapter(viewPagerAdapter);
        //去除滑动到末尾时的阴影
        viewpagerWordImage.setOverScrollMode(ScrollView.OVER_SCROLL_NEVER);

        //lvLyric=(ListView)findViewById(R.id.lvLyric);
        //imgExhibitIcon=(ImageView)findViewById(R.id.imgExhibitIcon);
        seekBarProgress=(SeekBar)findViewById(R.id.seekBarProgress);
        tvPlayTime=(TextView)findViewById(R.id.tvPlayTime);
        tvTotalTime=(TextView)findViewById(R.id.tvTotalTime);

        tvExhibitName=(TextView)findViewById(R.id.tvExhibitName);
        recycleMultiAngle = (RecyclerView) findViewById(R.id.recycleMultiAngle);
        ivPlayCtrl=(ImageView)findViewById(R.id.ivPlayCtrl);
        imgWordCtrl=(ImageView)findViewById(R.id.imgWordCtrl);
        ivTitleBarBack=(ImageView)findViewById(R.id.ivTitleBarBack);

        ivExhibitIcon=(ImageView)findViewById(R.id.ivExhibitIcon);

        multiAngleImgs=new ArrayList<>();
        mulTiAngleImgAdapter=new MultiAngleImgAdapter(this,multiAngleImgs);
        /*设置为横向*/
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        recycleMultiAngle.setLayoutManager(linearLayoutManager);
        recycleMultiAngle.setAdapter(mulTiAngleImgAdapter);
        recycleMultiAngle.setOverScrollMode(ScrollView.OVER_SCROLL_NEVER);
        // mLyricLoadHelper = new LyricLoadHelper();
        //mLyricAdapter = new LyricAdapter(this);
        //mLyricLoadHelper.setLyricListener(mLyricListener);
        //lvLyric.setAdapter(mLyricAdapter);
        // imgWordCtrl.setOnClickListener(onClickListener);
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

    View.OnClickListener onClickListener=new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.imgWordCtrl:
                    if(lvLyric.getVisibility()!=View.GONE){
                        lvLyric.setVisibility(View.GONE);
                        ivExhibitIcon.setAlpha(1.0f);
                    }else{
                        lvLyric.setVisibility(View.VISIBLE);
                        ivExhibitIcon.setAlpha(0.4f);
                    }
                    break;
                case R.id.ivTitleBarBack:
                    finish();
                    break;
                case R.id.ivPlayCtrl:
                    Intent intent=new Intent();
                    intent.setAction(INTENT_CHANGE_PLAY_STATE);
                    sendBroadcast(intent);
            }

        }
    };


    private LyricLoadHelper.LyricListener mLyricListener = new LyricLoadHelper.LyricListener() {

        @Override
        public void onLyricLoaded(List<LyricSentence> lyricSentences, int index) {
            if (lyricSentences != null) {
                //LogUtil.i(TAG, "onLyricLoaded--->歌词句子数目=" + lyricSentences.size() + ",当前句子索引=" + index);
                mLyricAdapter.setLyric(lyricSentences);
                mLyricAdapter.setCurrentSentenceIndex(index);
                mLyricAdapter.notifyDataSetChanged();
            }
        }
        @Override
        public void onLyricSentenceChanged(int indexOfCurSentence) {
            mLyricAdapter.setCurrentSentenceIndex(indexOfCurSentence);
            mLyricAdapter.notifyDataSetChanged();
            lvLyric.smoothScrollToPositionFromTop(indexOfCurSentence, lvLyric.getHeight() / 2, 500);
        }
    };

    /*加载数据*/
    private void initData() {
        if(currentExhibit==null){return;}
        currentMuseumId=currentExhibit.getMuseumId();
        handler.sendEmptyMessage(MSG_WHAT_REFRESH_VIEW);
    }

    private void loadLyricByHand() {
        if(currentExhibit==null){return;}

        lyricFragment.setExhibit(currentExhibit);
        lyricFragment.loadLyricByHand();

       /* try{
            currentLyricUrl = currentExhibit.getTexturl();
            String name = currentLyricUrl.replaceAll("/", "_");
            // 取得歌曲同目录下的歌词文件绝对路径
            String lyricFilePath = LOCAL_ASSETS_PATH+currentMuseumId+"/"+LOCAL_FILE_TYPE_LYRIC+"/"+ name;
            File lyricFile = new File(lyricFilePath);
            if (lyricFile.exists()) {
                // 本地有歌词，直接读取
                mLyricLoadHelper.loadLyric(lyricFilePath);
            } else {
                //mIsLyricDownloading = true;
                // 尝试网络获取歌词
                //LogUtil.i("ZHANG", "loadLyric()--->本地无歌词，尝试从网络获取");
                new LyricDownloadAsyncTask().execute(currentLyricUrl);
            }
        }catch (Exception e){
            ExceptionUtil.handleException(e);
        }*/
    }

    @Override
    public void onFragmentInteraction(ExhibitBean exhibit) {
        // TODO: 2016/2/16  
    }


    private class LyricDownloadAsyncTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            LyricDownloadManager mLyricDownloadManager = new LyricDownloadManager(PlayActivity.this);
            // 从网络获取歌词，然后保存到本地
            String lyricFilePath = mLyricDownloadManager.searchLyricFromWeb(params[0],
                    LOCAL_ASSETS_PATH + currentMuseumId + "/" + LOCAL_FILE_TYPE_LYRIC);
            // 返回本地歌词路径
            // mIsLyricDownloading = false;
            return lyricFilePath;
        }

        @Override
        protected void onPostExecute(String lyricSavePath) {
            // Log.i(TAG, "网络获取歌词完毕，歌词保存路径:" + result);
            // 读取保存到本地的歌曲
            mLyricLoadHelper.loadLyric(lyricSavePath);
        }
    }


    //加载多角度图片
    private void initMultiImgs() {
        multiAngleImgs.clear();
        //当前展品为空，返回
        if(currentExhibit==null){return;}
        String imgStr=currentExhibit.getImgsurl();
        // 没有多角度图片，返回
        if(TextUtils.isEmpty(imgStr)){
            hasMultiImage=false;
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
        unregisterReceiver(playStateReceiver);
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    private class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case MSG_WHAT_UPDATE_PROGRESS:
                    seekBarProgress.setMax(currentDuration);
                    seekBarProgress.setProgress(currentProgress);
//                    mLyricLoadHelper.notifyTime(currentProgress);
                    lyricFragment.notifyTime(currentProgress);
                    tvPlayTime.setText(TimeUtil.changeToTime(currentProgress).substring(3));
                    tvTotalTime.setText(TimeUtil.changeToTime(currentDuration).substring(3));
                    refreshIcon();
                    break;
                case MSG_WHAT_CHANGE_EXHIBIT:
                    initData();
                    break;
                case MSG_WHAT_REFRESH_VIEW:
                    refreshView();
                    break;
                case MSG_WHAT_CHANGE_PLAY_START:
                    ivPlayCtrl.setImageDrawable(getResources().getDrawable(R.drawable.iv_play_state_open_big));
                    break;
                case MSG_WHAT_CHANGE_PLAY_STOP:
                    ivPlayCtrl.setImageDrawable(getResources().getDrawable(R.drawable.iv_play_state_close_big));
                    break;
            }
        }
    }

    class PlayStateReceiver extends BroadcastReceiver{

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
                    handler.sendEmptyMessage(MSG_WHAT_CHANGE_PLAY_START);
                    break;
                case INTENT_CHANGE_PLAY_STOP:
                    handler.sendEmptyMessage(MSG_WHAT_CHANGE_PLAY_STOP);
                    break;
            }
        }
    }

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
