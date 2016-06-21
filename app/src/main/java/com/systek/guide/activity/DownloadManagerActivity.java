/*
 * Copyright (c) 2015 LingoChamp Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.systek.guide.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Button;

import com.systek.guide.R;
import com.systek.guide.download.TaskItemAdapter;
import com.systek.guide.download.TasksManager;
import com.systek.guide.entity.MuseumBean;

import java.lang.ref.WeakReference;

/**
 * Created by Jacksgong on 1/9/16.
 *
 */
public class DownloadManagerActivity extends BaseActivity {

    private TaskItemAdapter adapter;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);
        initDrawer();
        initView();
        addListener();
        initData();
    }

    private void initView() {
        final RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mErrorView=findViewById(R.id.mErrorView);
        if (mErrorView != null) {
            refreshBtn=(Button)mErrorView.findViewById(R.id.refreshBtn);
        }
        if (recyclerView != null) {
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
        }
        adapter = new TaskItemAdapter();
        if (recyclerView != null) {
            recyclerView.setAdapter(adapter);
        }
        setTitleBar();
        setTitleBarTitle(R.string.title_bar_download_center);
        setHomeIcon();
    }

    private void addListener() {
        toolbar.setNavigationOnClickListener(backOnClickListener);
    }

    private void initData() {
        Intent intent =getIntent();
        MuseumBean museumBean= (MuseumBean) intent.getSerializableExtra(INTENT_MUSEUM);
        TasksManager.getImpl().addTask(museumBean);
        TasksManager.getImpl().onCreate(new WeakReference<>(this));
    }

    private void refreshView() {
        postNotifyDataChanged();
    }



    public void postNotifyDataChanged() {
        if (adapter != null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (adapter != null) {
                        adapter.notifyDataSetChanged();
                    }
                }
            });
        }
    }

    @Override
    protected void onDestroy() {
        adapter = null;
        super.onDestroy();
    }


}
