package com.systekcn.guide.activity;

import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.systekcn.guide.R;
import com.systekcn.guide.activity.base.BaseActivity;
import com.systekcn.guide.adapter.MultiAngleImgAdapter;
import com.systekcn.guide.common.utils.LogUtil;
import com.systekcn.guide.common.utils.ViewUtils;
import com.systekcn.guide.entity.MultiAngleImg;

import java.util.ArrayList;

public class PlayActivity extends BaseActivity {


    private ImageView ivPlayBack;
    private ListView lvLyric;
    private ImageView imgExhibitIcon;
    private SeekBar seekBarProgress;
    private TextView tvPlayTime;
    private RecyclerView recycleMultiAngle;
    private ImageView ivPlayCtrl;
    private ImageView imgWordCtrl;
    private final int MSG_WHAT_CHANGE_ICON=2;
    private final int MSG_WHAT_CHANGE_EXHIBIT=3;
    private final int MSG_WHAT_PAUSE_MUSIC=4;
    private final int MSG_WHAT_CONTINUE_MUSIC=5;
    private MyHandler handler;
    private ArrayList<MultiAngleImg> multiAngleImgs;
    private ArrayList<Integer> imgsTimeList;
    private boolean hasMultiImg;
    private MultiAngleImgAdapter mulTiAngleImgAdapter;

    @Override
    protected void initialize() {
        ViewUtils.setStateBarToAlpha(this);
        setContentView(R.layout.activity_play);
        handler =new MyHandler();
        initView();
        initData();
        initMultiImgs();
    }


    private void initView() {
        ivPlayBack=(ImageView)findViewById(R.id.ivPlayBack);
        lvLyric=(ListView)findViewById(R.id.lvLyric);
        imgExhibitIcon=(ImageView)findViewById(R.id.imgExhibitIcon);
        seekBarProgress=(SeekBar)findViewById(R.id.seekBarProgress);
        tvPlayTime=(TextView)findViewById(R.id.tvPlayTime);
        recycleMultiAngle=(RecyclerView)findViewById(R.id.recycleMultiAngle);
        ivPlayCtrl=(ImageView)findViewById(R.id.ivPlayCtrl);
        imgWordCtrl=(ImageView)findViewById(R.id.imgWordCtrl);
        multiAngleImgs=new ArrayList<>();
        mulTiAngleImgAdapter=new MultiAngleImgAdapter(this,multiAngleImgs);
        /*设置为横向*/
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        recycleMultiAngle.setLayoutManager(linearLayoutManager);
        recycleMultiAngle.setAdapter(mulTiAngleImgAdapter);

    }

    private void initData() {
        if(application.currentExhibitBean==null){return;}
        handler.sendEmptyMessage(MSG_WHAT_CHANGE_EXHIBIT);
    }

    /**
     * 多角度图片
     */
    private void initMultiImgs() {
        long startT=System.currentTimeMillis();
        /*当前展品为空，返回*/
        if(application.currentExhibitBean==null){return;}
        String imgStr=application.currentExhibitBean.getImgsurl();
        /*没有多角度图片，返回*/
        if(imgStr==null||imgStr.equals("")){return;}
        imgsTimeList=new ArrayList<>();
        /*获取多角度图片地址数组*/
        String[] imgs = imgStr.split(",");
        if (imgs[0].equals("") && imgs.length != 0) {return;}

        for (String singleUrl : imgs) {
            String[] nameTime = singleUrl.split("\\*");
            MultiAngleImg multiAngleImg=new MultiAngleImg();
            int time=Integer.valueOf(nameTime[1]);
            multiAngleImg.setTime(time);
            multiAngleImg.setUrl(nameTime[0]);
            imgsTimeList.add(time);
            multiAngleImgs.add(multiAngleImg);
        }
        mulTiAngleImgAdapter.notifyDataSetChanged();


           /* Iterator<Map.Entry<Integer, String>> multiImgIterator = multiImgMap.entrySet().iterator();
            while (multiImgIterator.hasNext()) {
                Map.Entry<Integer, String> e = multiImgIterator.next();
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
            }*/
        long costTime=System.currentTimeMillis()-startT;
        LogUtil.i("ZHANG", "GuideFragment_initMultiImgs耗时" + costTime);
    }


    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    private class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            /**当信息类型为更换歌词背景*/
            if (msg.what == MSG_WHAT_CHANGE_ICON) {
                /*String imgPath = (String) msg.obj;
                String currentIconPath=(String)iv_frag_largest_img.getTag();
                String imgLocalPath = application.getCurrentImgDir() + Tools.changePathToName(imgPath);
                *//**若歌词路径不为空，判断图片url,加载图片*//*
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
                }*/
                /**若信息类型为展品切换，刷新数据，刷新界面*/
            } else if (msg.what == MSG_WHAT_CHANGE_EXHIBIT) {
                /**数据初始化好之前显示加载对话框*/
               /* showProgressDialog();
                refreshData();
                refreshView();
                mediaServiceManager.notifyAllDataChange();
                if(progressDialog!=null&&progressDialog.isShowing()){
                    progressDialog.dismiss();
                }*/
            }else if(msg.what==MSG_WHAT_PAUSE_MUSIC){
                /**暂停播放*//*
                if (mediaServiceManager != null && mediaServiceManager.isPlaying()) {
                    music_play_and_ctrl.setBackgroundResource(R.mipmap.media_stop);
                    mediaServiceManager.pause();
                }*/
            }else if(msg.what==MSG_WHAT_CONTINUE_MUSIC){
               /* *//**继续播放*//*
                music_play_and_ctrl.setBackgroundResource(R.mipmap.media_play);
                mediaServiceManager.toContinue();*/
            }
        }
    }

}
