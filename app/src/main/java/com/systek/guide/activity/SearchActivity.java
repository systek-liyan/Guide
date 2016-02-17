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

import com.alibaba.fastjson.JSON;
import com.lidroid.xutils.DbUtils;
import com.lidroid.xutils.db.sqlite.Selector;
import com.lidroid.xutils.exception.DbException;
import com.systek.guide.R;
import com.systek.guide.adapter.ExhibitAdapter;
import com.systek.guide.custom.ClearEditText;
import com.systek.guide.entity.ExhibitBean;
import com.systek.guide.utils.ExceptionUtil;

import java.util.ArrayList;
import java.util.List;


/**
 * 搜索 activity
 */
public class SearchActivity extends BaseActivity {

    private ListView listViewExhibit;
    private Handler handler;
    private ClearEditText mClearEditText;
    private List<ExhibitBean> exhibitBeanList;
    private ExhibitAdapter exhibitAdapter;

    @Override
    protected void initialize(Bundle savedInstanceState) {
        setContentView(R.layout.activity_search);
        initDrawer();
        initView();
        addListener();
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
                ExhibitBean exhibitBean=exhibitBeanList.get(position);
                String str= JSON.toJSONString(exhibitBean);
                Intent intent =new Intent();
                intent.setAction(INTENT_EXHIBIT);
                intent.putExtra(INTENT_EXHIBIT, str);
                sendBroadcast(intent);
                Intent intent1 =new Intent(SearchActivity.this,PlayActivity.class);
                intent1.putExtra(INTENT_EXHIBIT,str);
                startActivity(intent1);
                finish();
            }
        });

    }

    private void filterData(final String s) {
        new Thread(){
            @Override
            public void run() {
                //exhibitBeanList=new ArrayList<>();
                if(TextUtils.isEmpty(s)){
                    exhibitBeanList=new ArrayList<>();
                    handler.sendEmptyMessage(MSG_WHAT_UPDATE_DATA_SUCCESS);
                    return;
                }
                DbUtils db=DbUtils.create(SearchActivity.this);
                try {
                    exhibitBeanList= db.findAll(Selector.from(ExhibitBean.class).where(LABELS,LIKE,"%"+s+"%").or(NAME,LIKE,"%"+s+"%"));
                } catch (DbException e) {
                    ExceptionUtil.handleException(e);
                }finally {
                    if(db!=null){
                        db.close();
                    }
                }
                if(exhibitBeanList==null){
                    exhibitBeanList=new ArrayList<>();
                }
                handler.sendEmptyMessage(MSG_WHAT_UPDATE_DATA_SUCCESS);
            }
        }.start();

    }

    private void initView() {
        handler=new MyHandler();
        exhibitBeanList=new ArrayList<>();
        mClearEditText = (ClearEditText) findViewById(R.id.filter_edit);
        listViewExhibit=(ListView)findViewById(R.id.listViewExhibit);
        exhibitAdapter=new ExhibitAdapter(this,exhibitBeanList);
        listViewExhibit.setAdapter(exhibitAdapter);
        listViewExhibit.setOverScrollMode(ScrollView.OVER_SCROLL_NEVER);
    }

    class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            if(msg.what==MSG_WHAT_UPDATE_DATA_SUCCESS){
                exhibitAdapter.updateData(exhibitBeanList);
            }
        }
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

}
