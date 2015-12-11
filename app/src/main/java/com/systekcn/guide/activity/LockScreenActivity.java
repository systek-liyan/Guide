package com.systekcn.guide.activity;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.systekcn.guide.MyApplication;
import com.systekcn.guide.R;
import com.systekcn.guide.common.IConstants;
import com.systekcn.guide.common.utils.ImageLoaderUtil;
import com.systekcn.guide.common.utils.TimeUtil;
import com.systekcn.guide.common.utils.Tools;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class LockScreenActivity extends Activity implements IConstants{


    private ImageView fullscreen_image;
    private MyApplication application;
    private View view;
    private TextView timeNow;
    private Button btn_unlock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        application= (MyApplication) getApplication();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_FULLSCREEN
                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        view =getLayoutInflater().inflate(R.layout.activity_lock_screen,null);
        setContentView(view);
        fullscreen_image=(ImageView)view.findViewById(R.id.fullscreen_image);
        btn_unlock=(Button)view.findViewById(R.id.btn_unlock);
        timeNow=(TextView)view.findViewById(R.id.tv_lock_time);
        timeNow.setText(TimeUtil.getTime());
        btn_unlock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Tools.virbate(200);
                finish();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        displayLockImage();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode==KeyEvent.KEYCODE_BACK){
            return true;
        }else if(keyCode==KeyEvent.KEYCODE_HOME){
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void displayLockImage() {

        if(application.currentExhibitBean!=null){
            String imgUrl=application.currentExhibitBean.getIconurl();
            String localName= Tools.changePathToName(imgUrl);
            String localPath=application.getCurrentImgDir()+localName;
            if(Tools.isFileExist(localPath)){
                /*本地图片存在，将锁屏界面设为图片Icon*/
                Bitmap bitmap= BitmapFactory.decodeFile(localPath);
                Drawable drawable=new BitmapDrawable(bitmap);
                view.setBackground(drawable);
                //ImageLoaderUtil.displaySdcardImage(this, localPath, fullscreen_image);
            }else{
                ImageLoaderUtil.displayNetworkImage(this, BASEURL + imgUrl, fullscreen_image);
            }
        }
    }

}
