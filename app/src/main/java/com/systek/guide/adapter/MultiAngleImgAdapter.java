package com.systek.guide.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.systek.guide.IConstants;
import com.systek.guide.R;
import com.systek.guide.entity.MultiAngleImg;
import com.systek.guide.utils.ImageUtil;

import java.util.List;

/**
 * Created by Qiang on 2015/12/25.
 *
 * 自定义横向多角度图片的adapter
 */
public class MultiAngleImgAdapter extends RecyclerView.Adapter<MultiAngleImgAdapter.ViewHolder> implements IConstants{

    private Context context;
    private List<MultiAngleImg> list;
    private LayoutInflater inflater;

    private OnItemClickListener onItemClickListener;//点击监听

    public OnItemClickListener getOnItemClickListener() {
        return onItemClickListener;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public MultiAngleImgAdapter(Context c, List<MultiAngleImg> list) {
        this.context = c.getApplicationContext();
        this.list = list;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_multi_angle_img, parent, false);
        //FontManager.applyFont(context, view);
        ViewHolder viewHolder = new ViewHolder(view,onItemClickListener);
        viewHolder.ivMultiAngle = (ImageView) view.findViewById(R.id.ivMultiAngle);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        MultiAngleImg multiAngleImg=list.get(position);
        String url = multiAngleImg.getUrl();
        ImageUtil.displayImage(url, holder.ivMultiAngle,true,false);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    /**
     * 更新列表
     * @param list MultiAngleImg 集合
     */
    public void updateData(List<MultiAngleImg> list){
        this.list=list;
        notifyDataSetChanged();
    }

    /**
     * 内部接口，用于点击事件
     */
    public interface OnItemClickListener
    {
        void onItemClick(View view, int position);
    }



    static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
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
