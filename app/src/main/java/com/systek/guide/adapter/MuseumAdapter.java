package com.systek.guide.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.liulishuo.filedownloader.model.FileDownloadStatus;
import com.systek.guide.IConstants;
import com.systek.guide.MyApplication;
import com.systek.guide.R;
import com.systek.guide.activity.BaseActivity;
import com.systek.guide.activity.DownloadManagerActivity;
import com.systek.guide.entity.MuseumBean;
import com.systek.guide.utils.ImageUtil;
import com.systek.guide.utils.Tools;

import java.util.List;

/**
 * Created by Qiang on 2015/10/23.
 *
 * 博物馆列表adapter
 */
public class MuseumAdapter extends BaseAdapter implements IConstants {

    private List<MuseumBean> museumList;
    private Context context;

    public MuseumAdapter(Context c,List<MuseumBean> museumList) {
        this.museumList = museumList;
        this.context = c;
    }

    /**
     * 更新列表，刷新视图
     * @param museumList
     */
    public void updateData(List<MuseumBean> museumList) {
        this.museumList = museumList;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return museumList.size();
    }

    @Override
    public MuseumBean getItem(int position) {
        return museumList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        if (convertView == null || convertView.getTag() == null) {
            int theme= (int) Tools.getValue(context, BaseActivity.THEME,R.style.AppTheme);
            switch (theme){
                case R.style.AppTheme:
                    convertView = LinearLayout.inflate(context.getApplicationContext(),R.layout.item_museum, null);
                    break;
                case R.style.BlueAppTheme:
                    convertView = LinearLayout.inflate(context.getApplicationContext(),R.layout.item_museum_blue, null);
                    break;
                default:convertView = LinearLayout.inflate(context.getApplicationContext(),R.layout.item_museum, null);
            }
            //FontManager.applyFont(context, convertView);
            viewHolder = new ViewHolder();
            viewHolder.museumName = (TextView) convertView.findViewById(R.id.museumName);
            viewHolder.museumAddress = (TextView) convertView.findViewById(R.id.museumAddress);
            viewHolder.museumListOpenTime = (TextView) convertView.findViewById(R.id.museumListOpenTime);
            viewHolder.museumListIcon = (ImageView) convertView.findViewById(R.id.museumListIcon);
            viewHolder.museumFlagIsDownload = (TextView) convertView.findViewById(R.id.museumFlagIsDownload);
            viewHolder.museumImportantAlert = (TextView) convertView.findViewById(R.id.museumImportantAlert);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.museumImportantAlert.setVisibility(View.INVISIBLE);

        // 取数据
        final MuseumBean museumBean = museumList.get(position);
        viewHolder.museumName.setText(museumBean.getName());
        int state=museumBean.getmState();
        String downloadText=null;
        boolean isDownload=(boolean) Tools.getValue(MyApplication.get(), museumBean.getId(), false);
        if(state== FileDownloadStatus.completed||isDownload){
            downloadText="已下载";
            viewHolder.museumFlagIsDownload.setClickable(false);
        }else{
            if(state==0){
                downloadText="去下载";
                viewHolder.museumFlagIsDownload.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        museumBean.setmState(FileDownloadStatus.paused);
                        Intent intent=new Intent(context, DownloadManagerActivity.class);
                        intent.putExtra(INTENT_MUSEUM,museumBean);
                        context.startActivity(intent);
                    }
                });
            }else{
                downloadText="下载中";
                viewHolder.museumFlagIsDownload.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        museumBean.setmState(FileDownloadStatus.paused);
                        Intent intent=new Intent(context, DownloadManagerActivity.class);
                        //intent.putExtra(INTENT_MUSEUM,museumBean);
                        context.startActivity(intent);
                    }
                });
            }
        }
        viewHolder.museumFlagIsDownload.setText(downloadText);
        String address=museumBean.getAddress();
        if(address.length()>10){
            address=address.substring(0,10)+"...";
        }
        viewHolder.museumAddress.setText(address);
        String openTime=museumBean.getOpentime();
        if(openTime.length()>10){
            openTime=openTime.substring(0,10)+"...";
        }
        viewHolder.museumListOpenTime.setText(openTime);
        // 显示图片
        String imageUrl = museumBean.getIconUrl();
        //每个博物馆的资源以ID为目录
        String museumId = museumBean.getId();
        ImageUtil.displayImage(imageUrl, viewHolder.museumListIcon, museumId,true,false);
        return convertView;
    }

    class ViewHolder {
        TextView museumName, museumAddress, museumListOpenTime, museumImportantAlert, museumFlagIsDownload;
        ImageView museumListIcon;
    }


}
