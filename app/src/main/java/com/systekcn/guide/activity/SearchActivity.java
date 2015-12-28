package com.systekcn.guide.activity;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.lidroid.xutils.DbUtils;
import com.lidroid.xutils.db.sqlite.Selector;
import com.lidroid.xutils.exception.DbException;
import com.systekcn.guide.R;
import com.systekcn.guide.activity.base.BaseActivity;
import com.systekcn.guide.adapter.SearchAdapter;
import com.systekcn.guide.common.IConstants;
import com.systekcn.guide.common.utils.ExceptionUtil;
import com.systekcn.guide.common.utils.ViewUtils;
import com.systekcn.guide.custom.SearchView;
import com.systekcn.guide.entity.ExhibitBean;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends BaseActivity implements SearchView.SearchViewListener,IConstants{

    private static final String TAG = SearchActivity.class.getSimpleName();

    ///////////////////////// 搜索结果 ListView+data+adapter
    /**
     * 搜索结果list view
     */
    private ListView lvResults;
    /**
     * 搜索结果列表数据
     */
    private List<ExhibitBean> resultData;
    /**
     * 结果adapter
     */
    private SearchAdapter resultAdapter;

    //////////////////////////////////////////////////
    /**
     * 自定义的搜索view
     */
    private SearchView searchView;

    ///////////////////////// 自定义搜索SearchView 提示框 data+adapter
    /**
     * 热搜版数据（推荐精品）
     */
    private List<String> hintData;
    /**
     * 热搜版adapter
     */
    private ArrayAdapter<String> hintAdapter;

    ///////////////////////// 自定义搜索SearchView 提示框 data+adapter
    /**
     * 搜索过程中自动补全数据
     */
    private List<String> autoCompleteData;
    /**
     * 自动补全adapter
     */
    private ArrayAdapter<String> autoCompleteAdapter;

    /**
     * 默认提示列表的数据项个数
     */
    private static int DEFAULT_HINT_SIZE = 6;

    /**
     * 提示列表的数据项个数
     */
    private static int mHintSize = DEFAULT_HINT_SIZE;

    /**
     * 设置提示列表数据项个数
     *
     * @param hintSize
     */
    public static void setHintSize(int hintSize) {
        mHintSize = hintSize;
    }

    @Override
    protected void initialize() {
        ViewUtils.setStateBarColor(this, R.color.orange);
        setContentView(R.layout.activity_search);
        initViews();
    }

    private void initViews() {
        // 搜索结果
        lvResults = (ListView) findViewById(R.id.search_lv_results);
        // 自定义SearchView应该包含两个结构：输入栏+弹出框。
        searchView = (SearchView) findViewById(R.id.search_view);
        // 设置搜索监听回调
        searchView.setSearchViewListener(this);
        // 从服务端中获取到数据库 ，再从数据库中获得 获取精品推荐数据(热度搜索)
        getHintData();
        // 设置热度adapter,精品推荐，热度搜索
        searchView.setTipsHintAdapter(hintAdapter);
        // 自动补全提示框：数据+adapter
        autoCompleteData = new ArrayList<>(mHintSize);
        autoCompleteAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, autoCompleteData);
        searchView.setAutoCompleteAdapter(autoCompleteAdapter);
        // 搜索结果ListView: 数据+adapter
        resultData = new ArrayList<>();
        resultAdapter = new SearchAdapter(this, resultData);

        lvResults.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view,
                                    int position, long l) {
                application.currentExhibitBean= resultAdapter.getItem(position);
                application.refreshData();
                Intent intent =new Intent(SearchActivity.this,GuideActivity.class);
                startActivity(intent);
                finish();
            }
        });

    }

    /**
     * TODO 从服务端中获取到数据库 ，再从数据库中获得 获取精品推荐数据(热度搜索)
     */
    private void getHintData() {
        hintData = new ArrayList<>(mHintSize);
        for (int i = 0; i < mHintSize; i++) {
            hintData.add("分类/精品" + (i + 1));
        }
        hintAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, hintData);
    }

    /**
     * 获取所有的exhibitBean 匹配每个的name 获取自动补全data 和adapter
     * 当有文字输入时:
     * 在提示框显示自动补全的茶品名称，最多mHintSize个
     * 填充resultData，提供搜索结果展品
     */
    private void getAutoCompleteData(String text) {
        // 清除上次匹配的数据
        autoCompleteData.clear();
        resultData.clear();
        DbUtils db= DbUtils.create(this);
        List<ExhibitBean> list=null;
        try {
            list=  db.findAll(Selector.from(ExhibitBean.class).where("name","like","%"+text+"%"));
        } catch (DbException e) {
            ExceptionUtil.handleException(e);
        }
        List<String> nameList=new ArrayList<>();
        if(list!=null&&list.size()>0){
            for(ExhibitBean bean : list){
                nameList.add(bean.getName());
            }
            autoCompleteAdapter.addAll(nameList);
        }

        // 通知提示框数据改变
        autoCompleteAdapter.notifyDataSetChanged();
        // 通知搜索结果数据改变，在点击软键盘的serarch按键时触发，见onSearch()
        resultAdapter.updateData(list);
    }

    /**
     * 当 edit text 文本改变时 触发的回调,自动匹配展品名称
     *
     * @param text
     */
    @Override
    public void onAutoRefreshComplete(String text) {
        getAutoCompleteData(text);
    }

    /**
     * 点击软键盘的搜索键时edit text触发的回调
     *
     * @param text
     */
    @Override
    public void onSearch(String text) {
        // 在onAutoRefreshComplete(text)中，已经填充了lvResults,这里仅使用即可。
        lvResults.setVisibility(View.VISIBLE);
        // 第一次获取结果 还未配置适配器
        if (lvResults.getAdapter() == null) {
            // 获取搜索数据 设置适配器
            lvResults.setAdapter(resultAdapter);
        } else {
            // 更新搜索数据
            resultAdapter.notifyDataSetChanged();
        }
        // Toast.makeText(this, "完成搜索", Toast.LENGTH_SHORT).show();
        // 隐藏软键盘
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
    }
}
