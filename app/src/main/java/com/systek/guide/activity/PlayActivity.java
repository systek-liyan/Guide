package com.systek.guide.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.systek.guide.R;
import com.systek.guide.adapter.MultiAngleImgAdapter;
import com.systek.guide.adapter.base.ViewPagerAdapter;
import com.systek.guide.biz.MyBeaconTask;
import com.systek.guide.callback.BeaconChangeCallback;
import com.systek.guide.entity.BeaconBean;
import com.systek.guide.entity.ExhibitBean;
import com.systek.guide.entity.MultiAngleImg;
import com.systek.guide.fragment.BaseFragment;
import com.systek.guide.fragment.IconImageFragment;
import com.systek.guide.fragment.LyricFragment;
import com.systek.guide.service.PlayManager;
import com.systek.guide.utils.ExceptionUtil;
import com.systek.guide.utils.ImageUtil;
import com.systek.guide.utils.LogUtil;
import com.systek.guide.utils.TimeUtil;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class PlayActivity extends BaseActivity implements LyricFragment.OnFragmentInteractionListener,
        IconImageFragment.OnFragmentInteractionListener,BeaconConsumer {

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
    private TextView tvTotalTime;
    private ViewPager viewpagerWordImage;
    private LyricFragment lyricFragment;
    private IconImageFragment iconImageFragment;

    private static final int MSG_WHAT_REFRESH_STATE=2;
    private static final int MSG_WHAT_REFRESH_VIEW =4;
    private static final int MSG_WHAT_UPDATE_PROGRESS=5;

    private BeaconManager beaconManager;
    private ThreadPoolExecutor executor;
    private long scanTime;
    private static final int TAKE_PHOTE_QUEST=15;




    @Override
    public void onBeaconServiceConnect() {

        beaconManager.setRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, org.altbeacon.beacon.Region region) {

                if(beacons==null||beacons.size()==0){return ; }
                executor.execute(new MyBeaconTask(beacons, new BeaconChangeCallback() {
                    @Override
                    public void getExhibits(List<ExhibitBean> exhibits) {
                    }

                    @Override
                    public void getNearestExhibit(final ExhibitBean exhibit) {

                        if(exhibit==null){return;}
                        if(PlayManager.getInstance().getPlayMode()==PLAY_MODE_AUTO&&PlayManager.getInstance().isPlaying()){
                            if(currentExhibit!=null&&exhibit.equals(currentExhibit)){
                               /* SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
                                LogUtil.i("ZHANG","equals= "+df.format(new Date()));*/
                                return;}
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    PlayManager.getInstance().playFromBean(exhibit);
                                }
                            });
                        }
                    }


                    @Override
                    public void getNearestBeacon(BeaconBean bean) {

                    }

                }));
            }
        });
        try {
            beaconManager.startRangingBeaconsInRegion(new Region(BEACON_LAYOUT, null, null, null));
        } catch (RemoteException e) {
            ExceptionUtil.handleException(e);
        }
    }

    static class MyHandler extends Handler {

        WeakReference<PlayActivity> activityWeakReference;
        MyHandler(PlayActivity activity){
            this.activityWeakReference=new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {

            if(activityWeakReference==null){return;}
            PlayActivity activity=activityWeakReference.get();
            if(activity==null){return;}
            switch (msg.what){
                case MSG_WHAT_UPDATE_PROGRESS:
                    activity.refreshProgress();
                    break;
                case MSG_WHAT_REFRESH_STATE:
                    activity.refreshState();
                    break;
                case MSG_WHAT_REFRESH_VIEW:
                    activity.refreshView();
                    break;
                default:break;
            }
        }
    }

    @Override
    public void onStateChanged(final int state) {
        this.state=state;
        handler.sendEmptyMessage(MSG_WHAT_REFRESH_STATE);// TODO: 2016/5/26

    }

    @Override
    public void onExhibitChanged(ExhibitBean exhibit) {
        if(currentExhibit==null||!currentExhibit.equals(exhibit)){
            currentExhibit=exhibit;
            handler.sendEmptyMessage(MSG_WHAT_REFRESH_VIEW);
        }

    }

    @Override
    public void onPositionChanged(int duration, int position) {
        super.onPositionChanged(duration, position);
        this.currentDuration=duration;
        this.currentProgress=position;
        handler.sendEmptyMessage(MSG_WHAT_UPDATE_PROGRESS);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);
        beaconManager = BeaconManager.getInstanceForApplication(this);
        beaconManager.bind(this);
        executor= (ThreadPoolExecutor) Executors.newFixedThreadPool(3);
        handler=new MyHandler(this);
        initFragment();
        initView();
        addListener();
        initData();

    }

    private void initFragment() {
        if(iconImageFragment==null){
            iconImageFragment=new IconImageFragment();
        }
        if(lyricFragment==null){
            lyricFragment=new LyricFragment();
        }
        ArrayList<BaseFragment> fragments=new ArrayList<>();
        fragments.add(lyricFragment);
        fragments.add(iconImageFragment);
        ViewPagerAdapter viewPagerAdapter=new ViewPagerAdapter(getSupportFragmentManager(),fragments);
        viewpagerWordImage=(ViewPager)findViewById(R.id.viewpagerWordImage);
        if(viewpagerWordImage==null){return;}
        viewpagerWordImage.setAdapter(viewPagerAdapter);
    }

    private void initData() {
        Intent intent=getIntent();
        ExhibitBean exhibitBean= (ExhibitBean) intent.getSerializableExtra(INTENT_EXHIBIT);
        currentExhibit=PlayManager.getInstance().getCurrentExhibit();
        if(exhibitBean!=null){
            if(currentExhibit==null){
                currentExhibit=exhibitBean;
            }else{
                if(!currentExhibit.equals(exhibitBean)){
                    PlayManager.getInstance().playFromBean(exhibitBean);
                }
            }
        }
        handler.sendEmptyMessage(MSG_WHAT_REFRESH_VIEW);
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        initData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        PlayManager.getInstance().bindToService(this, this);
        if (beaconManager.isBound(this)) beaconManager.setBackgroundMode(false);
        registerReceiver();
        refreshState();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unRegisterReceiver();
        if (beaconManager.isBound(this)) beaconManager.setBackgroundMode(true);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater=getMenuInflater();
        menuInflater.inflate(R.menu.play_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.share:
                Intent intentShare=new Intent(Intent.ACTION_SEND);
                intentShare.setType("text/plain");
                intentShare.putExtra(Intent.EXTRA_SUBJECT, "分享");
                intentShare.putExtra(Intent.EXTRA_TEXT, "我是阿强，我正在分享我的数据...");
                startActivity(Intent.createChooser(intentShare, "分享到"));
                break;
            case R.id.camera:
                Intent intentCamera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intentCamera, TAKE_PHOTE_QUEST);
                break;
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {

            if(requestCode==TAKE_PHOTE_QUEST) {
                LogUtil.i("ZHANG", "请求码是" + TAKE_PHOTE_QUEST);

                String sdStatus = Environment.getExternalStorageState();
                if (!sdStatus.equals(Environment.MEDIA_MOUNTED)) { // 检测sd是否可用
                    Log.i("TestFile",
                            "SD card is not avaiable/writeable right now.");
                    return;
                }
                String name = new DateFormat().format("yyyyMMdd_hhmmss", Calendar.getInstance(Locale.CHINA)) + ".jpg";
                Bundle bundle = data.getExtras();
                Bitmap bitmap = (Bitmap) bundle.get("data");// 获取相机返回的数据，并转换为Bitmap图片格式
                FileOutputStream b = null;
                File file = new File("/sdcard/myImage/");
                if (!file.exists()) {
                    file.mkdirs();// 创建文件夹
                }
                String fileName = "/sdcard/myImage/" + name;
                try {
                    b = new FileOutputStream(fileName);
                    if (bitmap != null) {
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, b);// 把数据写入文件
                    }
                    Toast.makeText(this, "图片已保存在" + fileName, Toast.LENGTH_LONG).show();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (b != null) {
                            b.flush();
                        }
                        if (b != null) {
                            b.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (bitmap != null) {
                        bitmap.recycle();
                    }
                }
            }
        }


    }

    /**
     * 添加监听器
     */
    private void addListener() {
        ivPlayCtrl.setOnClickListener(onClickListener);
        seekBarProgress.setOnSeekBarChangeListener(onSeekBarChangeListener);

        mulTiAngleImgAdapter.setOnItemClickListener(new MultiAngleImgAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                MultiAngleImg multiAngleImg = multiAngleImgs.get(position);
                currentIconUrl = multiAngleImg.getUrl();
                PlayManager.getInstance().seekTo(multiAngleImg.getTime());
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
                    if(PlayManager.getInstance().isPlaying()){
                        PlayManager.getInstance().pause();
                    }else{
                        PlayManager.getInstance().play();
                    }
                    break;
            }
        }
    };

    /**
     * 注册广播接收器
     */
    private void registerReceiver() {
        registerBluetoothReceiver();
    }

    private void unRegisterReceiver() {
        unRegisterBluetoothReceiver();
    }

    /**
     * 刷新界面
     */
    private void refreshView() {
        if(currentExhibit==null){return;}
        currentMuseumId=currentExhibit.getMuseumId();
        setTitleBarTitle(currentExhibit.getName());
        initMultiImgs();
        currentIconUrl=currentExhibit.getIconurl();
        lyricFragment.setExhibit(currentExhibit);
        lyricFragment.loadLyricByHand();
        initIcon();
        refreshState();
    }

    private void refreshState() {
        if(PlayManager.getInstance().isPlaying()){//state==PLAY_STATE_START
            ivPlayCtrl.setImageDrawable(getResources().getDrawable(R.drawable.uamp_ic_pause_white_48dp));//iv_play_state_open_big,ic_pause_black_36dp
        }else{
            ivPlayCtrl.setImageDrawable(getResources().getDrawable(R.drawable.uamp_ic_play_arrow_white_48dp));
        }
    }

    private void refreshProgress() {
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
    private void initView() {

        setMyTitleBar();
        setHomeIcon();
        setHomeClickListener(backOnClickListener);
        //mediaServiceManager=MediaServiceManager.getInstance(this);
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
            PlayManager.getInstance().seekTo(progress);
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
        beaconManager.unbind(this);
        PlayManager.getInstance().unbindService(this,this);
        super.onDestroy();
    }

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
