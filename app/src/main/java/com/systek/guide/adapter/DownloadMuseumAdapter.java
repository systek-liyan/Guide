package com.systek.guide.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.systek.guide.IConstants;
import com.systek.guide.R;
import com.systek.guide.entity.MuseumBean;
import com.systek.guide.utils.ImageLoaderUtil;

import java.util.List;

/**
 * Created by Qiang on 2015/11/2.
 *
 */
public class DownloadMuseumAdapter extends BaseAdapter implements IConstants {

    private Context context;
    private List<MuseumBean> list;
    private LayoutInflater inflater;

    public  void updateData(List<MuseumBean> list){
        this.list=list;
        notifyDataSetChanged();
    }

    public void onDestroy(){
    }

    public DownloadMuseumAdapter(Context c, List<MuseumBean> list) {
        this.context = c.getApplicationContext();
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
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        final ViewHolder viewHolder;
        if(convertView==null){
            viewHolder=new ViewHolder();
            convertView=inflater.inflate(R.layout.item_download_museum,null);
            viewHolder.ivIcon=(ImageView)convertView.findViewById(R.id.museumIcon);
            viewHolder.progressBar=(ProgressBar)convertView.findViewById(R.id.downloadProgressBar);
            viewHolder.tvProgress=(TextView)convertView.findViewById(R.id.currentProgress);
            viewHolder.ivCtrl=(ImageView)convertView.findViewById(R.id.downloadBtn);
            convertView.setTag(viewHolder);
        }else{
            viewHolder=(ViewHolder)convertView.getTag();
        }
        final MuseumBean bean = getItem(position);
        int state=bean.getmState();
        if(state==1){
            viewHolder.ivCtrl.setImageDrawable(context.getDrawable(R.drawable.download_start));
        }else if(state==2){
            viewHolder.ivCtrl.setImageDrawable(context.getDrawable(R.drawable.download_start));
        }

        /*if(isDownload){
            viewHolder.ivCtrl.setVisibility(View.GONE);
            viewHolder.tvProgress.setVisibility(View.INVISIBLE);
        }else{
            viewHolder.ivCtrl.setVisibility(View.VISIBLE);
            viewHolder.ivCtrl.setBackground(context.getResources().getDrawable(R.drawable.uamp_ic_play_arrow_white_24dp));
        }*/
        String iconPath=bean.getIconUrl();
        ImageLoaderUtil.displayImage(iconPath, viewHolder.ivIcon);

        View.OnClickListener ll=new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if(bean.getmState()==0){
                    bean.setmState(1);
                    v.setBackground(context.getResources().getDrawable(R.drawable.uamp_ic_pause_white_24dp));
                }else if(bean.getmState()==1){
                    v.setBackground(context.getResources().getDrawable(R.drawable.uamp_ic_play_arrow_white_24dp));
                }else if(bean.getmState()==2){
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
        public TextView tvProgress;
    }
}
