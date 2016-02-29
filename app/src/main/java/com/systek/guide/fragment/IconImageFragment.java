package com.systek.guide.fragment;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.systek.guide.IConstants;
import com.systek.guide.R;
import com.systek.guide.entity.ExhibitBean;

public class IconImageFragment extends BaseFragment implements IConstants{

    private static final String IMAGE_URL = "image_url";
    private static final String CURRENT_MUSEUM_ID = "current_museum_id";

    private String imageUrl;
    private String currentMuseumId;

    public String getImageUrl() {
        return imageUrl;
    }


    private OnFragmentInteractionListener mListener;

    public IconImageFragment() {
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
