package com.systek.guide.fragment;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ScrollView;

import com.alibaba.fastjson.JSON;
import com.systek.guide.IConstants;
import com.systek.guide.R;
import com.systek.guide.activity.PlayActivity;
import com.systek.guide.adapter.ExhibitAdapter;
import com.systek.guide.entity.ExhibitBean;
import com.systek.guide.manager.MediaServiceManager;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * 展品列表 Fragment
 */
public class ExhibitListFragment extends BaseFragment implements IConstants {

    private Context activity;
    private ListView listView;
    private ExhibitAdapter exhibitAdapter;
    private Handler handler;
    private static ExhibitListFragment exhibitListFragment;
    private ListChangeReceiver listChangeReceiver;
    private List<ExhibitBean> currentExhibitList;
    private OnFragmentInteractionListener mListener;
    private MediaServiceManager mediaServiceManager;
    private ExhibitBean currentExhibit;


    private static final int MSG_WHAT_UPDATE_DATA_SUCCESS=1;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ExhibitListFragment() {
    }

    public static ExhibitListFragment newInstance() {
        if(exhibitListFragment==null){
            exhibitListFragment = new ExhibitListFragment();
        }
        return exhibitListFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        exhibitListFragment=this;
        mediaServiceManager=MediaServiceManager.getInstance(activity);
        handler=new MyHandler(this);
        registerReceiver();
    }

    @Override
    public void onResume() {
        super.onResume();
        if(mediaServiceManager!=null){
            currentExhibit=mediaServiceManager.getCurrentExhibit();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private void registerReceiver() {
        listChangeReceiver = new ListChangeReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(INTENT_EXHIBIT_LIST);
        activity.registerReceiver(listChangeReceiver, intentFilter);
    }

    @Override
    void initView() {
        setContentView(R.layout.fragment_exhibit_list);
        initView(contentView);
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initData();
    }

    private void initData() {
        currentExhibitList=new ArrayList<>();
        exhibitAdapter =new ExhibitAdapter(activity,currentExhibitList);
        listView.setAdapter(exhibitAdapter);
        listView.setOverScrollMode(ScrollView.OVER_SCROLL_NEVER);
    }



    private void initView(View view) {
        listView=(ListView)view.findViewById(R.id.lv_exhibit_list);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {


                ExhibitBean exhibitBean = exhibitAdapter.getItem(position);
                currentExhibit=mediaServiceManager.getCurrentExhibit();
                if(currentExhibit!=null&&!currentExhibit.equals(exhibitBean)){
                    mediaServiceManager.setPlayMode(PLAY_MODE_HAND);
                }
                exhibitAdapter.setSelectItem(position);
                exhibitAdapter.notifyDataSetInvalidated();
                Context context=getActivity();
                Intent intent1 = new Intent(context, PlayActivity.class);
                if (currentExhibit == null || !currentExhibit.equals(exhibitBean)) {
                    mListener.onFragmentInteraction(exhibitBean);
                    if(mediaServiceManager.getPlayMode()==PLAY_MODE_AUTO){
                        mediaServiceManager.setPlayMode(PLAY_MODE_AUTO_PAUSE);
                    }

                    String str = JSON.toJSONString(exhibitBean);
                    Intent intent = new Intent();
                    intent.setAction(INTENT_EXHIBIT);
                    intent.putExtra(INTENT_EXHIBIT, str);
                    context.sendBroadcast(intent);
                    intent1.putExtra(INTENT_EXHIBIT, str);
                }
                startActivity(intent1);

            }
        });
    }

    @Override
    public void onAttach(Activity a) {
        super.onAttach(a);
        this.activity=a;
        if (activity instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) activity;
        } else {
            throw new RuntimeException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(ExhibitBean bean);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onDestroy() {
        activity.unregisterReceiver(listChangeReceiver);
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
        exhibitListFragment=null;
    }

    public void updateView(){
        if(exhibitAdapter ==null||currentExhibitList==null){return;}
        exhibitAdapter.updateData(currentExhibitList);
    }


    static class MyHandler extends Handler{
        WeakReference<ExhibitListFragment> weakReference;
        MyHandler(ExhibitListFragment exhibitListFragment){
            this.weakReference=new WeakReference<>(exhibitListFragment);
        }
        @Override
        public void handleMessage(Message msg) {
            if(weakReference==null){return;}
            ExhibitListFragment exhibitListFragment=weakReference.get();
            if(exhibitListFragment==null){return;}
            if(msg.what==MSG_WHAT_UPDATE_DATA_SUCCESS){
                exhibitListFragment.updateView();
            }
        }
    }

    private  class ListChangeReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action=intent.getAction();
            if(action.equals(INTENT_EXHIBIT_LIST)){
                String exhibitJson=intent.getStringExtra(INTENT_EXHIBIT_LIST);
                currentExhibitList= JSON.parseArray(exhibitJson,ExhibitBean.class);
                if(currentExhibitList==null){return;}
                handler.sendEmptyMessage(MSG_WHAT_UPDATE_DATA_SUCCESS);
            }
        }
    }


}
