package com.systek.guide.download;


import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.liulishuo.filedownloader.model.FileDownloadStatus;
import com.systek.guide.IConstants;
import com.systek.guide.MyApplication;
import com.systek.guide.R;
import com.systek.guide.utils.ImageUtil;
import com.systek.guide.utils.NetworkUtil;

import java.io.File;

/**
 * Created by Qiang on 2016/3/4.
 *
 * 下载管理任务适配器
 */
public class TaskItemAdapter extends RecyclerView.Adapter<TaskItemViewHolder> implements IConstants{


    private View.OnClickListener taskActionOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.getTag() == null) {
                return;
            }
            TaskItemViewHolder holder = (TaskItemViewHolder) v.getTag();
            //无网络连接，返回错误
            if(!NetworkUtil.isOnline(MyApplication.get())){
                holder.updateNotDownloaded(FileDownloadStatus.error,0,0);
                return;
            }
            final int status = TasksManager.getImpl().getStatus(holder.id);

            if (TasksManager.getImpl().isDownloaded(status) && TasksManager.getImpl().isExist(holder.id)) {
                // has downloaded
                TasksMuseumModel model= TasksManager.getImpl().get(holder.position);
                String path=LOCAL_ASSETS_PATH+model.getDownloadId();
                new File(path).delete();
                holder.taskActionBtn.setEnabled(true);
                holder.updateNotDownloaded(FileDownloadStatus.INVALID_STATUS ,0, 0);
            } else if (TasksManager.getImpl().isDownloading(status)) {
                // downloading
                // to pause
                TasksManager.getImpl().pauseTask(holder.id);// TODO: 2016/3/9
                TasksManager.getImpl().setStatus(holder.id,FileDownloadStatus.progress);
                v.setBackgroundResource(R.drawable.download_start);
            } else {
                // to start
                final TasksMuseumModel model = TasksManager.getImpl().get(holder.position);
                TasksManager.getImpl().toDownload(model, holder);
                v.setBackgroundResource(R.drawable.download_stop);
                //TasksManager.getImpl().setStatus(holder.id,FileDownloadStatus.progress);
            }
        }
    };




    @Override
    public TaskItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        TaskItemViewHolder holder = new TaskItemViewHolder(
                LayoutInflater.from(
                        parent.getContext())
                        .inflate(R.layout.item_download_museum, parent, false));

        holder.taskActionBtn.setOnClickListener(taskActionOnClickListener);
        return holder;
    }

    @Override
    public void onBindViewHolder(TaskItemViewHolder holder, int position) {

        final TasksMuseumModel model = TasksManager.getImpl().get(position);

        holder.update(model.getDownloadId(), position);
        holder.taskActionBtn.setTag(holder);
        holder.taskNameTv.setText(model.getName());
        ImageUtil.displayImage(model.getIconUrl(), holder.museumIcon,true,false);
        TasksManager.getImpl().updateViewHolder(holder.id, holder);

        holder.taskActionBtn.setEnabled(true);

        final int status = TasksManager.getImpl().getStatus(model.getDownloadId());

        if (TasksManager.getImpl().isReady()) {
            if (!TasksManager.getImpl().isExist(model.getDownloadId())) {
                // not exist file
                holder.updateNotDownloaded(status, 0, 0);
            } else if (TasksManager.getImpl().isDownloaded(status)){
                // already downloaded and exist
                holder.updateDownloaded();
            } else if (TasksManager.getImpl().isDownloading(status)) {
                // downloading
                holder.updateDownloading(status, TasksManager.getImpl().getSoFar(model.getDownloadId())
                        , TasksManager.getImpl().getTotal(model.getDownloadId()));
            } else {
                // not start
                holder.updateNotDownloaded(status, TasksManager.getImpl().getSoFar(model.getDownloadId())
                        , TasksManager.getImpl().getTotal(model.getDownloadId()));
            }
        } else {
            //holder.taskStatusTv.setText("loading");//R.string.tasks_manager_demo_status_loading
            holder.taskActionBtn.setEnabled(false);
        }
    }

    @Override
    public int getItemCount() {
        return TasksManager.getImpl().getTaskCounts();
    }

}
