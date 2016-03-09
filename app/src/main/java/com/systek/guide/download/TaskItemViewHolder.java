package com.systek.guide.download;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.liulishuo.filedownloader.model.FileDownloadStatus;
import com.systek.guide.R;

import java.io.Serializable;

/**
 * Created by Qiang on 2016/3/4.
 *
 * 下载item的viewholder
 */
public class TaskItemViewHolder extends RecyclerView.ViewHolder implements Serializable {

    public TaskItemViewHolder(View itemView) {
        super(itemView);
        assignViews();
    }

    private View findViewById(final int id) {
        return itemView.findViewById(id);
    }

    /**
     * viewHolder position
     */
    public int position;
    /**
     * download id
     */
    public int id;

    public void update(final int id, final int position) {
        this.id = id;
        this.position = position;
    }


    public void updateDownloaded() {
        taskPb.setMax(1);
        taskPb.setProgress(1);

        taskStatusTv.setText("");
        taskActionBtn.setText("删除");
    }

    public void updateNotDownloaded(final int status, final long sofar, final long total) {
        if (sofar > 0 && total > 0) {
            final float percent = sofar
                    / (float) total;
            taskPb.setMax(100);
            taskPb.setProgress((int) (percent * 100));
        } else {
            taskPb.setMax(1);
            taskPb.setProgress(0);
        }

        switch (status) {
            case FileDownloadStatus.error:
                taskStatusTv.setText("下载发生错误。。。");//R.string.tasks_manager_demo_status_error
                break;
            case FileDownloadStatus.paused:
                taskStatusTv.setText("暂停中。。。");//R.string.tasks_manager_demo_status_paused
                break;
            default:
                taskStatusTv.setText("已下载。。。");//R.string.tasks_manager_demo_status_not_downloaded
                break;
        }
        taskActionBtn.setBackgroundResource(R.drawable.download_start);
        //taskActionBtn.setText("Start");
    }

    public void updateDownloading(final int status, final long sofar, final long total) {
        final float percent = sofar / (float) total;
        taskPb.setMax(100);
        taskPb.setProgress((int) (percent * 100));

        switch (status) {
            case FileDownloadStatus.pending:
                taskStatusTv.setText("pending");//R.string.tasks_manager_demo_status_pending
                break;
            case FileDownloadStatus.connected:
                taskStatusTv.setText("connected");//R.string.tasks_manager_demo_status_connected
                break;
            case FileDownloadStatus.progress:
                taskStatusTv.setText("progress");//R.string.tasks_manager_demo_status_progress
                break;
            default:
                taskStatusTv.setText("downloading");//;DemoApplication.CONTEXT.getString(R.string.tasks_manager_demo_status_downloading)
                break;
        }
        taskActionBtn.setBackgroundResource(R.drawable.download_stop);// TODO: 2016/3/9
        //taskActionBtn.setText("Pause");
        String currentSize=String.valueOf(percent * 100);
        if(currentSize.length()>4){
            currentSize=currentSize.substring(0,4);
        }
        currentProgress.setText(currentSize+"%");
    }

    public  TextView taskNameTv;
    public TextView taskStatusTv;
    public ProgressBar taskPb;
    public TextView taskActionBtn;
    public TextView currentProgress;

    private void assignViews() {
        taskNameTv = (TextView) findViewById(R.id.museumName);
        taskStatusTv = (TextView) findViewById(R.id.task_status_tv);
        taskPb = (ProgressBar) findViewById(R.id.downloadProgressBar);
        taskActionBtn = (TextView) findViewById(R.id.downloadBtn);
        currentProgress = (TextView) findViewById(R.id.currentProgress);
    }

}
