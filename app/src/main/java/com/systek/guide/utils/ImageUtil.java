package com.systek.guide.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.nostra13.universalimageloader.core.display.SimpleBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.systek.guide.IConstants;
import com.systek.guide.MyApplication;
import com.systek.guide.R;
import com.systek.guide.biz.DataBiz;

import java.io.File;

/**
 * Created by Qiang on 2016/3/29.
 *
 *
 */
public class ImageUtil implements IConstants {

    private static DisplayImageOptions normalOption;
    private static DisplayImageOptions roundOption;

    private static synchronized DisplayImageOptions newNormalOptions(){
        if(normalOption==null){
            normalOption = new DisplayImageOptions.Builder()
                    //.showImageOnLoading(R.drawable.ic_stub) // resource or drawable
                    //.showImageForEmptyUri(R.drawable.ic_empty) // resource or drawable
                    //.showImageOnFail(R.drawable.ic_error) // resource or drawable
                    .resetViewBeforeLoading(false)  // default
                    .delayBeforeLoading(500)
                    .cacheInMemory(false) // default
                    .cacheOnDisk(false) // default
                            //.preProcessor(...)
                            //.postProcessor(...)
                            //.extraForDownloader(...)
                    .considerExifParams(false) // default
                    .imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2) // default
                    //.bitmapConfig(Bitmap.Config.ARGB_8888) // default
                    .bitmapConfig(Bitmap.Config.RGB_565) // default
                            //.decodingOptions(...)
                    .displayer(new SimpleBitmapDisplayer()) // default
                    .handler(new Handler()) // default
                    .build();
        }
        return normalOption;
    }


    private static synchronized DisplayImageOptions newRoundOptions(){
        if(roundOption==null){
            roundOption = new DisplayImageOptions.Builder()
                    //.showImageOnLoading(R.drawable.ic_stub) // resource or drawable
                    //.showImageForEmptyUri(R.drawable.ic_empty) // resource or drawable
                    .showImageOnFail(R.drawable.emotionstore_progresscancelbtn) // resource or drawable
                    .resetViewBeforeLoading(false)  // default
                    .delayBeforeLoading(1000)
                    .cacheInMemory(false) // default
                    .cacheOnDisk(false) // default
                            //.preProcessor(...)
                            //.postProcessor(...)
                            //.extraForDownloader(...)
                    .considerExifParams(false) // default
                    .imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2) // default
                    //.bitmapConfig(Bitmap.Config.ARGB_8888) // default
                    .bitmapConfig(Bitmap.Config.RGB_565) // default
                            //.decodingOptions(...)
                    .displayer(new RoundedBitmapDisplayer(20)) // default
                    .handler(new Handler()) // default
                    .build();
        }
        return roundOption;
    }



    public static void displayNetworkImage(final Context context,final String imageUrl,final ImageView imageView){
        newNormalOptions();
        ImageLoader.getInstance().displayImage(imageUrl, imageView, normalOption, new MyImageLoadingListener(context, imageView));

    }
    public static void displayRoundNetworkImage(final Context context,final String imageUrl,final ImageView imageView){
        newRoundOptions();
        ImageLoader.getInstance().displayImage(imageUrl, imageView, roundOption, new MyImageLoadingListener(context, imageView));

    }

    public static void displaySdcardImage( String filePathName,final ImageView ivImage) {
        newNormalOptions();
        ImageLoader.getInstance().displayImage("file:///" + filePathName, ivImage, normalOption);
    }
    public static void displayRoundSdcardImage( String filePathName,final ImageView ivImage) {
        newRoundOptions();
        ImageLoader.getInstance().displayImage("file:///" + filePathName, ivImage, roundOption);
    }

    public static void displaySdcardBlurImage(final Context context, String filePathName,final ImageView imageView) {

        newNormalOptions();
        ImageLoader.getInstance().loadImage("file:///" + filePathName, new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String s, View view) {
            }

            @Override
            public void onLoadingFailed(String s, View view, FailReason failReason) {
            }

            @Override
            public void onLoadingComplete(String s, View view, Bitmap bitmap) {
                if (bitmap != null) {
                    Bitmap mBitmap = blurBitmap(context, bitmap);//FastBlur.doBlur(bitmap,2,true)
                    imageView.setImageBitmap(mBitmap);
                }
            }

            @Override
            public void onLoadingCancelled(String s, View view) {
            }
        });
    }

    public static void displayNetworkBlurImage(final Context context,final String imageUrl,final ImageView imageView){


        ImageLoader.getInstance().loadImage(imageUrl, new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String s, View view) {
            }

            @Override
            public void onLoadingFailed(String s, View view, FailReason failReason) {
            }

            @Override
            public void onLoadingComplete(String s, View view, Bitmap bitmap) {
                if(bitmap!=null){
                    Bitmap mBitmap= blurBitmap(context,bitmap);//FastBlur.doBlur(bitmap,2,true)
                    imageView.setImageBitmap(mBitmap);
                }
            }
            @Override
            public void onLoadingCancelled(String s, View view) {

            }
        });
    }


    private static class MyBlurImageLoadingListener implements ImageLoadingListener {

        ImageView imageView;

        Context context;

        public MyBlurImageLoadingListener(Context context, ImageView imageView) {
            this.context = context;
            this.imageView = imageView;
        }

        @Override
        public void onLoadingStarted(String imageUri, View view) {
        }

        @Override
        public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
            //imageView.setImageResource(R.drawable.emotionstore_progresscancelbtn);
            LogUtil.i("图片加载失败",failReason.toString());
        }

        @Override
        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
            Bitmap bitmap=blurBitmap(context,loadedImage);
            imageView.setImageBitmap(bitmap);
            try{
                LogUtil.i("ZHANG","onLoadingComplete");
                if(!imageUri.startsWith(BASE_URL)){return;}
                String path=imageUri.substring(imageUri.indexOf(BASE_URL) + BASE_URL.length());
                String name=Tools.changePathToName(path);
                String museumId= DataBiz.getCurrentMuseumId();
                if(TextUtils.isEmpty(museumId)){return;}
                String savePath=LOCAL_ASSETS_PATH+museumId+"/";
                File dir=new File(savePath);
                if(!dir.exists()){dir.mkdirs();}
                DataBiz.saveBitmap(savePath, name, loadedImage);
            }catch (Exception e){ExceptionUtil.handleException(e);}
        }
        @Override
        public void onLoadingCancelled(String imageUri, View view) {
            LogUtil.i("图片加载取消", imageUri);
        }
    }


    private static class MyImageLoadingListener extends MyBlurImageLoadingListener{

        public MyImageLoadingListener(Context context, ImageView imageView) {
            super(context, imageView);
        }

        @Override
        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
            try{
                //LogUtil.i("ZHANG","onLoadingComplete");
                if(!imageUri.startsWith(BASE_URL)){return;}
                String path=imageUri.substring(imageUri.indexOf(BASE_URL) + BASE_URL.length());
                String name=Tools.changePathToName(path);
                String museumId= DataBiz.getCurrentMuseumId();
                if(TextUtils.isEmpty(museumId)){return;}
                String savePath=LOCAL_ASSETS_PATH+museumId+"/";
                File dir=new File(savePath);
                if(!dir.exists()){dir.mkdirs();}
                DataBiz.saveBitmap(savePath, name, loadedImage);
            }catch (Exception e){ExceptionUtil.handleException(e);}
        }
    }

    public static Bitmap blurBitmap(Context c,Bitmap bitmap){
        if(c==null){c=MyApplication.getAppContext();}
        //Let's create an empty bitmap with the same size of the bitmap we want to blur
        Bitmap outBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);

        //Instantiate a new Renderscript
        RenderScript rs = RenderScript.create(c.getApplicationContext());

        //Create an Intrinsic Blur Script using the Renderscript
        ScriptIntrinsicBlur blurScript = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));

        //Create the Allocations (in/out) with the Renderscript and the in/out bitmaps
        Allocation allIn = Allocation.createFromBitmap(rs, bitmap);
        Allocation allOut = Allocation.createFromBitmap(rs, outBitmap);

        //Set the radius of the blur
        blurScript.setRadius(25f);
        //Perform the Renderscript
        blurScript.setInput(allIn);
        blurScript.forEach(allOut);

        //Copy the final bitmap created by the out Allocation to the outBitmap
        allOut.copyTo(outBitmap);

        //recycle the original bitmap
        bitmap.recycle();

        //After finishing everything, we destroy the Renderscript.
        rs.destroy();

        return outBitmap;
    }


    public static void displayImage(String iconPath,ImageView imageView){
        displayImage(iconPath, imageView, null);
    }
    public static void displayImage(String iconPath,ImageView imageView,String museumId){
        displayImage(iconPath,imageView,museumId,false);
    }
    public static void displayImage(String iconPath,ImageView imageView,String museumId,boolean isBlur){

        String name= Tools.changePathToName(iconPath);
        String id=museumId;
        if(TextUtils.isEmpty(id)){
            id=DataBiz.getCurrentMuseumId();
        }
        String path=LOCAL_ASSETS_PATH+id+"/"+name;
        if(Tools.isFileExist(path)){
            if(isBlur){
                displaySdcardBlurImage(MyApplication.get(), path, imageView);
            }else{
                displaySdcardImage(path, imageView);
            }
        }else{
            if(isBlur){
                displayNetworkBlurImage(MyApplication.get(), BASE_URL + iconPath, imageView);
            }else{
                displayNetworkImage(MyApplication.get(), BASE_URL + iconPath, imageView);
            }
        }

    }

    public static void displayImage(String iconPath,ImageView imageView,boolean isRound,boolean isBlur){
        displayImage(iconPath, imageView, null,isRound,isBlur);
    }

    public static void displayImage(String iconPath,ImageView imageView,String museumId,boolean isRound,boolean isBlur){

        String name= Tools.changePathToName(iconPath);
        String id=museumId;
        if(TextUtils.isEmpty(id)){
            id=DataBiz.getCurrentMuseumId();
        }
        String path=LOCAL_ASSETS_PATH+id+"/"+name;
        if(Tools.isFileExist(path)){
            if(isBlur){
                displaySdcardBlurImage(MyApplication.get(), path, imageView);
            }else{
                if(isRound){
                    displayRoundSdcardImage(path,imageView);
                }else{
                    displaySdcardImage( path, imageView);
                }
            }
        }else{
            if(isBlur){
                displayNetworkBlurImage(MyApplication.get(), BASE_URL + iconPath, imageView);
            }else{

                if(isRound){
                    displayRoundNetworkImage(MyApplication.get(), BASE_URL + iconPath, imageView);
                }else{
                    displayNetworkImage(MyApplication.get(), BASE_URL + iconPath, imageView);
                }
            }
        }

    }

}
