package com.systek.guide.adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.AnimationDrawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.systek.guide.R;
import com.systek.guide.biz.DataBiz;
import com.systek.guide.entity.ExhibitBean;
import com.systek.guide.service.PlayManager;
import com.systek.guide.utils.ExceptionUtil;
import com.systek.guide.utils.ImageUtil;

import java.util.List;

/**
 * Created by Qiang on 2016/7/13.
 */
public class ExhibitRecycleAdapter extends RecyclerView.Adapter<ExhibitRecycleAdapter.ViewHolder> {

    private Context context;
    private List<ExhibitBean> list;
    private LayoutInflater inflater;
    private int state;
    public static final int STATE_NONE = 0;
    public static final int STATE_PLAYABLE = 1;
    public static final int STATE_PAUSED = 2;
    public static final int STATE_PLAYING = 3;
    private ExhibitBean selectExhibit;
    private static ColorStateList sColorStatePlaying;
    private static ColorStateList sColorStateNotPlaying;

    private OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }
    public void setSelectExhibit(ExhibitBean selectExhibit) {
        this.selectExhibit = selectExhibit;
    }

    public ExhibitBean getItem(int position){
        return list==null?null:list.get(position);
    }

    public ExhibitRecycleAdapter(Context context,List<ExhibitBean> list){
        this.context=context.getApplicationContext();
        this.list=list;
        inflater= LayoutInflater.from(this.context);
        //initializeColorStateLists(this.context);
    }

    public void updateData(List<ExhibitBean>  exhibits){
        this.list=exhibits;
        notifyDataSetChanged();
    }


    private static void initializeColorStateLists(Context context) {
        sColorStateNotPlaying = ColorStateList.valueOf(context.getResources().getColor(
                R.color.media_item_icon_not_playing));
        sColorStatePlaying = ColorStateList.valueOf(context.getResources().getColor(
                R.color.media_item_icon_playing));
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view=inflater.inflate(R.layout.item_list_exhibit,parent,false);
        ViewHolder holder=new ViewHolder(view,onItemClickListener);
        holder.tvExhibitName = (TextView) view.findViewById(R.id.tvExhibitName);
        holder.tvExhibitYears = (TextView) view.findViewById(R.id.tvExhibitYears);
        holder.tvExhibitPosition = (TextView) view.findViewById(R.id.tvExhibitPosition);
        holder.ivExhibitIcon = (ImageView) view.findViewById(R.id.ivExhibitIcon);
        holder.tvExhibitDistance = (TextView) view.findViewById(R.id.tvExhibitDistance);
        holder.llCollectionBtn = (LinearLayout) view.findViewById(R.id.llCollectionBtn);
        holder.ivCollection = (ImageView) view.findViewById(R.id.ivCollection);
        holder.tvExhibitNumber = (TextView) view.findViewById(R.id.tvExhibitNumber);
        holder.ivPlayAnim = (ImageView) view.findViewById(R.id.ivPlayAnim);
        return holder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final ExhibitBean exhibit=list.get(position);
        holder.tvExhibitName.setText(exhibit.getName());
        holder.tvExhibitYears.setText(exhibit.getLabels());
        holder.tvExhibitNumber.setText(exhibit.getNumber());

        String distance=String.valueOf(exhibit.getDistance());
        if(distance.length()>6){
            distance=distance.substring(0,6);
        }
        holder.tvExhibitDistance.setText(distance);
        if(exhibit.isSaveForPerson()){
            holder.ivCollection.setImageDrawable(context.getResources().getDrawable(R.drawable.iv_heart_full));
        }else{
            holder.ivCollection.setImageDrawable(context.getResources().getDrawable(R.drawable.iv_heart_empty));
        }
         holder.llCollectionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if(exhibit.isSaveForPerson()){
                        exhibit.setSaveForPerson(false);
                        holder.ivCollection.setImageDrawable(context.getResources().getDrawable(R.drawable.iv_heart_empty));
                    }else{
                        exhibit.setSaveForPerson(true);
                        holder.ivCollection.setImageDrawable(context.getResources().getDrawable(R.drawable.iv_heart_full));
                    }
                    DataBiz.saveOrUpdate(exhibit);
                } catch (Exception e) {
                    ExceptionUtil.handleException(e);
                }
            }
        });


        // 显示图片
        String iconUrl = exhibit.getIconurl();
        holder.ivExhibitIcon.setImageDrawable(context.getResources().getDrawable(R.drawable.emotionstore_progresscancelbtn));
        ImageUtil.displayImage(iconUrl, holder.ivExhibitIcon,true,false);

        ExhibitBean currentExhibit= PlayManager.getInstance().getCurrentExhibit();
        if(currentExhibit!=null){
            setSelectExhibit(currentExhibit);
        }
        boolean isPlaying=PlayManager.getInstance().isPlaying();
        if(isPlaying){
            state= STATE_PLAYING;
        }
        if (currentExhibit!=null&&exhibit.equals(currentExhibit)){
            switch (state) {
                case STATE_PLAYABLE:
                    holder.ivPlayAnim.setImageDrawable(
                            context.getResources().getDrawable(R.drawable.uamp_ic_play_arrow_white_24dp));
                    holder.ivPlayAnim.setVisibility(View.VISIBLE);
                    break;
                case STATE_PLAYING:
                    AnimationDrawable animation = (AnimationDrawable)
                            context.getResources().getDrawable(R.drawable.ic_equalizer_white_36dp);
                    holder.ivPlayAnim.setImageDrawable(animation);
                    holder.ivPlayAnim.setVisibility(View.VISIBLE);
                    if (animation != null) animation.start();
                    break;
                case STATE_PAUSED:
                    holder.ivPlayAnim.setImageDrawable(
                            context.getResources().getDrawable(R.drawable.ic_equalizer1_white_36dp));
                    holder.ivPlayAnim.setVisibility(View.VISIBLE);
                    break;
                default:
                    holder.ivPlayAnim.setVisibility(View.GONE);
            }

        }else{
            holder.ivPlayAnim.setVisibility(View.GONE);
        }

    }

    @Override
    public int getItemCount() {
        return list.size();
    }


    static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        TextView tvExhibitName, tvExhibitYears, tvExhibitPosition,tvExhibitDistance,tvExhibitNumber;
        ImageView ivExhibitIcon;
        LinearLayout llCollectionBtn;
        ImageView ivCollection;
        ImageView ivPlayAnim;
        OnItemClickListener onItemClickListener;

        public ViewHolder(View itemView,OnItemClickListener itemClickListener) {
            super(itemView);
            this.onItemClickListener=itemClickListener;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if(onItemClickListener!=null){
                onItemClickListener.onItemClick(v,getLayoutPosition());
            }
        }
    }

    /**
     * 内部接口，用于点击事件
     */
    public interface OnItemClickListener
    {
        void onItemClick(View view, int position);
    }

}
