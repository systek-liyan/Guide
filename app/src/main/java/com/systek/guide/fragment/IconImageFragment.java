package com.systek.guide.fragment;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.systek.guide.IConstants;
import com.systek.guide.R;
import com.systek.guide.entity.ExhibitBean;
import com.systek.guide.utils.ImageLoaderUtil;
import com.systek.guide.utils.Tools;

import java.io.File;

public class IconImageFragment extends BaseFragment implements IConstants{

    private static final String IMAGE_URL = "image_url";
    private static final String CURRENT_MUSEUM_ID = "current_museum_id";

    private String imageUrl;
    private String currentMuseumId;
    private ImageView imgExhibitIcon;

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getCurrentMuseumId() {
        return currentMuseumId;
    }

    public void setCurrentMuseumId(String currentMuseumId) {
        this.currentMuseumId = currentMuseumId;
    }

    private OnFragmentInteractionListener mListener;

    public IconImageFragment() {
    }

    public void initIcon() {
        if(imageUrl==null||imgExhibitIcon==null){return;}
        String imageName = Tools.changePathToName(imageUrl);
        String imgLocalUrl = LOCAL_ASSETS_PATH+currentMuseumId + "/" + LOCAL_FILE_TYPE_IMAGE+"/"+imageName;
        File file = new File(imgLocalUrl);
        // 判断sdcard上有没有图片
        if (file.exists()) {
            // 显示sdcard
            ImageLoaderUtil.displaySdcardImage(getActivity(), imgLocalUrl, imgExhibitIcon);
        } else {
            ImageLoaderUtil.displayNetworkImage(getActivity(), BASE_URL + imageUrl, imgExhibitIcon);// TODO: 2016/2/16
        }
    }


    public static IconImageFragment newInstance(String imageUrl,String currentMuseumId) {
        IconImageFragment fragment = new IconImageFragment();
        if(!TextUtils.isEmpty(imageUrl)){
            Bundle args = new Bundle();
            args.putString(IMAGE_URL, imageUrl);
            args.putString(CURRENT_MUSEUM_ID, currentMuseumId);
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            imageUrl = getArguments().getString(IMAGE_URL);
            currentMuseumId = getArguments().getString(CURRENT_MUSEUM_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view= inflater.inflate(R.layout.fragment_icon_image, container, false);
        imgExhibitIcon=(ImageView)view.findViewById(R.id.iv_exhibit_icon);
        initIcon();
        return view;

    }

    public void onButtonPressed(ExhibitBean exhibit) {
        if (mListener != null) {
            mListener.onFragmentInteraction(exhibit);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(ExhibitBean exhibit);
    }
}
