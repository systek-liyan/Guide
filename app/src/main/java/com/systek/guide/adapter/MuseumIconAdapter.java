package com.systek.guide.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.systek.guide.IConstants;
import com.systek.guide.R;
import com.systek.guide.utils.ImageLoaderUtil;

import java.util.List;

/**
 * Created by Qiang on 2016/3/16.
 */
public class MuseumIconAdapter extends RecyclerView.Adapter<MuseumIconAdapter.ViewHolder> implements IConstants {

    private Context context;
    private List<String> list;
    private LayoutInflater inflater;

    public MuseumIconAdapter(Context context, List<String> list) {
        this.context = context.getApplicationContext();
        this.list = list;
        inflater=LayoutInflater.from(this.context);
    }

    public void updateData(List<String> list){
        this.list=list;
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_image, parent, false);
        //FontManager.applyFont(context, view);
        ViewHolder viewHolder = new ViewHolder(view);
        viewHolder.imageView = (ImageView) view.findViewById(R.id.imageView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String  url=list.get(position);
        ImageLoaderUtil.displayImage(url, holder.imageView);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public ViewHolder(View itemView) {
            super(itemView);
        }
    }

}
