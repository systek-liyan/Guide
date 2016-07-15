package com.systek.guide.custom.recyclerView;

import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;

/**
 * Created by Qiang on 2016/7/13.
 *
 */
public class RecyclerViewOnScroll extends RecyclerView.OnScrollListener{

    private QRecyclerView qRecyclerView;

    public RecyclerViewOnScroll(QRecyclerView qRecyclerView) {
        this.qRecyclerView = qRecyclerView;
    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);

        int lastCompletelyVisibleItem = 0;
        int firstVisibleItem = 0;
        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        int totalItemCount = layoutManager.getItemCount();
        if (layoutManager instanceof GridLayoutManager){
            GridLayoutManager gridLayoutManager = ((GridLayoutManager) layoutManager);
            //Position to find the final item of the current LayoutManager
            lastCompletelyVisibleItem = gridLayoutManager.findLastCompletelyVisibleItemPosition();
            firstVisibleItem = gridLayoutManager.findFirstCompletelyVisibleItemPosition();
        } else if (layoutManager instanceof LinearLayoutManager) {
            LinearLayoutManager linearLayoutManager = ((LinearLayoutManager) layoutManager);
            lastCompletelyVisibleItem = linearLayoutManager.findLastCompletelyVisibleItemPosition();
            firstVisibleItem = linearLayoutManager.findFirstCompletelyVisibleItemPosition();
        } else if (layoutManager instanceof StaggeredGridLayoutManager) {
            StaggeredGridLayoutManager staggeredGridLayoutManager = ((StaggeredGridLayoutManager) layoutManager);
            // since may lead to the final item has more than one StaggeredGridLayoutManager the particularity of the so here that is an array
            // this array into an array of position and then take the maximum value that is the last show the position value
            int[] lastPositions = new int[((StaggeredGridLayoutManager) layoutManager).getSpanCount()];
            staggeredGridLayoutManager.findLastCompletelyVisibleItemPositions(lastPositions);
            lastCompletelyVisibleItem = findMax(lastPositions);
            firstVisibleItem = staggeredGridLayoutManager.findFirstVisibleItemPositions(lastPositions)[0];
        }
        if (firstVisibleItem == 0 || firstVisibleItem == RecyclerView.NO_POSITION) {
            if (qRecyclerView.getPullRefreshEnable())
                qRecyclerView.setSwipeRefreshEnable(true);
        } else {
            qRecyclerView.setSwipeRefreshEnable(false);
        }
        if (qRecyclerView.getPushRefreshEnable()
                && !qRecyclerView.isRefresh()
                && qRecyclerView.isHasMore()
                && (lastCompletelyVisibleItem == totalItemCount - 1)
                && !qRecyclerView.isLoadMore()
                && (dx > 0 || dy > 0)) {
            qRecyclerView.setIsLoadMore(true);
            qRecyclerView.loadMore();
        }

    }

    private int findMax(int[] lastPositions) {
        int max = lastPositions[0];
        for (int value : lastPositions) {
            //       int max    = Math.max(lastPositions,value);
            if (value > max) {
                max = value;
            }
        }
        return max;
    }

}
