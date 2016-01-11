package com.systekcn.guide.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.systekcn.guide.IConstants;
import com.systekcn.guide.R;
import com.systekcn.guide.entity.ExhibitBean;
import com.systekcn.guide.utils.ImageLoaderUtil;
import com.systekcn.guide.utils.Tools;

import java.util.List;

/**
 * Created by Qiang on 2015/12/10.
 */
public class NearlyGalleryAdapter extends RecyclerView.Adapter<NearlyGalleryAdapter.ViewHolder> implements IConstants {

    private LayoutInflater inflater;
    private List<ExhibitBean> exhibitBeanList;
    private Context context;
    private int selectIndex;


    public NearlyGalleryAdapter(Context c,List<ExhibitBean> exhibitBeans) {
        this.context=c;
        this.exhibitBeanList = exhibitBeans;
        inflater=LayoutInflater.from(context);
    }

    public void updateData(List<ExhibitBean> list){
        this.exhibitBeanList=list;
        notifyDataSetChanged();
    }



    public interface OnItemClickListener
    {
        void onItemClick(View view, int position);
    }

    private OnItemClickListener onItemClickListener;

    public void setOnItemClickLitener(OnItemClickListener onItemClickListener)
    {
        this.onItemClickListener = onItemClickListener;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = inflater.inflate(R.layout.item_nearly_gallery, viewGroup, false);
        ViewHolder viewHolder = new ViewHolder(view);
        viewHolder.mImg = (ImageView) view.findViewById(R.id.iv_item_nearly_exhibit);
        viewHolder.mTxt = (TextView) view.findViewById(R.id.tv_item_nearly_exhibit);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        ExhibitBean exhibitBean=exhibitBeanList.get(position);
        String museumId=exhibitBean.getMuseumId();
        String path=exhibitBean.getIconurl();
        String imageName=path.replaceAll("/", "_");
        String imgLocalUrl = LOCAL_ASSETS_PATH + museumId + "/" + LOCAL_FILE_TYPE_IMAGE +"/"+ imageName;
        if(Tools.isFileExist(imgLocalUrl)){
            ImageLoaderUtil.displaySdcardImage(context, imgLocalUrl, holder.mImg);
        }else{
            ImageLoaderUtil.displayNetworkImage(context, BASE_URL +path,holder.mImg);
        }

        holder.mTxt.setText(exhibitBean.getName());

        if(position == selectIndex){
            holder.itemView.setSelected(true);
        }else{
            holder.itemView.setSelected(false);
        }
        if (onItemClickListener != null)
        {
            holder.itemView.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v) {
                    onItemClickListener.onItemClick(holder.itemView, position);
                }
            });

        }
    }

    @Override
    public int getItemCount() {
        return exhibitBeanList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder
    {
        public ViewHolder(View arg0)
        {
            super(arg0);
        }

        ImageView mImg;
        TextView mTxt;
    }

    public void setSelectIndex(int index){
        selectIndex = index;
    }

}
