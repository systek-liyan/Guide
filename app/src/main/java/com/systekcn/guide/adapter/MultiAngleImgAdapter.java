package com.systekcn.guide.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.systekcn.guide.MyApplication;
import com.systekcn.guide.R;
import com.systekcn.guide.common.IConstants;
import com.systekcn.guide.common.utils.ImageLoaderUtil;
import com.systekcn.guide.common.utils.Tools;
import com.systekcn.guide.entity.MultiAngleImg;

import java.util.List;

/**
 * Created by Qiang on 2015/12/25.
 */
public class MultiAngleImgAdapter extends RecyclerView.Adapter<MultiAngleImgAdapter.ViewHolder> implements IConstants{

    private Context context;
    private List<MultiAngleImg> list;
    private LayoutInflater inflater;


    public MultiAngleImgAdapter(Context c, List<MultiAngleImg> list) {
        this.context = c;
        this.list = list;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_multi_angle_img, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        viewHolder.ivMultiAngle = (ImageView) view.findViewById(R.id.ivMultiAngle);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        MultiAngleImg multiAngleImg=list.get(position);
        String url = multiAngleImg.getUrl();
        String name = Tools.changePathToName(url);
        MyApplication application = MyApplication.get();
        String currentMuseumId = application.getCurrentMuseumId();
        if (currentMuseumId != null) {
            String path = SDCARD_ROOT + "/Guide/" + currentMuseumId + "/" + LOCAL_FILE_TYPE_IMAGE + "/" + name;
            if (Tools.isFileExist(path)) {
                ImageLoaderUtil.displaySdcardImage(context, path, holder.ivMultiAngle);
            } else {
                ImageLoaderUtil.displayNetworkImage(context, BASEURL + url, holder.ivMultiAngle);
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

    private OnItemClickListener onItemClickListener;

    public void setOnItemClickLitener(OnItemClickListener onItemClickListener)
    {
        this.onItemClickListener = onItemClickListener;
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        ImageView ivMultiAngle;
        public ViewHolder(View itemView) {
            super(itemView);
        }
    }
}
