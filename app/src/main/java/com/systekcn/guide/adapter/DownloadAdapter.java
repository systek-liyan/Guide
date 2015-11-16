package com.systekcn.guide.adapter;

import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.systekcn.guide.R;
import com.systekcn.guide.activity.DownloadActivity;
import com.systekcn.guide.service.MuseumDownloadService;
import com.systekcn.guide.common.IConstants;
import com.systekcn.guide.common.utils.ImageLoaderUtil;
import com.systekcn.guide.common.utils.Tools;
import com.systekcn.guide.entity.MuseumBean;

import java.util.List;

/**
 * Created by Qiang on 2015/11/2.
 */
public class DownloadAdapter extends BaseAdapter implements IConstants{

    DownloadActivity activity;
    List<MuseumBean> list;
    LayoutInflater inflater;
    private DownloadProgressListener downloadProgressListener;

    public void setDownloadProgressListener(DownloadProgressListener downloadProgressListener) {
        this.downloadProgressListener = downloadProgressListener;
    }

   public  void updateData(List<MuseumBean> list){
        this.list=list;
       notifyDataSetChanged();
    }

    public DownloadAdapter(DownloadActivity c, List<MuseumBean> list) {
        this.activity = c;
        this.list = list;
        inflater= LayoutInflater.from(activity);
        setDownloadProgressListener(activity);
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
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder viewHolder=null;
        if(convertView==null){
            viewHolder=new ViewHolder();
            convertView=inflater.inflate(R.layout.item_download,null);
            viewHolder.ivIcon=(ImageView)convertView.findViewById(R.id.iv_download_item_icon);
            viewHolder.progressBar=(ProgressBar)convertView.findViewById(R.id.pb_download_item_progress);
            viewHolder.tvProgress=(TextView)convertView.findViewById(R.id.tv_download_item_size);
            viewHolder.ivCtrl=(ImageView)convertView.findViewById(R.id.iv_download_ctrl_btn);
            convertView.setTag(viewHolder);
        }else{
            viewHolder=(ViewHolder)convertView.getTag();
        }
        final MuseumBean bean = getItem(position);
        String iconPath=bean.getIconUrl();
        String name=Tools.changePathToName(iconPath);
        String path=SDCARD_ROOT+"/Guide/"+bean.getId()+"/"+LOCAL_FILE_TYPE_IMAGE+"/"+name;
        if(Tools.isFileExist(path)){
            ImageLoaderUtil.displaySdcardImage(activity,path,viewHolder.ivIcon);
        }else{
            ImageLoaderUtil.displayNetworkImage(activity, BASEURL + iconPath, viewHolder.ivIcon);
        }
        SharedPreferences settings = activity.getSharedPreferences(bean.getId(), 0);
        boolean isDownload = settings.getBoolean(HAS_DOWNLOAD, false);
        if(isDownload){
            viewHolder.ivCtrl.setVisibility(View.GONE);
            viewHolder.tvProgress.setVisibility(View.INVISIBLE);
        }
        final ViewHolder tempViewHolder=viewHolder;
        viewHolder.ivCtrl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activity, MuseumDownloadService.class);
                intent.putExtra(INTENT_EXHIBIT_ID, bean.getId());
                activity.startService(intent);
                tempViewHolder.ivCtrl.setTag(bean.getId());
                tempViewHolder.ivCtrl.setBackgroundResource(R.mipmap.btn_download_pause);
                downloadProgressListener.onProgressChanged(tempViewHolder.progressBar, tempViewHolder.tvProgress, tempViewHolder.ivCtrl);
            }
        });
        return convertView;
    }

    class ViewHolder {
        ImageView ivIcon,ivCtrl;
        ProgressBar progressBar;
        TextView tvProgress;

    }
}
