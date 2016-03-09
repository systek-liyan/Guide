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

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.liulishuo.filedownloader.FileDownloader;
import com.systek.guide.R;
import com.systek.guide.download.TaskItemAdapter;
import com.systek.guide.download.TasksManager;

import java.lang.ref.WeakReference;

/**
 * Created by Jacksgong on 1/9/16.
 *
 */
public class TasksManagerDemoActivity extends BaseActivity {

    private TaskItemAdapter adapter;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download_test);
        handler=new MyHandler();
        final RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter = new TaskItemAdapter());

        TasksManager.getImpl().onCreate(new WeakReference<>(this));


    }


    class MyHandler  extends Handler {

        @Override
        public void handleMessage(Message msg) {
            if(msg.what==MSG_WHAT_UPDATE_DATA_SUCCESS){
                adapter.notifyDataSetChanged();
            }
        }
    };



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
        TasksManager.getImpl().onDestroy();
        adapter = null;
        FileDownloader.getImpl().pauseAll();
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}
