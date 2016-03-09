package com.systek.guide.download;

import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;

import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.FileDownloadConnectListener;
import com.liulishuo.filedownloader.FileDownloadListener;
import com.liulishuo.filedownloader.FileDownloader;
import com.liulishuo.filedownloader.model.FileDownloadStatus;
import com.liulishuo.filedownloader.util.FileDownloadUtils;
import com.systek.guide.IConstants;
import com.systek.guide.activity.TasksManagerDemoActivity;
import com.systek.guide.biz.BizFactory;
import com.systek.guide.biz.DownloadBiz;
import com.systek.guide.entity.MuseumBean;
import com.systek.guide.utils.LogUtil;
import com.systek.guide.utils.Tools;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class TasksManager implements IConstants {


    /**
     * Created by Qiang on 2016/3/4.
     *
     * 下载管理类
     */
    private final static class HolderClass {

        private final static TasksManager INSTANCE = new TasksManager();

    }

    private List<TasksMuseumModel> modelList;


    private TasksManagerDBController dbController;

    private TasksManager() {
        dbController = new TasksManagerDBController();
        refreshTaskList();
    }

    private void refreshTaskList() {
        modelList = dbController.getAllTasks();
    }

    public static TasksManager getImpl() {
        return HolderClass.INSTANCE;
    }

    private SparseArray<MyFileDownloadQueueSet> taskSparseArray = new SparseArray<>();

    public void addTaskForViewHoder(final MyFileDownloadQueueSet task) {
        taskSparseArray.put(task.getDownloadId(), task);
    }

    public MyFileDownloadQueueSet getTaskForViewHoder(final int taskId) {
        return taskSparseArray.get(taskId);
    }


    public void removeTaskForViewHolder(final int id){
        taskSparseArray.remove(id);
    }

    public void updateViewHolder(final int id, final TaskItemViewHolder holder) {
        final MyFileDownloadQueueSet task = taskSparseArray.get(id);
        if (task == null) {
            return;
        }

        task.setHolder(holder);
    }

    public void releaseTask(){
        taskSparseArray.clear();
    }

    private FileDownloadConnectListener listener;

    public void onCreate(final WeakReference<TasksManagerDemoActivity> activityWeakReference) {

        refreshTaskList();
        FileDownloader.getImpl().bindService();

        if (listener != null) {
            FileDownloader.getImpl().removeServiceConnectListener(listener);
        }

        listener = new FileDownloadConnectListener() {

            @Override
            public void connected() {
                if (activityWeakReference == null
                        || activityWeakReference.get() == null) {
                    return;
                }

                activityWeakReference.get().postNotifyDataChanged();
            }

            @Override
            public void disconnected() {
                if (activityWeakReference == null
                        || activityWeakReference.get() == null) {
                    return;
                }

                activityWeakReference.get().postNotifyDataChanged();
            }
        };

        FileDownloader.getImpl().addServiceConnectListener(listener);
    }

    public void onDestroy() {
        FileDownloader.getImpl().removeServiceConnectListener(listener);
        listener = null;
        releaseTask();
    }

    public boolean isReady() {
        return FileDownloader.getImpl().isServiceConnected();
    }

    public TasksMuseumModel get(final int position) {
        return modelList.get(position);
    }

    public TasksMuseumModel getbyId(final int id) {
        for (TasksMuseumModel model : modelList) {
            if (model.getId() == id) {
                return model;
            }
        }

        return null;
    }

    /**
     * @param status Download Status
     * @return has already downloaded
     * @see FileDownloadStatus
     */
    public boolean isDownloaded( final int status) {
        return status == FileDownloadStatus.completed;
    }

    public boolean isDownloading(final int status) {
        switch (status) {
            case FileDownloadStatus.pending:
            case FileDownloadStatus.connected:
            case FileDownloadStatus.progress:
                return true;
            default:
                return false;
        }
    }

    public boolean isExist(final int id) {
        return new File(getbyId(id).getPath()).exists();
    }

    public int getStatus(final int id) {
        TasksMuseumModel task=dbController.getSingleTask(id);
        if(task==null){return FileDownloadStatus.error; }
        return task.getStatus();
    }

    public void setStatus(final int id,final int status) {
        dbController.setStatus(id,status);
    }


    public long getTotal(final int id) {
        TasksMuseumModel task=getbyId(id);
        if(task==null){return 0;}
        return task.getTotal();
    }

    public long getSoFar(final int id) {
        return FileDownloader.getImpl().getSoFar(id);
    }

    public int getTaskCounts() {
        return modelList.size();
    }

    public TasksMuseumModel addTask(MuseumBean museum) {
        if (museum==null) {
            return null;
        }
        String url=BASE_URL+URL_ALL_MUSEUM_ASSETS+museum.getId();
        String path=LOCAL_ASSETS_PATH+museum.getId();
        // have to use FileDownloadUtils.generateId to associate TasksMuseumModel with FileDownloader
        final int id = FileDownloadUtils.generateId(url, path);
        TasksMuseumModel mTask = getbyId(id);
        if (mTask != null) {
            return mTask;
        }
        TasksMuseumModel task=new TasksMuseumModel();

        task.setId(id);
        task.setIconUrl(museum.getIconUrl());
        task.setMuseumId(museum.getId());
        task.setName(museum.getName());
        task.setUrl(url);
        task.setPath(path);
        return dbController.addTask(task);
    }
    private FileDownloadListener getFileDownloadListener(int downloadId,int totalSize){
        return new MyFileDownloadListener(downloadId,totalSize);
    }

    private class MyFileDownloadListener extends FileDownloadListener{


        private int totalSize;
        private int currentSize;
        private int downloadId;

        MyFileDownloadListener(int downloadId,int totalSize){
            this.downloadId=downloadId;
            this.totalSize=totalSize;
        }


        @Override
        protected void pending(BaseDownloadTask task, int soFarBytes, int totalBytes) {

        }

        @Override
        protected void progress(BaseDownloadTask task, int soFarBytes, int totalBytes) {

        }

        @Override
        protected void blockComplete(BaseDownloadTask task) {

        }

        @Override
        protected void completed(BaseDownloadTask task) {
            currentSize++;
            LogUtil.i("ZHANG","completed--count="+currentSize+"  name="+task.getPath());
            if(downloadId!=-1){
                final MyFileDownloadQueueSet queueSet = getTaskForViewHoder(downloadId);
                if(queueSet==null){return;}
                TaskItemViewHolder holder=queueSet.getHolder();

                holder.updateDownloading(FileDownloadStatus.progress,currentSize,totalSize);
            }
        }

        @Override
        protected void paused(BaseDownloadTask task, int soFarBytes, int totalBytes) {

        }

        @Override
        protected void error(BaseDownloadTask task, Throwable e) {

        }

        @Override
        protected void warn(BaseDownloadTask task) {

        }
    }


    public void toDownload(final String museumId,final TaskItemViewHolder holder){

        new Thread(){

            @Override
            public void run() {
                FileDownloadListener downloadListener=null;

                DownloadBiz downloadBiz= (DownloadBiz) BizFactory.getDownloadBiz();
                String assetsJson=downloadBiz.getAssetsJSON(museumId);
                if(TextUtils.isEmpty(assetsJson)){
                    LogUtil.i("ZHANG", "assetsJson获取失败");
                    return;
                }
                List<String> assetsList=downloadBiz.parseAssetsJson(assetsJson);
                if(assetsList==null||assetsList.size()==0){
                    LogUtil.i("ZHANG","assetsJson为 0");
                    return;}
                int count =assetsList.size();
                //TasksManager.getImpl().setStatus();

                final String mUrl=BASE_URL+URL_ALL_MUSEUM_ASSETS+museumId;
                String path=LOCAL_ASSETS_PATH+museumId;
                int downloadId=FileDownloadUtils.generateId(mUrl,path);
                downloadListener = getFileDownloadListener(downloadId,count);
                final MyFileDownloadQueueSet queueSet = new MyFileDownloadQueueSet(downloadListener);
                queueSet.setUrl(mUrl);
                queueSet.setPath(path);
                TasksManager.getImpl().addTaskForViewHoder(queueSet);
                TasksManager.getImpl().updateViewHolder(holder.id, holder);

                if(!path.endsWith(File.separator)){
                    path = path + File.separator;
                }
                File dir = new File(path);
                if(!dir.isDirectory()){
                    if(dir.mkdirs()){
                        Log.i("ZHANG", "文件夹已经创建");
                    }
                }
                final List<BaseDownloadTask> tasks = new ArrayList<>();
                for (int i = 0; i < count; i++) {
                    String url=BASE_URL+assetsList.get(i);
                    String name= Tools.changePathToName(assetsList.get(i));
                    BaseDownloadTask task=FileDownloader.getImpl()
                            .create(url)
                            .setPath(path + name);
                    task.ready();
                    tasks.add(task);
                }
                // 由于是队列任务, 这里是我们假设了现在不需要每个任务都回调`FileDownloadListener#progress`,
                // 我们只关系每个任务是否完成, 所以这里这样设置可以很有效的减少ipc.
                queueSet.disableCallbackProgressTimes();
                // 所有任务在下载失败的时候都自动重试一次
                queueSet.setAutoRetryTimes(1);
                queueSet.downloadTogether(tasks);
                queueSet.start();
                TasksManager.getImpl().setStatus(holder.id,FileDownloadStatus.progress);
            }
        }.start();

    }


}
