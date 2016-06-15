package com.systek.guide.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ScrollView;

import com.systek.guide.R;
import com.systek.guide.adapter.ExhibitAdapter;
import com.systek.guide.biz.DataBiz;
import com.systek.guide.custom.ClearEditText;
import com.systek.guide.entity.ExhibitBean;
import com.systek.guide.manager.MediaServiceManager;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;


/**
 * 搜索 activity
 */
public class SearchActivity extends BaseActivity {

    private ListView listViewExhibit;
    private ClearEditText mClearEditText;
    private List<ExhibitBean> exhibitBeanList;
    private ExhibitAdapter exhibitAdapter;
    private String currentMuseumId;

    private static final int MSG_WHAT_UPDATE_DATA_SUCCESS=1;

    static class MyHandler extends Handler {

        WeakReference<SearchActivity> activityWeakReference;
        MyHandler(SearchActivity activity){
            this.activityWeakReference=new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {

            if(activityWeakReference==null){return;}
            SearchActivity activity=activityWeakReference.get();
            if(activity==null){return;}
            switch (msg.what){
                case MSG_WHAT_UPDATE_DATA_SUCCESS:
                    activity.refreshView();
                    break;
                default:break;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        handler=new MyHandler(this);
        initDrawer();
        initView();
        addListener();
        initData();

    }


    private void addListener() {

        mClearEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //当输入框里面的值为空，更新为原来的列表，否则为过滤数据列表
                filterData(s.toString());
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        listViewExhibit.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                ExhibitBean clickExhibit = exhibitAdapter.getItem(position);
                //ExhibitBean bean = mediaServiceManager.getCurrentExhibit();
                ExhibitBean currentExhibit = MediaServiceManager.getInstance(getActivity()).getCurrentExhibit();
                //exhibitAdapter.setSelectItem(position);
                exhibitAdapter.setSelectExhibit(clickExhibit);
                if(currentExhibit==null||!currentExhibit.equals(clickExhibit)){
                    exhibitAdapter.setState(position,ExhibitAdapter.STATE_PLAYING);
                }
                MediaServiceManager.getInstance(getActivity()).setPlayMode(PLAY_MODE_HAND);
                exhibitAdapter.notifyDataSetInvalidated();
                MediaServiceManager.getInstance(getActivity()).notifyExhibitChange(clickExhibit);
                startActivity(new Intent(SearchActivity.this,PlayActivity.class));
                finish();
            }
        });

    }

    private void initView() {
        setTitleBar();
        setTitleBarTitle("搜索");
        setHomeIcon();
        setHomeClickListener(backOnClickListener);
        exhibitBeanList=new ArrayList<>();
        mClearEditText = (ClearEditText) findViewById(R.id.filter_edit);
        listViewExhibit=(ListView)findViewById(R.id.listViewExhibit);
        exhibitAdapter=new ExhibitAdapter(this,exhibitBeanList);
        listViewExhibit.setAdapter(exhibitAdapter);
        listViewExhibit.setOverScrollMode(ScrollView.OVER_SCROLL_NEVER);
    }


    private void initData() {
        Intent intent=getIntent();
        currentMuseumId=intent.getStringExtra(MUSEUM_ID);
    }

    private void refreshView() {
        if(exhibitAdapter!=null){
            exhibitAdapter.updateData(exhibitBeanList);
        }
    }



    private  void filterData(final String s) {
        new Thread(){
            @Override
            public void run() {
                if(TextUtils.isEmpty(s)){
                    exhibitBeanList=new ArrayList<>();
                    handler.sendEmptyMessage(MSG_WHAT_UPDATE_DATA_SUCCESS);
                    return;
                }
                if(TextUtils.isEmpty(currentMuseumId)){return;}
                exhibitBeanList= DataBiz.searchFromSQLite(currentMuseumId,s);
                if(exhibitBeanList==null){
                    exhibitBeanList=new ArrayList<>();
                }
                handler.sendEmptyMessage(MSG_WHAT_UPDATE_DATA_SUCCESS);
            }
        }.start();

    }

}
