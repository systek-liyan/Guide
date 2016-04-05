package com.systek.guide.adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.systek.guide.IConstants;
import com.systek.guide.R;
import com.systek.guide.biz.DataBiz;
import com.systek.guide.entity.ExhibitBean;
import com.systek.guide.utils.ExceptionUtil;
import com.systek.guide.utils.ImageUtil;

import java.util.List;

/**
 * Created by Qiang on 2015/10/27.
 */
public class ExhibitAdapter extends BaseAdapter implements IConstants {

    private Context context;
    private List<ExhibitBean> list;
    private LayoutInflater inflater;
    private int state;
    private ExhibitBean selectIndex;

    private int  selectItem=-1;

    static final int STATE_INVALID = -1;

    public static final int STATE_NONE = 0;
    public static final int STATE_PLAYABLE = 1;
    public static final int STATE_PAUSED = 2;
    public static final int STATE_PLAYING = 3;
    private static ColorStateList sColorStatePlaying;


    private static ColorStateList sColorStateNotPlaying;

    public void setState(int item ,int state) {
        this.selectItem = item;
        this.state = state;
    }

    public ExhibitAdapter(Context context, List<ExhibitBean> list) {
        this.context = context.getApplicationContext();
        this.list = list;
        inflater=LayoutInflater.from(context);
    }

    public void updateData(List<ExhibitBean> list){
        this.list=list;
        notifyDataSetChanged();
    }
    public  void setSelectItem(int selectItem) {
        this.selectItem = selectItem;
        selectIndex=list.get(selectItem);
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
    public View getView(final int position, View convertView, ViewGroup parent) {

        if (sColorStateNotPlaying == null || sColorStatePlaying == null) {
            initializeColorStateLists(context);
        }
        //Integer cachedState = STATE_INVALID;
        ViewHolder viewHolder = null;

        if (convertView == null||convertView.getTag() == null) {
            convertView = inflater.inflate( R.layout.item_list_exhibit, null);
            //FontManager.applyFont(context, convertView);
            viewHolder = new ViewHolder();
            viewHolder.tvExhibitName = (TextView) convertView.findViewById(R.id.tvExhibitName);
            viewHolder.tvExhibitYears = (TextView) convertView.findViewById(R.id.tvExhibitYears);
            viewHolder.tvExhibitPosition = (TextView) convertView.findViewById(R.id.tvExhibitPosition);
            viewHolder.ivExhibitIcon = (ImageView) convertView.findViewById(R.id.ivExhibitIcon);
            viewHolder.tvExhibitDistance = (TextView) convertView.findViewById(R.id.tvExhibitDistance);
            viewHolder.llCollectionBtn = (LinearLayout) convertView.findViewById(R.id.llCollectionBtn);
            viewHolder.ivCollection = (ImageView) convertView.findViewById(R.id.ivCollection);
            viewHolder.tvExhibitNumber = (TextView) convertView.findViewById(R.id.tvExhibitNumber);
            viewHolder.ivPlayAnim = (ImageView) convertView.findViewById(R.id.ivPlayAnim);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }


        // 取数据
        final ExhibitBean exhibitBean = list.get(position);
        viewHolder.tvExhibitName.setText(exhibitBean.getName());
        viewHolder.tvExhibitYears.setText(exhibitBean.getLabels());
        viewHolder.tvExhibitNumber.setText(exhibitBean.getNumber());
        //String beaconId=exhibitBean.getBeaconId();
        //if(!TextUtils.isEmpty(beaconId)){
           // String disId=beaconId.substring(beaconId.length()-6,beaconId.length());
            //viewHolder.tvExhibitPosition.setText(disId);// TODO: 2015/12/30 改为展厅
        //}
        String distance=String.valueOf(exhibitBean.getDistance());
        if(distance.length()>6){
            distance=distance.substring(0,6);
        }
        viewHolder.tvExhibitDistance.setText(distance);
        if(exhibitBean.isSaveForPerson()){
            viewHolder.ivCollection.setImageDrawable(context.getResources().getDrawable(R.drawable.iv_heart_full));
        }else{
            viewHolder.ivCollection.setImageDrawable(context.getResources().getDrawable(R.drawable.iv_heart_empty));
        }
        final ViewHolder finalViewHolder = viewHolder;
        viewHolder.llCollectionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if(exhibitBean.isSaveForPerson()){
                        exhibitBean.setSaveForPerson(false);
                        finalViewHolder.ivCollection.setImageDrawable(context.getResources().getDrawable(R.drawable.iv_heart_empty));
                        //Toast.makeText(context, "取消收藏", Toast.LENGTH_SHORT).show();
                    }else{
                        exhibitBean.setSaveForPerson(true);
                        finalViewHolder.ivCollection.setImageDrawable(context.getResources().getDrawable(R.drawable.iv_heart_full));
                        //Toast.makeText(context, "已收藏", Toast.LENGTH_SHORT).show();
                    }
                    DataBiz.saveOrUpdate(exhibitBean);
                } catch (Exception e) {
                    ExceptionUtil.handleException(e);
                }
            }
        });
        // 显示图片
        String iconUrl = exhibitBean.getIconurl();
        ImageUtil.displayImage(iconUrl, viewHolder.ivExhibitIcon,true,false);


        if (exhibitBean.equals(selectIndex)) {
            switch (state) {
                case STATE_PLAYABLE:
                    viewHolder.ivPlayAnim.setImageDrawable(
                            context.getResources().getDrawable(R.drawable.uamp_ic_play_arrow_white_24dp));
                    viewHolder.ivPlayAnim.setVisibility(View.VISIBLE);
                    break;
                case STATE_PLAYING:
                    AnimationDrawable animation = (AnimationDrawable)
                            context.getResources().getDrawable(R.drawable.ic_equalizer_white_36dp);
                    viewHolder.ivPlayAnim.setImageDrawable(animation);
                    viewHolder.ivPlayAnim.setVisibility(View.VISIBLE);
                    if (animation != null) animation.start();
                    break;
                case STATE_PAUSED:
                    viewHolder.ivPlayAnim.setImageDrawable(
                            context.getResources().getDrawable(R.drawable.ic_equalizer1_white_36dp));
                    viewHolder.ivPlayAnim.setVisibility(View.VISIBLE);
                    break;
                default:
                    viewHolder.ivPlayAnim.setVisibility(View.GONE);
            }

        }else{
            viewHolder.ivPlayAnim.setVisibility(View.GONE);
        }




        return convertView;
    }

    private static void initializeColorStateLists(Context context) {
        sColorStateNotPlaying = ColorStateList.valueOf(context.getResources().getColor(
                R.color.media_item_icon_not_playing));
        sColorStatePlaying = ColorStateList.valueOf(context.getResources().getColor(
                R.color.media_item_icon_playing));
    }


    public static void tintColor(Context context,ImageView imageView,@DrawableRes int drawableId,@ColorRes int colorId){
        Drawable icon = context.getResources().getDrawable(drawableId);
        Drawable tintIcon = DrawableCompat.wrap(icon);
        DrawableCompat.setTintList(tintIcon, context.getResources().getColorStateList(colorId));
        imageView.setImageDrawable(tintIcon);
    }

    class ViewHolder{
        TextView tvExhibitName, tvExhibitYears, tvExhibitPosition,tvExhibitDistance,tvExhibitNumber;
        ImageView ivExhibitIcon;
        LinearLayout llCollectionBtn;
        ImageView ivCollection;
        ImageView ivPlayAnim;
    }
}
