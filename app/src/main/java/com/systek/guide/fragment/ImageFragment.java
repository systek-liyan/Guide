package com.systek.guide.fragment;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;

import com.systek.guide.IConstants;
import com.systek.guide.R;
import com.systek.guide.utils.ExceptionUtil;

import java.io.InputStream;

/**
 * 图片fragment
 */
public class ImageFragment extends BaseFragment implements IConstants {

    private String imgUrl = "";

    public ImageFragment() {
    }

    public static ImageFragment newInstance(String imgUrl) {
        ImageFragment fragment = new ImageFragment();
        fragment.imgUrl=imgUrl;
        return fragment;
    }

    @Override
    public void initView() {
        setContentView(R.layout.fragment_image);
        // 控件实例化
        ImageView imageView = (ImageView) contentView.findViewById(R.id.imageView);
        // 显示网络图片
        InputStream open = null;
        try {
            String temp = WELCOME_IMAGES + "/"+imgUrl;
            open = getActivity().getAssets().open(temp);
            Bitmap bitmap = BitmapFactory.decodeStream(open);
            imageView.setImageBitmap(bitmap);
        } catch (Exception e) {
            ExceptionUtil.handleException(e);
        }
    }


}
