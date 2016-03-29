package com.systek.guide.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.IdRes;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.systek.guide.IConstants;
import com.systek.guide.R;
import com.systek.guide.adapter.NearlyGalleryAdapter;
import com.systek.guide.entity.ExhibitBean;
import com.systek.guide.manager.MediaServiceManager;
import com.systek.guide.utils.ImageUtil;
import com.systek.guide.utils.TimeUtil;

import java.util.ArrayList;
import java.util.List;

public class LockScreenFragment extends BaseFragment implements IConstants {


    private static final String ARG_EXHIBIT = "arg_exhibit";

    private String mExhibitStr;
    private OnFragmentInteractionListener mListener;
    private ImageView fullscreenImage;
    private ImageView ivPlayCtrl;
    private MediaServiceManager mediaServiceManager;
    private Handler handler;
    private int currentDuration;
    private int currentProgress;
    //private SeekBar seekBarProgress;
    private TextView tvPlayTime;
    private TextView tvTotalTime;
    private TextView tvLockTime;
    private String currentIconUrl;
    private RecyclerView recycleNearly;
    private List<ExhibitBean> nearlyExhibitList;
    private NearlyGalleryAdapter nearlyGalleryAdapter;
    private ExhibitBean currentExhibit;
    private List<ExhibitBean> currentExhibitList;

    private View view;

    public void setCurrentExhibit(ExhibitBean currentExhibit) {
        this.currentExhibit = currentExhibit;
    }


    public static LockScreenFragment newInstance(String param1) {
        LockScreenFragment fragment = new LockScreenFragment();
        if(param1!=null){
            Bundle args = new Bundle();
            args.putString(ARG_EXHIBIT, param1);
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mExhibitStr = getArguments().getString(ARG_EXHIBIT);
        }
    }

    @Override
    protected void initView() {
        setContentView(R.layout.fragment_lock_screen);
        findView();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);



    }


    @Override
    public void onStart() {
        super.onStart();
        if(currentExhibit==null){return;}
        initIcon();
    }
    @Override
    public void onResume() {
        super.onResume();
    }

    private void initIcon() {
        if(currentExhibit==null){return;}
        currentIconUrl=currentExhibit.getIconurl();
        ImageUtil.displayImage(currentIconUrl, fullscreenImage);
        /*String imageName = Tools.changePathToName(currentIconUrl);
        String imgLocalUrl = LOCAL_ASSETS_PATH+currentExhibit.getMuseumId()+"/"+imageName;
        File file = new File(imgLocalUrl);
        // 判断sdcard上有没有图片
        if (file.exists()) {
            // 显示sdcard
            ImageLoaderUtil.displaySdcardImage(getActivity(), imgLocalUrl, fullscreenImage);
        } else {
            ImageLoaderUtil.displayNetworkImage(getActivity(), BASE_URL + currentIconUrl, fullscreenImage);
        }*/
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(ExhibitBean exhibit);

    }


    public void onButtonPressed(ExhibitBean exhibit) {
        if (mListener != null) {
            mListener.onFragmentInteraction(exhibit);
        }
    }

    public View findViewById(@IdRes int id){
        if(contentView==null){return null;}
        return contentView.findViewById(id);
    }

    public void findView(){
        fullscreenImage=(ImageView)findViewById(R.id.fullscreenImage);
        ivPlayCtrl=(ImageView)findViewById(R.id.ivPlayCtrl);
        tvPlayTime=(TextView)findViewById(R.id.tvPlayTime);
        tvTotalTime=(TextView)findViewById(R.id.tvTotalTime);
        tvLockTime=(TextView)findViewById(R.id.tvLockTime);
        tvLockTime.setText(TimeUtil.getTime());

        //seekBarProgress=(SeekBar)findViewById(R.id.seekBarProgress);

        recycleNearly = (RecyclerView)findViewById(R.id.recycleNearly);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        recycleNearly.setLayoutManager(linearLayoutManager);

        nearlyExhibitList=new ArrayList<>();
        nearlyGalleryAdapter=new NearlyGalleryAdapter(getActivity(),nearlyExhibitList);
        recycleNearly.setAdapter(nearlyGalleryAdapter);
        recycleNearly.setOverScrollMode(ScrollView.OVER_SCROLL_NEVER);

    }


    public void addListener(){
        nearlyGalleryAdapter.setOnItemClickListener(new NearlyGalleryAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {

                nearlyGalleryAdapter.notifyItemChanged(position);
                ExhibitBean exhibitBean = currentExhibitList.get(position);
                ExhibitBean bean = mediaServiceManager.getCurrentExhibit();
                nearlyGalleryAdapter.setSelectIndex(exhibitBean);
                if (bean == null || !bean.equals(exhibitBean)) {
                    String str = JSON.toJSONString(exhibitBean);
                    Intent intent = new Intent();
                    intent.setAction(INTENT_EXHIBIT);
                    intent.putExtra(INTENT_EXHIBIT, str);
                    getActivity().sendBroadcast(intent);
                }
            }
        });

        ivPlayCtrl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*unregisterReceiver(listChangeReceiver);
                handler.removeCallbacksAndMessages(null);
                finish();// TODO: 2016/1/14*/
                Intent intent = new Intent();
                intent.setAction(INTENT_CHANGE_PLAY_STATE);
                getActivity().sendBroadcast(intent);

            }
        });


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
*/



}
 /*class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_WHAT_UPDATE_PROGRESS:
                    //seekBarProgress.setMax(currentDuration);
                    //seekBarProgress.setProgress(currentProgress);
                    //tvPlayTime.setText(TimeUtil.changeToTime(currentProgress).substring(3));
                    //tvTotalTime.setText(TimeUtil.changeToTime(currentDuration).substring(3));
                    break;
                case MSG_WHAT_CHANGE_EXHIBIT:
                    //refreshView();
                    break;
                case MSG_WHAT_CHANGE_PLAY_START:
                    ivPlayCtrl.setImageDrawable(getResources().getDrawable(R.drawable.uamp_ic_pause_white_48dp));
                    break;
                case MSG_WHAT_CHANGE_PLAY_STOP:
                    ivPlayCtrl.setImageDrawable(getResources().getDrawable(R.drawable.uamp_ic_play_arrow_white_48dp));
                    break;
                case MSG_WHAT_UPDATE_DATA_SUCCESS:
                    if(nearlyGalleryAdapter ==null||currentExhibitList==null){return;}
                    nearlyGalleryAdapter.updateData(currentExhibitList);
                    break;
            }
        }
    }*/

    /* private void registerReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(INTENT_EXHIBIT);
        filter.addAction(INTENT_EXHIBIT_PROGRESS);
        filter.addAction(INTENT_EXHIBIT_DURATION);
        filter.addAction(INTENT_CHANGE_PLAY_PLAY);
        filter.addAction(INTENT_CHANGE_PLAY_STOP);
        filter.addAction(INTENT_EXHIBIT_LIST);
        getActivity().registerReceiver(listChangeReceiver, filter);
    }*/
