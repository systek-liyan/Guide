package com.systekcn.guide.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.systekcn.guide.MyApplication;
import com.systekcn.guide.R;
import com.systekcn.guide.activity.DownloadActivity;
import com.systekcn.guide.activity.MuseumHomePageActivity;
import com.systekcn.guide.biz.BizFactory;
import com.systekcn.guide.biz.DownloadBiz;
import com.systekcn.guide.common.IConstants;
import com.systekcn.guide.common.utils.ImageLoaderUtil;
import com.systekcn.guide.common.utils.LogUtil;
import com.systekcn.guide.common.utils.Tools;
import com.systekcn.guide.entity.MuseumBean;

import java.util.List;

/**
 * Created by Qiang on 2015/11/2.
 */
public class DownloadAdapter extends BaseAdapter implements IConstants{

    Context context;
    List<MuseumBean> list;
    LayoutInflater inflater;
    private final int  DOWNLOAD_STATE_NOT =1;
    private final int  DOWNLOAD_STATE_DOWNLOADING =2;
    private final int  DOWNLOAD_STATE_PAUSE =3;
    private int download_state=DOWNLOAD_STATE_NOT;
    private DownloadProgressListener downloadProgressListener;

    public void setDownloadProgressListener(DownloadProgressListener downloadProgressListener) {
        this.downloadProgressListener = downloadProgressListener;
    }

    public  void updateData(List<MuseumBean> list){
        this.list=list;
        notifyDataSetChanged();
    }

    public DownloadAdapter(DownloadActivity c, List<MuseumBean> list) {
        this.context = c;
        this.list = list;
        inflater= LayoutInflater.from(context);
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

    int count;
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        final ViewHolder viewHolder;
        if(convertView==null){
            viewHolder=new ViewHolder();
            convertView=inflater.inflate(R.layout.item_download,null);
            viewHolder.ivIcon=(ImageView)convertView.findViewById(R.id.iv_download_item_icon);
            viewHolder.progressBar=(ProgressBar)convertView.findViewById(R.id.pb_download_item_progress);
            viewHolder.tvProgress=(TextView)convertView.findViewById(R.id.tv_download_item_size);
            viewHolder.ivCtrl=(ImageView)convertView.findViewById(R.id.iv_download_ctrl_btn);
            viewHolder.tvState=(TextView)convertView.findViewById(R.id.tv_download_state);

            convertView.setTag(viewHolder);
        }else{
            viewHolder=(ViewHolder)convertView.getTag();
        }

        final MuseumBean bean = getItem(position);
        String iconPath=bean.getIconUrl();
        String name=Tools.changePathToName(iconPath);
        String path=SDCARD_ROOT+"/Guide/"+bean.getId()+"/"+LOCAL_FILE_TYPE_IMAGE+"/"+name;
        if(Tools.isFileExist(path)){
            ImageLoaderUtil.displaySdcardImage(context,path,viewHolder.ivIcon);
        }else{
            ImageLoaderUtil.displayNetworkImage(context, BASEURL + iconPath, viewHolder.ivIcon);
        }
        count++;
        LogUtil.i("ZHANG",bean.getId()+">>>>>"+count);
        String isDownload = String.valueOf(Tools.getValue(context, bean.getId(),""));
        LogUtil.i("ZHANG","测试数据isDownload----"+isDownload);
        viewHolder.ivCtrl.setTag(position);
        viewHolder.ivIcon.setTag(bean.getId());
        if(isDownload.equals("true")){
            viewHolder.ivCtrl.setVisibility(View.GONE);
            viewHolder.tvProgress.setVisibility(View.INVISIBLE);
            viewHolder.tvState.setVisibility(View.VISIBLE);
            viewHolder.tvState.setText("已下载");
            viewHolder.ivIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent=new Intent(context, MuseumHomePageActivity.class);
                    intent.putExtra(INTENT_MUSEUM_ID,bean.getId());
                    context.startActivity(intent);
                }
            });
        }else{
            viewHolder.tvState.setVisibility(View.GONE);
            viewHolder.ivCtrl.setVisibility(View.VISIBLE);
            viewHolder.ivCtrl.setBackgroundResource(R.mipmap.btn_download_pause);
            viewHolder.ivCtrl.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = Integer.parseInt(v.getTag().toString());
                    ImageView imageView = (ImageView) v;
                    MuseumBean museum = getItem(position);
                    if (download_state == DOWNLOAD_STATE_NOT) {
                        if(MyApplication.currentNetworkType!=INTERNET_TYPE_NONE){
                            DownloadBiz downloadBiz = (DownloadBiz) BizFactory.getDownloadBiz(context);
                            downloadBiz.download(museum.getId());
                            imageView.setBackgroundResource(R.mipmap.btn_download_downloading);
                            download_state = DOWNLOAD_STATE_DOWNLOADING;
                        }else{
                            Tools.showMessage(context,"当前无网络，请检查网络！");
                        }
                    } else if (download_state == DOWNLOAD_STATE_DOWNLOADING) {
                        Intent intent = new Intent();
                        intent.setAction(ACTION_DOWNLOAD_PAUSE);
                        intent.putExtra(ACTION_DOWNLOAD_PAUSE, museum.getId());
                        context.sendBroadcast(intent);
                        LogUtil.i("ZHANG", "发送了广播DOWNLOAD_STATE_PAUSE");
                        imageView.setBackgroundResource(R.mipmap.btn_download_pause);
                        download_state = DOWNLOAD_STATE_PAUSE;
                    } else if (download_state == DOWNLOAD_STATE_PAUSE) {
                        Intent intent = new Intent();
                        intent.setAction(ACTION_DOWNLOAD_CONTINUE);
                        context.sendBroadcast(intent);
                        LogUtil.i("ZHANG", "发送了广播DOWNLOAD_STATE_DOWNLOADING");
                        download_state = DOWNLOAD_STATE_DOWNLOADING;
                        imageView.setBackgroundResource(R.mipmap.btn_download_downloading);
                    }
                    downloadProgressListener.onProgressChanged(viewHolder);
                }
            });
        }
        return convertView;
    }
    public class ViewHolder {
        public   ImageView ivIcon,ivCtrl;
        public ProgressBar progressBar;
        public TextView tvProgress,tvState;
    }

    public interface DownloadProgressListener {
        void onProgressChanged(ViewHolder viewHolder);
    }
}
