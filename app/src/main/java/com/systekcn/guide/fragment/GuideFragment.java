package com.systekcn.guide.fragment;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.magic.mapdemo.R;
import com.systekcn.guide.MyApplication;
import com.systekcn.guide.biz.BeansManageBiz;
import com.systekcn.guide.biz.BizFactory;
import com.systekcn.guide.common.IConstants;
import com.systekcn.guide.common.utils.ExceptionUtil;
import com.systekcn.guide.common.utils.ImageLoaderUtil;
import com.systekcn.guide.common.utils.LogUtil;
import com.systekcn.guide.common.utils.Tools;
import com.systekcn.guide.entity.ExhibitBean;
import com.systekcn.guide.lyric.LyricAdapter;
import com.systekcn.guide.lyric.LyricDownloadManager;
import com.systekcn.guide.lyric.LyricLoadHelper;
import com.systekcn.guide.lyric.LyricSentence;
import com.systekcn.guide.manager.MediaServiceManager;
import com.systekcn.guide.service.MediaPlayService;
import com.systekcn.guide.widget.SwitchButton;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

public class GuideFragment extends Fragment implements IConstants {

    public static final String TAG = "com.systekcn.guide.fragment.Guide_Fragment";
    /**
     * 屏幕宽度
     */
    private int mScreenWidth;
    /**
     * 歌词加载管理
     */
    private LyricLoadHelper mLyricLoadHelper;
    /**
     * 歌词下载管理
     */
    private LyricDownloadManager mLyricDownloadManager;
    private Activity activity;
    /**
     * 播放进度广播接收器
     */
    private PlayStateChangeReceiver playStateChangeReceiver;
    /**
     * 当前播放进度条
     */
    private SeekBar music_seekBar;
    /**
     * 播放控制键
     */
    private ImageView iv_lyric_ctrl;
    /**
     * 当歌词为空时显示
     */
    private TextView lyric_empty;
    /**
     * 多角度图片布局
     */
    private LinearLayout ll_multi_angle_img;
    /**
     * 附近展品布局
     */
    private LinearLayout ll_nearly_exhibit;
    /**
     * 歌词显示
     */
    private ListView lyricShow;
    /**
     * 歌词显示适配器
     */
    private LyricAdapter mLyricAdapter;

    /**
     * 当前fragment的view
     */
    private View rootView;
    /**
     * 当前是否在下载歌词
     */
    private boolean mIsLyricDownloading;
    /**
     * 进度条最大值
     */
    private int currentMax;

    private String currentExhibitId;
    /**歌词背景图片*/
    private ImageView iv_frag_largest_img;
    /**
     * 信息类型
     */
    private final int MSG_WHAT_CHANGE_ICON = 1;//切换主图
    private final int MSG_WHAT_CHANGE_EXHIBIT = 2;//切换展品
    private final int MSG_WHAT_UPDATE_NEARLY_EXHIBIT = 3;//刷新附近展品

    /**播放器管理类*/
    private MediaServiceManager mediaServiceManager;
    /**
     * 当前歌词路径
     */
    private String currentLyricUrl;
    /**
     * 播放控制键
     */
    private ImageView music_play_and_ctrl;
    /**歌词是否显示*/
    private boolean isLyricShowing = true;
    /**多角度图片时间段集合*/
    private MyApplication application;
    /**多角度时间集合*/
    private ArrayList<Integer> imgsTimeList;
    /**多角度图片URL与时间键值对集合*/
    private HashMap<Integer, String> multiImgMap;
    /**是否有多角度图片*/
    private boolean hasMultiImg =false;
    /**专题开关*/
    private SwitchButton btn_switch_topic;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            /**当信息类型为更换歌词背景*/
            if (msg.what == MSG_WHAT_CHANGE_ICON) {
                String imgPath = (String) msg.obj;
                String currentIconPath=(String)iv_frag_largest_img.getTag();
                String imgLocalPath = application.getCurrentImgDir() + Tools.changePathToName(imgPath);
                /**若歌词路径不为空，判断图片url,加载图片*/
                if(currentIconPath!=null&&!imgPath.equals(currentIconPath)&&!imgPath.equals(imgLocalPath)){
                    if (Tools.isFileExist(imgLocalPath)) {
                        iv_frag_largest_img.setTag(imgLocalPath);
                        ImageLoaderUtil.displaySdcardImage(activity, imgLocalPath, iv_frag_largest_img);
                    } else {
                        String httpPath = BASEURL + imgPath;
                        iv_frag_largest_img.setTag(httpPath);
                        ImageLoaderUtil.displayNetworkImage(activity, httpPath, iv_frag_largest_img);
                    }
                }else{
                    currentIconPath=null;
                    imgPath=null;
                }
                /**若信息类型为展品切换，刷新数据，刷新界面*/
            } else if (msg.what == MSG_WHAT_CHANGE_EXHIBIT) {
                refreshData();
                refreshView();
                mediaServiceManager.notifyAllDataChange();
            }else if(msg.what==MSG_WHAT_UPDATE_NEARLY_EXHIBIT){/**更新附近展品*/
                refreshNearly();
            }
        }
    };


    public static GuideFragment newInstance() {
        GuideFragment fragment = new GuideFragment();
        return fragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        long time =System.currentTimeMillis();
        super.onCreate(savedInstanceState);
        application= (MyApplication) activity.getApplication();
        mediaServiceManager = application.mServiceManager;
        registerReceiver();
        /**获得屏幕宽度*/
        DisplayMetrics metric = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(metric);
        mScreenWidth = metric.widthPixels;
        mLyricLoadHelper = new LyricLoadHelper();
        mLyricAdapter = new LyricAdapter(activity);
        mLyricLoadHelper.setLyricListener(mLyricListener);
        rootView = activity.getLayoutInflater().inflate(R.layout.fragment_guide, null);
        initialize();
        long costTime=System.currentTimeMillis()-time;
        LogUtil.i("ZHANG", "GuideFragment_onCreate耗时" + costTime);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        long time =System.currentTimeMillis();
        if(rootView==null){
            rootView = activity.getLayoutInflater().inflate(R.layout.fragment_guide, null);
            initialize();
        }
        long costTime=System.currentTimeMillis()-time;
        LogUtil.i("ZHANG", "GuideFragment_onCreateView耗时" + costTime);
        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        LogUtil.i("ZHANG", "GuideFragment_执行了onDestroyView");
        ((ViewGroup) (rootView.getParent())).removeView(rootView);
    }

    private void initialize() {
        long time =System.currentTimeMillis();
        initView();
        addListener();
        long costTime=System.currentTimeMillis()-time;
        LogUtil.i("ZHANG", "GuideFragment_initialize耗时" + costTime);
    }

    private void refreshView() {
        long time=System.currentTimeMillis();
        /**加载歌词*/
        loadLyricByHand();
        /**加载主图*/
        initIcon();
        /**加载多角度图片*/
        initMultiImgs();
        long costTime=System.currentTimeMillis()-time;
        LogUtil.i("ZHANG", "GuideFragment_refreshView耗时" + costTime);
    }

    /**加载主要数据*/
    private void refreshData() {
        long time=System.currentTimeMillis();
        /**如果当前展品不为空，刷新数据*/
        if(application.currentExhibitBean!=null){
            application.refreshData();
        }else {/**如果当前展品为空*/
            application.currentExhibitBean = application.totalExhibitBeanList.get(0);
        }
        /**当前展品ID*/
        currentExhibitId = application.currentExhibitBean.getId();
        /**加载歌词*/
        currentLyricUrl = application.currentExhibitBean.getTexturl();

        if(application.isTopicOpen){
            btn_switch_topic.setChecked(true);
        }else{
            btn_switch_topic.setChecked(false);
            initNearlyExhibit();
        }
        long costTime=System.currentTimeMillis()-time;
        LogUtil.i("ZHANG", "GuideFragment_refreshData耗时" + costTime);
    }

    /**加载附近列表*/
    private void initNearlyExhibit() {
        new Thread(){
            @Override
            public void run() {
                /**若附近展品列表为空，*/
                BeansManageBiz biz= (BeansManageBiz) BizFactory.getBeansManageBiz(activity);
                List<ExhibitBean> list=biz.getExhibitListByBeaconId(application.getCurrentMuseumId(), application.currentBeaconId);
                if(list!=null&&list.size()>0){
                    application.currentExhibitBeanList=list;
                    handler.sendEmptyMessage(MSG_WHAT_UPDATE_NEARLY_EXHIBIT);
                }
            }
        }.start();
    }
    /**刷新附近展品图片*/
    private void refreshNearly() {
        long time=System.currentTimeMillis();

        ll_nearly_exhibit.removeAllViews();

        if(application.isTopicOpen){
            application.nearlyExhibitBeanList=application.topicExhibitBeanList;
        }else{
            if(application.currentExhibitBeanList!=null&&application.currentExhibitBeanList.size()>0){
                application.nearlyExhibitBeanList.removeAll(application.currentExhibitBeanList);
                application.nearlyExhibitBeanList.addAll(application.currentExhibitBeanList);
            }
        }

        /**当附近展品列表不为空时*/
        if(application.nearlyExhibitBeanList!=null&&application.nearlyExhibitBeanList.size()>0) {
            /**循环遍历附近展品集合*/
            for(int i=0;i<application.nearlyExhibitBeanList.size();i++){
                ExhibitBean bean=application.nearlyExhibitBeanList.get(i);
                /**如果类中不包含附近列表中的展品，则加载*/
                ImageView imageView = new ImageView(activity);
                imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                imageView.setTag(bean);
                ll_nearly_exhibit.addView(imageView, new LinearLayout.LayoutParams(mScreenWidth / 3, LinearLayout.LayoutParams.MATCH_PARENT));
                /**判断本地是否有图片文件，决定加载本地或网络图片*/
                String imgUrl =application.nearlyExhibitBeanList.get(i).getIconurl();
                String name = Tools.changePathToName(imgUrl);
                String localUrl = application.getCurrentImgDir() + name;
                if (Tools.isFileExist(localUrl)) {
                    ImageLoaderUtil.displaySdcardImage(activity, localUrl, imageView);
                } else {
                    ImageLoaderUtil.displayNetworkImage(activity, BASEURL + imgUrl, imageView);
                }
                imageView.setOnClickListener(nearlyExhibitOnclickListener);
                /**如果看过的展品集合中包含这个展品*/
                if(application.everSeenExhibitBeanList.contains(bean)){
                    /**如果这个展品是当前展品，设置背景框，高亮，否则设置为灰色*/
                    if(bean.equals(application.currentExhibitBean)){
                        imageView.setBackgroundResource(R.drawable.img_back);
                        imageView.setAlpha(1f);
                    }else{
                        imageView.setAlpha(0.5f);
                    }
                }
                /**遍历附近图片控件父容器，如果大于8个，删除剩8个*/
                if(!application.isTopicOpen){
                    while(ll_nearly_exhibit.getChildCount()>8){
                        ll_nearly_exhibit.removeViewAt(0);
                        application.nearlyExhibitBeanList.remove(0);
                    }
                }
            }
            /**不管附近展品列表是否为空，都遍历附近展品列表*/
            ExhibitBean b=null;
            for(int i=0;i<ll_nearly_exhibit.getChildCount();i++){
                b= (ExhibitBean) ll_nearly_exhibit.getChildAt(i).getTag();
                /**如果看过的展品包括这个展品，*/
                if(application.everSeenExhibitBeanList.contains(b)){
                    /**如果这个展品是当前展品，设置背景框，高亮，否则设置为灰色*/
                    if(b.equals(application.currentExhibitBean)){
                        ll_nearly_exhibit.getChildAt(i).setBackgroundResource(R.drawable.img_back);
                        ll_nearly_exhibit.getChildAt(i).setAlpha(1f);
                    }else{
                        ll_nearly_exhibit.getChildAt(i).setAlpha(0.5f);
                        ll_nearly_exhibit.getChildAt(i).setBackgroundResource(0);
                    }
                }else{/**如果看过的展品不包括这个展品*/
                    /**未看过的当前展品，都设置为高亮，如果是当前展品，则加背景框*/
                    if(b.equals(application.currentExhibitBean)){
                        ll_nearly_exhibit.getChildAt(i).setBackgroundResource(R.drawable.img_back);
                    }
                    ll_nearly_exhibit.getChildAt(i).setAlpha(1f);
                }
            }
        }

        long costTime=System.currentTimeMillis()-time;
        LogUtil.i("ZHANG", "GuideFragment_refreshNearly耗时" + costTime);

    }

    /**附近列表监听器*/
    View.OnClickListener nearlyExhibitOnclickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            ExhibitBean exhibit = (ExhibitBean) v.getTag();
            if (exhibit != null) {
                if(!exhibit.equals(application.currentExhibitBean)){
                    application.currentExhibitBean=exhibit;
                    v.setBackgroundResource(R.drawable.img_back);
                    handler.sendEmptyMessage(MSG_WHAT_CHANGE_EXHIBIT);
                }
            }
        }
    };

    /**多角度图片监听器*/
    View.OnClickListener multiImgListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Entry<Integer, String> entry = (Entry<Integer, String>) v.getTag();
            int time = entry.getKey();
            String imgPath = entry.getValue();
            Message msg = Message.obtain();
            msg.what = MSG_WHAT_CHANGE_ICON;
            msg.obj = imgPath;
            handler.sendMessage(msg);
            if(mediaServiceManager.isPlaying()){
                mediaServiceManager.seekTo(time);
            }
        }
    };
    /**
     * 多角度图片
     */
    private void initMultiImgs() {
        long startT=System.currentTimeMillis();

        imgsTimeList=new ArrayList<>();
        multiImgMap = new HashMap<>();
        hasMultiImg=false;
        if(ll_multi_angle_img!=null&&ll_multi_angle_img.getChildCount()!=0){
            ll_multi_angle_img.removeAllViews();
        }
        String imgStr = application.currentExhibitBean.getImgsurl();
        String[] imgs = imgStr.split(",");
        if (imgs != null && !imgs[0].equals("") && imgs.length != 0) {
            for (int i = 0; i < imgs.length; i++) {
                String singleUrl = imgs[i];
                String[] nameTime = singleUrl.split("\\*");
                multiImgMap.put(Integer.valueOf(nameTime[1]), nameTime[0]);
            }
            Iterator<Entry<Integer, String>> multiImgIterator = multiImgMap.entrySet().iterator();
            while (multiImgIterator.hasNext()) {
                Entry<Integer, String> e = multiImgIterator.next();
                String imgPath = e.getValue();
                int time=e.getKey();
                imgsTimeList.add(time);
                ImageView imageView = new ImageView(activity);
                imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                imageView.setTag(e);
                ll_multi_angle_img.addView(imageView, new LinearLayout.LayoutParams(mScreenWidth / 3, LinearLayout.LayoutParams.MATCH_PARENT));
                String imgLocalPath = application.getCurrentImgDir() + Tools.changePathToName(imgPath);
                if (Tools.isFileExist(imgLocalPath)) {
                    ImageLoaderUtil.displaySdcardImage(activity, imgLocalPath, imageView);
                } else {
                    String httpPath = BASEURL + imgPath;
                    ImageLoaderUtil.displayNetworkImage(activity, httpPath, imageView);
                }
                imageView.setOnClickListener(multiImgListener);
            }

            if (imgsTimeList.size() > 1) {

                Collections.sort(imgsTimeList, new Comparator<Integer>() {
                    @Override
                    public int compare(Integer lhs, Integer rhs) {
                        return lhs - rhs;
                    }
                });
            }
            hasMultiImg=true;
        } else {
            ImageView imageView = new ImageView(activity);
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
            ll_multi_angle_img.addView(imageView, new LinearLayout.LayoutParams(mScreenWidth / 3, LinearLayout.LayoutParams.MATCH_PARENT));
            String path = (String) iv_frag_largest_img.getTag();
            if (path.startsWith("http")) {
                ImageLoaderUtil.displayNetworkImage(activity, path, imageView);
            } else {
                ImageLoaderUtil.displaySdcardImage(activity, path, imageView);
            }
        }
        long costTime=System.currentTimeMillis()-startT;
        LogUtil.i("ZHANG", "GuideFragment_initMultiImgs耗时" + costTime);
    }

    /**播放控制监听器*/
    private View.OnClickListener musicCtrlListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.music_play_and_ctrl:
                    if (mediaServiceManager != null && mediaServiceManager.isPlaying()) {
                        music_play_and_ctrl.setBackgroundResource(R.mipmap.media_stop);
                        mediaServiceManager.pause();
                    } else if (mediaServiceManager != null && !mediaServiceManager.isPlaying()) {
                        music_play_and_ctrl.setBackgroundResource(R.mipmap.media_play);
                        mediaServiceManager.toContinue();
                    }
                    break;
                case R.id.iv_lyric_ctrl:
                    if (isLyricShowing) {
                        lyricShow.setVisibility(View.GONE);
                        iv_lyric_ctrl.setBackgroundResource(R.mipmap.lyric_ctrl_btn_open);
                        isLyricShowing = false;
                    } else {
                        lyricShow.setVisibility(View.VISIBLE);
                        iv_lyric_ctrl.setBackgroundResource(R.mipmap.lyric_ctrl_btn_close);
                        isLyricShowing = true;
                    }
                    break;
            }
        }
    };

    private void initIcon() {
        long startT=System.currentTimeMillis();

        String iconUrl = application.currentExhibitBean.getIconurl();
        String localUrl = getLocalImgUrl(iconUrl);
        if (Tools.isFileExist(localUrl)) {
            iv_frag_largest_img.setTag(localUrl);
            ImageLoaderUtil.displaySdcardImage(activity, localUrl, iv_frag_largest_img);
        } else {
            String httpUrl = BASEURL + iconUrl;
            iv_frag_largest_img.setTag(httpUrl);
            ImageLoaderUtil.displayNetworkImage(activity, httpUrl, iv_frag_largest_img);
        }
        long costTime=System.currentTimeMillis()-startT;
        LogUtil.i("ZHANG", "GuideActivity_initIcon耗时" + costTime);

    }

    private String getLocalImgUrl(String iconUrl) {
        String name = iconUrl.replaceAll("/", "_");
        String localUrl = application.getCurrentImgDir() + name;
        return localUrl;
    }

    @Override
    public void onResume() {
        long time =System.currentTimeMillis();
        super.onResume();
        hasMultiImg=true;
        handler.sendEmptyMessage(MSG_WHAT_CHANGE_EXHIBIT);
        ImgObserver imgObserver=new ImgObserver();
        imgObserver.start();
        long costTime=System.currentTimeMillis()-time;
        LogUtil.i("ZHANG", "GuideActivity_onResume耗时" + costTime);
    }

    @Override
    public void onPause() {
        super.onPause();
        LogUtil.i("ZHANG", "执行了GuideActivity_onPause");
    }

    @Override
    public void onStop() {
        super.onStop();
        LogUtil.i("ZHANG","执行了GuideActivity_onStop");
        hasMultiImg=false;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        activity.unregisterReceiver(playStateChangeReceiver);
    }
    private void initView() {
        long time =System.currentTimeMillis();
        iv_frag_largest_img = (ImageView) rootView.findViewById(R.id.iv_frag_largest_img);
        lyricShow = (ListView) rootView.findViewById(R.id.lyricShow);
        music_seekBar = (SeekBar) rootView.findViewById(R.id.music_seekBar);
        iv_lyric_ctrl = (ImageView) rootView.findViewById(R.id.iv_lyric_ctrl);
        music_play_and_ctrl = (ImageView) rootView.findViewById(R.id.music_play_and_ctrl);
        lyric_empty = (TextView) rootView.findViewById(R.id.lyric_empty);
        ll_nearly_exhibit = (LinearLayout) rootView.findViewById(R.id.ll_nearly_exhibit);
        ll_multi_angle_img = (LinearLayout) rootView.findViewById(R.id.ll_multi_angle_img);
        btn_switch_topic=(SwitchButton) rootView.findViewById(R.id.btn_switch_topic);
        lyricShow.setAdapter(mLyricAdapter);
        lyricShow.startAnimation(AnimationUtils.loadAnimation(activity, android.R.anim.fade_in));
        long costTime=System.currentTimeMillis()-time;
        LogUtil.i("ZHANG","GuideActivity_initView耗时"+costTime);
    }

    private void addListener() {
        long time =System.currentTimeMillis();
        btn_switch_topic.setOnCheckedChangeListener(compoundChangeListener);
        music_seekBar.setOnSeekBarChangeListener(seekBarChangeListener);
        music_play_and_ctrl.setOnClickListener(musicCtrlListener);
        iv_lyric_ctrl.setOnClickListener(musicCtrlListener);
        long costTime=System.currentTimeMillis()-time;
        LogUtil.i("ZHANG", "GuideActivity_addListener耗时" + costTime);
    }

    private SeekBar.OnSeekBarChangeListener seekBarChangeListener=new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
        }
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser) {
                if(mediaServiceManager.isPlaying()){
                    mediaServiceManager.seekTo(progress);
                }
            }
        }
    };


    private CompoundButton.OnCheckedChangeListener compoundChangeListener=new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if(isChecked){
                application.isTopicOpen=true;
                if(application.topicExhibitBeanList!=null){
                    application.nearlyExhibitBeanList.clear();
                    refreshNearly();
                }
            }else{
                application.nearlyExhibitBeanList=new ArrayList<>();
                application.isTopicOpen=false;
                initNearlyExhibit();
            }
        }
    };


    private LyricLoadHelper.LyricListener mLyricListener = new LyricLoadHelper.LyricListener() {

        @Override
        public void onLyricLoaded(List<LyricSentence> lyricSentences, int index) {
            if (lyricSentences != null) {
                LogUtil.i(TAG, "onLyricLoaded--->歌词句子数目=" + lyricSentences.size() + ",当前句子索引=" + index);
                mLyricAdapter.setLyric(lyricSentences);
                mLyricAdapter.setCurrentSentenceIndex(index);
                mLyricAdapter.notifyDataSetChanged();
            }
        }

        @Override
        public void onLyricSentenceChanged(int indexOfCurSentence) {
            mLyricAdapter.setCurrentSentenceIndex(indexOfCurSentence);
            mLyricAdapter.notifyDataSetChanged();
            lyricShow.smoothScrollToPositionFromTop(indexOfCurSentence, lyricShow.getHeight() / 2, 500);
        }
    };

    private void registerReceiver() {
        playStateChangeReceiver = new PlayStateChangeReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MediaPlayService.ACTION_UPDATE_PROGRESS);
        intentFilter.addAction(MediaPlayService.ACTION_UPDATE_DURATION);
        intentFilter.addAction(MediaPlayService.ACTION_UPDATE_CURRENT_EXHIBIT);
        intentFilter.addAction(ACTION_NOTIFY_CURRENT_EXHIBIT_CHANGE);
        activity.registerReceiver(playStateChangeReceiver, intentFilter);
    }

    private void loadLyricByHand() {
        long time =System.currentTimeMillis();
        try{
            String name = currentLyricUrl.replaceAll("/", "_");
            // 取得歌曲同目录下的歌词文件绝对路径
            String lyricFilePath = application.getCurrentLyricDir() + name;
            File lyricFile = new File(lyricFilePath);
            if (lyricFile.exists()) {
                // 本地有歌词，直接读取
                mLyricLoadHelper.loadLyric(lyricFilePath);
            } else {
                mIsLyricDownloading = true;
                // 尝试网络获取歌词
                LogUtil.i(TAG, "loadLyric()--->本地无歌词，尝试从网络获取");
                new LyricDownloadAsyncTask().execute(currentLyricUrl);
            }
            long costTime=System.currentTimeMillis()-time;
            LogUtil.i("ZHANG", "GuideActivity_loadLyricByHand耗时" + costTime);
        }catch (Exception e){
            ExceptionUtil.handleException(e);
        }
    }

    private class LyricDownloadAsyncTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            mLyricDownloadManager = new LyricDownloadManager(getActivity());
            // 从网络获取歌词，然后保存到本地
            String lyricFilePath = mLyricDownloadManager.searchLyricFromWeb(params[0],application.getCurrentLyricDir());
            // 返回本地歌词路径
            mIsLyricDownloading = false;
            return lyricFilePath;
        }

        @Override
        protected void onPostExecute(String lyricSavePath) {
            // Log.i(TAG, "网络获取歌词完毕，歌词保存路径:" + result);
            // 读取保存到本地的歌曲
            mLyricLoadHelper.loadLyric(lyricSavePath);
        }
    }

    private class ImgObserver extends Thread{

        @Override
        public void run() {
            while(hasMultiImg){
                if (imgsTimeList!=null&&imgsTimeList.size() > 0) {
                    for (int i = 0; i < imgsTimeList.size(); i++) {
                        try{
                            int imgTime = imgsTimeList.get(i);
                            int playTime = mediaServiceManager.getCurrentPosition();
                            int overTime= 0;
                            if((i+1)>=imgsTimeList.size()){
                                overTime=imgsTimeList.get(i);
                            }else{
                                overTime=imgsTimeList.get(i+1);
                            }
                            String imgPath=multiImgMap.get(imgsTimeList.get(i));
                            String currentIconPath= (String) iv_frag_largest_img.getTag();
                            if (playTime > imgTime && playTime < overTime&&!imgPath.equals(currentIconPath)) {
                                Message msg = Message.obtain();
                                msg.what = MSG_WHAT_CHANGE_ICON;
                                msg.obj = multiImgMap.get(imgsTimeList.get(i));
                                handler.sendMessage(msg);
                            }else{
                                imgPath=null;
                                currentIconPath=null;
                            }
                        }catch (Exception e){
                            ExceptionUtil.handleException(e);
                        }
                    }
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        ExceptionUtil.handleException(e);
                    }
                }
            }
        }
    }

    private  class PlayStateChangeReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (MediaPlayService.ACTION_UPDATE_PROGRESS.equals(action)) {
                int progress = intent.getIntExtra(MediaPlayService.ACTION_UPDATE_PROGRESS, 0);
                if (progress > 0) {
                    music_seekBar.setProgress(progress);
                    mLyricLoadHelper.notifyTime(progress);
                }
            } else if (MediaPlayService.ACTION_UPDATE_DURATION.equals(action)) {
                currentMax = intent.getIntExtra(MediaPlayService.ACTION_UPDATE_DURATION, 0);
                music_seekBar.setMax(currentMax);
            }else if(MediaPlayService.ACTION_UPDATE_CURRENT_EXHIBIT.equals(action)){
                refreshData();
                refreshView();
            }else if(ACTION_NOTIFY_CURRENT_EXHIBIT_CHANGE.equals(action)){
                handler.sendEmptyMessage(MSG_WHAT_CHANGE_EXHIBIT);
            }
        }
    }
}
