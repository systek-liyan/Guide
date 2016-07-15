package com.systek.guide.custom.recyclerView;

import android.support.v4.widget.SwipeRefreshLayout;

/**
 * Created by Qiang on 2016/7/13.
 */
public class SwipeRefreshLayoutOnRefresh implements SwipeRefreshLayout.OnRefreshListener {


    private QRecyclerView mPullLoadMoreRecyclerView;

    public SwipeRefreshLayoutOnRefresh(QRecyclerView pullLoadMoreRecyclerView) {
        this.mPullLoadMoreRecyclerView = pullLoadMoreRecyclerView;
    }

    @Override
    public void onRefresh() {
        if (!mPullLoadMoreRecyclerView.isRefresh()) {
            mPullLoadMoreRecyclerView.setIsRefresh(true);
            mPullLoadMoreRecyclerView.refresh();
        }
    }


}
