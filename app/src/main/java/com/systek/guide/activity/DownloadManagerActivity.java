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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;

import com.liulishuo.filedownloader.FileDownloader;
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
    void setView() {
        View view = View.inflate(this, R.layout.activity_download, null);
        setContentView(view);
        handler=new MyHandler(this);
        initDrawer();
    }
    @Override
    void initView() {
        final RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mErrorView=findViewById(R.id.mErrorView);
        refreshBtn=(Button)mErrorView.findViewById(R.id.refreshBtn);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TaskItemAdapter();
        recyclerView.setAdapter(adapter);
        setTitleBar();
        setTitleBarTitle(R.string.title_bar_download_center);
        setHomeIcon();
    }

    @Override
    void addListener() {
        toolbar.setNavigationOnClickListener(backOnClickListener);
    }

    @Override
    void initData() {
        Intent intent =getIntent();
        MuseumBean museumBean= (MuseumBean) intent.getSerializableExtra(INTENT_MUSEUM);
        TasksManager.getImpl().addTask(museumBean);
        TasksManager.getImpl().onCreate(new WeakReference<>(this));
    }

    @Override
    void registerReceiver() {

    }

    @Override
    void unRegisterReceiver() {

    }

    @Override
    void refreshView() {
        postNotifyDataChanged();
    }

    @Override
    void refreshExhibit() {

    }

    @Override
    void refreshTitle() {

    }

    @Override
    void refreshViewBottomTab() {

    }

    @Override
    void refreshProgress() {

    }

    @Override
    void refreshIcon() {

    }

    @Override
    void refreshState() {

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
        TasksManager.getImpl().pauseAllTask();
        TasksManager.getImpl().onDestroy();
        adapter = null;
        FileDownloader.getImpl().pauseAll();
        super.onDestroy();
    }
}
