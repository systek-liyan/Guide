package com.systekcn.guide.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.systekcn.guide.R;
import com.systekcn.guide.common.IConstants;
import com.systekcn.guide.common.utils.ImageLoaderUtil;
import com.systekcn.guide.entity.MuseumBean;

import java.io.File;
import java.util.List;

/**
 * Created by Qiang on 2015/10/23.
 */
public class MuseumAdapter extends BaseAdapter implements IConstants{

    private List<MuseumBean> museumList;
    private Context context;
    private LayoutInflater inflater;

    public MuseumAdapter(List<MuseumBean> museumList, Context context) {
        super();
        this.museumList = museumList;
        this.context = context;
        inflater = LayoutInflater.from(context);
    }

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
            convertView = inflater.inflate(R.layout.item_museum, null);
            viewHolder = new ViewHolder();
            viewHolder.museum_name = (TextView) convertView.findViewById(R.id.museum_name);
            viewHolder.museum_address = (TextView) convertView.findViewById(R.id.museum_address);
            viewHolder.museum_list_openTime = (TextView) convertView.findViewById(R.id.museum_list_openTime);
            viewHolder.museum_list_icon = (ImageView) convertView.findViewById(R.id.museum_list_icon);
            viewHolder.museum_flag_isDownload = (TextView) convertView.findViewById(R.id.museum_flag_isDownload);
            viewHolder.museum_important_alert = (TextView) convertView.findViewById(R.id.museum_important_alert);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.museum_important_alert.setVisibility(View.INVISIBLE);
        // 取数据
        MuseumBean museumModel = museumList.get(position);
        viewHolder.museum_name.setText(museumModel.getName());
        viewHolder.museum_address.setText(museumModel.getAddress());
        viewHolder.museum_list_openTime.setText(museumModel.getOpentime());
        // 显示图片
        String imageUrl = museumModel.getIconUrl();
        //每个博物馆的资源以ID为目录
        String museumId = museumModel.getId();
        // 判断sdcard上有没有图片
        String imageName = imageUrl.replaceAll("/", "_");
        String imgLocalUrl = LOCAL_ASSETS_PATH + museumId + "/" + LOCAL_FILE_TYPE_IMAGE +"/"+ imageName;
        File file = new File(imgLocalUrl);
        if (file.exists()) {
            // 显示sdcard
            ImageLoaderUtil.displaySdcardImage(context, imgLocalUrl, viewHolder.museum_list_icon);
        } else {
            // 服务器上存的imageUrl有域名如http://www.systek.com.cn/1.png
            imageUrl = BASEURL + imageUrl;
            ImageLoaderUtil.displayNetworkImage(context, imageUrl, viewHolder.museum_list_icon);
        }
        return convertView;
    }

    class ViewHolder {
        TextView museum_name, museum_address, museum_list_openTime, museum_important_alert, museum_flag_isDownload;
        ImageView museum_list_icon;
    }


}
