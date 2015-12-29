package com.systekcn.guide.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.systekcn.guide.R;
import com.systekcn.guide.common.IConstants;
import com.systekcn.guide.common.utils.ImageLoaderUtil;
import com.systekcn.guide.custom.RoundImageView;
import com.systekcn.guide.entity.ExhibitBean;

import java.io.File;
import java.util.List;

/**
 * Created by Qiang on 2015/10/27.
 */
public class NearlyExhibitAdapter extends BaseAdapter implements IConstants{

    private Context context;
    private List<ExhibitBean> list;
    private LayoutInflater inflater;

    public NearlyExhibitAdapter(Context context, List<ExhibitBean> list) {
        super();
        this.context = context;
        this.list = list;
        inflater=LayoutInflater.from(context);
    }
    public void updateData(List<ExhibitBean> list){
        this.list=list;
        notifyDataSetChanged();
    }

    @Override
    public boolean isEnabled(int position) {
        return true;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public ExhibitBean getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        if (convertView == null||convertView.getTag() == null) {
            convertView = inflater.inflate( R.layout.item_list_exhibit, null);
            viewHolder = new ViewHolder();

            viewHolder.tv_exhibit_name = (TextView) convertView.findViewById(R.id.tv_exhibit_name);
            viewHolder.tv_exhibit_years = (TextView) convertView.findViewById(R.id.tv_exhibit_years);
            viewHolder.tv_exhibit_position = (TextView) convertView.findViewById(R.id.tv_exhibit_position);
            viewHolder.iv_exhibit_icon = (RoundImageView) convertView.findViewById(R.id.iv_exhibit_icon);
            viewHolder.tvExhibitDistance = (TextView) convertView.findViewById(R.id.tvExhibitDistance);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        // 取数据
        ExhibitBean exhibitBean = list.get(position);
        viewHolder.tv_exhibit_name.setText(exhibitBean.getName());
        viewHolder.tv_exhibit_years.setText(exhibitBean.getLabels());
        viewHolder.tv_exhibit_position.setText(exhibitBean.getAddress());
        String distance=String.valueOf(exhibitBean.getDistance());
        if(distance.length()>6){
            distance=distance.substring(0,6);
        }
        viewHolder.tvExhibitDistance.setText(distance);

        // 显示图片
        String iconUrl = exhibitBean.getIconurl();

        //每个博物馆的资源以ID为目录
        String museumId = exhibitBean.getMuseumId();

        String imageName = iconUrl.replaceAll("/", "_");
        String imgLocalUrl = LOCAL_ASSETS_PATH+museumId + "/" + LOCAL_FILE_TYPE_IMAGE+"/"+imageName;
        File file = new File(imgLocalUrl);
        // 判断sdcard上有没有图片
        if (file.exists()) {
            // 显示sdcard
            ImageLoaderUtil.displaySdcardImage(context, imgLocalUrl, viewHolder.iv_exhibit_icon);
        } else {
            // 服务器上存的imageUrl有域名如http://www.systek.com.cn/1.png
            iconUrl = BASEURL+ iconUrl;
            ImageLoaderUtil.displayNetworkImage(context, iconUrl, viewHolder.iv_exhibit_icon);
        }
        return convertView;
    }


    class ViewHolder{
        TextView tv_exhibit_name,tv_exhibit_years,tv_exhibit_position,tvExhibitDistance;
        RoundImageView iv_exhibit_icon;
    }
}
