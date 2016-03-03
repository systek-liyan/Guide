package com.systek.guide.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.FileDownloadListener;
import com.liulishuo.filedownloader.FileDownloadQueueSet;
import com.liulishuo.filedownloader.FileDownloader;
import com.systek.guide.IConstants;
import com.systek.guide.biz.BizFactory;
import com.systek.guide.biz.DownloadBiz;
import com.systek.guide.utils.ExceptionUtil;
import com.systek.guide.utils.LogUtil;
import com.systek.guide.utils.Tools;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class DownloadService extends IntentService implements IConstants{
    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_FOO = "com.systek.guide.service.action.FOO";
    private static final String ACTION_BAZ = "com.systek.guide.service.action.BAZ";

    // TODO: Rename parameters
    private static final String EXTRA_PARAM1 = "com.systek.guide.service.extra.PARAM1";
    private static final String EXTRA_PARAM2 = "com.systek.guide.service.extra.PARAM2";

    public DownloadService() {
        super("DownloadService");
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionFoo(Context context, String param1, String param2) {
        Intent intent = new Intent(context, DownloadService.class);
        intent.setAction(ACTION_FOO);
        intent.putExtra(EXTRA_PARAM1, param1);
        intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action Baz with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionBaz(Context context, String param1) {
        Intent intent = new Intent(context, DownloadService.class);
        intent.setAction(ACTION_BAZ);
        intent.putExtra(EXTRA_PARAM1, param1);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_FOO.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_PARAM1);
                final String param2 = intent.getStringExtra(EXTRA_PARAM2);
                handleActionFoo(param1, param2);
            } else if (ACTION_BAZ.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_PARAM1);
                handleActionBaz(param1);
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionFoo(String param1, String param2) {
        // TODO: Handle action Foo
        // throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Handle action Baz in the provided background thread with the provided
     * parameters.
     */
    private void handleActionBaz(String museumId) {

        DownloadBiz downloadBiz= (DownloadBiz) BizFactory.getDownloadBiz();
        String assetsJson=downloadBiz.getAssetsJSON(museumId);
        if(TextUtils.isEmpty(assetsJson)){
            LogUtil.i("ZHANG","assetsJson获取失败");
            return;
        }
        List<String> assetsList=downloadBiz.parseAssetsJson(assetsJson);
        if(assetsList==null||assetsList.size()==0){
            LogUtil.i("ZHANG","assetsJson为 0");
            return;}
        int count =assetsList.size();
        try {

            downloadListener = getFileDownloadListener();
            final FileDownloadQueueSet queueSet = new FileDownloadQueueSet(downloadListener);
            String path= LOCAL_ASSETS_PATH+museumId;
            if(!path.endsWith(File.separator)){
                path = path + File.separator;
            }
            File dir = new File(path);
            if(!dir.isDirectory()){
                if(dir.mkdirs()){
                    Log.i("ZHANG","文件夹已经创建");
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
        }catch (Exception e){
            ExceptionUtil.handleException(e);
        }

    }
    FileDownloadListener downloadListener;
    private FileDownloadListener getFileDownloadListener(){
        return new FileDownloadListener() {
            @Override
            protected void pending(BaseDownloadTask task, int soFarBytes, int totalBytes) {
            }

            @Override
            protected void connected(BaseDownloadTask task, String etag, boolean isContinue, int soFarBytes, int totalBytes) {
            }

            @Override
            protected void progress(BaseDownloadTask task, int soFarBytes, int totalBytes) {
            }

            @Override
            protected void blockComplete(BaseDownloadTask task) {
            }

            @Override
            protected void retry(final BaseDownloadTask task, final Throwable ex, final int retryingTimes, final int soFarBytes) {
                LogUtil.i("ZHANG","retry");
            }

            @Override
            protected void completed(BaseDownloadTask task) {
                LogUtil.i("ZHANG","completed"+task.getPath());
            }

            @Override
            protected void paused(BaseDownloadTask task, int soFarBytes, int totalBytes) {
            }

            @Override
            protected void error(BaseDownloadTask task, Throwable e) {
                LogUtil.i("ZHANG","error");
            }

            @Override
            protected void warn(BaseDownloadTask task) {
                LogUtil.i("ZHANG","warn");
            }
        };
    }


}
