package com.systek.guide.fragment;

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

import com.alibaba.fastjson.JSON;
import com.systek.guide.IConstants;
import com.systek.guide.R;
import com.systek.guide.activity.PlayActivity;
import com.systek.guide.adapter.ExhibitAdapter;
import com.systek.guide.entity.ExhibitBean;
import com.systek.guide.manager.MediaServiceManager;

import java.util.ArrayList;
import java.util.List;

public class ExhibitListFragment extends Fragment implements IConstants {

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
        handler=new MyHandler();
        registerReceiver();
    }

    @Override
    public void onResume() {
        super.onResume();
        currentExhibit=mediaServiceManager.getCurrentExhibit();
    }

    private void registerReceiver() {
        listChangeReceiver = new ListChangeReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(INTENT_EXHIBIT_LIST);
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
        currentExhibitList=new ArrayList<>();
        exhibitAdapter =new ExhibitAdapter(activity,currentExhibitList);
        listView.setAdapter(exhibitAdapter);
    }

    private void initView(View view) {
        listView=(ListView)view.findViewById(R.id.lv_exhibit_list);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                ExhibitBean exhibitBean= exhibitAdapter.getItem(position);
                currentExhibit=mediaServiceManager.getCurrentExhibit();
                Intent intent1 =new Intent(activity,PlayActivity.class);
                if(currentExhibit==null||!currentExhibit.equals(exhibitBean)){
                    mListener.onFragmentInteraction(exhibitBean);

                    String str= JSON.toJSONString(exhibitBean);
                    Intent intent =new Intent();
                    intent.setAction(INTENT_EXHIBIT);
                    intent.putExtra(INTENT_EXHIBIT, str);
                    activity.sendBroadcast(intent);
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
    }
    class MyHandler extends Handler{
        @Override
        public void handleMessage(Message msg) {
            if(msg.what==MSG_WHAT_UPDATE_DATA_SUCCESS){
                if(exhibitAdapter ==null||currentExhibitList==null){return;}
                exhibitAdapter.updateData(currentExhibitList);
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