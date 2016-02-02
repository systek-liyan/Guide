package com.systek.guide.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.systek.guide.IConstants;
import com.systek.guide.R;
import com.systek.guide.custom.RoundImageView;
import com.systek.guide.entity.MuseumBean;
import com.systek.guide.utils.ImageLoaderUtil;

import java.io.File;
import java.util.List;

/**
 * Created by Qiang on 2015/10/23.
 *
 * 博物馆列表adapter
 */
public class MuseumAdapter extends BaseAdapter implements IConstants {

    private List<MuseumBean> museumList;
    private Context context;
    private LayoutInflater inflater;

    public MuseumAdapter(Context context,List<MuseumBean> museumList) {
        this.museumList = museumList;
        this.context = context;
        inflater = LayoutInflater.from(context);
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
            convertView = inflater.inflate(R.layout.item_museum, null);
            viewHolder = new ViewHolder();
            viewHolder.museumName = (TextView) convertView.findViewById(R.id.museumName);
            viewHolder.museumAddress = (TextView) convertView.findViewById(R.id.museumAddress);
            viewHolder.museumListOpenTime = (TextView) convertView.findViewById(R.id.museumListOpenTime);
            viewHolder.museumListIcon = (RoundImageView) convertView.findViewById(R.id.museumListIcon);
            viewHolder.museumFlagIsDownload = (TextView) convertView.findViewById(R.id.museumFlagIsDownload);
            viewHolder.museumImportantAlert = (TextView) convertView.findViewById(R.id.museumImportantAlert);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.museumImportantAlert.setVisibility(View.INVISIBLE);

        // 取数据
        MuseumBean museumBean = museumList.get(position);
        viewHolder.museumName.setText(museumBean.getName());
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
        // 判断sdcard上有没有图片
        String imageName = imageUrl.replaceAll("/", "_");
        String imgLocalUrl = LOCAL_ASSETS_PATH + museumId + "/" + LOCAL_FILE_TYPE_IMAGE +"/"+ imageName;
        File file = new File(imgLocalUrl);
        if (file.exists()) {
            // 显示sdcard
            ImageLoaderUtil.displaySdcardImage(context, imgLocalUrl, viewHolder.museumListIcon);
        } else {
            // 服务器上存的imageUrl有域名如http://www.systek.com.cn/1.png
            imageUrl = BASE_URL + imageUrl;
            ImageLoaderUtil.displayNetworkImage(context, imageUrl, viewHolder.museumListIcon);
        }
        return convertView;
    }

    class ViewHolder {
        TextView museumName, museumAddress, museumListOpenTime, museumImportantAlert, museumFlagIsDownload;
        RoundImageView museumListIcon;
    }


}
