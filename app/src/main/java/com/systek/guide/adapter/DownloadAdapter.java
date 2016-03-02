package com.systek.guide.adapter;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.systek.guide.IConstants;
import com.systek.guide.MyApplication;
import com.systek.guide.R;
import com.systek.guide.biz.DownloadTask;
import com.systek.guide.entity.MuseumBean;
import com.systek.guide.utils.ImageLoaderUtil;
import com.systek.guide.utils.LogUtil;
import com.systek.guide.utils.Tools;

import java.util.List;

/**
 * Created by Qiang on 2015/11/2.
 *
 */
public class DownloadAdapter extends BaseAdapter implements IConstants {

    private Context context;
    private List<MuseumBean> list;
    private LayoutInflater inflater;
    public Handler handler;
    public  void updateData(List<MuseumBean> list){
        this.list=list;
        notifyDataSetChanged();
    }

    public void onDestroy(){
        if(handler!=null){
            handler.removeCallbacksAndMessages(null);
        }
    }

    public DownloadAdapter(Context c, List<MuseumBean> list) {
        this.context = c.getApplicationContext();
        this.list = list;
        inflater= LayoutInflater.from(context);
        handler=new Handler(Looper.getMainLooper());
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public MuseumBean getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return list.size();
    }

    @Override
    public boolean isEnabled(int position) {
        return false;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        final ViewHolder viewHolder;
        if(convertView==null){
            viewHolder=new ViewHolder();
            convertView=inflater.inflate(R.layout.item_download,null);
            viewHolder.ivIcon=(ImageView)convertView.findViewById(R.id.iv_download_item_icon);
            viewHolder.progressBar=(ProgressBar)convertView.findViewById(R.id.pb_download_item_progress);
            viewHolder.progressBar.setMax(100);
            viewHolder.tvProgress=(TextView)convertView.findViewById(R.id.tv_download_item_size);
            viewHolder.ivCtrl=(ImageView)convertView.findViewById(R.id.iv_download_ctrl_btn);
            viewHolder.tvState=(TextView)convertView.findViewById(R.id.tv_download_state);

            convertView.setTag(viewHolder);
        }else{
            viewHolder=(ViewHolder)convertView.getTag();
        }
        final MuseumBean bean = getItem(position);
        final boolean isDownload= (boolean) Tools.getValue(MyApplication.get(), bean.getId(), false);
        LogUtil.i("ZHANG", "测试数据isDownload----" + isDownload);
        if(isDownload){
            viewHolder.ivCtrl.setVisibility(View.GONE);
            viewHolder.tvProgress.setVisibility(View.INVISIBLE);
            viewHolder.tvState.setVisibility(View.VISIBLE);
            viewHolder.tvState.setText("已下载");
            /*viewHolder.ivIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent=new Intent(context, MuseumHomeActivity.class);
                    intent.putExtra(INTENT_MUSEUM_ID,bean.getId());
                    context.startActivity(intent);
                }
            });*/
        }else{
            viewHolder.tvState.setVisibility(View.GONE);
            viewHolder.ivCtrl.setVisibility(View.VISIBLE);
            viewHolder.ivCtrl.setBackground(context.getResources().getDrawable(R.drawable.uamp_ic_play_arrow_white_24dp));
        }
        String iconPath=bean.getIconUrl();
        ImageLoaderUtil.displayImage(iconPath, viewHolder.ivIcon);
        /*String name= Tools.changePathToName(iconPath);
        String path=LOCAL_ASSETS_PATH+bean.getId()+"/"+LOCAL_FILE_TYPE_IMAGE+"/"+name;
        if(Tools.isFileExist(path)){
            ImageLoaderUtil.displaySdcardImage(context, path, viewHolder.ivIcon);
        }else{
            ImageLoaderUtil.displayNetworkImage(context, BASE_URL + iconPath, viewHolder.ivIcon);
        }*/

        View.OnClickListener ll=new View.OnClickListener() {


            DownloadTask task=null;
            @Override
            public void onClick(View v) {
                if(isDownload){return;}
                if(bean.getmState()==0){
                    task=new DownloadTask(bean.getId());
                    viewHolder.setNewTask(task);
                    task.start();
                    bean.setmState(1);
                    v.setBackground(context.getResources().getDrawable(R.drawable.uamp_ic_pause_white_24dp));
                }else if(bean.getmState()==1){
                    if(task==null){return;}
                    task.pause();
                    v.setBackground(context.getResources().getDrawable(R.drawable.uamp_ic_play_arrow_white_24dp));
                    bean.setmState(2);
                }else if(bean.getmState()==2){
                    if(task==null){return;}
                    task.toContinue();
                    v.setBackground(context.getResources().getDrawable(R.drawable.uamp_ic_pause_white_24dp));
                    bean.setmState(1);
                }
            }
        };
        viewHolder.ivCtrl.setOnClickListener(ll);
        return convertView;
    }


    public class ViewHolder {
        public   ImageView ivIcon,ivCtrl;
        public ProgressBar progressBar;
        public TextView tvProgress,tvState;
        public DownloadTask downloadTask;


        public DownloadTask.TaskListener listener=new DownloadTask.TaskListener() {
            @Override
            public void onProgressChanged(final int progress) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (progress < 100) {
                            progressBar.setProgress(progress);
                            tvProgress.setText("已下载" + progress+"%");
                        } else {
                            progressBar.setVisibility(View.INVISIBLE);
                            tvProgress.setVisibility(View.GONE);
                            ivCtrl.setVisibility(View.GONE);
                            tvState.setVisibility(View.VISIBLE);
                            tvState.setText("已下载");
                        }
                    }
                });
            }
        };

        public void removeListener() {
            if (downloadTask!=null && listener !=null )
                downloadTask.removeListener(listener);
        }
        public void addListener() {
            if (downloadTask!=null )
                downloadTask.addListener(listener);
        }
        public void setNewTask(DownloadTask t) {
            removeListener();
            this.downloadTask=t;
            addListener();
        }

    }
}
