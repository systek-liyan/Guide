package com.systek.guide.activity;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;

import com.systek.guide.R;

public class TestActivity extends AppCompatActivity {

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        mSwipeRefreshLayout=(SwipeRefreshLayout)findViewById(R.id.swipeRefreshLayout);
        recyclerView=(RecyclerView)findViewById(R.id.recyclerView);
        mSwipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.accent),
                getResources().getColor(R.color.colorPrimaryBlue),
                getResources().getColor(R.color.colorPrimary));

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

            }
        });

        //mSwipeRefreshLayout.setProgressViewOffset(false,(int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, getResources().getDisplayMetrics()));

        recyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
               /* if (newState == RecyclerView.SCROLL_STATE_IDLE
                        && lastVisibleItem + 1 == adapter.getItemCount()) {
                    mSwipeRefreshWidget.setRefreshing(true);
                    // 此处在现实项目中，请换成网络请求数据代码，sendRequest .....
                    handler.sendEmptyMessageDelayed(0, 3000);
*/
                }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });
    }

}

