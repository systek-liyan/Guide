package com.systekcn.guide.activity;

import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.systekcn.guide.R;
import com.systekcn.guide.activity.base.BaseActivity;
import com.systekcn.guide.adapter.CityAdapter;
import com.systekcn.guide.biz.BeansManageBiz;
import com.systekcn.guide.biz.BizFactory;
import com.systekcn.guide.common.IConstants;
import com.systekcn.guide.common.utils.ExceptionUtil;
import com.systekcn.guide.common.utils.PinyinComparator;
import com.systekcn.guide.common.utils.ViewUtils;
import com.systekcn.guide.custom.ClearEditText;
import com.systekcn.guide.custom.SideBar;
import com.systekcn.guide.entity.CityBean;
import com.systekcn.guide.parser.CharacterParser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CityChooseActivity extends BaseActivity implements IConstants{

    private ListView sortListView;
    private SideBar sideBar;
    private TextView dialog;
    private CityAdapter adapter;
    private ClearEditText mClearEditText;
    private List<CityBean> cities;
    private  final int MSG_WHAT_CITIES=1;
    private  final int MSG_WHAT_FAIL_TO_GET_DATA=2;
    /**
     * 汉字转换成拼音的类
     */
    private CharacterParser characterParser;
    private List<CityBean> SourceDateList;

    /**
     * 根据拼音来排列ListView里面的数据类
     */
    private PinyinComparator pinyinComparator;
    private Handler handler;


    @Override
    protected void initialize() {
        ViewUtils.setStateBarToAlpha(this);
        setContentView(R.layout.activity_city_choose);
        initViews();
        initData();
    }

    long startTime;
    private void initData() {
        try{
            new Thread(){
                public void run() {
                    int msg=MSG_WHAT_CITIES;
                    BeansManageBiz biz=(BeansManageBiz) BizFactory.getBeansManageBiz(CityChooseActivity.this);
                    cities=biz.getAllBeans(URL_TYPE_GET_CITY,CityBean.class,"");
                    while(cities==null){
                        if(System.currentTimeMillis()-startTime>5){
                            msg=MSG_WHAT_FAIL_TO_GET_DATA;
                            break;
                        }
                    }
                    handler.sendEmptyMessage(msg);
                }
            }.start();
        }catch (Exception e){
            ExceptionUtil.handleException(e);
        }
    }

    private void initViews() {
        handler=new MyHandler();
        //实例化汉字转拼音类
        characterParser = CharacterParser.getInstance();
        pinyinComparator = new PinyinComparator();
        sideBar = (SideBar) findViewById(R.id.sidrbar);
        dialog = (TextView) findViewById(R.id.dialog);
        sideBar.setTextView(dialog);
        sortListView = (ListView) findViewById(R.id.country_lvcountry);
        mClearEditText = (ClearEditText) findViewById(R.id.filter_edit);
        SourceDateList = new ArrayList<>();
        // 根据a-z进行排序源数据
        Collections.sort(SourceDateList, pinyinComparator);
        adapter = new CityAdapter(this, SourceDateList);
        sortListView.setAdapter(adapter);
        addListener();
    }

    private void addListener() {
        //设置右侧触摸监听
        sideBar.setOnTouchingLetterChangedListener(new SideBar.OnTouchingLetterChangedListener() {
            @Override
            public void onTouchingLetterChanged(String s) {
                //该字母首次出现的位置
                int position = adapter.getPositionForSection(s.charAt(0));
                if(position != -1){
                    sortListView.setSelection(position);
                }
            }
        });
        sortListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                //这里要利用adapter.getItem(position)来获取当前position所对应的对象
                Toast.makeText(getApplication(), ((CityBean) adapter.getItem(position)).getName(), Toast.LENGTH_SHORT).show();
            }
        });
        //根据输入框输入值的改变来过滤搜索
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
    }

    /**
     * 为ListView填充数据
     * @param date
     * @return
     */
    private List<CityBean> filledData(String [] date){
        List<CityBean> mSortList = new ArrayList<>();

        for(int i=0; i<date.length; i++){
            CityBean sortModel = new CityBean();
            sortModel.setName(date[i]);
            //汉字转换成拼音
            String pinyin = characterParser.getSelling(date[i]);
            String sortString = pinyin.substring(0, 1).toUpperCase();
            // 正则表达式，判断首字母是否是英文字母
            if(sortString.matches("[A-Z]")){
                sortModel.setAlpha(sortString.toUpperCase());
            }else{
                sortModel.setAlpha("#");
            }
            mSortList.add(sortModel);
        }
        return mSortList;

    }

    /**
     * 根据输入框中的值来过滤数据并更新ListView
     * @param filterStr
     */
    private void filterData(String filterStr){
        List<CityBean> filterDateList = new ArrayList<CityBean>();

        if(TextUtils.isEmpty(filterStr)){
            filterDateList = SourceDateList;
        }else{
            filterDateList.clear();
            for(CityBean sortModel : SourceDateList){
                String name = sortModel.getName();
                if(name.contains(filterStr) || characterParser.getSelling(name).startsWith(filterStr)){
                    filterDateList.add(sortModel);
                }
            }
        }
        // 根据a-z进行排序
        Collections.sort(filterDateList, pinyinComparator);
        adapter.updateListView(filterDateList);
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    class MyHandler extends  Handler{
        @Override
        public void handleMessage(Message msg) {
            if(msg.what==MSG_WHAT_CITIES){
                adapter.updateListView(cities);
            }
        }
    }
}
