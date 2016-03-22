package com.systek.guide.activity;

import android.content.Intent;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ScrollView;

import com.alibaba.fastjson.JSON;
import com.systek.guide.R;
import com.systek.guide.adapter.ExhibitAdapter;
import com.systek.guide.biz.DataBiz;
import com.systek.guide.custom.ClearEditText;
import com.systek.guide.entity.ExhibitBean;

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

    @Override
    protected void setView() {
        setContentView(R.layout.activity_search);
        initDrawer();
    }

    @Override
    protected void addListener() {

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
                ExhibitBean exhibitBean = exhibitBeanList.get(position);
                String str = JSON.toJSONString(exhibitBean);
                Intent intent = new Intent();
                intent.setAction(INTENT_EXHIBIT);
                intent.putExtra(INTENT_EXHIBIT, str);
                sendBroadcast(intent);
                Intent intent1 = new Intent(SearchActivity.this, PlayActivity.class);
                intent1.putExtra(INTENT_EXHIBIT, str);
                startActivity(intent1);
                finish();
            }
        });

    }

    @Override
    protected void initView() {

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


    @Override
    protected void initData() {
        Intent intent=getIntent();
        currentMuseumId=intent.getStringExtra(MUSEUM_ID);
    }

    @Override
    protected void refreshView() {
        if(exhibitAdapter!=null){
            exhibitAdapter.updateData(exhibitBeanList);
        }
    }

    @Override
    protected void registerReceiver() {

    }

    @Override
    protected void unRegisterReceiver() {

    }

    @Override
    protected void refreshExhibit() {

    }

    @Override
    protected void refreshTitle() {

    }

    @Override
    protected void refreshViewBottomTab() {

    }

    @Override
    protected void refreshProgress() {

    }

    @Override
    protected void refreshIcon() {

    }

    @Override
    protected void refreshState() {

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


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}
