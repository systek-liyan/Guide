package com.systek.guide.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.systek.guide.IConstants;
import com.systek.guide.R;
import com.systek.guide.biz.DataBiz;
import com.systek.guide.entity.MultiAngleImg;
import com.systek.guide.utils.ImageLoaderUtil;
import com.systek.guide.utils.Tools;

import java.util.List;

/**
 * Created by Qiang on 2015/12/25.
 */
public class MultiAngleImgAdapter extends RecyclerView.Adapter<MultiAngleImgAdapter.ViewHolder> implements IConstants{

    private Context context;
    private List<MultiAngleImg> list;
    private LayoutInflater inflater;

    private OnItemClickListener onItemClickListener;

    public OnItemClickListener getOnItemClickListener() {
        return onItemClickListener;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public MultiAngleImgAdapter(Context c, List<MultiAngleImg> list) {
        this.context = c;
        this.list = list;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_multi_angle_img, parent, false);
        ViewHolder viewHolder = new ViewHolder(view,onItemClickListener);
        viewHolder.ivMultiAngle = (ImageView) view.findViewById(R.id.ivMultiAngle);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        MultiAngleImg multiAngleImg=list.get(position);
        String url = multiAngleImg.getUrl();
        String name = Tools.changePathToName(url);
        String currentMuseumId = (String) DataBiz.getTempValue(context,SP_MUSEUM_ID,"");
        if (currentMuseumId != null) {
            String path = LOCAL_ASSETS_PATH + currentMuseumId + "/" + LOCAL_FILE_TYPE_IMAGE + "/" + name;
            if (Tools.isFileExist(path)) {
                ImageLoaderUtil.displaySdcardImage(context, path, holder.ivMultiAngle);
            } else {
                ImageLoaderUtil.displayNetworkImage(context, BASE_URL + url, holder.ivMultiAngle);
            }
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void updateData(List<MultiAngleImg> list){
        this.list=list;
        notifyDataSetChanged();
    }


    public interface OnItemClickListener
    {
        void onItemClick(View view, int position);
    }



    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        OnItemClickListener onItemClickListener;
        ImageView ivMultiAngle;
        public ViewHolder(View itemView,OnItemClickListener onItemClickListener) {
            super(itemView);
            this.onItemClickListener=onItemClickListener;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if(onItemClickListener!=null){
                onItemClickListener.onItemClick(v,getPosition());
            }
        }
    }
}
