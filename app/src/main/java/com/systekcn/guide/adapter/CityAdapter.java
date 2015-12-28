package com.systekcn.guide.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.systekcn.guide.R;
import com.systekcn.guide.entity.CityBean;

import java.util.List;

/**
 * Created by Qiang on 2015/11/26.
 */
public class CityAdapter extends BaseAdapter implements SectionIndexer {
    private List<CityBean> cityList;
    private Context context;
    private LayoutInflater inflater;


    public CityAdapter(Context c,List<CityBean> cityList) {
        super();
        this.cityList = cityList;
        this.context = c;
        inflater=LayoutInflater.from(context);
    }

    /**
     * 当ListView数据发生变化时,调用此方法来更新ListView
     * @param list
     */
    public void updateListView(List<CityBean> list){
        this.cityList = list;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return cityList.size();
    }

    @Override
    public Object getItem(int position) {
        return cityList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        final CityBean city  = cityList.get(position);
        ViewHolder viewHolder=null;
        if(convertView==null){
            convertView=inflater.inflate(R.layout.item_city, null);
            viewHolder=new ViewHolder();
            viewHolder.alpha=(TextView)convertView.findViewById(R.id.itemCityAlpha);
            viewHolder.name=(TextView)convertView.findViewById(R.id.itemCityName);
            convertView.setTag(viewHolder);
        }else{
            viewHolder=(ViewHolder)convertView.getTag();
        }

        //根据position获取分类的首字母的char ascii值
        int section = getSectionForPosition(position);

        //如果当前位置等于该分类首字母的Char的位置 ，则认为是第一次出现
        if(position == getPositionForSection(section)){
            viewHolder.alpha.setVisibility(View.VISIBLE);
            viewHolder.alpha.setText(city.getAlpha());
            viewHolder.alpha.setTextSize(13f);
        }else{
            viewHolder.alpha.setVisibility(View.GONE);
        }

        viewHolder.name.setText(this.cityList.get(position).getName());

        return convertView;
    }
    class ViewHolder{
        TextView alpha,name;
    }

    @Override
    public Object[] getSections() {
        return null;
    }

    /**
     * 根据分类的首字母的Char ascii值获取其第一次出现该首字母的位置
     */
    @Override
    public int getPositionForSection(int sectionIndex) {
        for (int i = 0; i < getCount(); i++) {
            String sortStr = cityList.get(i).getAlpha();
            char firstChar = sortStr.toUpperCase().charAt(0);
            if (firstChar == sectionIndex) {
                return i;
            }
        }
        return -1;
    }


    /**
     * 根据ListView的当前位置获取分类的首字母的char ascii值
     */
    @Override
    public int getSectionForPosition(int position) {
        return cityList.get(position).getAlpha().charAt(0);
    }
}
