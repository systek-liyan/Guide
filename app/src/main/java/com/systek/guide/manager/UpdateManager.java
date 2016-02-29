package com.systek.guide.manager;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;

import com.alibaba.fastjson.JSON;
import com.systek.guide.IConstants;
import com.systek.guide.R;
import com.systek.guide.entity.base.VersionBean;
import com.systek.guide.utils.ExceptionUtil;
import com.systek.guide.utils.MyHttpUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Qiang on 2016/1/19.
 *
 * 版本更新类
 */
public class UpdateManager implements IConstants {

    private Context mContext;

    //提示语
    private String updateMsg = "有最新的软件包哦，亲快下载吧~";

    // 下载包安装路径
    private static final String savePath = "/sdcard/Guide/";
    private static final String saveFileName = savePath + "Guide.apk";

    //进度条与通知ui刷新的handler和msg常量
    private ProgressBar mProgress;

    private static final int DOWN_UPDATE = 1;

    private static final int DOWN_OVER = 2;

    private int progress;

    //返回的安装包url
    private String apkUrl;

    private boolean interceptFlag = false;

    public UpdateManager(Context context) {
        this.mContext = context;
    }

    public void setApkUrl(String apkUrl) {
        this.apkUrl = apkUrl;
    }

    private Handler mHandler = new Handler(){
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case DOWN_UPDATE:
                    mProgress.setProgress(progress);
                    break;
                case DOWN_OVER:
                    installApk();
                    break;
                default:
                    break;
            }
        }
    };

    //外部接口让主Activity调用
    public void checkUpdateInfo(){
        showNoticeDialog();
    }


    /**
     * 检查最新版本号
     * @return 版本号
     */
    public  VersionBean checkVersion(){
        String url=BASE_URL+URL_CHECK_FOR_UPDATE;
        String response= MyHttpUtil.sendGet(url);
        if(TextUtils.isEmpty(response)||response.equals("[]")){return null;}
        return JSON.parseObject(response, VersionBean.class);
    }

    /**
     2  * 获取版本号
     3  * @return 当前应用的版本号
     4  */
    public String getVersion(Context context) {
        try {
            PackageManager manager = context.getPackageManager();
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
            return info.versionName;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }


    private void showNoticeDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle("软件版本更新");
        builder.setMessage(updateMsg);
        builder.setPositiveButton("下载", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                showDownloadDialog();
            }
        });
        builder.setNegativeButton("以后再说", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        Dialog noticeDialog = builder.create();
        noticeDialog.show();
    }

    private void showDownloadDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle("软件版本更新");

        final LayoutInflater inflater = LayoutInflater.from(mContext);
        View v = inflater.inflate(R.layout.dialog_app_update, null);
        mProgress = (ProgressBar)v.findViewById(R.id.progress);

        builder.setView(v);
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                interceptFlag = true;
            }
        });
        Dialog downloadDialog = builder.create();
        downloadDialog.show();
        downloadApk();
    }


    private void deleteAllFiles(File root) {
        File files[] = root.listFiles();
        if (files == null){return;}
        for (File f : files) {
            if (f.isDirectory()) { // 判断是否为文件夹
                deleteAllFiles(f);
                try {
                    f.delete();
                } catch (Exception e) {
                    ExceptionUtil.handleException(e);
                }
            } else {
                if (f.exists()) { // 判断是否存在
                    deleteAllFiles(f);
                    try {
                        f.delete();
                    } catch (Exception e) {
                        ExceptionUtil.handleException(e);
                    }
                }
            }
        }
    }

    private Runnable mdownApkRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                URL url = new URL(apkUrl);
                HttpURLConnection conn = (HttpURLConnection)url.openConnection();
                conn.connect();
                int length = conn.getContentLength();
                InputStream is = conn.getInputStream();

                File file = new File(savePath);
                if(!file.exists()){
                    file.mkdir();
                }
                String apkFile = saveFileName;
                File ApkFile = new File(apkFile);
                FileOutputStream fos = new FileOutputStream(ApkFile);
                int count = 0;
                byte buf[] = new byte[1024];
                do{
                    int numread = is.read(buf);
                    count += numread;
                    progress =(int)(((float)count / length) * 100);
                    //更新进度
                    mHandler.sendEmptyMessage(DOWN_UPDATE);
                    if(numread <= 0){
                        //下载完成通知安装
                        mHandler.sendEmptyMessage(DOWN_OVER);
                        break;
                    }
                    fos.write(buf,0,numread);
                }while(!interceptFlag);//点击取消就停止下载.
                fos.close();
                is.close();
            } catch(IOException e){
                e.printStackTrace();
            }
        }
    };

    /**
     * 下载apk
     */
    private void downloadApk(){
        File file=new File(savePath);
        if(file.isDirectory()){
            deleteAllFiles(file);
        }
        Thread downLoadThread = new Thread(mdownApkRunnable);
        downLoadThread.start();
    }
    /**
     * 安装apk
     */
    private void installApk(){
        File apkfile = new File(saveFileName);
        if (!apkfile.exists()) {
            return;
        }
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setDataAndType(Uri.parse("file://" + apkfile.toString()), "application/vnd.android.package-archive");
        mContext.startActivity(i);

    }

}
