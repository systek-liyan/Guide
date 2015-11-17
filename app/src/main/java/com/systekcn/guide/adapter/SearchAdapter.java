package com.systekcn.guide.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.magic.mapdemo.R;
import com.systekcn.guide.common.IConstants;
import com.systekcn.guide.common.utils.ImageLoaderUtil;
import com.systekcn.guide.common.utils.Tools;
import com.systekcn.guide.entity.ExhibitBean;

import java.util.List;

/**
 * Created by Qiang on 2015/11/11.
 */
public class SearchAdapter extends BaseAdapter implements IConstants{

    private List<ExhibitBean> list;
    private Context context;
    private LayoutInflater inflater;

    public void updateData(List<ExhibitBean> list){
        this.list=list;
        notifyDataSetChanged();
    }

    public SearchAdapter( Context c,List<ExhibitBean> list) {
        this.list = list;
        this.context = c;
        inflater=LayoutInflater.from(context);
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
        ViewHolder viewHolder=null;
        if(convertView==null){
            viewHolder=new ViewHolder();
            convertView =inflater.inflate(R.layout.item_search_exhibit,null);
            viewHolder.iv_icon= (ImageView) convertView.findViewById(R.id.iv_item_search_exhibit_icon);
            viewHolder.name= (TextView) convertView.findViewById(R.id.tv_item_search_name);
            viewHolder.labels= (TextView) convertView.findViewById(R.id.tv_item_search_content);
            convertView.setTag(viewHolder);
        }else{
            viewHolder= (ViewHolder) convertView.getTag();
        }
        ExhibitBean exhibitBean=getItem(position);
        viewHolder.name.setText(exhibitBean.getName());
        viewHolder.labels.setText(exhibitBean.getLabels());
        String iconUrl=exhibitBean.getIconurl();
        String localUrl= Tools.changePathToName(iconUrl);
        if(Tools.isFileExist(localUrl)){
            ImageLoaderUtil.displaySdcardImage(context,localUrl,viewHolder.iv_icon);
        }else{
            ImageLoaderUtil.displayNetworkImage(context,BASEURL+iconUrl , viewHolder.iv_icon);
        }
        return convertView;
    }
    class ViewHolder {
        ImageView iv_icon;
        TextView name,labels;
    }
}
