package com.systekcn.guide.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.systekcn.guide.MyApplication;
import com.systekcn.guide.R;
import com.systekcn.guide.activity.PlayActivity;
import com.systekcn.guide.adapter.NearlyExhibitAdapter;
import com.systekcn.guide.common.IConstants;
import com.systekcn.guide.common.utils.LogUtil;
import com.systekcn.guide.entity.ExhibitBean;

import java.util.ArrayList;

public class ExhibitListFragment extends Fragment implements IConstants{

    private Activity activity;
    private ListView listView;
    private MyApplication application;
    private NearlyExhibitAdapter nearlyExhibitAdapter;
    private final int MSG_WHAT_UPDATE_DATA=1;
    private MyHandler handler;
    private static ExhibitListFragment exhibitListFragment;
    private ListChangeReceiver listChangeReceiver;

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
        handler=new MyHandler();
        registerReceiver();
    }

    private void registerReceiver() {
        listChangeReceiver = new ListChangeReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_NOTIFY_NEARLY_EXHIBIT_LIST_CHANGE);
        activity.registerReceiver(listChangeReceiver, intentFilter);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_exhibit_list, container, false);
        initView(view);
        initData();
        return view;
    }

    private void initData() {

        if(application.currentExhibitBeanList!=null){
            nearlyExhibitAdapter=new NearlyExhibitAdapter(activity,application.currentExhibitBeanList);
        }else{
            nearlyExhibitAdapter=new NearlyExhibitAdapter(activity,new ArrayList<ExhibitBean>());
        }
        listView.setAdapter(nearlyExhibitAdapter);
    }

    private void initView(View view) {
        listView=(ListView)view.findViewById(R.id.lv_exhibit_list);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(application.currentExhibitBeanList!=null&&application.currentExhibitBeanList.size()>0){
                    ExhibitBean exhibitBean=application.currentExhibitBeanList.get(position);
                    application.currentExhibitBean=exhibitBean;
                    application.mServiceManager.notifyAllDataChange();
                    Intent intent=new Intent(activity, PlayActivity.class);
                    activity.startActivity(intent);
                }
            }
        });
    }


    @Override
    public void onAttach(Activity activity) {
        application=MyApplication.get();
        this.activity=getActivity();
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }


    private  class ListChangeReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            LogUtil.i("ZHANG", "ListChangeReceiver");
            handler.sendEmptyMessage(MSG_WHAT_UPDATE_DATA);
        }
    }


    @Override
    public void onDestroy() {
        activity.unregisterReceiver(listChangeReceiver);
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
    class MyHandler extends Handler{
        @Override
        public void handleMessage(Message msg) {
            if(msg.what==MSG_WHAT_UPDATE_DATA){
                if(nearlyExhibitAdapter!=null){
                    nearlyExhibitAdapter.updateData(application.currentExhibitBeanList);
                }
            }
        }
    }


}
